package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(TechDocumentServiceImpl.class);

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

    /**
     * 导入技术文档数据
     * 
     * @param docList 技术文档数据列表
     * @param isUpdateSupport 是否更新支持
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importDocuments(List<TechDocument> docList, boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(docList) || docList.size() == 0)
        {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (TechDocument doc : docList)
        {
            try
            {
                if (StringUtils.isEmpty(doc.getDocName()))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、文档名称为空，跳过");
                    continue;
                }
                doc.setCreateBy(operName);
                doc.setCreateTime(DateUtils.getNowDate());
                if (StringUtils.isEmpty(doc.getStatus()))
                {
                    doc.setStatus("0");
                }
                if (StringUtils.isEmpty(doc.getVersion()))
                {
                    doc.setVersion("V1.0");
                }
                // 检查是否已存在相同编号的文档
                if (StringUtils.isNotEmpty(doc.getDocCode()))
                {
                    TechDocument query = new TechDocument();
                    query.setDocCode(doc.getDocCode());
                    List<TechDocument> existList = techDocumentMapper.selectTechDocumentList(query);
                    if (existList != null && existList.size() > 0)
                    {
                        if (isUpdateSupport)
                        {
                            TechDocument existDoc = existList.get(0);
                            doc.setDocId(existDoc.getDocId());
                            doc.setUpdateBy(operName);
                            doc.setUpdateTime(DateUtils.getNowDate());
                            techDocumentMapper.updateTechDocument(doc);
                            successNum++;
                            successMsg.append("<br/>").append(successNum).append("、文档 ").append(doc.getDocName()).append(" 更新成功");
                        }
                        else
                        {
                            failureNum++;
                            failureMsg.append("<br/>").append(failureNum).append("、文档编号 ").append(doc.getDocCode()).append(" 已存在");
                        }
                        continue;
                    }
                }
                techDocumentMapper.insertTechDocument(doc);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、文档 ").append(doc.getDocName()).append(" 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、文档 " + doc.getDocName() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
