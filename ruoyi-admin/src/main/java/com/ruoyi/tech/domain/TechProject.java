package com.ruoyi.tech.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目对象 tech_project
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 项目编号 */
    @Excel(name = "项目编号")
    private String projectCode;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 项目类型 */
    @Excel(name = "项目类型")
    private String projectType;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customer;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;

    /** 项目阶段 */
    @Excel(name = "项目阶段")
    private String stage;

    /** 项目状态（0正常 1暂停 2终止） */
    @Excel(name = "项目状态", readConverterExp = "0=正常,1=暂停,2=终止")
    private String status;

    /** 优先级（0低 1中 2高 3紧急） */
    @Excel(name = "优先级", readConverterExp = "0=低,1=中,2=高,3=紧急")
    private String priority;

    /** 计划开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 计划结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 实际结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualEndDate;

    /** 项目负责人ID */
    @Excel(name = "项目负责人ID")
    private Long managerId;

    /** 项目负责人姓名 */
    @Excel(name = "项目负责人姓名")
    private String managerName;

    /** 所属部门ID */
    @Excel(name = "所属部门ID")
    private Long deptId;

    /** 项目描述 */
    @Excel(name = "项目描述")
    private String description;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public void setProjectId(Long projectId) 
    {
        this.projectId = projectId;
    }

    public Long getProjectId() 
    {
        return projectId;
    }

    public void setProjectCode(String projectCode) 
    {
        this.projectCode = projectCode;
    }

    public String getProjectCode() 
    {
        return projectCode;
    }

    public void setProjectName(String projectName) 
    {
        this.projectName = projectName;
    }

    public String getProjectName() 
    {
        return projectName;
    }

    public void setProjectType(String projectType) 
    {
        this.projectType = projectType;
    }

    public String getProjectType() 
    {
        return projectType;
    }

    public void setCustomer(String customer) 
    {
        this.customer = customer;
    }

    public String getCustomer() 
    {
        return customer;
    }

    public void setProductName(String productName) 
    {
        this.productName = productName;
    }

    public String getProductName() 
    {
        return productName;
    }

    public void setStage(String stage) 
    {
        this.stage = stage;
    }

    public String getStage() 
    {
        return stage;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setPriority(String priority) 
    {
        this.priority = priority;
    }

    public String getPriority() 
    {
        return priority;
    }

    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }

    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }

    public void setActualEndDate(Date actualEndDate) 
    {
        this.actualEndDate = actualEndDate;
    }

    public Date getActualEndDate() 
    {
        return actualEndDate;
    }

    public void setManagerId(Long managerId) 
    {
        this.managerId = managerId;
    }

    public Long getManagerId() 
    {
        return managerId;
    }

    public void setManagerName(String managerName) 
    {
        this.managerName = managerName;
    }

    public String getManagerName() 
    {
        return managerName;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
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
            .append("projectId", getProjectId())
            .append("projectCode", getProjectCode())
            .append("projectName", getProjectName())
            .append("projectType", getProjectType())
            .append("customer", getCustomer())
            .append("productName", getProductName())
            .append("stage", getStage())
            .append("status", getStatus())
            .append("priority", getPriority())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("actualEndDate", getActualEndDate())
            .append("managerId", getManagerId())
            .append("managerName", getManagerName())
            .append("deptId", getDeptId())
            .append("description", getDescription())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
