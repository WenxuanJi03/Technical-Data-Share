package com.ruoyi.tech.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OE试制跟踪对象 tech_trial_track
 * 对应 DX-OE试制跟踪表 全部列
 */
public class TechTrialTrack extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long trackId;

    // ========== 基础信息 ==========
    @Excel(name = "模号")
    private String moldCode;

    @Excel(name = "产品规格")
    private String productSpec;

    @Excel(name = "模具类型")
    private String moldType;

    @Excel(name = "表面状态")
    private String surfaceStatus;

    @Excel(name = "上机类型")
    private String machineType;

    @Excel(name = "上机次数")
    private Integer machineCount;

    @Excel(name = "预计上机时间")
    private String planMachineTime;

    // ========== 热工阶段 ==========
    @Excel(name = "热工上机日期")
    private String hotMachineDate;

    @Excel(name = "保圆时间")
    private String roundKeepTime;

    @Excel(name = "热工生产情况")
    private String hotProduction;

    @Excel(name = "改善记录")
    private String improveRecord;

    @Excel(name = "热工改善负责人")
    private String hotImprovePerson;

    // ========== 旋压阶段 ==========
    @Excel(name = "旋压上机日期")
    private String spinMachineDate;

    @Excel(name = "旋压机台")
    private String spinMachineStation;

    @Excel(name = "旋压生产情况")
    private String spinProduction;

    @Excel(name = "改模记录2")
    private String moldModifyRecord;

    @Excel(name = "旋压改善负责人")
    private String spinImprovePerson;

    // ========== 粗车阶段 ==========
    @Excel(name = "粗车上机日期")
    private String roughMachineDate;

    @Excel(name = "粗车生产情况")
    private String roughProduction;

    @Excel(name = "粗车问题改善负责人")
    private String roughImprovePerson;

    @Excel(name = "改善方案")
    private String improvePlan;

    // ========== 精车+涂装阶段 ==========
    @Excel(name = "精车上机日期")
    private String fineMachineDate;

    @Excel(name = "精车生产情况")
    private String fineProduction;

    @Excel(name = "涂装上机日期")
    private String paintMachineDate;

    @Excel(name = "涂装生产情况")
    private String paintProduction;

    @Excel(name = "涂装问题改善负责人")
    private String paintImprovePerson;

    // ========== 实验/总结 ==========
    @Excel(name = "冲击试验日")
    private String impactTestDate;

    @Excel(name = "冲击试验结果")
    private String impactTestResult;

    @Excel(name = "生产完成日期")
    private String completeDate;

    @Excel(name = "失效产品溯源")
    private String failProductTrace;

    @Excel(name = "实验失效分析")
    private String failAnalysis;

    @Excel(name = "本次生产总结")
    private String productionSummary;

    @Excel(name = "改善措施简述")
    private String improveMeasures;

    @Excel(name = "经验教训总结")
    private String lessonsLearned;

    @Excel(name = "全序是否完成")
    private String allProcessDone;

    private String delFlag;

    // ========== Getters and Setters ==========
    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }
    public String getMoldCode() { return moldCode; }
    public void setMoldCode(String moldCode) { this.moldCode = moldCode; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public String getMoldType() { return moldType; }
    public void setMoldType(String moldType) { this.moldType = moldType; }
    public String getSurfaceStatus() { return surfaceStatus; }
    public void setSurfaceStatus(String surfaceStatus) { this.surfaceStatus = surfaceStatus; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public Integer getMachineCount() { return machineCount; }
    public void setMachineCount(Integer machineCount) { this.machineCount = machineCount; }
    public String getPlanMachineTime() { return planMachineTime; }
    public void setPlanMachineTime(String planMachineTime) { this.planMachineTime = planMachineTime; }
    public String getHotMachineDate() { return hotMachineDate; }
    public void setHotMachineDate(String hotMachineDate) { this.hotMachineDate = hotMachineDate; }
    public String getRoundKeepTime() { return roundKeepTime; }
    public void setRoundKeepTime(String roundKeepTime) { this.roundKeepTime = roundKeepTime; }
    public String getHotProduction() { return hotProduction; }
    public void setHotProduction(String hotProduction) { this.hotProduction = hotProduction; }
    public String getImproveRecord() { return improveRecord; }
    public void setImproveRecord(String improveRecord) { this.improveRecord = improveRecord; }
    public String getHotImprovePerson() { return hotImprovePerson; }
    public void setHotImprovePerson(String hotImprovePerson) { this.hotImprovePerson = hotImprovePerson; }
    public String getSpinMachineDate() { return spinMachineDate; }
    public void setSpinMachineDate(String spinMachineDate) { this.spinMachineDate = spinMachineDate; }
    public String getSpinMachineStation() { return spinMachineStation; }
    public void setSpinMachineStation(String spinMachineStation) { this.spinMachineStation = spinMachineStation; }
    public String getSpinProduction() { return spinProduction; }
    public void setSpinProduction(String spinProduction) { this.spinProduction = spinProduction; }
    public String getMoldModifyRecord() { return moldModifyRecord; }
    public void setMoldModifyRecord(String moldModifyRecord) { this.moldModifyRecord = moldModifyRecord; }
    public String getSpinImprovePerson() { return spinImprovePerson; }
    public void setSpinImprovePerson(String spinImprovePerson) { this.spinImprovePerson = spinImprovePerson; }
    public String getRoughMachineDate() { return roughMachineDate; }
    public void setRoughMachineDate(String roughMachineDate) { this.roughMachineDate = roughMachineDate; }
    public String getRoughProduction() { return roughProduction; }
    public void setRoughProduction(String roughProduction) { this.roughProduction = roughProduction; }
    public String getRoughImprovePerson() { return roughImprovePerson; }
    public void setRoughImprovePerson(String roughImprovePerson) { this.roughImprovePerson = roughImprovePerson; }
    public String getImprovePlan() { return improvePlan; }
    public void setImprovePlan(String improvePlan) { this.improvePlan = improvePlan; }
    public String getFineMachineDate() { return fineMachineDate; }
    public void setFineMachineDate(String fineMachineDate) { this.fineMachineDate = fineMachineDate; }
    public String getFineProduction() { return fineProduction; }
    public void setFineProduction(String fineProduction) { this.fineProduction = fineProduction; }
    public String getPaintMachineDate() { return paintMachineDate; }
    public void setPaintMachineDate(String paintMachineDate) { this.paintMachineDate = paintMachineDate; }
    public String getPaintProduction() { return paintProduction; }
    public void setPaintProduction(String paintProduction) { this.paintProduction = paintProduction; }
    public String getPaintImprovePerson() { return paintImprovePerson; }
    public void setPaintImprovePerson(String paintImprovePerson) { this.paintImprovePerson = paintImprovePerson; }
    public String getImpactTestDate() { return impactTestDate; }
    public void setImpactTestDate(String impactTestDate) { this.impactTestDate = impactTestDate; }
    public String getImpactTestResult() { return impactTestResult; }
    public void setImpactTestResult(String impactTestResult) { this.impactTestResult = impactTestResult; }
    public String getCompleteDate() { return completeDate; }
    public void setCompleteDate(String completeDate) { this.completeDate = completeDate; }
    public String getFailProductTrace() { return failProductTrace; }
    public void setFailProductTrace(String failProductTrace) { this.failProductTrace = failProductTrace; }
    public String getFailAnalysis() { return failAnalysis; }
    public void setFailAnalysis(String failAnalysis) { this.failAnalysis = failAnalysis; }
    public String getProductionSummary() { return productionSummary; }
    public void setProductionSummary(String productionSummary) { this.productionSummary = productionSummary; }
    public String getImproveMeasures() { return improveMeasures; }
    public void setImproveMeasures(String improveMeasures) { this.improveMeasures = improveMeasures; }
    public String getLessonsLearned() { return lessonsLearned; }
    public void setLessonsLearned(String lessonsLearned) { this.lessonsLearned = lessonsLearned; }
    public String getAllProcessDone() { return allProcessDone; }
    public void setAllProcessDone(String allProcessDone) { this.allProcessDone = allProcessDone; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
