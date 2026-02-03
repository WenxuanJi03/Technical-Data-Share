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
import com.ruoyi.tech.domain.TechMold;
import com.ruoyi.tech.service.ITechMoldService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 模具档案Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/mold")
public class TechMoldController extends BaseController
{
    @Autowired
    private ITechMoldService techMoldService;

    /**
     * 查询模具档案列表
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechMold techMold)
    {
        startPage();
        List<TechMold> list = techMoldService.selectTechMoldList(techMold);
        return getDataTable(list);
    }

    /**
     * 导出模具档案列表
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:export')")
    @Log(title = "模具档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechMold techMold)
    {
        List<TechMold> list = techMoldService.selectTechMoldList(techMold);
        ExcelUtil<TechMold> util = new ExcelUtil<TechMold>(TechMold.class);
        util.exportExcel(response, list, "模具档案数据");
    }

    /**
     * 获取模具档案详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:query')")
    @GetMapping(value = "/{moldId}")
    public AjaxResult getInfo(@PathVariable("moldId") Long moldId)
    {
        return success(techMoldService.selectTechMoldByMoldId(moldId));
    }

    /**
     * 新增模具档案
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:add')")
    @Log(title = "模具档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechMold techMold)
    {
        return toAjax(techMoldService.insertTechMold(techMold));
    }

    /**
     * 修改模具档案
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:edit')")
    @Log(title = "模具档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechMold techMold)
    {
        return toAjax(techMoldService.updateTechMold(techMold));
    }

    /**
     * 删除模具档案
     */
    @PreAuthorize("@ss.hasPermi('tech:mold:remove')")
    @Log(title = "模具档案", businessType = BusinessType.DELETE)
	@DeleteMapping("/{moldIds}")
    public AjaxResult remove(@PathVariable Long[] moldIds)
    {
        return toAjax(techMoldService.deleteTechMoldByMoldIds(moldIds));
    }
}
