package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechBlankImageMapper;
import com.ruoyi.tech.domain.TechBlankImage;
import com.ruoyi.tech.service.ITechBlankImageService;

/**
 * 毛胚图Service业务层处理
 */
@Service
public class TechBlankImageServiceImpl implements ITechBlankImageService {
    private static final Logger log = LoggerFactory.getLogger(TechBlankImageServiceImpl.class);

    @Autowired
    private TechBlankImageMapper techBlankImageMapper;

    @Override
    public TechBlankImage selectTechBlankImageByBlankId(Long blankId) {
        return techBlankImageMapper.selectTechBlankImageByBlankId(blankId);
    }

    @Override
    public List<TechBlankImage> selectTechBlankImageList(TechBlankImage techBlankImage) {
        return techBlankImageMapper.selectTechBlankImageList(techBlankImage);
    }

    @Override
    public int insertTechBlankImage(TechBlankImage techBlankImage) {
        techBlankImage.setCreateTime(DateUtils.getNowDate());
        return techBlankImageMapper.insertTechBlankImage(techBlankImage);
    }

    @Override
    public int updateTechBlankImage(TechBlankImage techBlankImage) {
        techBlankImage.setUpdateTime(DateUtils.getNowDate());
        return techBlankImageMapper.updateTechBlankImage(techBlankImage);
    }

    @Override
    public int deleteTechBlankImageByBlankIds(Long[] blankIds) {
        return techBlankImageMapper.deleteTechBlankImageByBlankIds(blankIds);
    }

    @Override
    public int deleteTechBlankImageByBlankId(Long blankId) {
        return techBlankImageMapper.deleteTechBlankImageByBlankId(blankId);
    }

    /**
     * 导入毛胚图数据
     * 以型号(modelCode)作为唯一标识判断是否重复
     */
    @Override
    public String importBlankImages(List<TechBlankImage> list, boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(list) || list.size() == 0) {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (TechBlankImage item : list) {
            try {
                String modelCode = item.getModelCode();
                if (StringUtils.isEmpty(modelCode)) {
                    continue;
                }
                modelCode = modelCode.trim();

                TechBlankImage exist = techBlankImageMapper.selectTechBlankImageByModelCode(modelCode);

                if (exist != null) {
                    if (isUpdateSupport) {
                        item.setBlankId(exist.getBlankId());
                        item.setUpdateBy(operName);
                        item.setUpdateTime(DateUtils.getNowDate());
                        techBlankImageMapper.updateTechBlankImage(item);
                        successNum++;
                        if (successNum <= 50) {
                            successMsg.append("<br/>").append(successNum).append("、型号 ").append(modelCode).append(" 更新成功");
                        }
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、型号 ").append(modelCode).append(" 已存在");
                    }
                } else {
                    item.setCreateBy(operName);
                    item.setCreateTime(DateUtils.getNowDate());
                    techBlankImageMapper.insertTechBlankImage(item);
                    successNum++;
                    if (successNum <= 50) {
                        successMsg.append("<br/>").append(successNum).append("、型号 ").append(modelCode).append(" 导入成功");
                    }
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、型号 " + item.getModelCode() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }

        if (successNum > 50) {
            successMsg.append("<br/>...等共 ").append(successNum).append(" 条数据。");
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，部分数据导入失败！共 " + failureNum + " 条错误：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条：");
        }
        return successMsg.toString();
    }

    /**
     * AirScript 批量同步入口
     * 按 modelCode 判重：存在则更新，不存在则新增
     */
    @Override
    public String syncBatch(List<TechBlankImage> list, String operName) {
        if (StringUtils.isNull(list) || list.size() == 0) {
            throw new ServiceException("同步数据不能为空！");
        }
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;

        for (TechBlankImage item : list) {
            try {
                String modelCode = item.getModelCode();
                if (StringUtils.isEmpty(modelCode)) {
                    skipCount++;
                    continue;
                }
                modelCode = modelCode.trim();
                item.setModelCode(modelCode);

                TechBlankImage exist = techBlankImageMapper.selectTechBlankImageByModelCode(modelCode);
                if (exist != null) {
                    item.setBlankId(exist.getBlankId());
                    item.setUpdateBy(operName);
                    item.setUpdateTime(DateUtils.getNowDate());
                    // 不覆盖已有图片
                    if (StringUtils.isEmpty(item.getBlankImage())) {
                        item.setBlankImage(null);
                    }
                    techBlankImageMapper.updateTechBlankImage(item);
                    updateCount++;
                } else {
                    item.setCreateBy(operName);
                    item.setCreateTime(DateUtils.getNowDate());
                    techBlankImageMapper.insertTechBlankImage(item);
                    insertCount++;
                }
            } catch (Exception e) {
                log.error("同步型号 {} 出错: {}", item.getModelCode(), e.getMessage());
                skipCount++;
            }
        }

        return "同步完成！新增 " + insertCount + " 条，更新 " + updateCount + " 条" +
                (skipCount > 0 ? "，跳过 " + skipCount + " 条" : "") + "。";
    }
}
