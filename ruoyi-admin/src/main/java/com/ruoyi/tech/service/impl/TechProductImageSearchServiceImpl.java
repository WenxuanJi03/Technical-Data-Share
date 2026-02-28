package com.ruoyi.tech.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import nu.pattern.OpenCV;

/**
 * 产品图片识别与相似检索实现（V2 - 增强现实场景鲁棒性）
 *
 * 针对轮毂半成品/成品识别优化:
 * 1. Hough圆检测隔离轮毂区域，去除工厂背景干扰
 * 2. CLAHE自适应对比度增强，适应不同光照/表面光泽
 * 3. 多尺度边缘检测 + 形态学增强，提取鲁棒形状特征
 * 4. 辐射状剖面(Radial Profile)利用轮毂圆形对称性匹配辐条模式
 * 5. 多预处理策略取最优，兼顾例图和现实照片
 */
@Service
public class TechProductImageSearchServiceImpl implements TechProductImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(TechProductImageSearchServiceImpl.class);

    private static final int STANDARD_SIZE = 512;
    private static final int AKAZE_MIN_FEATURES = 5;
    private static final float MATCH_RATIO_THRESHOLD = 0.78f;
    private static final double MIN_SCORE_THRESHOLD = 0.005;
    private static final int RADIAL_BINS = 72;
    private static final int RADIAL_RINGS = 8;

    private static final double SHAPE_WEIGHT = 0.40;
    private static final double RADIAL_WEIGHT = 0.35;
    private static final double FEATURE_WEIGHT = 0.25;

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

                log.info("查询图片: 特征点={}, 边缘密度={}, ROI半径={}",
                        queryFeatures.akazeDescriptors != null ? queryFeatures.akazeDescriptors.rows() : 0,
                        String.format("%.3f", queryFeatures.edgeDensity),
                        queryFeatures.wheelRadius);

                List<MatchResult> matchResults = new ArrayList<>();

                for (IndexEntry entry : index) {
                    double bestScore = 0;
                    double bestFeature = 0, bestShape = 0, bestRadial = 0;

                    for (WheelFeatures qf : queryFeatures.variants) {
                        double shapeScore = compareShapes(qf, entry);
                        double radialScore = compareRadialProfiles(qf.radialProfile, entry.radialProfile);

                        double featureScore = 0;
                        if (qf.akazeDescriptors != null && entry.akazeDescriptors != null
                                && qf.akazeDescriptors.rows() >= AKAZE_MIN_FEATURES
                                && entry.akazeDescriptors.rows() >= AKAZE_MIN_FEATURES) {
                            featureScore = matchAkazeDescriptors(qf.akazeDescriptors, entry.akazeDescriptors,
                                    qf.akazeDescriptors.rows(), entry.akazeDescriptors.rows());
                        }

                        double combined = shapeScore * SHAPE_WEIGHT + radialScore * RADIAL_WEIGHT
                                + featureScore * FEATURE_WEIGHT;

                        if (combined > bestScore) {
                            bestScore = combined;
                            bestFeature = featureScore;
                            bestShape = shapeScore;
                            bestRadial = radialScore;
                        }
                    }

                    if (bestScore >= MIN_SCORE_THRESHOLD) {
                        matchResults.add(new MatchResult(entry.productId, entry.wheelCode,
                                bestScore, bestFeature, bestShape, bestRadial));
                    }
                }

                releaseFeatures(queryFeatures);

                log.info("找到 {} 个候选匹配", matchResults.size());
                matchResults.sort((a, b) -> Double.compare(b.score, a.score));

                int limit = Math.min(topK, matchResults.size());
                for (int i = 0; i < limit; i++) {
                    MatchResult mr = matchResults.get(i);
                    log.info("候选 {}: {} - 综合{}% (形状{}% + 辐射{}% + 特征{}%)",
                            i + 1, mr.wheelCode,
                            String.format("%.1f", mr.score * 100),
                            String.format("%.1f", mr.shapeScore * 100),
                            String.format("%.1f", mr.radialScore * 100),
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

    // ==================== 特征提取核心 ====================

    private WheelFeatures extractAllFeatures(Mat colorImg) {
        Mat standardColor = resizeToStandard(colorImg);
        Mat gray = toGray(standardColor);

        int[] circleInfo = detectWheelCircle(gray);
        int cx = circleInfo[0], cy = circleInfo[1], radius = circleInfo[2];

        Mat roiMask = createCircularMask(gray.size(), cx, cy, radius);

        List<WheelFeatures> variants = new ArrayList<>();

        WheelFeatures mainFeature = extractFeaturesWithPreprocessing(gray, roiMask, cx, cy, radius, "clahe");
        variants.add(mainFeature);

        WheelFeatures edgeFeature = extractFeaturesWithPreprocessing(gray, roiMask, cx, cy, radius, "edge_enhance");
        variants.add(edgeFeature);

        WheelFeatures adaptiveFeature = extractFeaturesWithPreprocessing(gray, roiMask, cx, cy, radius, "adaptive");
        variants.add(adaptiveFeature);

        mainFeature.variants = variants;

        gray.release();
        standardColor.release();
        roiMask.release();

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

        AKAZE akaze = AKAZE.create();
        akaze.setThreshold(0.0005);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        akaze.detectAndCompute(processed, roiMask, keyPoints, descriptors);
        feat.akazeDescriptors = descriptors;

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
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
            default: {
                Core.bitwise_and(gray, mask, result);
            }
        }

        return result;
    }

    // ==================== 轮毂区域检测 ====================

    private int[] detectWheelCircle(Mat gray) {
        int h = gray.rows(), w = gray.cols();
        int defaultCx = w / 2, defaultCy = h / 2;
        int defaultRadius = (int) (Math.min(w, h) * 0.42);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(9, 9), 2);

        Mat circles = new Mat();
        int minRadius = (int) (Math.min(w, h) * 0.2);
        int maxRadius = (int) (Math.min(w, h) * 0.48);

        try {
            Imgproc.HoughCircles(blurred, circles, Imgproc.HOUGH_GRADIENT,
                    1.2, Math.min(w, h) * 0.3, 100, 40, minRadius, maxRadius);

            if (circles.cols() > 0) {
                double bestScore = -1;
                int bestIdx = 0;

                for (int i = 0; i < circles.cols(); i++) {
                    double[] c = circles.get(0, i);
                    double x = c[0], y = c[1], r = c[2];
                    double centerScore = 1.0 / (1.0 + Math.sqrt(Math.pow(x - w / 2.0, 2) + Math.pow(y - h / 2.0, 2)) / Math.min(w, h));
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
        } catch (Exception e) {
            log.debug("Hough圆检测失败，使用默认区域: {}", e.getMessage());
        } finally {
            blurred.release();
            circles.release();
        }

        return new int[]{defaultCx, defaultCy, defaultRadius};
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

    // ==================== 辐射状剖面 ====================

    /**
     * 将轮毂边缘图按角度和半径分格，统计每格的边缘像素密度。
     * 生成 RADIAL_BINS x RADIAL_RINGS 的特征向量，用于比较辐条模式。
     */
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

        double bestCorr = 0;

        for (int shift = 0; shift < RADIAL_BINS; shift++) {
            double sum = 0, norm1 = 0, norm2 = 0;

            for (int ring = 0; ring < RADIAL_RINGS; ring++) {
                double ringWeight = (ring < 2) ? 0.5 : 1.0;
                for (int bin = 0; bin < RADIAL_BINS; bin++) {
                    int idx1 = ring * RADIAL_BINS + bin;
                    int idx2 = ring * RADIAL_BINS + ((bin + shift) % RADIAL_BINS);
                    double v1 = p1[idx1] * ringWeight;
                    double v2 = p2[idx2] * ringWeight;
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

    // ==================== 形状比较 ====================

    private double compareShapes(WheelFeatures query, IndexEntry entry) {
        double huScore = compareHuMoments(query.huMoments, entry.huMoments);

        double contourScore = 0;
        if (query.topContours != null && entry.topContours != null
                && !query.topContours.isEmpty() && !entry.topContours.isEmpty()) {
            contourScore = compareContourSets(query.topContours, entry.topContours);
        }

        double densityDiff = Math.abs(query.edgeDensity - entry.edgeDensity);
        double densityScore = 1.0 / (1.0 + densityDiff * 20);

        return huScore * 0.4 + contourScore * 0.4 + densityScore * 0.2;
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

    // ==================== 索引构建 ====================

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

                Mat processed = applyPreprocessing(gray, roiMask, "clahe");
                Mat edges = extractMultiScaleEdges(processed, roiMask);

                double[] huMoments = calculateHuMoments(edges);
                double edgeDensity = calculateEdgeDensity(edges, roiMask);
                double[] radialProfile = computeRadialProfile(edges, cx, cy, radius);

                AKAZE akaze = AKAZE.create();
                akaze.setThreshold(0.0005);
                MatOfKeyPoint keyPoints = new MatOfKeyPoint();
                Mat descriptors = new Mat();
                akaze.detectAndCompute(processed, roiMask, keyPoints, descriptors);

                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(edges.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                contours.sort((a, b) -> Double.compare(Imgproc.contourArea(b), Imgproc.contourArea(a)));
                int keepCount = Math.min(20, contours.size());
                List<MatOfPoint> topContours = new ArrayList<>(contours.subList(0, keepCount));

                IndexEntry entry = new IndexEntry(p.getProductId(), p.getWheelCode());
                entry.akazeDescriptors = descriptors;
                entry.huMoments = huMoments;
                entry.edgeDensity = edgeDensity;
                entry.radialProfile = radialProfile;
                entry.topContours = topContours;
                index.add(entry);

                processed.release();
                edges.release();
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
        int wheelRadius;
        List<MatOfPoint> topContours;
        List<WheelFeatures> variants;
    }

    private static class IndexEntry {
        final Long productId;
        final String wheelCode;
        Mat akazeDescriptors;
        double[] huMoments;
        double edgeDensity;
        double[] radialProfile;
        List<MatOfPoint> topContours;

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
        final double shapeScore;
        final double radialScore;

        MatchResult(Long productId, String wheelCode, double score,
                    double featureScore, double shapeScore, double radialScore) {
            this.productId = productId;
            this.wheelCode = wheelCode;
            this.score = score;
            this.featureScore = featureScore;
            this.shapeScore = shapeScore;
            this.radialScore = radialScore;
        }
    }
}
