package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechBlankImage;

/**
 * 毛胚图Service接口
 */
public interface ITechBlankImageService {
    public TechBlankImage selectTechBlankImageByBlankId(Long blankId);

    public List<TechBlankImage> selectTechBlankImageList(TechBlankImage techBlankImage);

    public int insertTechBlankImage(TechBlankImage techBlankImage);

    public int updateTechBlankImage(TechBlankImage techBlankImage);

    public int deleteTechBlankImageByBlankIds(Long[] blankIds);

    public int deleteTechBlankImageByBlankId(Long blankId);

    /**
     * 导入毛胚图数据（从Excel导入）
     */
    public String importBlankImages(List<TechBlankImage> list, boolean isUpdateSupport, String operName);

    /**
     * AirScript 批量同步入口
     * 按 modelCode 判重：存在则更新，不存在则新增
     */
    public String syncBatch(List<TechBlankImage> list, String operName);
}
