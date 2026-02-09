package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 待办任务对象 tech_todo_task
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
public class TechTodoTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 模具编号 */
    @Excel(name = "模具编号")
    private String moldCode;

    /** 开发类型 */
    @Excel(name = "开发类型")
    private String devType;

    /** 流程 */
    @Excel(name = "流程")
    private String processName;

    /** 工序 */
    @Excel(name = "工序")
    private String procedureName;

    /** 任务类别 */
    @Excel(name = "任务类别")
    private String taskCategory;

    /** 优先级 */
    @Excel(name = "优先级")
    private String priority;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String taskStatus;

    /** 责任人 */
    @Excel(name = "责任人")
    private String responsible;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deadline;

    /** 时效 */
    @Excel(name = "时效")
    private String timeliness;

    /** 天数 */
    @Excel(name = "天数")
    private Integer days;

    /** 上传责任人 */
    @Excel(name = "上传责任人")
    private String uploadResponsible;

    /** 删除标志 */
    private String delFlag;

    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getTaskId() { return taskId; }

    public void setMoldCode(String moldCode) { this.moldCode = moldCode; }
    public String getMoldCode() { return moldCode; }

    public void setDevType(String devType) { this.devType = devType; }
    public String getDevType() { return devType; }

    public void setProcessName(String processName) { this.processName = processName; }
    public String getProcessName() { return processName; }

    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }
    public String getProcedureName() { return procedureName; }

    public void setTaskCategory(String taskCategory) { this.taskCategory = taskCategory; }
    public String getTaskCategory() { return taskCategory; }

    public void setPriority(String priority) { this.priority = priority; }
    public String getPriority() { return priority; }

    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getTaskStatus() { return taskStatus; }

    public void setResponsible(String responsible) { this.responsible = responsible; }
    public String getResponsible() { return responsible; }

    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getStartTime() { return startTime; }

    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public Date getDeadline() { return deadline; }

    public void setTimeliness(String timeliness) { this.timeliness = timeliness; }
    public String getTimeliness() { return timeliness; }

    public void setDays(Integer days) { this.days = days; }
    public Integer getDays() { return days; }

    public void setUploadResponsible(String uploadResponsible) { this.uploadResponsible = uploadResponsible; }
    public String getUploadResponsible() { return uploadResponsible; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("moldCode", getMoldCode())
            .append("devType", getDevType())
            .append("processName", getProcessName())
            .append("procedureName", getProcedureName())
            .append("taskCategory", getTaskCategory())
            .append("priority", getPriority())
            .append("taskStatus", getTaskStatus())
            .append("responsible", getResponsible())
            .append("startTime", getStartTime())
            .append("deadline", getDeadline())
            .append("timeliness", getTimeliness())
            .append("days", getDays())
            .append("uploadResponsible", getUploadResponsible())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
