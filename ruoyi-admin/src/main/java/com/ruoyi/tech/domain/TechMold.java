package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 模具档案对象 tech_mold
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechMold extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模具ID */
    private Long moldId;

    /** 模具编号 */
    @Excel(name = "模具编号")
    private String moldCode;

    /** 模具名称 */
    @Excel(name = "模具名称")
    private String moldName;

    /** 模具类型 */
    @Excel(name = "模具类型")
    private String moldType;

    /** 关联项目ID */
    @Excel(name = "关联项目ID")
    private Long projectId;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specification;

    /** 模具材料 */
    @Excel(name = "模具材料")
    private String material;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;

    /** 模具状态 */
    @Excel(name = "模具状态")
    private String status;

    /** 存放位置 */
    @Excel(name = "存放位置")
    private String location;

    /** 设计寿命(次) */
    @Excel(name = "设计寿命(次)")
    private Long designLife;

    /** 当前使用次数 */
    @Excel(name = "当前使用次数")
    private Long currentCount;

    /** 制造日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "制造日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date manufactureDate;

    /** 验收日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "验收日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date acceptanceDate;

    /** 删除标志 */
    private String delFlag;

    public void setMoldId(Long moldId) 
    {
        this.moldId = moldId;
    }

    public Long getMoldId() 
    {
        return moldId;
    }

    public void setMoldCode(String moldCode) 
    {
        this.moldCode = moldCode;
    }

    public String getMoldCode() 
    {
        return moldCode;
    }

    public void setMoldName(String moldName) 
    {
        this.moldName = moldName;
    }

    public String getMoldName() 
    {
        return moldName;
    }

    public void setMoldType(String moldType) 
    {
        this.moldType = moldType;
    }

    public String getMoldType() 
    {
        return moldType;
    }

    public void setProjectId(Long projectId) 
    {
        this.projectId = projectId;
    }

    public Long getProjectId() 
    {
        return projectId;
    }

    public void setProductName(String productName) 
    {
        this.productName = productName;
    }

    public String getProductName() 
    {
        return productName;
    }

    public void setSpecification(String specification) 
    {
        this.specification = specification;
    }

    public String getSpecification() 
    {
        return specification;
    }

    public void setMaterial(String material) 
    {
        this.material = material;
    }

    public String getMaterial() 
    {
        return material;
    }

    public void setSupplier(String supplier) 
    {
        this.supplier = supplier;
    }

    public String getSupplier() 
    {
        return supplier;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setDesignLife(Long designLife) 
    {
        this.designLife = designLife;
    }

    public Long getDesignLife() 
    {
        return designLife;
    }

    public void setCurrentCount(Long currentCount) 
    {
        this.currentCount = currentCount;
    }

    public Long getCurrentCount() 
    {
        return currentCount;
    }

    public void setManufactureDate(Date manufactureDate) 
    {
        this.manufactureDate = manufactureDate;
    }

    public Date getManufactureDate() 
    {
        return manufactureDate;
    }

    public void setAcceptanceDate(Date acceptanceDate) 
    {
        this.acceptanceDate = acceptanceDate;
    }

    public Date getAcceptanceDate() 
    {
        return acceptanceDate;
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
            .append("moldId", getMoldId())
            .append("moldCode", getMoldCode())
            .append("moldName", getMoldName())
            .append("moldType", getMoldType())
            .append("projectId", getProjectId())
            .append("productName", getProductName())
            .append("specification", getSpecification())
            .append("material", getMaterial())
            .append("supplier", getSupplier())
            .append("status", getStatus())
            .append("location", getLocation())
            .append("designLife", getDesignLife())
            .append("currentCount", getCurrentCount())
            .append("manufactureDate", getManufactureDate())
            .append("acceptanceDate", getAcceptanceDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
