package com.ruoyi.tech.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试制流程记录对象 tech_trial_process
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechTrialProcess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流程ID */
    private Long processId;

    /** 试制ID */
    @Excel(name = "试制ID")
    private Long trialId;

    /** 车间顺序 */
    @Excel(name = "车间顺序")
    private Integer workshopOrder;

    /** 车间代码 */
    @Excel(name = "车间代码")
    private String workshopCode;

    /** 车间名称 */
    @Excel(name = "车间名称")
    private String workshopName;

    /** 处理状态 */
    @Excel(name = "处理状态")
    private String status;

    /** 接收人ID */
    @Excel(name = "接收人ID")
    private Long receiverId;

    /** 接收人姓名 */
    @Excel(name = "接收人姓名")
    private String receiverName;

    /** 接收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "接收时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date receiveTime;

    /** 处理人ID */
    @Excel(name = "处理人ID")
    private Long handlerId;

    /** 处理人姓名 */
    @Excel(name = "处理人姓名")
    private String handlerName;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date handleTime;

    /** 工时(小时) */
    @Excel(name = "工时(小时)")
    private BigDecimal workHours;

    /** 处理结果 */
    @Excel(name = "处理结果")
    private String result;

    /** 问题记录 */
    @Excel(name = "问题记录")
    private String problem;

    public void setProcessId(Long processId) 
    {
        this.processId = processId;
    }

    public Long getProcessId() 
    {
        return processId;
    }

    public void setTrialId(Long trialId) 
    {
        this.trialId = trialId;
    }

    public Long getTrialId() 
    {
        return trialId;
    }

    public void setWorkshopOrder(Integer workshopOrder) 
    {
        this.workshopOrder = workshopOrder;
    }

    public Integer getWorkshopOrder() 
    {
        return workshopOrder;
    }

    public void setWorkshopCode(String workshopCode) 
    {
        this.workshopCode = workshopCode;
    }

    public String getWorkshopCode() 
    {
        return workshopCode;
    }

    public void setWorkshopName(String workshopName) 
    {
        this.workshopName = workshopName;
    }

    public String getWorkshopName() 
    {
        return workshopName;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setReceiverId(Long receiverId) 
    {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() 
    {
        return receiverId;
    }

    public void setReceiverName(String receiverName) 
    {
        this.receiverName = receiverName;
    }

    public String getReceiverName() 
    {
        return receiverName;
    }

    public void setReceiveTime(Date receiveTime) 
    {
        this.receiveTime = receiveTime;
    }

    public Date getReceiveTime() 
    {
        return receiveTime;
    }

    public void setHandlerId(Long handlerId) 
    {
        this.handlerId = handlerId;
    }

    public Long getHandlerId() 
    {
        return handlerId;
    }

    public void setHandlerName(String handlerName) 
    {
        this.handlerName = handlerName;
    }

    public String getHandlerName() 
    {
        return handlerName;
    }

    public void setHandleTime(Date handleTime) 
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime() 
    {
        return handleTime;
    }

    public void setWorkHours(BigDecimal workHours) 
    {
        this.workHours = workHours;
    }

    public BigDecimal getWorkHours() 
    {
        return workHours;
    }

    public void setResult(String result) 
    {
        this.result = result;
    }

    public String getResult() 
    {
        return result;
    }

    public void setProblem(String problem) 
    {
        this.problem = problem;
    }

    public String getProblem() 
    {
        return problem;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("processId", getProcessId())
            .append("trialId", getTrialId())
            .append("workshopOrder", getWorkshopOrder())
            .append("workshopCode", getWorkshopCode())
            .append("workshopName", getWorkshopName())
            .append("status", getStatus())
            .append("receiverId", getReceiverId())
            .append("receiverName", getReceiverName())
            .append("receiveTime", getReceiveTime())
            .append("handlerId", getHandlerId())
            .append("handlerName", getHandlerName())
            .append("handleTime", getHandleTime())
            .append("workHours", getWorkHours())
            .append("result", getResult())
            .append("problem", getProblem())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
