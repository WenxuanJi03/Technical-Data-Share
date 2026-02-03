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
import com.ruoyi.tech.domain.TechTrial;
import com.ruoyi.tech.service.ITechTrialService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 试制任务Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/trial")
public class TechTrialController extends BaseController
{
    @Autowired
    private ITechTrialService techTrialService;

    /**
     * 查询试制任务列表
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechTrial techTrial)
    {
        startPage();
        List<TechTrial> list = techTrialService.selectTechTrialList(techTrial);
        return getDataTable(list);
    }

    /**
     * 导出试制任务列表
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:export')")
    @Log(title = "试制任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechTrial techTrial)
    {
        List<TechTrial> list = techTrialService.selectTechTrialList(techTrial);
        ExcelUtil<TechTrial> util = new ExcelUtil<TechTrial>(TechTrial.class);
        util.exportExcel(response, list, "试制任务数据");
    }

    /**
     * 获取试制任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:query')")
    @GetMapping(value = "/{trialId}")
    public AjaxResult getInfo(@PathVariable("trialId") Long trialId)
    {
        return success(techTrialService.selectTechTrialByTrialId(trialId));
    }

    /**
     * 新增试制任务
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:add')")
    @Log(title = "试制任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechTrial techTrial)
    {
        return toAjax(techTrialService.insertTechTrial(techTrial));
    }

    /**
     * 修改试制任务
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:edit')")
    @Log(title = "试制任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechTrial techTrial)
    {
        return toAjax(techTrialService.updateTechTrial(techTrial));
    }

    /**
     * 删除试制任务
     */
    @PreAuthorize("@ss.hasPermi('tech:trial:remove')")
    @Log(title = "试制任务", businessType = BusinessType.DELETE)
	@DeleteMapping("/{trialIds}")
    public AjaxResult remove(@PathVariable Long[] trialIds)
    {
        return toAjax(techTrialService.deleteTechTrialByTrialIds(trialIds));
    }
}
