package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目阶段记录对象 tech_project_stage
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechProjectStage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long stageId;

    /** 项目ID */
    @Excel(name = "项目ID")
    private Long projectId;

    /** 阶段代码 */
    @Excel(name = "阶段代码")
    private String stageCode;

    /** 阶段名称 */
    @Excel(name = "阶段名称")
    private String stageName;

    /** 阶段状态 */
    @Excel(name = "阶段状态")
    private String stageStatus;

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

    /** 操作人ID */
    @Excel(name = "操作人ID")
    private Long operatorId;

    /** 操作人姓名 */
    @Excel(name = "操作人姓名")
    private String operatorName;

    /** 阶段结果/结论 */
    @Excel(name = "阶段结果/结论")
    private String result;

    public void setStageId(Long stageId) 
    {
        this.stageId = stageId;
    }

    public Long getStageId() 
    {
        return stageId;
    }

    public void setProjectId(Long projectId) 
    {
        this.projectId = projectId;
    }

    public Long getProjectId() 
    {
        return projectId;
    }

    public void setStageCode(String stageCode) 
    {
        this.stageCode = stageCode;
    }

    public String getStageCode() 
    {
        return stageCode;
    }

    public void setStageName(String stageName) 
    {
        this.stageName = stageName;
    }

    public String getStageName() 
    {
        return stageName;
    }

    public void setStageStatus(String stageStatus) 
    {
        this.stageStatus = stageStatus;
    }

    public String getStageStatus() 
    {
        return stageStatus;
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

    public void setOperatorId(Long operatorId) 
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId() 
    {
        return operatorId;
    }

    public void setOperatorName(String operatorName) 
    {
        this.operatorName = operatorName;
    }

    public String getOperatorName() 
    {
        return operatorName;
    }

    public void setResult(String result) 
    {
        this.result = result;
    }

    public String getResult() 
    {
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("stageId", getStageId())
            .append("projectId", getProjectId())
            .append("stageCode", getStageCode())
            .append("stageName", getStageName())
            .append("stageStatus", getStageStatus())
            .append("planStartDate", getPlanStartDate())
            .append("planEndDate", getPlanEndDate())
            .append("actualStartDate", getActualStartDate())
            .append("actualEndDate", getActualEndDate())
            .append("operatorId", getOperatorId())
            .append("operatorName", getOperatorName())
            .append("result", getResult())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
