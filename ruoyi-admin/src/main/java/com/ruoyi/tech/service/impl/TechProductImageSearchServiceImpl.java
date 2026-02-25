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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import nu.pattern.OpenCV;

/**
 * 产品图片识别与相似检索实现（纯 Java + OpenCV 本地识别）
 *
 * 使用 ORB 特征提取 + 暴力匹配，在本地产品图库中查找最相似的轮型，无需外部服务。
 */
@Service
public class TechProductImageSearchServiceImpl implements TechProductImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(TechProductImageSearchServiceImpl.class);

    private static final int ORB_FEATURES = 800;
    private static final float MATCH_RATIO_THRESHOLD = 0.7f;  // Lowe 比率检验：最佳/次佳 < 0.7 才算可靠匹配
    private static final int MIN_GOOD_MATCHES = 10;

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

            Mat queryMat = bytesToGrayMat(bytes);
            if (queryMat == null || queryMat.empty()) {
                return result;
            }

            try {
                List<IndexEntry> index = getOrBuildIndex();
                if (index.isEmpty()) {
                    log.warn("产品图库索引为空，请先导入带正面图的产品");
                    return result;
                }

                MatOfKeyPoint queryKeyPoints = new MatOfKeyPoint();
                Mat queryDescriptors = new Mat();
                ORB orb = ORB.create(ORB_FEATURES);
                orb.detectAndCompute(queryMat, new Mat(), queryKeyPoints, queryDescriptors);

                if (queryDescriptors.rows() < MIN_GOOD_MATCHES) {
                    log.warn("上传图片特征点过少，无法识别");
                    return result;
                }

                DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                List<MatchResult> matchResults = new ArrayList<>();

                for (IndexEntry entry : index) {
                    if (entry.descriptors == null || entry.descriptors.rows() < MIN_GOOD_MATCHES) {
                        continue;
                    }
                    double score = matchDescriptors(matcher, queryDescriptors, entry.descriptors,
                            queryKeyPoints.rows(), entry.descriptors.rows());
                    if (score > 0) {
                        matchResults.add(new MatchResult(entry.productId, entry.wheelCode, score));
                    }
                }

                matchResults.sort((a, b) -> Double.compare(b.score, a.score));

                int limit = Math.min(topK, matchResults.size());
                for (int i = 0; i < limit; i++) {
                    MatchResult mr = matchResults.get(i);
                    TechProduct p = techProductService.selectTechProductByProductId(mr.productId);
                    if (p != null) {
                        p.setSimilarityScore(mr.score);
                        result.add(p);
                    }
                }
            } finally {
                queryMat.release();
            }

        } catch (Exception ex) {
            log.error("轮毂识别异常: {}", ex.getMessage(), ex);
        }

        return result;
    }

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

        ORB orb = ORB.create(ORB_FEATURES);

        for (TechProduct p : products) {
            String frontImage = p.getFrontImage();
            if (StringUtils.isEmpty(frontImage)) {
                continue;
            }

            // frontImage 存的是若依 URL 路径：/profile/upload/xxx、/profile/import/xxx 或相对路径 2024/01/01/xxx.jpg
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
            if (relative.isEmpty()) {
                continue;
            }
            Path fullPath = Paths.get(baseDir, relative);
            File file = fullPath.toFile();
            if (!file.exists() || !file.isFile()) {
                continue;
            }

            Mat img = Imgcodecs.imread(file.getAbsolutePath());
            if (img == null || img.empty()) {
                continue;
            }

            try {
                Mat gray = new Mat();
                if (img.channels() == 3) {
                    Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
                } else {
                    img.copyTo(gray);
                }

                MatOfKeyPoint keyPoints = new MatOfKeyPoint();
                Mat descriptors = new Mat();
                orb.detectAndCompute(gray, new Mat(), keyPoints, descriptors);

                if (descriptors.rows() >= MIN_GOOD_MATCHES) {
                    index.add(new IndexEntry(p.getProductId(), p.getWheelCode(), descriptors));
                } else {
                    descriptors.release();
                }
                gray.release();
            } finally {
                img.release();
            }
        }

        log.info("轮毂图库索引构建完成，共 {} 个有效产品", index.size());
        return index;
    }

    private Mat bytesToGrayMat(byte[] bytes) {
        MatOfByte mob = new MatOfByte(bytes);
        Mat img = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);
        mob.release();
        if (img == null || img.empty()) {
            return null;
        }
        Mat gray = new Mat();
        if (img.channels() == 3) {
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        } else {
            img.copyTo(gray);
        }
        img.release();
        return gray;
    }

    /**
     * 使用 knnMatch + Lowe 比率检验 匹配两张图片，返回 0~1 的相似度
     * 比率检验可有效过滤歧义匹配，降低不同轮型间的误匹配率
     */
    private double matchDescriptors(DescriptorMatcher matcher, Mat queryDescriptors, Mat trainDescriptors,
            int queryRows, int trainRows) {
        if (queryDescriptors.rows() < 2 || trainDescriptors.rows() < 2) {
            return 0;
        }

        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(queryDescriptors, trainDescriptors, knnMatches, 2);

        int goodCount = 0;
        double sumDistance = 0;

        for (MatOfDMatch knn : knnMatches) {
            List<DMatch> list = knn.toList();
            knn.release();
            if (list.size() < 2) {
                continue;
            }
            DMatch best = list.get(0);
            DMatch second = list.get(1);
            // Lowe 比率检验：最佳距离 显著小于 次佳距离 时才是可靠匹配
            if (best.distance < MATCH_RATIO_THRESHOLD * second.distance) {
                goodCount++;
                sumDistance += best.distance;
            }
        }

        if (goodCount < MIN_GOOD_MATCHES) {
            return 0;
        }

        double avgDistance = sumDistance / goodCount;
        int denominator = Math.min(queryRows, trainRows);
        double countScore = (double) goodCount / denominator;
        // 距离质量：ORB Hamming 距离越小越好，除以 (1 + avgDist/50) 让低距离得分更高
        double distanceScore = 1.0 / (1.0 + avgDistance / 50.0);
        double rawScore = countScore * distanceScore * 2.0;
        return Math.min(1.0, rawScore);
    }

    private static class IndexEntry {
        final Long productId;
        final String wheelCode;
        final Mat descriptors;

        IndexEntry(Long productId, String wheelCode, Mat descriptors) {
            this.productId = productId;
            this.wheelCode = wheelCode;
            this.descriptors = descriptors;
        }
    }

    private static class MatchResult {
        final Long productId;
        final double score;

        MatchResult(Long productId, String wheelCode, double score) {
            this.productId = productId;
            this.score = score;
        }
    }
}
