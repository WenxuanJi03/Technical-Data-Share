package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechTrial;

/**
 * 试制任务Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechTrialService 
{
    /**
     * 查询试制任务
     * 
     * @param trialId 试制任务主键
     * @return 试制任务
     */
    public TechTrial selectTechTrialByTrialId(Long trialId);

    /**
     * 查询试制任务列表
     * 
     * @param techTrial 试制任务
     * @return 试制任务集合
     */
    public List<TechTrial> selectTechTrialList(TechTrial techTrial);

    /**
     * 新增试制任务
     * 
     * @param techTrial 试制任务
     * @return 结果
     */
    public int insertTechTrial(TechTrial techTrial);

    /**
     * 修改试制任务
     * 
     * @param techTrial 试制任务
     * @return 结果
     */
    public int updateTechTrial(TechTrial techTrial);

    /**
     * 批量删除试制任务
     * 
     * @param trialIds 需要删除的试制任务主键集合
     * @return 结果
     */
    public int deleteTechTrialByTrialIds(Long[] trialIds);

    /**
     * 删除试制任务信息
     * 
     * @param trialId 试制任务主键
     * @return 结果
     */
    public int deleteTechTrialByTrialId(Long trialId);
}
