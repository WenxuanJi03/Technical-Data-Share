package com.ruoyi.tech.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tech.domain.TechDocument;
import com.ruoyi.tech.service.ITechDocumentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 技术文档Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/document")
public class TechDocumentController extends BaseController
{
    @Autowired
    private ITechDocumentService techDocumentService;

    /**
     * 查询技术文档列表
     */
    @PreAuthorize("@ss.hasPermi('tech:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechDocument techDocument)
    {
        startPage();
        List<TechDocument> list = techDocumentService.selectTechDocumentList(techDocument);
        return getDataTable(list);
    }

    /**
     * 导出技术文档列表
     */
    @PreAuthorize("@ss.hasPermi('tech:document:export')")
    @Log(title = "技术文档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechDocument techDocument)
    {
        List<TechDocument> list = techDocumentService.selectTechDocumentList(techDocument);
        ExcelUtil<TechDocument> util = new ExcelUtil<TechDocument>(TechDocument.class);
        util.exportExcel(response, list, "技术文档数据");
    }

    /**
     * 获取技术文档详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:document:query')")
    @GetMapping(value = "/{docId}")
    public AjaxResult getInfo(@PathVariable("docId") Long docId)
    {
        return success(techDocumentService.selectTechDocumentByDocId(docId));
    }

    /**
     * 新增技术文档
     */
    @PreAuthorize("@ss.hasPermi('tech:document:add')")
    @Log(title = "技术文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechDocument techDocument)
    {
        techDocument.setCreateBy(getUsername());
        return toAjax(techDocumentService.insertTechDocument(techDocument));
    }

    /**
     * 修改技术文档
     */
    @PreAuthorize("@ss.hasPermi('tech:document:edit')")
    @Log(title = "技术文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechDocument techDocument)
    {
        techDocument.setUpdateBy(getUsername());
        return toAjax(techDocumentService.updateTechDocument(techDocument));
    }

    /**
     * 删除技术文档
     */
    @PreAuthorize("@ss.hasPermi('tech:document:remove')")
    @Log(title = "技术文档", businessType = BusinessType.DELETE)
	@DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds)
    {
        return toAjax(techDocumentService.deleteTechDocumentByDocIds(docIds));
    }

    /**
     * 导入技术文档数据
     */
    @Log(title = "技术文档", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:document:add')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<TechDocument> util = new ExcelUtil<TechDocument>(TechDocument.class);
        List<TechDocument> docList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = techDocumentService.importDocuments(docList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<TechDocument> util = new ExcelUtil<TechDocument>(TechDocument.class);
        util.importTemplateExcel(response, "技术文档数据");
    }
}
