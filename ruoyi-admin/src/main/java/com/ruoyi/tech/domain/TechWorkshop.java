package com.ruoyi.tech.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车间对象 tech_workshop
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechWorkshop extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 车间ID */
    private Long workshopId;

    /** 车间代码 */
    @Excel(name = "车间代码")
    private String workshopCode;

    /** 车间名称 */
    @Excel(name = "车间名称")
    private String workshopName;

    /** 车间顺序 */
    @Excel(name = "车间顺序")
    private Integer workshopOrder;

    /** 负责人ID */
    @Excel(name = "负责人ID")
    private Long leaderId;

    /** 负责人姓名 */
    @Excel(name = "负责人姓名")
    private String leaderName;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setWorkshopId(Long workshopId) 
    {
        this.workshopId = workshopId;
    }

    public Long getWorkshopId() 
    {
        return workshopId;
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

    public void setWorkshopOrder(Integer workshopOrder) 
    {
        this.workshopOrder = workshopOrder;
    }

    public Integer getWorkshopOrder() 
    {
        return workshopOrder;
    }

    public void setLeaderId(Long leaderId) 
    {
        this.leaderId = leaderId;
    }

    public Long getLeaderId() 
    {
        return leaderId;
    }

    public void setLeaderName(String leaderName) 
    {
        this.leaderName = leaderName;
    }

    public String getLeaderName() 
    {
        return leaderName;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("workshopId", getWorkshopId())
            .append("workshopCode", getWorkshopCode())
            .append("workshopName", getWorkshopName())
            .append("workshopOrder", getWorkshopOrder())
            .append("leaderId", getLeaderId())
            .append("leaderName", getLeaderName())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
