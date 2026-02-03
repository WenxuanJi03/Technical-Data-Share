package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechProjectStageMapper;
import com.ruoyi.tech.domain.TechProjectStage;
import com.ruoyi.tech.service.ITechProjectStageService;

/**
 * 项目阶段记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechProjectStageServiceImpl implements ITechProjectStageService 
{
    @Autowired
    private TechProjectStageMapper techProjectStageMapper;

    /**
     * 查询项目阶段记录
     * 
     * @param stageId 项目阶段记录主键
     * @return 项目阶段记录
     */
    @Override
    public TechProjectStage selectTechProjectStageByStageId(Long stageId)
    {
        return techProjectStageMapper.selectTechProjectStageByStageId(stageId);
    }

    /**
     * 查询项目阶段记录列表
     * 
     * @param techProjectStage 项目阶段记录
     * @return 项目阶段记录
     */
    @Override
    public List<TechProjectStage> selectTechProjectStageList(TechProjectStage techProjectStage)
    {
        return techProjectStageMapper.selectTechProjectStageList(techProjectStage);
    }

    /**
     * 新增项目阶段记录
     * 
     * @param techProjectStage 项目阶段记录
     * @return 结果
     */
    @Override
    public int insertTechProjectStage(TechProjectStage techProjectStage)
    {
        techProjectStage.setCreateTime(DateUtils.getNowDate());
        return techProjectStageMapper.insertTechProjectStage(techProjectStage);
    }

    /**
     * 修改项目阶段记录
     * 
     * @param techProjectStage 项目阶段记录
     * @return 结果
     */
    @Override
    public int updateTechProjectStage(TechProjectStage techProjectStage)
    {
        techProjectStage.setUpdateTime(DateUtils.getNowDate());
        return techProjectStageMapper.updateTechProjectStage(techProjectStage);
    }

    /**
     * 批量删除项目阶段记录
     * 
     * @param stageIds 需要删除的项目阶段记录主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectStageByStageIds(Long[] stageIds)
    {
        return techProjectStageMapper.deleteTechProjectStageByStageIds(stageIds);
    }

    /**
     * 删除项目阶段记录信息
     * 
     * @param stageId 项目阶段记录主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectStageByStageId(Long stageId)
    {
        return techProjectStageMapper.deleteTechProjectStageByStageId(stageId);
    }
}
