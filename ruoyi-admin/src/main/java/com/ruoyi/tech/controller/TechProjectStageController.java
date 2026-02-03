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
import com.ruoyi.tech.domain.TechProjectStage;
import com.ruoyi.tech.service.ITechProjectStageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目阶段记录Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/stage")
public class TechProjectStageController extends BaseController
{
    @Autowired
    private ITechProjectStageService techProjectStageService;

    /**
     * 查询项目阶段记录列表
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechProjectStage techProjectStage)
    {
        startPage();
        List<TechProjectStage> list = techProjectStageService.selectTechProjectStageList(techProjectStage);
        return getDataTable(list);
    }

    /**
     * 导出项目阶段记录列表
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:export')")
    @Log(title = "项目阶段记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechProjectStage techProjectStage)
    {
        List<TechProjectStage> list = techProjectStageService.selectTechProjectStageList(techProjectStage);
        ExcelUtil<TechProjectStage> util = new ExcelUtil<TechProjectStage>(TechProjectStage.class);
        util.exportExcel(response, list, "项目阶段记录数据");
    }

    /**
     * 获取项目阶段记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:query')")
    @GetMapping(value = "/{stageId}")
    public AjaxResult getInfo(@PathVariable("stageId") Long stageId)
    {
        return success(techProjectStageService.selectTechProjectStageByStageId(stageId));
    }

    /**
     * 新增项目阶段记录
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:add')")
    @Log(title = "项目阶段记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechProjectStage techProjectStage)
    {
        return toAjax(techProjectStageService.insertTechProjectStage(techProjectStage));
    }

    /**
     * 修改项目阶段记录
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:edit')")
    @Log(title = "项目阶段记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechProjectStage techProjectStage)
    {
        return toAjax(techProjectStageService.updateTechProjectStage(techProjectStage));
    }

    /**
     * 删除项目阶段记录
     */
    @PreAuthorize("@ss.hasPermi('tech:stage:remove')")
    @Log(title = "项目阶段记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{stageIds}")
    public AjaxResult remove(@PathVariable Long[] stageIds)
    {
        return toAjax(techProjectStageService.deleteTechProjectStageByStageIds(stageIds));
    }
}
