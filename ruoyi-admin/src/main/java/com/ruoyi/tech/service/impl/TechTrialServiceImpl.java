package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechTrialMapper;
import com.ruoyi.tech.domain.TechTrial;
import com.ruoyi.tech.service.ITechTrialService;

/**
 * 试制任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechTrialServiceImpl implements ITechTrialService 
{
    @Autowired
    private TechTrialMapper techTrialMapper;

    /**
     * 查询试制任务
     * 
     * @param trialId 试制任务主键
     * @return 试制任务
     */
    @Override
    public TechTrial selectTechTrialByTrialId(Long trialId)
    {
        return techTrialMapper.selectTechTrialByTrialId(trialId);
    }

    /**
     * 查询试制任务列表
     * 
     * @param techTrial 试制任务
     * @return 试制任务
     */
    @Override
    public List<TechTrial> selectTechTrialList(TechTrial techTrial)
    {
        return techTrialMapper.selectTechTrialList(techTrial);
    }

    /**
     * 新增试制任务
     * 
     * @param techTrial 试制任务
     * @return 结果
     */
    @Override
    public int insertTechTrial(TechTrial techTrial)
    {
        techTrial.setCreateTime(DateUtils.getNowDate());
        return techTrialMapper.insertTechTrial(techTrial);
    }

    /**
     * 修改试制任务
     * 
     * @param techTrial 试制任务
     * @return 结果
     */
    @Override
    public int updateTechTrial(TechTrial techTrial)
    {
        techTrial.setUpdateTime(DateUtils.getNowDate());
        return techTrialMapper.updateTechTrial(techTrial);
    }

    /**
     * 批量删除试制任务
     * 
     * @param trialIds 需要删除的试制任务主键
     * @return 结果
     */
    @Override
    public int deleteTechTrialByTrialIds(Long[] trialIds)
    {
        return techTrialMapper.deleteTechTrialByTrialIds(trialIds);
    }

    /**
     * 删除试制任务信息
     * 
     * @param trialId 试制任务主键
     * @return 结果
     */
    @Override
    public int deleteTechTrialByTrialId(Long trialId)
    {
        return techTrialMapper.deleteTechTrialByTrialId(trialId);
    }
}
