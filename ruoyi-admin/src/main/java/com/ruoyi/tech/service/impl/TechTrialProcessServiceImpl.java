package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechTrialProcessMapper;
import com.ruoyi.tech.domain.TechTrialProcess;
import com.ruoyi.tech.service.ITechTrialProcessService;

/**
 * 试制流程记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechTrialProcessServiceImpl implements ITechTrialProcessService 
{
    @Autowired
    private TechTrialProcessMapper techTrialProcessMapper;

    /**
     * 查询试制流程记录
     * 
     * @param processId 试制流程记录主键
     * @return 试制流程记录
     */
    @Override
    public TechTrialProcess selectTechTrialProcessByProcessId(Long processId)
    {
        return techTrialProcessMapper.selectTechTrialProcessByProcessId(processId);
    }

    /**
     * 查询试制流程记录列表
     * 
     * @param techTrialProcess 试制流程记录
     * @return 试制流程记录
     */
    @Override
    public List<TechTrialProcess> selectTechTrialProcessList(TechTrialProcess techTrialProcess)
    {
        return techTrialProcessMapper.selectTechTrialProcessList(techTrialProcess);
    }

    /**
     * 新增试制流程记录
     * 
     * @param techTrialProcess 试制流程记录
     * @return 结果
     */
    @Override
    public int insertTechTrialProcess(TechTrialProcess techTrialProcess)
    {
        techTrialProcess.setCreateTime(DateUtils.getNowDate());
        return techTrialProcessMapper.insertTechTrialProcess(techTrialProcess);
    }

    /**
     * 修改试制流程记录
     * 
     * @param techTrialProcess 试制流程记录
     * @return 结果
     */
    @Override
    public int updateTechTrialProcess(TechTrialProcess techTrialProcess)
    {
        techTrialProcess.setUpdateTime(DateUtils.getNowDate());
        return techTrialProcessMapper.updateTechTrialProcess(techTrialProcess);
    }

    /**
     * 批量删除试制流程记录
     * 
     * @param processIds 需要删除的试制流程记录主键
     * @return 结果
     */
    @Override
    public int deleteTechTrialProcessByProcessIds(Long[] processIds)
    {
        return techTrialProcessMapper.deleteTechTrialProcessByProcessIds(processIds);
    }

    /**
     * 删除试制流程记录信息
     * 
     * @param processId 试制流程记录主键
     * @return 结果
     */
    @Override
    public int deleteTechTrialProcessByProcessId(Long processId)
    {
        return techTrialProcessMapper.deleteTechTrialProcessByProcessId(processId);
    }
}
