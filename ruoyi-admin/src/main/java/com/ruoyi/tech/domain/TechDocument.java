package com.ruoyi.tech.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 技术文档对象 tech_document
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class TechDocument extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long docId;

    /** 文档编号 */
    @Excel(name = "文档编号")
    private String docCode;

    /** 文档名称 */
    @Excel(name = "文档名称")
    private String docName;

    /** 文档类型（design-设计 process-工艺 quality-质量 other-其他） */
    @Excel(name = "文档类型", readConverterExp = "d=esign-设计,p=rocess-工艺,q=uality-质量,o=ther-其他")
    private String docType;

    /** 关联项目ID */
    @Excel(name = "关联项目ID")
    private Long projectId;

    /** 关联项目名称 */
    @Excel(name = "关联项目名称")
    private String projectName;

    /** 版本号 */
    @Excel(name = "版本号")
    private String version;

    /** 文件路径 */
    @Excel(name = "文件路径")
    private String filePath;

    /** 文件大小(字节) */
    @Excel(name = "文件大小(字节)")
    private Long fileSize;

    /** 文件类型(pdf/doc/xls等) */
    @Excel(name = "文件类型(pdf/doc/xls等)")
    private String fileType;

    /** 状态（0草稿 1已发布 2已归档） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已发布,2=已归档")
    private String status;

    /** 作者ID */
    @Excel(name = "作者ID")
    private Long authorId;

    /** 作者姓名 */
    @Excel(name = "作者姓名")
    private String authorName;

    /** 删除标志 */
    private String delFlag;

    public void setDocId(Long docId) 
    {
        this.docId = docId;
    }

    public Long getDocId() 
    {
        return docId;
    }

    public void setDocCode(String docCode) 
    {
        this.docCode = docCode;
    }

    public String getDocCode() 
    {
        return docCode;
    }

    public void setDocName(String docName) 
    {
        this.docName = docName;
    }

    public String getDocName() 
    {
        return docName;
    }

    public void setDocType(String docType) 
    {
        this.docType = docType;
    }

    public String getDocType() 
    {
        return docType;
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

    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }

    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }

    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setAuthorId(Long authorId) 
    {
        this.authorId = authorId;
    }

    public Long getAuthorId() 
    {
        return authorId;
    }

    public void setAuthorName(String authorName) 
    {
        this.authorName = authorName;
    }

    public String getAuthorName() 
    {
        return authorName;
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
            .append("docId", getDocId())
            .append("docCode", getDocCode())
            .append("docName", getDocName())
            .append("docType", getDocType())
            .append("projectId", getProjectId())
            .append("projectName", getProjectName())
            .append("version", getVersion())
            .append("filePath", getFilePath())
            .append("fileSize", getFileSize())
            .append("fileType", getFileType())
            .append("status", getStatus())
            .append("authorId", getAuthorId())
            .append("authorName", getAuthorName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
