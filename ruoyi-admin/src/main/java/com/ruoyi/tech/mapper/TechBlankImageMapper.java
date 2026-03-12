package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechBlankImage;

/**
 * 毛胚图Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-12
 */
public interface TechBlankImageMapper
{
    public TechBlankImage selectTechBlankImageByBlankId(Long blankId);

    public List<TechBlankImage> selectTechBlankImageList(TechBlankImage techBlankImage);

    public TechBlankImage selectTechBlankImageByModelCode(String modelCode);

    public int insertTechBlankImage(TechBlankImage techBlankImage);

    public int updateTechBlankImage(TechBlankImage techBlankImage);

    public int deleteTechBlankImageByBlankId(Long blankId);

    public int deleteTechBlankImageByBlankIds(Long[] blankIds);
}
