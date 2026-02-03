package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechDocument;

/**
 * 技术文档Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechDocumentService 
{
    /**
     * 查询技术文档
     * 
     * @param docId 技术文档主键
     * @return 技术文档
     */
    public TechDocument selectTechDocumentByDocId(Long docId);

    /**
     * 查询技术文档列表
     * 
     * @param techDocument 技术文档
     * @return 技术文档集合
     */
    public List<TechDocument> selectTechDocumentList(TechDocument techDocument);

    /**
     * 新增技术文档
     * 
     * @param techDocument 技术文档
     * @return 结果
     */
    public int insertTechDocument(TechDocument techDocument);

    /**
     * 修改技术文档
     * 
     * @param techDocument 技术文档
     * @return 结果
     */
    public int updateTechDocument(TechDocument techDocument);

    /**
     * 批量删除技术文档
     * 
     * @param docIds 需要删除的技术文档主键集合
     * @return 结果
     */
    public int deleteTechDocumentByDocIds(Long[] docIds);

    /**
     * 删除技术文档信息
     * 
     * @param docId 技术文档主键
     * @return 结果
     */
    public int deleteTechDocumentByDocId(Long docId);
}
