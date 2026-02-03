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
import com.ruoyi.tech.domain.TechWorkshop;
import com.ruoyi.tech.service.ITechWorkshopService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 车间Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/workshop")
public class TechWorkshopController extends BaseController
{
    @Autowired
    private ITechWorkshopService techWorkshopService;

    /**
     * 查询车间列表
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechWorkshop techWorkshop)
    {
        startPage();
        List<TechWorkshop> list = techWorkshopService.selectTechWorkshopList(techWorkshop);
        return getDataTable(list);
    }

    /**
     * 导出车间列表
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:export')")
    @Log(title = "车间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechWorkshop techWorkshop)
    {
        List<TechWorkshop> list = techWorkshopService.selectTechWorkshopList(techWorkshop);
        ExcelUtil<TechWorkshop> util = new ExcelUtil<TechWorkshop>(TechWorkshop.class);
        util.exportExcel(response, list, "车间数据");
    }

    /**
     * 获取车间详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:query')")
    @GetMapping(value = "/{workshopId}")
    public AjaxResult getInfo(@PathVariable("workshopId") Long workshopId)
    {
        return success(techWorkshopService.selectTechWorkshopByWorkshopId(workshopId));
    }

    /**
     * 新增车间
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:add')")
    @Log(title = "车间", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechWorkshop techWorkshop)
    {
        return toAjax(techWorkshopService.insertTechWorkshop(techWorkshop));
    }

    /**
     * 修改车间
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:edit')")
    @Log(title = "车间", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechWorkshop techWorkshop)
    {
        return toAjax(techWorkshopService.updateTechWorkshop(techWorkshop));
    }

    /**
     * 删除车间
     */
    @PreAuthorize("@ss.hasPermi('tech:workshop:remove')")
    @Log(title = "车间", businessType = BusinessType.DELETE)
	@DeleteMapping("/{workshopIds}")
    public AjaxResult remove(@PathVariable Long[] workshopIds)
    {
        return toAjax(techWorkshopService.deleteTechWorkshopByWorkshopIds(workshopIds));
    }
}
