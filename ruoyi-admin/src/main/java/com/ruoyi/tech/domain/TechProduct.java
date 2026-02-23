package com.ruoyi.tech.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品清单对象 tech_product
 * 完整对应公司 DX-产品清单.xlsm 全部列（约40+列）
 * 
 * @author ruoyi
 * @date 2026-02-09
 */
public class TechProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 产品ID */
    private Long productId;

    // ==================== 基础信息 ====================

    /** 序号 */
    @Excel(name = "序号")
    private Integer serialNo;

    /** 轮型号 */
    @Excel(name = "轮型")
    private String wheelCode;

    /** 正面图路径 */
    private String frontImage;

    /** 客户 */
    @Excel(name = "客户")
    private String customer;

    /** 发货地 */
    @Excel(name = "发货地")
    private String shipLocation;

    /** 产品类型 */
    @Excel(name = "产品类型")
    private String productType;

    /** 产品来源 */
    @Excel(name = "产品来源")
    private String productSource;

    /** 首模号 */
    @Excel(name = "首模号")
    private String firstMoldNo;

    /** 首模来源 */
    @Excel(name = "首模来源")
    private String moldSource;

    /** 产品状态 */
    @Excel(name = "产品状态")
    private String productStatus;

    /** 状态备注 */
    @Excel(name = "状态备注")
    private String statusRemark;

    /** 相似轮型 */
    @Excel(name = "相似轮型")
    private String similarWheels;

    /** 相似轮型差异点 */
    @Excel(name = "相似轮型差异点")
    private String similarDiff;

    /** 规格 */
    @Excel(name = "规格")
    private String sizeSpec;

    /** 偏距ET */
    @Excel(name = "偏距ET")
    private String offsetEt;

    /** PCD */
    @Excel(name = "PCD")
    private String pcd;

    /** 中心孔直径 */
    @Excel(name = "中心孔直径")
    private String centerHole;

    // ==================== 基础参数 ====================

    /** 设计单重(kg) */
    @Excel(name = "设计单重")
    private String designWeight;

    /** 表面处理 */
    @Excel(name = "表面处理")
    private String surfaceTreatment;

    /** 颜色 */
    @Excel(name = "颜色")
    private String color;

    /** 标签 */
    @Excel(name = "标签")
    private String labelInfo;

    // ==================== 准备阶段里程碑 ====================

    /** 转移时间 */
    @Excel(name = "转移时间", dateFormat = "yyyy-MM-dd")
    private String transferTime;

    /** 内评时间 */
    @Excel(name = "内评时间", dateFormat = "yyyy-MM-dd")
    private String internalEvalTime;

    /** 工检清单时间 */
    @Excel(name = "工检清单时间", dateFormat = "yyyy-MM-dd")
    private String qualityCheckTime;

    /** 开模时间 */
    @Excel(name = "开模时间", dateFormat = "yyyy-MM-dd")
    private String moldOpenTime;

    /** 工检全部到厂时间 */
    @Excel(name = "工检全部到厂时间", dateFormat = "yyyy-MM-dd")
    private String qualityArrivalTime;

    /** 样轮到厂时间 */
    @Excel(name = "样轮到厂时间", dateFormat = "yyyy-MM-dd")
    private String sampleWheelTime;

    /** 油漆到厂时间 */
    @Excel(name = "油漆到厂时间", dateFormat = "yyyy-MM-dd")
    private String paintArrivalTime;

    /** 图纸文件全部签发时间 */
    @Excel(name = "图纸文件全部签发", dateFormat = "yyyy-MM-dd")
    private String drawingIssueTime;

    // ==================== 产品开发里程碑 ====================

    /** FEA分析结果 */
    @Excel(name = "FEA分析结果")
    private String feaResult;

    /** 首模到厂时间 */
    @Excel(name = "首模到厂时间", dateFormat = "yyyy-MM-dd")
    private String firstMoldArrival;

    /** 首上机时间 */
    @Excel(name = "首上机时间", dateFormat = "yyyy-MM-dd")
    private String firstMachineTime;

    /** 送样时间 */
    @Excel(name = "送样时间", dateFormat = "yyyy-MM-dd")
    private String sampleSubmitTime;

    /** 样件合格时间 */
    @Excel(name = "样件合格时间", dateFormat = "yyyy-MM-dd")
    private String samplePassTime;

    /** 送样履历 */
    @Excel(name = "送样履历")
    private String sampleHistory;

    /** 试制总结时间 */
    @Excel(name = "试制总结时间", dateFormat = "yyyy-MM-dd")
    private String trialSummaryTime;

    // ==================== 量产阶段里程碑 ====================

    /** 试制情况 */
    @Excel(name = "试制情况")
    private String trialSituation;

    /** 小批量总结时间 */
    @Excel(name = "小批量总结时间", dateFormat = "yyyy-MM-dd")
    private String batchSummaryTime;

    /** 小批量情况 */
    @Excel(name = "小批量情况")
    private String batchSituation;

    /** 影响交付时间 */
    @Excel(name = "影响交付时间", dateFormat = "yyyy-MM-dd")
    private String impactDeliveryTime;

    /** 影响交付事项 */
    @Excel(name = "影响交付事项")
    private String impactDeliveryItem;

    /** 量产时间 */
    @Excel(name = "量产时间", dateFormat = "yyyy-MM-dd")
    private String massProdTime;

    // ==================== 持续改进里程碑 ====================

    /** 最新变更断点关闭时间 */
    @Excel(name = "最新变更断点关闭时间", dateFormat = "yyyy-MM-dd")
    private String latestChangeTime;

    /** 最新变更内容 */
    @Excel(name = "最新变更内容")
    private String latestChangeContent;

    /** 控制要点 */
    @Excel(name = "控制要点")
    private String controlPoints;

    /** 删除标志 */
    private String delFlag;

    // ==================== Getters and Setters ====================

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(Integer serialNo) {
        this.serialNo = serialNo;
    }

    public String getWheelCode() {
        return wheelCode;
    }

    public void setWheelCode(String wheelCode) {
        this.wheelCode = wheelCode;
    }

    public String getFrontImage() {
        return frontImage;
    }

    public void setFrontImage(String frontImage) {
        this.frontImage = frontImage;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getShipLocation() {
        return shipLocation;
    }

    public void setShipLocation(String shipLocation) {
        this.shipLocation = shipLocation;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductSource() {
        return productSource;
    }

    public void setProductSource(String productSource) {
        this.productSource = productSource;
    }

    public String getFirstMoldNo() {
        return firstMoldNo;
    }

    public void setFirstMoldNo(String firstMoldNo) {
        this.firstMoldNo = firstMoldNo;
    }

    public String getMoldSource() {
        return moldSource;
    }

    public void setMoldSource(String moldSource) {
        this.moldSource = moldSource;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getStatusRemark() {
        return statusRemark;
    }

    public void setStatusRemark(String statusRemark) {
        this.statusRemark = statusRemark;
    }

    public String getSimilarWheels() {
        return similarWheels;
    }

    public void setSimilarWheels(String similarWheels) {
        this.similarWheels = similarWheels;
    }

    public String getSimilarDiff() {
        return similarDiff;
    }

    public void setSimilarDiff(String similarDiff) {
        this.similarDiff = similarDiff;
    }

    public String getSizeSpec() {
        return sizeSpec;
    }

    public void setSizeSpec(String sizeSpec) {
        this.sizeSpec = sizeSpec;
    }

    public String getOffsetEt() {
        return offsetEt;
    }

    public void setOffsetEt(String offsetEt) {
        this.offsetEt = offsetEt;
    }

    public String getPcd() {
        return pcd;
    }

    public void setPcd(String pcd) {
        this.pcd = pcd;
    }

    public String getCenterHole() {
        return centerHole;
    }

    public void setCenterHole(String centerHole) {
        this.centerHole = centerHole;
    }

    public String getDesignWeight() {
        return designWeight;
    }

    public void setDesignWeight(String designWeight) {
        this.designWeight = designWeight;
    }

    public String getSurfaceTreatment() {
        return surfaceTreatment;
    }

    public void setSurfaceTreatment(String surfaceTreatment) {
        this.surfaceTreatment = surfaceTreatment;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLabelInfo() {
        return labelInfo;
    }

    public void setLabelInfo(String labelInfo) {
        this.labelInfo = labelInfo;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public String getInternalEvalTime() {
        return internalEvalTime;
    }

    public void setInternalEvalTime(String internalEvalTime) {
        this.internalEvalTime = internalEvalTime;
    }

    public String getQualityCheckTime() {
        return qualityCheckTime;
    }

    public void setQualityCheckTime(String qualityCheckTime) {
        this.qualityCheckTime = qualityCheckTime;
    }

    public String getMoldOpenTime() {
        return moldOpenTime;
    }

    public void setMoldOpenTime(String moldOpenTime) {
        this.moldOpenTime = moldOpenTime;
    }

    public String getQualityArrivalTime() {
        return qualityArrivalTime;
    }

    public void setQualityArrivalTime(String qualityArrivalTime) {
        this.qualityArrivalTime = qualityArrivalTime;
    }

    public String getSampleWheelTime() {
        return sampleWheelTime;
    }

    public void setSampleWheelTime(String sampleWheelTime) {
        this.sampleWheelTime = sampleWheelTime;
    }

    public String getPaintArrivalTime() {
        return paintArrivalTime;
    }

    public void setPaintArrivalTime(String paintArrivalTime) {
        this.paintArrivalTime = paintArrivalTime;
    }

    public String getDrawingIssueTime() {
        return drawingIssueTime;
    }

    public void setDrawingIssueTime(String drawingIssueTime) {
        this.drawingIssueTime = drawingIssueTime;
    }

    public String getFeaResult() {
        return feaResult;
    }

    public void setFeaResult(String feaResult) {
        this.feaResult = feaResult;
    }

    public String getFirstMoldArrival() {
        return firstMoldArrival;
    }

    public void setFirstMoldArrival(String firstMoldArrival) {
        this.firstMoldArrival = firstMoldArrival;
    }

    public String getFirstMachineTime() {
        return firstMachineTime;
    }

    public void setFirstMachineTime(String firstMachineTime) {
        this.firstMachineTime = firstMachineTime;
    }

    public String getSampleSubmitTime() {
        return sampleSubmitTime;
    }

    public void setSampleSubmitTime(String sampleSubmitTime) {
        this.sampleSubmitTime = sampleSubmitTime;
    }

    public String getSamplePassTime() {
        return samplePassTime;
    }

    public void setSamplePassTime(String samplePassTime) {
        this.samplePassTime = samplePassTime;
    }

    public String getSampleHistory() {
        return sampleHistory;
    }

    public void setSampleHistory(String sampleHistory) {
        this.sampleHistory = sampleHistory;
    }

    public String getTrialSummaryTime() {
        return trialSummaryTime;
    }

    public void setTrialSummaryTime(String trialSummaryTime) {
        this.trialSummaryTime = trialSummaryTime;
    }

    public String getTrialSituation() {
        return trialSituation;
    }

    public void setTrialSituation(String trialSituation) {
        this.trialSituation = trialSituation;
    }

    public String getBatchSummaryTime() {
        return batchSummaryTime;
    }

    public void setBatchSummaryTime(String batchSummaryTime) {
        this.batchSummaryTime = batchSummaryTime;
    }

    public String getBatchSituation() {
        return batchSituation;
    }

    public void setBatchSituation(String batchSituation) {
        this.batchSituation = batchSituation;
    }

    public String getImpactDeliveryTime() {
        return impactDeliveryTime;
    }

    public void setImpactDeliveryTime(String impactDeliveryTime) {
        this.impactDeliveryTime = impactDeliveryTime;
    }

    public String getImpactDeliveryItem() {
        return impactDeliveryItem;
    }

    public void setImpactDeliveryItem(String impactDeliveryItem) {
        this.impactDeliveryItem = impactDeliveryItem;
    }

    public String getMassProdTime() {
        return massProdTime;
    }

    public void setMassProdTime(String massProdTime) {
        this.massProdTime = massProdTime;
    }

    public String getLatestChangeTime() {
        return latestChangeTime;
    }

    public void setLatestChangeTime(String latestChangeTime) {
        this.latestChangeTime = latestChangeTime;
    }

    public String getLatestChangeContent() {
        return latestChangeContent;
    }

    public void setLatestChangeContent(String latestChangeContent) {
        this.latestChangeContent = latestChangeContent;
    }

    public String getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(String controlPoints) {
        this.controlPoints = controlPoints;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("productId", getProductId())
                .append("wheelCode", getWheelCode())
                .append("customer", getCustomer())
                .append("productStatus", getProductStatus())
                .append("sizeSpec", getSizeSpec())
                .toString();
    }
}
