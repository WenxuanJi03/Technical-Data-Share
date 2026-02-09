package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechTrialProcess;

/**
 * 试制流程记录Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechTrialProcessService 
{
    /**
     * 检查用户是否有逾期任务
     */
    public boolean checkUserOverdue(String userName);

    /**
     * 查询试制流程记录
     * 
     * @param processId 试制流程记录主键
     * @return 试制流程记录
     */
    public TechTrialProcess selectTechTrialProcessByProcessId(Long processId);

    /**
     * 查询试制流程记录列表
     * 
     * @param techTrialProcess 试制流程记录
     * @return 试制流程记录集合
     */
    public List<TechTrialProcess> selectTechTrialProcessList(TechTrialProcess techTrialProcess);

    /**
     * 新增试制流程记录
     * 
     * @param techTrialProcess 试制流程记录
     * @return 结果
     */
    public int insertTechTrialProcess(TechTrialProcess techTrialProcess);

    /**
     * 修改试制流程记录
     * 
     * @param techTrialProcess 试制流程记录
     * @return 结果
     */
    public int updateTechTrialProcess(TechTrialProcess techTrialProcess);

    /**
     * 批量删除试制流程记录
     * 
     * @param processIds 需要删除的试制流程记录主键集合
     * @return 结果
     */
    public int deleteTechTrialProcessByProcessIds(Long[] processIds);

    /**
     * 删除试制流程记录信息
     * 
     * @param processId 试制流程记录主键
     * @return 结果
     */
    public int deleteTechTrialProcessByProcessId(Long processId);
}
