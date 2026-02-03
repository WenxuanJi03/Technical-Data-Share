package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试制任务对象 tech_trial
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechTrial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 试制ID */
    private Long trialId;

    /** 试制编号 */
    @Excel(name = "试制编号")
    private String trialCode;

    /** 试制名称 */
    @Excel(name = "试制名称")
    private String trialName;

    /** 关联项目ID */
    @Excel(name = "关联项目ID")
    private Long projectId;

    /** 关联项目名称 */
    @Excel(name = "关联项目名称")
    private String projectName;

    /** 试制类型 */
    @Excel(name = "试制类型")
    private String trialType;

    /** 试制数量 */
    @Excel(name = "试制数量")
    private Long quantity;

    /** 试制状态 */
    @Excel(name = "试制状态")
    private String status;

    /** 当前车间 */
    @Excel(name = "当前车间")
    private String currentWorkshop;

    /** 发起人ID（试制工程师） */
    @Excel(name = "发起人ID", readConverterExp = "试=制工程师")
    private Long initiatorId;

    /** 发起人姓名 */
    @Excel(name = "发起人姓名")
    private String initiatorName;

    /** 计划开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planStartDate;

    /** 计划结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planEndDate;

    /** 实际开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualStartDate;

    /** 实际结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualEndDate;

    /** 试制要求 */
    @Excel(name = "试制要求")
    private String requirement;

    /** 试制结果 */
    @Excel(name = "试制结果")
    private String result;

    /** 删除标志 */
    private String delFlag;

    public void setTrialId(Long trialId) 
    {
        this.trialId = trialId;
    }

    public Long getTrialId() 
    {
        return trialId;
    }

    public void setTrialCode(String trialCode) 
    {
        this.trialCode = trialCode;
    }

    public String getTrialCode() 
    {
        return trialCode;
    }

    public void setTrialName(String trialName) 
    {
        this.trialName = trialName;
    }

    public String getTrialName() 
    {
        return trialName;
    }

    public void setProjectId(Long projectId) 
    {
        this.projectId = projectId;
    }

    public Long getProjectId() 
    {
        return projectId;
    }

    public void setProjectName(String projectName) 
    {
        this.projectName = projectName;
    }

    public String getProjectName() 
    {
        return projectName;
    }

    public void setTrialType(String trialType) 
    {
        this.trialType = trialType;
    }

    public String getTrialType() 
    {
        return trialType;
    }

    public void setQuantity(Long quantity) 
    {
        this.quantity = quantity;
    }

    public Long getQuantity() 
    {
        return quantity;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setCurrentWorkshop(String currentWorkshop) 
    {
        this.currentWorkshop = currentWorkshop;
    }

    public String getCurrentWorkshop() 
    {
        return currentWorkshop;
    }

    public void setInitiatorId(Long initiatorId) 
    {
        this.initiatorId = initiatorId;
    }

    public Long getInitiatorId() 
    {
        return initiatorId;
    }

    public void setInitiatorName(String initiatorName) 
    {
        this.initiatorName = initiatorName;
    }

    public String getInitiatorName() 
    {
        return initiatorName;
    }

    public void setPlanStartDate(Date planStartDate) 
    {
        this.planStartDate = planStartDate;
    }

    public Date getPlanStartDate() 
    {
        return planStartDate;
    }

    public void setPlanEndDate(Date planEndDate) 
    {
        this.planEndDate = planEndDate;
    }

    public Date getPlanEndDate() 
    {
        return planEndDate;
    }

    public void setActualStartDate(Date actualStartDate) 
    {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualStartDate() 
    {
        return actualStartDate;
    }

    public void setActualEndDate(Date actualEndDate) 
    {
        this.actualEndDate = actualEndDate;
    }

    public Date getActualEndDate() 
    {
        return actualEndDate;
    }

    public void setRequirement(String requirement) 
    {
        this.requirement = requirement;
    }

    public String getRequirement() 
    {
        return requirement;
    }

    public void setResult(String result) 
    {
        this.result = result;
    }

    public String getResult() 
    {
        return result;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("trialId", getTrialId())
            .append("trialCode", getTrialCode())
            .append("trialName", getTrialName())
            .append("projectId", getProjectId())
            .append("projectName", getProjectName())
            .append("trialType", getTrialType())
            .append("quantity", getQuantity())
            .append("status", getStatus())
            .append("currentWorkshop", getCurrentWorkshop())
            .append("initiatorId", getInitiatorId())
            .append("initiatorName", getInitiatorName())
            .append("planStartDate", getPlanStartDate())
            .append("planEndDate", getPlanEndDate())
            .append("actualStartDate", getActualStartDate())
            .append("actualEndDate", getActualEndDate())
            .append("requirement", getRequirement())
            .append("result", getResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
