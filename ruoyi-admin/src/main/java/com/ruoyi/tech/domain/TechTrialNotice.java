package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品试制通知单对象 tech_trial_notice
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
public class TechTrialNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 通知单ID */
    private Long noticeId;

    /** 通知单编号 */
    @Excel(name = "通知单编号")
    private String noticeCode;

    /** 轮型号 */
    @Excel(name = "轮型号")
    private String wheelCode;

    /** 试制状态 */
    @Excel(name = "试制状态")
    private String trialStatus;

    /** 试制类型 */
    @Excel(name = "试制类型")
    private String trialType;

    /** 开发类型 */
    @Excel(name = "开发类型")
    private String devType;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 负责人 */
    @Excel(name = "负责人")
    private String responsible;

    /** 表面状态 */
    @Excel(name = "表面状态")
    private String surfaceStatus;

    /** 尺寸规格 */
    @Excel(name = "尺寸规格")
    private String sizeSpec;

    /** 紧急程度 */
    private String urgency;

    /** 试制数量 */
    @Excel(name = "试制数量")
    private Integer trialQuantity;

    /** 计划交样数量 */
    @Excel(name = "计划交样数量")
    private Integer sampleQuantity;

    /** 试制开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "试制开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date trialStart;

    /** 送样时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "送样时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date sampleDate;

    /** 工艺流程 */
    @Excel(name = "工艺流程")
    private String craftProcess;

    /** 实验项目(逗号分隔) */
    @Excel(name = "实验项目")
    private String experimentItems;

    /** 轮毂图片 */
    private String wheelImage;

    /** 工序数据JSON */
    private String processData;

    /** 通知单状态 */
    @Excel(name = "状态")
    private String status;

    /** 删除标志 */
    private String delFlag;

    // ===== Getters and Setters =====

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }

    public String getNoticeCode() { return noticeCode; }
    public void setNoticeCode(String noticeCode) { this.noticeCode = noticeCode; }

    public String getWheelCode() { return wheelCode; }
    public void setWheelCode(String wheelCode) { this.wheelCode = wheelCode; }

    public String getTrialStatus() { return trialStatus; }
    public void setTrialStatus(String trialStatus) { this.trialStatus = trialStatus; }

    public String getTrialType() { return trialType; }
    public void setTrialType(String trialType) { this.trialType = trialType; }

    public String getDevType() { return devType; }
    public void setDevType(String devType) { this.devType = devType; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getResponsible() { return responsible; }
    public void setResponsible(String responsible) { this.responsible = responsible; }

    public String getSurfaceStatus() { return surfaceStatus; }
    public void setSurfaceStatus(String surfaceStatus) { this.surfaceStatus = surfaceStatus; }

    public String getSizeSpec() { return sizeSpec; }
    public void setSizeSpec(String sizeSpec) { this.sizeSpec = sizeSpec; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public Integer getTrialQuantity() { return trialQuantity; }
    public void setTrialQuantity(Integer trialQuantity) { this.trialQuantity = trialQuantity; }

    public Integer getSampleQuantity() { return sampleQuantity; }
    public void setSampleQuantity(Integer sampleQuantity) { this.sampleQuantity = sampleQuantity; }

    public Date getTrialStart() { return trialStart; }
    public void setTrialStart(Date trialStart) { this.trialStart = trialStart; }

    public Date getSampleDate() { return sampleDate; }
    public void setSampleDate(Date sampleDate) { this.sampleDate = sampleDate; }

    public String getCraftProcess() { return craftProcess; }
    public void setCraftProcess(String craftProcess) { this.craftProcess = craftProcess; }

    public String getExperimentItems() { return experimentItems; }
    public void setExperimentItems(String experimentItems) { this.experimentItems = experimentItems; }

    public String getWheelImage() { return wheelImage; }
    public void setWheelImage(String wheelImage) { this.wheelImage = wheelImage; }

    public String getProcessData() { return processData; }
    public void setProcessData(String processData) { this.processData = processData; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("noticeId", getNoticeId())
            .append("noticeCode", getNoticeCode())
            .append("wheelCode", getWheelCode())
            .append("trialStatus", getTrialStatus())
            .append("trialType", getTrialType())
            .append("customerName", getCustomerName())
            .append("responsible", getResponsible())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
