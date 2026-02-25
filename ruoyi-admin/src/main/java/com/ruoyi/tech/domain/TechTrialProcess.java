package com.ruoyi.tech.domain;

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

    /** 项目编号 */
    @Excel(name = "项目编号")
    private String projectCode;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 模号（关联OE试制跟踪的模号） */
    @Excel(name = "模号")
    private String moldCode;

    /** 发起人 */
    @Excel(name = "发起人")
    private String initiator;

    /** 试制说明 */
    private String description;

    /** 流程状态 */
    @Excel(name = "流程状态")
    private String status;

    // ===== 6个步骤的状态 =====
    private String step1Status;
    private String step2Status;
    private String step3Status;
    private String step4Status;
    private String step5Status;
    private String step6Status;

    // ===== 6个步骤的截止日期 =====
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step1Deadline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step2Deadline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step3Deadline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step4Deadline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step5Deadline;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date step6Deadline;

    // ===== 6个步骤的负责人 =====
    private String step1Responsible;
    private String step2Responsible;
    private String step3Responsible;
    private String step4Responsible;
    private String step5Responsible;
    private String step6Responsible;

    // ===== 6个步骤的文件(JSON) =====
    private String step1Files;
    private String step2Files;
    private String step3Files;
    private String step4Files;
    private String step5Files;
    private String step6Files;

    // ===== 6个步骤的意见(JSON) =====
    private String step1Comments;
    private String step2Comments;
    private String step3Comments;
    private String step4Comments;
    private String step5Comments;
    private String step6Comments;

    /** 删除标志 */
    private String delFlag;

    // ===== Getters and Setters =====
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getMoldCode() { return moldCode; }
    public void setMoldCode(String moldCode) { this.moldCode = moldCode; }

    public String getInitiator() { return initiator; }
    public void setInitiator(String initiator) { this.initiator = initiator; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStep1Status() { return step1Status; }
    public void setStep1Status(String step1Status) { this.step1Status = step1Status; }
    public String getStep2Status() { return step2Status; }
    public void setStep2Status(String step2Status) { this.step2Status = step2Status; }
    public String getStep3Status() { return step3Status; }
    public void setStep3Status(String step3Status) { this.step3Status = step3Status; }
    public String getStep4Status() { return step4Status; }
    public void setStep4Status(String step4Status) { this.step4Status = step4Status; }
    public String getStep5Status() { return step5Status; }
    public void setStep5Status(String step5Status) { this.step5Status = step5Status; }
    public String getStep6Status() { return step6Status; }
    public void setStep6Status(String step6Status) { this.step6Status = step6Status; }

    public Date getStep1Deadline() { return step1Deadline; }
    public void setStep1Deadline(Date step1Deadline) { this.step1Deadline = step1Deadline; }
    public Date getStep2Deadline() { return step2Deadline; }
    public void setStep2Deadline(Date step2Deadline) { this.step2Deadline = step2Deadline; }
    public Date getStep3Deadline() { return step3Deadline; }
    public void setStep3Deadline(Date step3Deadline) { this.step3Deadline = step3Deadline; }
    public Date getStep4Deadline() { return step4Deadline; }
    public void setStep4Deadline(Date step4Deadline) { this.step4Deadline = step4Deadline; }
    public Date getStep5Deadline() { return step5Deadline; }
    public void setStep5Deadline(Date step5Deadline) { this.step5Deadline = step5Deadline; }
    public Date getStep6Deadline() { return step6Deadline; }
    public void setStep6Deadline(Date step6Deadline) { this.step6Deadline = step6Deadline; }

    public String getStep1Responsible() { return step1Responsible; }
    public void setStep1Responsible(String step1Responsible) { this.step1Responsible = step1Responsible; }
    public String getStep2Responsible() { return step2Responsible; }
    public void setStep2Responsible(String step2Responsible) { this.step2Responsible = step2Responsible; }
    public String getStep3Responsible() { return step3Responsible; }
    public void setStep3Responsible(String step3Responsible) { this.step3Responsible = step3Responsible; }
    public String getStep4Responsible() { return step4Responsible; }
    public void setStep4Responsible(String step4Responsible) { this.step4Responsible = step4Responsible; }
    public String getStep5Responsible() { return step5Responsible; }
    public void setStep5Responsible(String step5Responsible) { this.step5Responsible = step5Responsible; }
    public String getStep6Responsible() { return step6Responsible; }
    public void setStep6Responsible(String step6Responsible) { this.step6Responsible = step6Responsible; }

    public String getStep1Files() { return step1Files; }
    public void setStep1Files(String step1Files) { this.step1Files = step1Files; }
    public String getStep2Files() { return step2Files; }
    public void setStep2Files(String step2Files) { this.step2Files = step2Files; }
    public String getStep3Files() { return step3Files; }
    public void setStep3Files(String step3Files) { this.step3Files = step3Files; }
    public String getStep4Files() { return step4Files; }
    public void setStep4Files(String step4Files) { this.step4Files = step4Files; }
    public String getStep5Files() { return step5Files; }
    public void setStep5Files(String step5Files) { this.step5Files = step5Files; }
    public String getStep6Files() { return step6Files; }
    public void setStep6Files(String step6Files) { this.step6Files = step6Files; }

    public String getStep1Comments() { return step1Comments; }
    public void setStep1Comments(String step1Comments) { this.step1Comments = step1Comments; }
    public String getStep2Comments() { return step2Comments; }
    public void setStep2Comments(String step2Comments) { this.step2Comments = step2Comments; }
    public String getStep3Comments() { return step3Comments; }
    public void setStep3Comments(String step3Comments) { this.step3Comments = step3Comments; }
    public String getStep4Comments() { return step4Comments; }
    public void setStep4Comments(String step4Comments) { this.step4Comments = step4Comments; }
    public String getStep5Comments() { return step5Comments; }
    public void setStep5Comments(String step5Comments) { this.step5Comments = step5Comments; }
    public String getStep6Comments() { return step6Comments; }
    public void setStep6Comments(String step6Comments) { this.step6Comments = step6Comments; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("processId", getProcessId())
            .append("projectCode", getProjectCode())
            .append("projectName", getProjectName())
            .append("initiator", getInitiator())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
