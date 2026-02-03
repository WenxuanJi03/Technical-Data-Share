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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tech.domain.TechChange;
import com.ruoyi.tech.service.ITechChangeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 变更申请Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/change")
public class TechChangeController extends BaseController
{
    @Autowired
    private ITechChangeService techChangeService;

    /**
     * 查询变更申请列表
     */
    @PreAuthorize("@ss.hasPermi('tech:change:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechChange techChange)
    {
        startPage();
        List<TechChange> list = techChangeService.selectTechChangeList(techChange);
        return getDataTable(list);
    }

    /**
     * 导出变更申请列表
     */
    @PreAuthorize("@ss.hasPermi('tech:change:export')")
    @Log(title = "变更申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechChange techChange)
    {
        List<TechChange> list = techChangeService.selectTechChangeList(techChange);
        ExcelUtil<TechChange> util = new ExcelUtil<TechChange>(TechChange.class);
        util.exportExcel(response, list, "变更申请数据");
    }

    /**
     * 获取变更申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:change:query')")
    @GetMapping(value = "/{changeId}")
    public AjaxResult getInfo(@PathVariable("changeId") Long changeId)
    {
        return success(techChangeService.selectTechChangeByChangeId(changeId));
    }

    /**
     * 新增变更申请
     */
    @PreAuthorize("@ss.hasPermi('tech:change:add')")
    @Log(title = "变更申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechChange techChange)
    {
        return toAjax(techChangeService.insertTechChange(techChange));
    }

    /**
     * 修改变更申请
     */
    @PreAuthorize("@ss.hasPermi('tech:change:edit')")
    @Log(title = "变更申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechChange techChange)
    {
        return toAjax(techChangeService.updateTechChange(techChange));
    }

    /**
     * 删除变更申请
     */
    @PreAuthorize("@ss.hasPermi('tech:change:remove')")
    @Log(title = "变更申请", businessType = BusinessType.DELETE)
	@DeleteMapping("/{changeIds}")
    public AjaxResult remove(@PathVariable Long[] changeIds)
    {
        return toAjax(techChangeService.deleteTechChangeByChangeIds(changeIds));
    }
}
