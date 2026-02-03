package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechDocumentMapper;
import com.ruoyi.tech.domain.TechDocument;
import com.ruoyi.tech.service.ITechDocumentService;

/**
 * 技术文档Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechDocumentServiceImpl implements ITechDocumentService 
{
    @Autowired
    private TechDocumentMapper techDocumentMapper;

    /**
     * 查询技术文档
     * 
     * @param docId 技术文档主键
     * @return 技术文档
     */
    @Override
    public TechDocument selectTechDocumentByDocId(Long docId)
    {
        return techDocumentMapper.selectTechDocumentByDocId(docId);
    }

    /**
     * 查询技术文档列表
     * 
     * @param techDocument 技术文档
     * @return 技术文档
     */
    @Override
    public List<TechDocument> selectTechDocumentList(TechDocument techDocument)
    {
        return techDocumentMapper.selectTechDocumentList(techDocument);
    }

    /**
     * 新增技术文档
     * 
     * @param techDocument 技术文档
     * @return 结果
     */
    @Override
    public int insertTechDocument(TechDocument techDocument)
    {
        techDocument.setCreateTime(DateUtils.getNowDate());
        return techDocumentMapper.insertTechDocument(techDocument);
    }

    /**
     * 修改技术文档
     * 
     * @param techDocument 技术文档
     * @return 结果
     */
    @Override
    public int updateTechDocument(TechDocument techDocument)
    {
        techDocument.setUpdateTime(DateUtils.getNowDate());
        return techDocumentMapper.updateTechDocument(techDocument);
    }

    /**
     * 批量删除技术文档
     * 
     * @param docIds 需要删除的技术文档主键
     * @return 结果
     */
    @Override
    public int deleteTechDocumentByDocIds(Long[] docIds)
    {
        return techDocumentMapper.deleteTechDocumentByDocIds(docIds);
    }

    /**
     * 删除技术文档信息
     * 
     * @param docId 技术文档主键
     * @return 结果
     */
    @Override
    public int deleteTechDocumentByDocId(Long docId)
    {
        return techDocumentMapper.deleteTechDocumentByDocId(docId);
    }
}
