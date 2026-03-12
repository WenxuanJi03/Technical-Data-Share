package com.ruoyi.tech.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 毛胚图对象 tech_blank_image
 * 对应金山文档"长沙戴湘最新版本毛坯图"表格
 *
 * @author ruoyi
 * @date 2026-03-12
 */
public class TechBlankImage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long blankId;

    /** 模号（如 009） */
    @Excel(name = "模号")
    private String moldNo;

    /** 型号（如 00919F03），唯一业务键 */
    @Excel(name = "型号")
    private String modelCode;

    /** 版本（A/B/C） */
    @Excel(name = "版本")
    private String version;

    /** 下发时间 */
    @Excel(name = "下发时间")
    private String releaseDate;

    /** 毛胚图图片路径 */
    private String blankImage;

    /** 删除标志（0正常 1删除） */
    private String delFlag;

    // ==================== Getters and Setters ====================

    public Long getBlankId() {
        return blankId;
    }

    public void setBlankId(Long blankId) {
        this.blankId = blankId;
    }

    public String getMoldNo() {
        return moldNo;
    }

    public void setMoldNo(String moldNo) {
        this.moldNo = moldNo;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getBlankImage() {
        return blankImage;
    }

    public void setBlankImage(String blankImage) {
        this.blankImage = blankImage;
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
                .append("blankId", getBlankId())
                .append("moldNo", getMoldNo())
                .append("modelCode", getModelCode())
                .append("version", getVersion())
                .append("releaseDate", getReleaseDate())
                .toString();
    }
}
