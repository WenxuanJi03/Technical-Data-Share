package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 变更申请对象 tech_change
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechChange extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 变更ID */
    private Long changeId;

    /** 变更编号 */
    @Excel(name = "变更编号")
    private String changeCode;

    /** 变更标题 */
    @Excel(name = "变更标题")
    private String changeTitle;

    /** 变更类型（design-设计变更 process-工艺变更 material-材料变更） */
    @Excel(name = "变更类型", readConverterExp = "d=esign-设计变更,p=rocess-工艺变更,m=aterial-材料变更")
    private String changeType;

    /** 关联项目ID */
    @Excel(name = "关联项目ID")
    private Long projectId;

    /** 关联项目名称 */
    @Excel(name = "关联项目名称")
    private String projectName;

    /** 变更状态 */
    @Excel(name = "变更状态")
    private String status;

    /** 紧急程度（0低 1中 2高） */
    @Excel(name = "紧急程度", readConverterExp = "0=低,1=中,2=高")
    private String urgency;

    /** 变更原因 */
    @Excel(name = "变更原因")
    private String changeReason;

    /** 变更内容 */
    @Excel(name = "变更内容")
    private String changeContent;

    /** 变更影响 */
    @Excel(name = "变更影响")
    private String changeEffect;

    /** 申请人ID */
    @Excel(name = "申请人ID")
    private Long applicantId;

    /** 申请人姓名 */
    @Excel(name = "申请人姓名")
    private String applicantName;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyTime;

    /** 审批人ID */
    @Excel(name = "审批人ID")
    private Long approverId;

    /** 审批人姓名 */
    @Excel(name = "审批人姓名")
    private String approverName;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date approveTime;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approveOpinion;

    /** 删除标志 */
    private String delFlag;

    public void setChangeId(Long changeId) 
    {
        this.changeId = changeId;
    }

    public Long getChangeId() 
    {
        return changeId;
    }

    public void setChangeCode(String changeCode) 
    {
        this.changeCode = changeCode;
    }

    public String getChangeCode() 
    {
        return changeCode;
    }

    public void setChangeTitle(String changeTitle) 
    {
        this.changeTitle = changeTitle;
    }

    public String getChangeTitle() 
    {
        return changeTitle;
    }

    public void setChangeType(String changeType) 
    {
        this.changeType = changeType;
    }

    public String getChangeType() 
    {
        return changeType;
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

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setUrgency(String urgency) 
    {
        this.urgency = urgency;
    }

    public String getUrgency() 
    {
        return urgency;
    }

    public void setChangeReason(String changeReason) 
    {
        this.changeReason = changeReason;
    }

    public String getChangeReason() 
    {
        return changeReason;
    }

    public void setChangeContent(String changeContent) 
    {
        this.changeContent = changeContent;
    }

    public String getChangeContent() 
    {
        return changeContent;
    }

    public void setChangeEffect(String changeEffect) 
    {
        this.changeEffect = changeEffect;
    }

    public String getChangeEffect() 
    {
        return changeEffect;
    }

    public void setApplicantId(Long applicantId) 
    {
        this.applicantId = applicantId;
    }

    public Long getApplicantId() 
    {
        return applicantId;
    }

    public void setApplicantName(String applicantName) 
    {
        this.applicantName = applicantName;
    }

    public String getApplicantName() 
    {
        return applicantName;
    }

    public void setApplyTime(Date applyTime) 
    {
        this.applyTime = applyTime;
    }

    public Date getApplyTime() 
    {
        return applyTime;
    }

    public void setApproverId(Long approverId) 
    {
        this.approverId = approverId;
    }

    public Long getApproverId() 
    {
        return approverId;
    }

    public void setApproverName(String approverName) 
    {
        this.approverName = approverName;
    }

    public String getApproverName() 
    {
        return approverName;
    }

    public void setApproveTime(Date approveTime) 
    {
        this.approveTime = approveTime;
    }

    public Date getApproveTime() 
    {
        return approveTime;
    }

    public void setApproveOpinion(String approveOpinion) 
    {
        this.approveOpinion = approveOpinion;
    }

    public String getApproveOpinion() 
    {
        return approveOpinion;
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
            .append("changeId", getChangeId())
            .append("changeCode", getChangeCode())
            .append("changeTitle", getChangeTitle())
            .append("changeType", getChangeType())
            .append("projectId", getProjectId())
            .append("projectName", getProjectName())
            .append("status", getStatus())
            .append("urgency", getUrgency())
            .append("changeReason", getChangeReason())
            .append("changeContent", getChangeContent())
            .append("changeEffect", getChangeEffect())
            .append("applicantId", getApplicantId())
            .append("applicantName", getApplicantName())
            .append("applyTime", getApplyTime())
            .append("approverId", getApproverId())
            .append("approverName", getApproverName())
            .append("approveTime", getApproveTime())
            .append("approveOpinion", getApproveOpinion())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
