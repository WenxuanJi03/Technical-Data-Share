package com.ruoyi.tech.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.tech.domain.TechProduct;

/**
 * 产品图片识别与相似检索服务
 *
 * 通过上传的轮毂图片，调用外部识别服务获取候选轮型，
 * 再在本地产品清单中查询对应产品并返回（附带相似度）。
 */
public interface TechProductImageSearchService {

    /**
     * 根据图片进行轮毂识别，返回相似产品列表（按相似度降序）。
     *
     * @param file 上传的图片
     * @param topK 需要的候选数量
     * @return 匹配到的产品列表（带 similarityScore）
     */
    List<TechProduct> searchByImage(MultipartFile file, int topK);
}

