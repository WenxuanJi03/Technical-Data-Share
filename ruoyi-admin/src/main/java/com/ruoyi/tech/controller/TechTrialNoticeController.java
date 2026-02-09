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
import com.ruoyi.tech.domain.TechTrialNotice;
import com.ruoyi.tech.service.ITechTrialNoticeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 产品试制通知单Controller
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@RestController
@RequestMapping("/tech/notice")
public class TechTrialNoticeController extends BaseController {
    @Autowired
    private ITechTrialNoticeService techTrialNoticeService;

    /**
     * 查询产品试制通知单列表
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechTrialNotice techTrialNotice) {
        startPage();
        List<TechTrialNotice> list = techTrialNoticeService.selectTechTrialNoticeList(techTrialNotice);
        return getDataTable(list);
    }

    /**
     * 导出产品试制通知单列表
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:export')")
    @Log(title = "产品试制通知单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechTrialNotice techTrialNotice) {
        List<TechTrialNotice> list = techTrialNoticeService.selectTechTrialNoticeList(techTrialNotice);
        ExcelUtil<TechTrialNotice> util = new ExcelUtil<TechTrialNotice>(TechTrialNotice.class);
        util.exportExcel(response, list, "产品试制通知单数据");
    }

    /**
     * 获取产品试制通知单详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return success(techTrialNoticeService.selectTechTrialNoticeByNoticeId(noticeId));
    }

    /**
     * 新增产品试制通知单
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:add')")
    @Log(title = "产品试制通知单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechTrialNotice techTrialNotice) {
        techTrialNotice.setCreateBy(getUsername());
        return toAjax(techTrialNoticeService.insertTechTrialNotice(techTrialNotice));
    }

    /**
     * 修改产品试制通知单
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:edit')")
    @Log(title = "产品试制通知单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechTrialNotice techTrialNotice) {
        techTrialNotice.setUpdateBy(getUsername());
        return toAjax(techTrialNoticeService.updateTechTrialNotice(techTrialNotice));
    }

    /**
     * 删除产品试制通知单
     */
    @PreAuthorize("@ss.hasPermi('tech:notice:remove')")
    @Log(title = "产品试制通知单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(techTrialNoticeService.deleteTechTrialNoticeByNoticeIds(noticeIds));
    }

    /**
     * 导入产品试制通知单数据
     */
    @Log(title = "产品试制通知单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:notice:add')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<TechTrialNotice> util = new ExcelUtil<TechTrialNotice>(TechTrialNotice.class);
        List<TechTrialNotice> noticeList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = techTrialNoticeService.importNotices(noticeList, updateSupport, operName);
        return success(message);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<TechTrialNotice> util = new ExcelUtil<TechTrialNotice>(TechTrialNotice.class);
        util.importTemplateExcel(response, "产品试制通知单数据");
    }
}
