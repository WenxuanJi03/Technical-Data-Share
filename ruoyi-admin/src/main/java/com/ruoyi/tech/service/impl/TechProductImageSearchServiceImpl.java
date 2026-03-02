package com.ruoyi.tech.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.tech.domain.TechProduct;
import com.ruoyi.tech.service.ITechProductService;
import com.ruoyi.tech.service.TechProductImageSearchService;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import nu.pattern.OpenCV;

/**
 * 产品图片识别与相似检索实现（V3 - 结构几何特征优先）
 *
 * 针对轮毂半成品/成品跨域识别优化:
 * 1. 辐条计数硬过滤，快速排除不可能的候选
 * 2. 二值化辐射剖面，消除边缘强度差异
 * 3. 辐条角度签名，捕获辐条排列几何关系
 * 4. 傅里叶描述子，旋转不变的整体形状匹配
 * 5. 双边滤波+形态学梯度预处理，对光照/纹理更鲁棒
 * 6. 多参数Hough圆检测+椭圆拟合后备
 * 7. 双策略索引(clahe + bilateral_gradient)取最优匹配
 * 8. 异步索引预热，避免首次请求冷启动超时
 */
@Service
public class TechProductImageSearchServiceImpl implements TechProductImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(TechProductImageSearchServiceImpl.class);

    private static final int STANDARD_SIZE = 512;
    private static final int AKAZE_MIN_FEATURES = 5;
    private static final float MATCH_RATIO_THRESHOLD = 0.85f;
    private static final double MIN_SCORE_THRESHOLD = 0.005;
    private static final int RADIAL_BINS = 72;
    private static final int RADIAL_RINGS = 8;
    private static final int FOURIER_COEFFS = 32;
    private static final int SPOKE_COUNT_TOLERANCE = 1;

    private static final double INTENSITY_WEIGHT = 0.30;
    private static final double RADIAL_WEIGHT = 0.25;
    private static final double SHAPE_WEIGHT = 0.20;
    private static final double SPOKE_WEIGHT = 0.15;
    private static final double FEATURE_WEIGHT = 0.10;

    static {
        try {
            OpenCV.loadLocally();
        } catch (Throwable t) {
            LoggerFactory.getLogger(TechProductImageSearchServiceImpl.class)
                    .error("OpenCV 本地库加载失败，轮毂识别功能不可用: {}", t.getMessage());
        }
    }

    private final ITechProductService techProductService;

    private volatile List<IndexEntry> indexCache = null;
    private volatile int lastProductCount = -1;

    public TechProductImageSearchServiceImpl(ITechProductService techProductService) {
        this.techProductService = techProductService;
    }

    @PostConstruct
    public void warmUpIndex() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                log.info("开始异步预构建轮毂图库索引...");
                List<IndexEntry> index = getOrBuildIndex();
                log.info("轮毂图库索引预构建完成，共 {} 个有效产品", index.size());
            } catch (Exception e) {
                log.warn("索引预构建失败，将在首次请求时重试: {}", e.getMessage());
            }
        }, "wheel-index-warmup").start();
    }

    // ==================== 主入口 ====================

    @Override
    public List<TechProduct> searchByImage(MultipartFile file, int topK) {
        List<TechProduct> result = new ArrayList<>();
        if (file == null || file.isEmpty()) {
            return result;
        }

        try {
            byte[] bytes = file.getBytes();
            if (bytes == null || bytes.length == 0) {
                return result;
            }

            Mat rawColor = bytesToColorMat(bytes);
            if (rawColor == null || rawColor.empty()) {
                log.warn("图像解码失败");
                return result;
            }

            try {
                List<IndexEntry> index = getOrBuildIndex();
                if (index.isEmpty()) {
                    log.warn("产品图库索引为空，请先导入带正面图的产品");
                    return result;
                }

                WheelFeatures queryFeatures = extractAllFeatures(rawColor);
                if (queryFeatures == null) {
                    log.warn("查询图片特征提取失败");
                    return result;
                }

                StringBuilder variantInfo = new StringBuilder();
                for (int vi = 0; vi < queryFeatures.variants.size(); vi++) {
                    WheelFeatures v = queryFeatures.variants.get(vi);
                    variantInfo.append(String.format("变体%d[辐条=%d,密度=%.3f] ", vi, v.spokeCount, v.edgeDensity));
                }
                log.info("查询图片: ROI半径={}, {}", queryFeatures.wheelRadius, variantInfo);

                // Compute consensus spoke count (median of non-zero values across variants)
                int consensusSpokes = computeConsensusSpokeCount(queryFeatures.variants);
                log.info("共识辐条数={}", consensusSpokes);

                // === 阶段1: 结构特征快速筛选（不含AKAZE），产生粗排名 ===
                List<MatchResult> phase1Results = new ArrayList<>();
                int spokeFilteredCount = 0;

                for (IndexEntry entry : index) {
                    if (consensusSpokes > 0 && entry.spokeCount > 0) {
                        boolean match = Math.abs(consensusSpokes - entry.spokeCount) <= SPOKE_COUNT_TOLERANCE;
                        if (!match && entry.spokeCount2 > 0) {
                            match = Math.abs(consensusSpokes - entry.spokeCount2) <= SPOKE_COUNT_TOLERANCE;
                        }
                        if (!match) {
                            spokeFilteredCount++;
                            continue;
                        }
                    }

                    double bestStructural = 0;
                    double bestShape = 0, bestRadial = 0, bestSpoke = 0, bestIntensity = 0;

                    for (WheelFeatures qf : queryFeatures.variants) {
                        double[] comps1 = getStructuralComponents(qf, entry, false);
                        double[] comps2 = getStructuralComponents(qf, entry, true);
                        double s1 = comps1[0] * INTENSITY_WEIGHT + comps1[1] * SHAPE_WEIGHT
                                + comps1[2] * RADIAL_WEIGHT + comps1[3] * SPOKE_WEIGHT;
                        double s2 = comps2[0] * INTENSITY_WEIGHT + comps2[1] * SHAPE_WEIGHT
                                + comps2[2] * RADIAL_WEIGHT + comps2[3] * SPOKE_WEIGHT;

                        double[] best = s1 >= s2 ? comps1 : comps2;
                        double structural = Math.max(s1, s2);

                        if (structural > bestStructural) {
                            bestStructural = structural;
                            bestIntensity = best[0];
                            bestShape = best[1];
                            bestRadial = best[2];
                            bestSpoke = best[3];
                        }
                    }

                    if (bestStructural >= MIN_SCORE_THRESHOLD) {
                        phase1Results.add(new MatchResult(entry.productId, entry.wheelCode,
                                bestStructural, 0, bestIntensity, bestShape, bestRadial, bestSpoke));
                    }
                }

                log.info("辐条过滤排除 {} 个, 阶段1通过 {} 个候选", spokeFilteredCount, phase1Results.size());
                phase1Results.sort((a, b) -> Double.compare(b.score, a.score));

                // === 阶段2: 对top候选做完整AKAZE匹配 ===
                int phase2Limit = Math.min(30, phase1Results.size());
                List<MatchResult> matchResults = new ArrayList<>();

                for (int i = 0; i < phase1Results.size(); i++) {
                    MatchResult p1 = phase1Results.get(i);

                    if (i < phase2Limit) {
                        IndexEntry entry = findEntryByProductId(index, p1.productId);
                        if (entry != null) {
                            double bestAkaze = 0;
                            for (WheelFeatures qf : queryFeatures.variants) {
                                if (qf.akazeDescriptors != null && entry.akazeDescriptors != null
                                        && qf.akazeDescriptors.rows() >= AKAZE_MIN_FEATURES
                                        && entry.akazeDescriptors.rows() >= AKAZE_MIN_FEATURES) {
                                    double ak = matchAkazeDescriptors(qf.akazeDescriptors, entry.akazeDescriptors,
                                            qf.akazeDescriptors.rows(), entry.akazeDescriptors.rows());
                                    if (ak > bestAkaze) bestAkaze = ak;
                                }
                            }
                            double fullScore = p1.score + bestAkaze * FEATURE_WEIGHT;
                            matchResults.add(new MatchResult(p1.productId, p1.wheelCode,
                                    fullScore, bestAkaze, p1.intensityScore, p1.shapeScore,
                                    p1.radialScore, p1.spokeScore));
                        }
                    } else {
                        matchResults.add(p1);
                    }
                }

                releaseFeatures(queryFeatures);

                log.info("阶段2完成，共 {} 个最终结果", matchResults.size());
                matchResults.sort((a, b) -> Double.compare(b.score, a.score));

                // 低置信度检查
                if (matchResults.isEmpty()) {
                    log.warn("未找到任何候选匹配");
                    return result;
                }

                double topScore = matchResults.get(0).score;
                if (topScore < 0.40) {
                    log.warn("最高匹配度仅{}%，低于可信阈值40%", String.format("%.1f", topScore * 100));
                    log.info("识别建议: 1.调整拍摄角度至正面 2.确保光线充足 3.清洁轮毂表面 4.避免手指或物体遮挡");
                    // 仍然返回结果，但前端可根据相似度分数显示"低置信度"警告
                }

                int limit = Math.min(topK, matchResults.size());
                for (int i = 0; i < limit; i++) {
                    MatchResult mr = matchResults.get(i);
                    log.info("候选 {}: {} - 综合{}% (强度{}% + 形状{}% + 辐射{}% + 辐条{}% + 特征{}%)",
                            i + 1, mr.wheelCode,
                            String.format("%.1f", mr.score * 100),
                            String.format("%.1f", mr.intensityScore * 100),
                            String.format("%.1f", mr.shapeScore * 100),
                            String.format("%.1f", mr.radialScore * 100),
                            String.format("%.1f", mr.spokeScore * 100),
                            String.format("%.1f", mr.featureScore * 100));
                    TechProduct p = techProductService.selectTechProductByProductId(mr.productId);
                    if (p != null) {
                        p.setSimilarityScore(mr.score);
                        result.add(p);
                    }
                }
            } finally {
                rawColor.release();
            }

        } catch (Exception ex) {
            log.error("轮毂识别异常: {}", ex.getMessage(), ex);
        }

        return result;
    }

    /**
     * Returns [intensityScore, shapeScore, radialScore, spokeScore]
     */
    private double[] getStructuralComponents(WheelFeatures qf, IndexEntry entry, boolean useSecondary) {
        double[] huMom = useSecondary && entry.huMoments2 != null ? entry.huMoments2 : entry.huMoments;
        List<MatOfPoint> contours = useSecondary && entry.topContours2 != null
                ? entry.topContours2 : entry.topContours;
        double[] radProf = useSecondary && entry.radialProfile2 != null
                ? entry.radialProfile2 : entry.radialProfile;
        double[] fourierDesc = useSecondary && entry.fourierDescriptors2 != null
                ? entry.fourierDescriptors2 : entry.fourierDescriptors;
        double[] spokeSig = useSecondary && entry.spokeSignature2 != null
                ? entry.spokeSignature2 : entry.spokeSignature;

        double intensityScore = compareIntensityPatterns(qf.intensityPattern, entry.intensityPattern);
        double shapeScore = compareShapes(qf, huMom, contours, fourierDesc);
        double radialScore = compareRadialProfiles(qf.radialProfile, radProf);
        double spokeScore = compareSpokeSignatures(qf.spokeSignature, spokeSig);

        return new double[]{intensityScore, shapeScore, radialScore, spokeScore};
    }

    private IndexEntry findEntryByProductId(List<IndexEntry> index, Long productId) {
        for (IndexEntry e : index) {
            if (e.productId.equals(productId)) return e;
        }
        return null;
    }

    // ==================== 特征提取核心 ====================

    private WheelFeatures extractAllFeatures(Mat colorImg) {
        Mat standardColor = resizeToStandard(colorImg);
        Mat gray = toGray(standardColor);

        int[] circleInfo = detectWheelCircle(gray);
        int cx = circleInfo[0], cy = circleInfo[1], radius = circleInfo[2];

        Mat roiMask = createCircularMask(gray.size(), cx, cy, radius);

        double[] intensityPattern = computeIntensityPattern(gray, cx, cy, radius);

        String[] strategies = {"clahe", "bilateral_gradient", "heavy_blur"};
        List<WheelFeatures> variants = new ArrayList<>();

        for (String strategy : strategies) {
            try {
                WheelFeatures feat = extractFeaturesWithPreprocessing(
                        gray, roiMask, cx, cy, radius, strategy);
                feat.intensityPattern = intensityPattern;
                variants.add(feat);
                log.debug("策略 {}: 辐条数={}, 边缘密度={}", strategy, feat.spokeCount,
                        String.format("%.3f", feat.edgeDensity));
            } catch (Exception e) {
                log.debug("预处理策略 {} 失败: {}", strategy, e.getMessage());
            }
        }

        gray.release();
        standardColor.release();
        roiMask.release();

        if (variants.isEmpty()) {
            return null;
        }

        WheelFeatures mainFeature = variants.get(0);
        mainFeature.variants = variants;
        return mainFeature;
    }

    private WheelFeatures extractFeaturesWithPreprocessing(Mat gray, Mat roiMask,
                                                           int cx, int cy, int radius, String strategy) {
        WheelFeatures feat = new WheelFeatures();
        feat.wheelRadius = radius;

        Mat processed = applyPreprocessing(gray, roiMask, strategy);

        Mat edges = extractMultiScaleEdges(processed, roiMask);

        feat.huMoments = calculateHuMoments(edges);
        feat.edgeDensity = calculateEdgeDensity(edges, roiMask);
        feat.radialProfile = computeRadialProfile(edges, cx, cy, radius);
        feat.spokeCount = countSpokesFromEdges(edges, cx, cy, radius);
        feat.spokeSignature = computeSpokeSignatureFromEdges(edges, cx, cy, radius);
        feat.fourierDescriptors = computeFourierDescriptors(edges, cx, cy, radius);

        AKAZE akaze = AKAZE.create();
        akaze.setThreshold(0.0005);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        akaze.detectAndCompute(processed, roiMask, keyPoints, descriptors);
        feat.akazeDescriptors = descriptors;

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges.clone(), contours, new Mat(),
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        contours.sort((a, b) -> Double.compare(Imgproc.contourArea(b), Imgproc.contourArea(a)));
        int keepCount = Math.min(20, contours.size());
        feat.topContours = new ArrayList<>(contours.subList(0, keepCount));

        processed.release();
        edges.release();

        return feat;
    }

    // ==================== 预处理策略 ====================

    private Mat applyPreprocessing(Mat gray, Mat mask, String strategy) {
        Mat result = new Mat();

        switch (strategy) {
            case "clahe": {
                Mat claheResult = new Mat();
                Imgproc.createCLAHE(3.0, new Size(8, 8)).apply(gray, claheResult);
                Mat blurred = new Mat();
                Imgproc.GaussianBlur(claheResult, blurred, new Size(3, 3), 0);
                claheResult.release();
                Core.bitwise_and(blurred, mask, result);
                blurred.release();
                break;
            }
            case "edge_enhance": {
                Mat claheResult = new Mat();
                Imgproc.createCLAHE(4.0, new Size(4, 4)).apply(gray, claheResult);

                Mat laplacian = new Mat();
                Imgproc.Laplacian(claheResult, laplacian, CvType.CV_16S, 3);
                Mat absLap = new Mat();
                Core.convertScaleAbs(laplacian, absLap);
                laplacian.release();

                Mat enhanced = new Mat();
                Core.addWeighted(claheResult, 1.0, absLap, 0.3, 0, enhanced);
                claheResult.release();
                absLap.release();

                Core.bitwise_and(enhanced, mask, result);
                enhanced.release();
                break;
            }
            case "adaptive": {
                Mat blurred = new Mat();
                Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
                Mat binary = new Mat();
                Imgproc.adaptiveThreshold(blurred, binary, 255,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
                blurred.release();

                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
                Imgproc.morphologyEx(binary, result, Imgproc.MORPH_CLOSE, kernel);
                kernel.release();
                binary.release();

                Core.bitwise_and(result, mask, result);
                break;
            }
            case "bilateral_gradient": {
                Mat bilateral = new Mat();
                Imgproc.bilateralFilter(gray, bilateral, 9, 75, 75);

                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
                Mat gradient = new Mat();
                Imgproc.morphologyEx(bilateral, gradient, Imgproc.MORPH_GRADIENT, kernel);
                kernel.release();
                bilateral.release();

                Core.bitwise_and(gradient, mask, result);
                gradient.release();
                break;
            }
            case "heavy_blur": {
                Mat blurred = new Mat();
                Imgproc.GaussianBlur(gray, blurred, new Size(15, 15), 0);
                Core.bitwise_and(blurred, mask, result);
                blurred.release();
                break;
            }
            default: {
                Core.bitwise_and(gray, mask, result);
            }
        }

        return result;
    }

    // ==================== 轮毂区域检测（增强版） ====================

    private int[] detectWheelCircle(Mat gray) {
        int h = gray.rows(), w = gray.cols();
        int defaultCx = w / 2, defaultCy = h / 2;
        int defaultRadius = (int) (Math.min(w, h) * 0.42);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(9, 9), 2);

        int minRadius = (int) (Math.min(w, h) * 0.15);
        int maxRadius = (int) (Math.min(w, h) * 0.55);

        int[][] paramSets = {{100, 40}, {80, 30}, {60, 25}};

        for (int[] params : paramSets) {
            Mat circles = new Mat();
            try {
                Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT,
                        1.2, Math.min(w, h) * 0.3, params[0], params[1], minRadius, maxRadius);

                if (circles.cols() > 0) {
                    int[] found = selectBestCircle(circles, w, h, maxRadius);
                    blurred.release();
                    circles.release();
                    return found;
                }
            } catch (Exception e) {
                log.debug("Hough检测失败(param1={}, param2={}): {}", params[0], params[1], e.getMessage());
            } finally {
                circles.release();
            }
        }

        try {
            Mat edges = new Mat();
            Imgproc.Canny(blurred, edges, 50, 150);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, new Mat(),
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            edges.release();

            MatOfPoint largestContour = null;
            double maxArea = 0;
            for (MatOfPoint c : contours) {
                double area = Imgproc.contourArea(c);
                if (area > maxArea) {
                    maxArea = area;
                    largestContour = c;
                }
            }

            if (largestContour != null && largestContour.toArray().length >= 5) {
                MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
                RotatedRect ellipse = Imgproc.fitEllipse(contour2f);
                contour2f.release();

                int cx = (int) ellipse.center.x;
                int cy = (int) ellipse.center.y;
                int radius = (int) (Math.max(ellipse.size.width, ellipse.size.height) / 2);

                if (radius >= minRadius && radius <= maxRadius) {
                    int margin = (int) (radius * 0.05);
                    radius = Math.min(radius + margin, Math.min(w, h) / 2);
                    log.debug("椭圆拟合: center=({},{}), radius={}", cx, cy, radius);
                    blurred.release();
                    return new int[]{cx, cy, radius};
                }
            }
        } catch (Exception e) {
            log.debug("椭圆拟合失败: {}", e.getMessage());
        }

        blurred.release();
        return new int[]{defaultCx, defaultCy, defaultRadius};
    }

    private int[] selectBestCircle(Mat circles, int w, int h, int maxRadius) {
        double bestScore = -1;
        int bestIdx = 0;

        for (int i = 0; i < circles.cols(); i++) {
            double[] c = circles.get(0, i);
            double x = c[0], y = c[1], r = c[2];
            double centerScore = 1.0 / (1.0 + Math.sqrt(
                    Math.pow(x - w / 2.0, 2) + Math.pow(y - h / 2.0, 2)) / Math.min(w, h));
            double sizeScore = r / maxRadius;
            double score = centerScore * 0.6 + sizeScore * 0.4;
            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }

        double[] best = circles.get(0, bestIdx);
        int cx = (int) best[0], cy = (int) best[1], radius = (int) best[2];
        int margin = (int) (radius * 0.05);
        radius = Math.min(radius + margin, Math.min(w, h) / 2);

        log.debug("Hough圆检测: center=({},{}), radius={}", cx, cy, radius);
        return new int[]{cx, cy, radius};
    }

    private Mat createCircularMask(Size size, int cx, int cy, int radius) {
        Mat mask = Mat.zeros(size, CvType.CV_8UC1);
        Imgproc.circle(mask, new Point(cx, cy), radius, new Scalar(255), -1);
        return mask;
    }

    // ==================== 多尺度边缘检测 ====================

    private Mat extractMultiScaleEdges(Mat processed, Mat mask) {
        Mat combined = Mat.zeros(processed.size(), CvType.CV_8UC1);

        int[][] thresholds = {{30, 90}, {50, 150}, {80, 200}};
        for (int[] t : thresholds) {
            Mat edges = new Mat();
            Imgproc.Canny(processed, edges, t[0], t[1]);
            Core.bitwise_and(edges, mask, edges);
            Core.bitwise_or(combined, edges, combined);
            edges.release();
        }

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
        Imgproc.morphologyEx(combined, combined, Imgproc.MORPH_CLOSE, kernel);
        kernel.release();

        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
        Imgproc.dilate(combined, combined, dilateKernel);
        dilateKernel.release();

        return combined;
    }

    // ==================== 辐射状剖面（二值化比较） ====================

    private double[] computeRadialProfile(Mat edges, int cx, int cy, int radius) {
        double[] profile = new double[RADIAL_BINS * RADIAL_RINGS];
        double[] cellArea = new double[RADIAL_RINGS];

        double ringWidth = (double) radius / RADIAL_RINGS;
        double angleStep = 2.0 * Math.PI / RADIAL_BINS;

        for (int ring = 0; ring < RADIAL_RINGS; ring++) {
            double rInner = ring * ringWidth;
            double rOuter = (ring + 1) * ringWidth;
            cellArea[ring] = Math.PI * (rOuter * rOuter - rInner * rInner) / RADIAL_BINS;
            if (cellArea[ring] < 1) cellArea[ring] = 1;
        }

        int rows = edges.rows(), cols = edges.cols();
        byte[] edgeData = new byte[rows * cols];
        edges.get(0, 0, edgeData);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if ((edgeData[y * cols + x] & 0xFF) == 0) continue;

                double dx = x - cx;
                double dy = y - cy;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > radius || dist < ringWidth * 0.5) continue;

                int ring = Math.min((int) (dist / ringWidth), RADIAL_RINGS - 1);
                double angle = Math.atan2(dy, dx) + Math.PI;
                int bin = Math.min((int) (angle / angleStep), RADIAL_BINS - 1);

                profile[ring * RADIAL_BINS + bin]++;
            }
        }

        for (int ring = 0; ring < RADIAL_RINGS; ring++) {
            for (int bin = 0; bin < RADIAL_BINS; bin++) {
                profile[ring * RADIAL_BINS + bin] /= cellArea[ring];
            }
        }

        normalizeVector(profile);
        return profile;
    }

    private double compareRadialProfiles(double[] p1, double[] p2) {
        if (p1 == null || p2 == null || p1.length != p2.length) return 0;

        double[] b1 = binarizeProfile(p1);
        double[] b2 = binarizeProfile(p2);

        double bestCorr = 0;

        for (int shift = 0; shift < RADIAL_BINS; shift++) {
            double sum = 0, norm1 = 0, norm2 = 0;

            for (int ring = 0; ring < RADIAL_RINGS; ring++) {
                double ringWeight = (ring < 2) ? 0.5 : 1.0;
                for (int bin = 0; bin < RADIAL_BINS; bin++) {
                    int idx1 = ring * RADIAL_BINS + bin;
                    int idx2 = ring * RADIAL_BINS + ((bin + shift) % RADIAL_BINS);
                    double v1 = b1[idx1] * ringWeight;
                    double v2 = b2[idx2] * ringWeight;
                    sum += v1 * v2;
                    norm1 += v1 * v1;
                    norm2 += v2 * v2;
                }
            }

            double denom = Math.sqrt(norm1) * Math.sqrt(norm2);
            double corr = (denom > 1e-10) ? sum / denom : 0;
            if (corr > bestCorr) bestCorr = corr;
        }

        return Math.max(0, bestCorr);
    }

    private double[] binarizeProfile(double[] profile) {
        double[] sorted = profile.clone();
        Arrays.sort(sorted);
        double median = sorted[sorted.length / 2];
        double threshold = Math.max(median * 1.5, 0.01);

        double[] binary = new double[profile.length];
        for (int i = 0; i < profile.length; i++) {
            binary[i] = profile[i] > threshold ? 1.0 : 0.0;
        }
        return binary;
    }

    // ==================== 辐条计数（直接从边缘图计算） ====================

    /**
     * 从原始边缘图直接统计各角度的边缘像素数，检测辐条峰值。
     * 只使用中间半径范围（30%-85%），跳过中心轮毂和外圈轮缘以避免干扰。
     */
    private int countSpokesFromEdges(Mat edges, int cx, int cy, int radius) {
        double[] angularDensity = computeAngularEdgeDensity(edges, cx, cy, radius);
        double[] smoothed = smoothCircular(angularDensity, 3);

        double mean = 0, max = 0;
        for (double v : smoothed) {
            mean += v;
            if (v > max) max = v;
        }
        mean /= RADIAL_BINS;

        if (mean < 1 || max < 3) return 0;

        double threshold = mean + (max - mean) * 0.3;

        int spokeCount = 0;
        boolean inPeak = false;
        for (int i = 0; i < RADIAL_BINS; i++) {
            if (smoothed[i] > threshold && !inPeak) {
                spokeCount++;
                inPeak = true;
            } else if (smoothed[i] <= threshold) {
                inPeak = false;
            }
        }

        if (smoothed[0] > threshold && smoothed[RADIAL_BINS - 1] > threshold) {
            spokeCount = Math.max(0, spokeCount - 1);
        }

        return spokeCount;
    }

    /**
     * 统计各角度bin在中间半径区域的原始边缘像素数（未归一化）。
     */
    private double[] computeAngularEdgeDensity(Mat edges, int cx, int cy, int radius) {
        int cols = edges.cols(), rows = edges.rows();
        byte[] edgeData = new byte[rows * cols];
        edges.get(0, 0, edgeData);

        double innerR = radius * 0.3;
        double outerR = radius * 0.85;
        double angleStep = 2.0 * Math.PI / RADIAL_BINS;

        double[] density = new double[RADIAL_BINS];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if ((edgeData[y * cols + x] & 0xFF) == 0) continue;

                double dx = x - cx;
                double dy = y - cy;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < innerR || dist > outerR) continue;

                double angle = Math.atan2(dy, dx) + Math.PI;
                int bin = Math.min((int) (angle / angleStep), RADIAL_BINS - 1);
                density[bin]++;
            }
        }

        return density;
    }

    // ==================== 灰度强度角度分布（不依赖边缘，跨域鲁棒） ====================

    /**
     * 从原始灰度图计算各角度bin的平均亮度，捕获辐条（实体）vs间隙（镂空）的亮度差异。
     * 归一化到0-1范围，使其对整体亮度不敏感。
     */
    private double[] computeIntensityPattern(Mat gray, int cx, int cy, int radius) {
        int cols = gray.cols(), rows = gray.rows();
        byte[] grayData = new byte[rows * cols];
        gray.get(0, 0, grayData);

        double innerR = radius * 0.3;
        double outerR = radius * 0.85;
        double angleStep = 2.0 * Math.PI / RADIAL_BINS;

        double[] sumIntensity = new double[RADIAL_BINS];
        double[] pixelCount = new double[RADIAL_BINS];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                double dx = x - cx;
                double dy = y - cy;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < innerR || dist > outerR) continue;

                double angle = Math.atan2(dy, dx) + Math.PI;
                int bin = Math.min((int) (angle / angleStep), RADIAL_BINS - 1);

                int intensity = grayData[y * cols + x] & 0xFF;
                sumIntensity[bin] += intensity;
                pixelCount[bin]++;
            }
        }

        double[] pattern = new double[RADIAL_BINS];
        for (int i = 0; i < RADIAL_BINS; i++) {
            pattern[i] = pixelCount[i] > 0 ? sumIntensity[i] / pixelCount[i] : 0;
        }

        double min = Double.MAX_VALUE, max = 0;
        for (double v : pattern) {
            if (v > 0 && v < min) min = v;
            if (v > max) max = v;
        }
        double range = max - min;
        if (range > 1) {
            for (int i = 0; i < RADIAL_BINS; i++) {
                pattern[i] = pixelCount[i] > 0 ? (pattern[i] - min) / range : 0;
            }
        }

        return pattern;
    }

    /**
     * 比较两个灰度强度角度分布，支持旋转对齐和亮度反转。
     * 亮度反转处理：例图背景为白色（间隙亮），实拍背景暗（间隙暗），模式互补。
     */
    private double compareIntensityPatterns(double[] p1, double[] p2) {
        if (p1 == null || p2 == null || p1.length != p2.length || p1.length == 0) return 0;

        int bins = p1.length;
        double bestCorr = 0;

        double[] p2inv = new double[bins];
        for (int i = 0; i < bins; i++) {
            p2inv[i] = 1.0 - p2[i];
        }

        for (int shift = 0; shift < bins; shift++) {
            double sum1 = 0, sum2 = 0;
            double n1a = 0, n2a = 0, n1b = 0, n2b = 0;
            for (int i = 0; i < bins; i++) {
                double v1 = p1[i];
                int j = (i + shift) % bins;
                double v2 = p2[j];
                double v2i = p2inv[j];

                sum1 += v1 * v2;
                n1a += v1 * v1;
                n2a += v2 * v2;

                sum2 += v1 * v2i;
                n1b += v1 * v1;
                n2b += v2i * v2i;
            }
            double denom1 = Math.sqrt(n1a) * Math.sqrt(n2a);
            double corr1 = denom1 > 1e-10 ? sum1 / denom1 : 0;

            double denom2 = Math.sqrt(n1b) * Math.sqrt(n2b);
            double corr2 = denom2 > 1e-10 ? sum2 / denom2 : 0;

            double corr = Math.max(corr1, corr2);
            if (corr > bestCorr) bestCorr = corr;
        }

        return Math.max(0, bestCorr);
    }

    // ==================== 辐条共识计数 ====================

    private int computeConsensusSpokeCount(List<WheelFeatures> variants) {
        List<Integer> counts = new ArrayList<>();
        for (WheelFeatures v : variants) {
            if (v.spokeCount > 0) counts.add(v.spokeCount);
        }
        if (counts.isEmpty()) return 0;
        counts.sort(null);
        return counts.get(counts.size() / 2);
    }

    // ==================== 辐条角度签名（直接从边缘图计算） ====================

    private double[] computeSpokeSignatureFromEdges(Mat edges, int cx, int cy, int radius) {
        double[] angularDensity = computeAngularEdgeDensity(edges, cx, cy, radius);
        double[] smoothed = smoothCircular(angularDensity, 3);

        double mean = 0, max = 0;
        for (double v : smoothed) {
            mean += v;
            if (v > max) max = v;
        }
        mean /= RADIAL_BINS;
        if (mean < 1 || max < 3) return new double[0];

        double threshold = mean + (max - mean) * 0.3;

        List<Integer> peakCenters = new ArrayList<>();
        boolean inPeak = false;
        int peakStart = -1;

        for (int i = 0; i < RADIAL_BINS; i++) {
            if (smoothed[i] > threshold && !inPeak) {
                inPeak = true;
                peakStart = i;
            } else if (smoothed[i] <= threshold && inPeak) {
                inPeak = false;
                peakCenters.add((peakStart + i) / 2);
            }
        }
        if (inPeak) {
            if (!peakCenters.isEmpty() && smoothed[0] > threshold) {
                int firstCenter = peakCenters.remove(0);
                int mergedCenter = ((peakStart + RADIAL_BINS + firstCenter) / 2) % RADIAL_BINS;
                peakCenters.add(mergedCenter);
            } else {
                peakCenters.add((peakStart + RADIAL_BINS) / 2 % RADIAL_BINS);
            }
        }

        if (peakCenters.size() < 2) return new double[0];

        int n = peakCenters.size();
        double[] interAngles = new double[n];
        double anglePerBin = 360.0 / RADIAL_BINS;

        peakCenters.sort(Integer::compareTo);

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            int diff = peakCenters.get(next) - peakCenters.get(i);
            if (diff <= 0) diff += RADIAL_BINS;
            interAngles[i] = diff * anglePerBin;
        }

        Arrays.sort(interAngles);
        return interAngles;
    }

    private double compareSpokeSignatures(double[] sig1, double[] sig2) {
        if (sig1 == null || sig2 == null || sig1.length == 0 || sig2.length == 0) return 0;
        if (sig1.length != sig2.length) {
            return 0.1 / (1.0 + Math.abs(sig1.length - sig2.length));
        }

        double totalDiff = 0;
        for (int i = 0; i < sig1.length; i++) {
            totalDiff += Math.abs(sig1[i] - sig2[i]);
        }

        double avgDiff = totalDiff / sig1.length;
        return 1.0 / (1.0 + avgDiff / 10.0);
    }

    // ==================== 傅里叶描述子 ====================

    private double[] computeFourierDescriptors(Mat edges, int cx, int cy, int radius) {
        int numSamples = 360;
        double[] edgeDensityByAngle = new double[numSamples];

        int cols = edges.cols();
        int rows = edges.rows();
        byte[] edgeData = new byte[rows * cols];
        edges.get(0, 0, edgeData);

        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / numSamples;
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            int edgeCount = 0;
            int totalPoints = 0;

            for (double r = radius * 0.25; r < radius * 0.95; r += 1.0) {
                int x = cx + (int) (r * cosA);
                int y = cy + (int) (r * sinA);
                if (x >= 0 && x < cols && y >= 0 && y < rows) {
                    totalPoints++;
                    if ((edgeData[y * cols + x] & 0xFF) > 0) {
                        edgeCount++;
                    }
                }
            }

            edgeDensityByAngle[i] = totalPoints > 0 ? (double) edgeCount / totalPoints : 0;
        }

        double[] magnitudes = new double[FOURIER_COEFFS];

        for (int k = 0; k < FOURIER_COEFFS; k++) {
            double real = 0, imag = 0;
            for (int n = 0; n < numSamples; n++) {
                double phase = -2.0 * Math.PI * k * n / numSamples;
                real += edgeDensityByAngle[n] * Math.cos(phase);
                imag += edgeDensityByAngle[n] * Math.sin(phase);
            }
            magnitudes[k] = Math.sqrt(real * real + imag * imag) / numSamples;
        }

        if (magnitudes[0] > 1e-10) {
            double dc = magnitudes[0];
            for (int i = 0; i < FOURIER_COEFFS; i++) {
                magnitudes[i] /= dc;
            }
        }

        return magnitudes;
    }

    private double compareFourierDescriptors(double[] fd1, double[] fd2) {
        if (fd1 == null || fd2 == null) return 0;
        int len = Math.min(fd1.length, fd2.length);
        if (len < 2) return 0;

        double sumProduct = 0, norm1 = 0, norm2 = 0;
        for (int i = 1; i < len; i++) {
            sumProduct += fd1[i] * fd2[i];
            norm1 += fd1[i] * fd1[i];
            norm2 += fd2[i] * fd2[i];
        }

        double denom = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denom > 1e-10 ? Math.max(0, sumProduct / denom) : 0;
    }

    // ==================== 形状比较 ====================

    private double compareShapes(WheelFeatures query, double[] targetHuMoments,
                                 List<MatOfPoint> targetContours, double[] targetFourierDesc) {
        double huScore = compareHuMoments(query.huMoments, targetHuMoments);

        double contourScore = 0;
        if (query.topContours != null && targetContours != null
                && !query.topContours.isEmpty() && !targetContours.isEmpty()) {
            contourScore = compareContourSets(query.topContours, targetContours);
        }

        double fourierScore = compareFourierDescriptors(query.fourierDescriptors, targetFourierDesc);

        return huScore * 0.3 + contourScore * 0.3 + fourierScore * 0.4;
    }

    private double compareContourSets(List<MatOfPoint> contours1, List<MatOfPoint> contours2) {
        int compare = Math.min(10, Math.min(contours1.size(), contours2.size()));
        if (compare == 0) return 0;

        double totalSim = 0;
        int count = 0;

        for (int i = 0; i < compare; i++) {
            MatOfPoint c1 = contours1.get(i);
            double bestMatch = Double.MAX_VALUE;

            for (int j = 0; j < compare; j++) {
                MatOfPoint c2 = contours2.get(j);
                double matchVal = Imgproc.matchShapes(c1, c2, Imgproc.CONTOURS_MATCH_I2, 0);
                if (matchVal < bestMatch) bestMatch = matchVal;
            }

            totalSim += 1.0 / (1.0 + bestMatch);
            count++;
        }

        return count > 0 ? totalSim / count : 0;
    }

    // ==================== Hu矩 ====================

    private double[] calculateHuMoments(Mat binary) {
        try {
            Moments moments = Imgproc.moments(binary, false);
            Mat hu = new Mat();
            Imgproc.HuMoments(moments, hu);

            double[] huValues = new double[7];
            for (int i = 0; i < 7; i++) {
                double val = hu.get(i, 0)[0];
                huValues[i] = (val == 0) ? 0 : -Math.signum(val) * Math.log10(Math.abs(val));
            }

            hu.release();
            return huValues;
        } catch (Exception e) {
            log.warn("Hu矩计算失败: {}", e.getMessage());
            return null;
        }
    }

    private double compareHuMoments(double[] hu1, double[] hu2) {
        if (hu1 == null || hu2 == null || hu1.length != 7 || hu2.length != 7) return 0;

        double distance = 0;
        for (int i = 0; i < 7; i++) {
            distance += Math.abs(hu1[i] - hu2[i]);
        }

        return 1.0 / (1.0 + distance * 0.15);
    }

    // ==================== AKAZE特征匹配 ====================

    private double matchAkazeDescriptors(Mat queryDesc, Mat trainDesc, int queryRows, int trainRows) {
        if (queryDesc.rows() < 2 || trainDesc.rows() < 2) return 0;

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        try {
            matcher.knnMatch(queryDesc, trainDesc, knnMatches, 2);
        } catch (Exception e) {
            return 0;
        }

        int goodCount = 0;
        double sumDistance = 0;

        for (MatOfDMatch knn : knnMatches) {
            List<DMatch> list = knn.toList();
            knn.release();
            if (list.size() < 2) continue;
            DMatch best = list.get(0);
            DMatch second = list.get(1);
            if (best.distance < MATCH_RATIO_THRESHOLD * second.distance) {
                goodCount++;
                sumDistance += best.distance;
            }
        }

        if (goodCount < AKAZE_MIN_FEATURES) return 0;

        double avgDistance = sumDistance / goodCount;
        int denominator = Math.min(queryRows, trainRows);
        double countScore = (double) goodCount / denominator;
        double distanceScore = 1.0 / (1.0 + avgDistance / 80.0);
        double rawScore = countScore * distanceScore * 2.5;

        return Math.min(1.0, rawScore);
    }

    // ==================== 边缘密度 ====================

    private double calculateEdgeDensity(Mat edges, Mat mask) {
        double edgePixels = Core.countNonZero(edges);
        double maskPixels = Core.countNonZero(mask);
        return maskPixels > 0 ? edgePixels / maskPixels : 0;
    }

    // ==================== 工具方法 ====================

    private double[] smoothCircular(double[] data, int halfWindow) {
        int n = data.length;
        double[] smoothed = new double[n];
        for (int i = 0; i < n; i++) {
            double s = 0;
            int count = 0;
            for (int j = -halfWindow; j <= halfWindow; j++) {
                s += data[(i + j + n) % n];
                count++;
            }
            smoothed[i] = s / count;
        }
        return smoothed;
    }

    private Mat resizeToStandard(Mat img) {
        double scale = (double) STANDARD_SIZE / Math.max(img.width(), img.height());
        Mat resized = new Mat();
        if (scale < 1.0) {
            Imgproc.resize(img, resized, new Size(), scale, scale, Imgproc.INTER_AREA);
        } else if (scale > 1.0) {
            Imgproc.resize(img, resized, new Size(), scale, scale, Imgproc.INTER_LINEAR);
        } else {
            img.copyTo(resized);
        }
        return resized;
    }

    private Mat toGray(Mat colorImg) {
        Mat gray = new Mat();
        if (colorImg.channels() >= 3) {
            Imgproc.cvtColor(colorImg, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            colorImg.copyTo(gray);
        }
        return gray;
    }

    private Mat bytesToColorMat(byte[] bytes) {
        MatOfByte mob = new MatOfByte(bytes);
        Mat img = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        mob.release();
        return img;
    }

    private void normalizeVector(double[] vec) {
        double maxVal = 0;
        for (double v : vec) {
            if (v > maxVal) maxVal = v;
        }
        if (maxVal > 0) {
            for (int i = 0; i < vec.length; i++) {
                vec[i] /= maxVal;
            }
        }
    }

    private void releaseFeatures(WheelFeatures features) {
        if (features == null) return;
        if (features.variants != null) {
            for (WheelFeatures v : features.variants) {
                if (v.akazeDescriptors != null) v.akazeDescriptors.release();
            }
        }
    }

    // ==================== 索引构建（双策略） ====================

    private List<IndexEntry> getOrBuildIndex() {
        List<TechProduct> allProducts = techProductService.selectTechProductList(new TechProduct());
        int count = allProducts == null ? 0 : allProducts.size();

        if (indexCache != null && lastProductCount == count) {
            return indexCache;
        }

        synchronized (this) {
            if (indexCache != null && lastProductCount == count) {
                return indexCache;
            }
            indexCache = buildIndex(allProducts);
            lastProductCount = count;
        }
        return indexCache;
    }

    private List<IndexEntry> buildIndex(List<TechProduct> products) {
        List<IndexEntry> index = new ArrayList<>();
        if (products == null || products.isEmpty()) {
            return index;
        }

        String profile = RuoYiConfig.getProfile();
        String uploadPath = RuoYiConfig.getUploadPath();
        if (StringUtils.isEmpty(profile) || StringUtils.isEmpty(uploadPath)) {
            log.warn("上传路径未配置，无法构建图库索引");
            return index;
        }

        for (TechProduct p : products) {
            String frontImage = p.getFrontImage();
            if (StringUtils.isEmpty(frontImage)) continue;

            File file = resolveImageFile(frontImage, uploadPath);
            if (file == null || !file.exists() || !file.isFile()) continue;

            Mat img = Imgcodecs.imread(file.getAbsolutePath());
            if (img == null || img.empty()) continue;

            try {
                Mat standardColor = resizeToStandard(img);
                Mat gray = toGray(standardColor);

                int[] circleInfo = detectWheelCircle(gray);
                int cx = circleInfo[0], cy = circleInfo[1], radius = circleInfo[2];
                Mat roiMask = createCircularMask(gray.size(), cx, cy, radius);

                double[] intensityPattern = computeIntensityPattern(gray, cx, cy, radius);

                WheelFeatures primary = extractFeaturesWithPreprocessing(
                        gray, roiMask, cx, cy, radius, "clahe");

                IndexEntry entry = new IndexEntry(p.getProductId(), p.getWheelCode());
                entry.akazeDescriptors = primary.akazeDescriptors;
                entry.huMoments = primary.huMoments;
                entry.radialProfile = primary.radialProfile;
                entry.intensityPattern = intensityPattern;
                entry.topContours = primary.topContours;
                entry.spokeCount = primary.spokeCount;
                entry.spokeSignature = primary.spokeSignature;
                entry.fourierDescriptors = primary.fourierDescriptors;

                try {
                    WheelFeatures secondary = extractFeaturesWithPreprocessing(
                            gray, roiMask, cx, cy, radius, "bilateral_gradient");
                    entry.huMoments2 = secondary.huMoments;
                    entry.radialProfile2 = secondary.radialProfile;
                    entry.topContours2 = secondary.topContours;
                    entry.spokeCount2 = secondary.spokeCount;
                    entry.spokeSignature2 = secondary.spokeSignature;
                    entry.fourierDescriptors2 = secondary.fourierDescriptors;
                    if (secondary.akazeDescriptors != null) {
                        secondary.akazeDescriptors.release();
                    }
                } catch (Exception e) {
                    log.debug("索引二级策略提取失败({}): {}", p.getWheelCode(), e.getMessage());
                }

                log.debug("索引产品 {}: 辐条数={}(clahe)/{}(bilateral)", p.getWheelCode(),
                        entry.spokeCount, entry.spokeCount2);
                index.add(entry);

                roiMask.release();
                gray.release();
                standardColor.release();
            } finally {
                img.release();
            }
        }

        log.info("轮毂图库索引构建完成，共 {} 个有效产品", index.size());
        return index;
    }

    private File resolveImageFile(String frontImage, String uploadPath) {
        String relative;
        String baseDir;
        if (frontImage.contains("/profile/upload/") || frontImage.contains("profile/upload/")) {
            relative = frontImage.replaceFirst("^/?profile/upload/?", "");
            baseDir = uploadPath;
        } else if (frontImage.contains("/profile/import/") || frontImage.contains("profile/import/")) {
            relative = frontImage.replaceFirst("^/?profile/import/?", "");
            baseDir = RuoYiConfig.getImportPath();
        } else {
            relative = frontImage;
            baseDir = uploadPath;
        }
        relative = relative.replace("/", File.separator);
        if (relative.isEmpty()) return null;

        Path fullPath = Paths.get(baseDir, relative);
        return fullPath.toFile();
    }

    // ==================== 数据结构 ====================

    private static class WheelFeatures {
        Mat akazeDescriptors;
        double[] huMoments;
        double edgeDensity;
        double[] radialProfile;
        double[] intensityPattern;
        int wheelRadius;
        int spokeCount;
        double[] spokeSignature;
        double[] fourierDescriptors;
        List<MatOfPoint> topContours;
        List<WheelFeatures> variants;
    }

    private static class IndexEntry {
        final Long productId;
        final String wheelCode;
        Mat akazeDescriptors;
        double[] huMoments;
        double[] radialProfile;
        double[] intensityPattern;
        List<MatOfPoint> topContours;
        int spokeCount;
        double[] spokeSignature;
        double[] fourierDescriptors;
        double[] huMoments2;
        double[] radialProfile2;
        List<MatOfPoint> topContours2;
        int spokeCount2;
        double[] spokeSignature2;
        double[] fourierDescriptors2;

        IndexEntry(Long productId, String wheelCode) {
            this.productId = productId;
            this.wheelCode = wheelCode;
        }
    }

    private static class MatchResult {
        final Long productId;
        final String wheelCode;
        final double score;
        final double featureScore;
        final double intensityScore;
        final double shapeScore;
        final double radialScore;
        final double spokeScore;

        MatchResult(Long productId, String wheelCode, double score,
                    double featureScore, double intensityScore,
                    double shapeScore, double radialScore, double spokeScore) {
            this.productId = productId;
            this.wheelCode = wheelCode;
            this.score = score;
            this.featureScore = featureScore;
            this.intensityScore = intensityScore;
            this.shapeScore = shapeScore;
            this.radialScore = radialScore;
            this.spokeScore = spokeScore;
        }
    }
}
