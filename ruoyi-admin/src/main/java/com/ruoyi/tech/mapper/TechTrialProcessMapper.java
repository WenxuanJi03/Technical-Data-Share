package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechTrialProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 试制流程记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Mapper
public interface TechTrialProcessMapper {
    /**
     * 检查用户是否有逾期任务（截止日期 <= 今天 且 步骤未完成）
     */
    public int checkUserOverdue(@Param("userName") String userName);

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
     * 按模号精确查询试制流程记录（仅返回一条）
     *
     * @param moldCode 模号
     * @return 试制流程记录
     */
    public TechTrialProcess selectTechTrialProcessByMoldCode(String moldCode);

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
     * 删除试制流程记录
     * 
     * @param processId 试制流程记录主键
     * @return 结果
     */
    public int deleteTechTrialProcessByProcessId(Long processId);

    /**
     * 批量删除试制流程记录
     * 
     * @param processIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTechTrialProcessByProcessIds(Long[] processIds);
}
