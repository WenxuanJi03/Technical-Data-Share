package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechProjectStage;

/**
 * 项目阶段记录Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechProjectStageService 
{
    /**
     * 查询项目阶段记录
     * 
     * @param stageId 项目阶段记录主键
     * @return 项目阶段记录
     */
    public TechProjectStage selectTechProjectStageByStageId(Long stageId);

    /**
     * 查询项目阶段记录列表
     * 
     * @param techProjectStage 项目阶段记录
     * @return 项目阶段记录集合
     */
    public List<TechProjectStage> selectTechProjectStageList(TechProjectStage techProjectStage);

    /**
     * 新增项目阶段记录
     * 
     * @param techProjectStage 项目阶段记录
     * @return 结果
     */
    public int insertTechProjectStage(TechProjectStage techProjectStage);

    /**
     * 修改项目阶段记录
     * 
     * @param techProjectStage 项目阶段记录
     * @return 结果
     */
    public int updateTechProjectStage(TechProjectStage techProjectStage);

    /**
     * 批量删除项目阶段记录
     * 
     * @param stageIds 需要删除的项目阶段记录主键集合
     * @return 结果
     */
    public int deleteTechProjectStageByStageIds(Long[] stageIds);

    /**
     * 删除项目阶段记录信息
     * 
     * @param stageId 项目阶段记录主键
     * @return 结果
     */
    public int deleteTechProjectStageByStageId(Long stageId);
}
