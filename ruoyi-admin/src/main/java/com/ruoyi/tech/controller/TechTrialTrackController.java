package com.ruoyi.tech.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.tech.domain.TechTrialTrack;
import com.ruoyi.tech.service.ITechTrialTrackService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * OE试制跟踪Controller
 */
@RestController
@RequestMapping("/tech/trialTrack")
public class TechTrialTrackController extends BaseController {
    @Autowired
    private ITechTrialTrackService techTrialTrackService;

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechTrialTrack techTrialTrack) {
        startPage();
        List<TechTrialTrack> list = techTrialTrackService.selectTechTrialTrackList(techTrialTrack);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:export')")
    @Log(title = "OE试制跟踪", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechTrialTrack techTrialTrack) {
        List<TechTrialTrack> list = techTrialTrackService.selectTechTrialTrackList(techTrialTrack);
        ExcelUtil<TechTrialTrack> util = new ExcelUtil<TechTrialTrack>(TechTrialTrack.class);
        util.exportExcel(response, list, "OE试制跟踪数据");
    }

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:query')")
    @GetMapping(value = "/{trackId}")
    public AjaxResult getInfo(@PathVariable("trackId") Long trackId) {
        return success(techTrialTrackService.selectTechTrialTrackByTrackId(trackId));
    }

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:add')")
    @Log(title = "OE试制跟踪", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechTrialTrack techTrialTrack) {
        techTrialTrack.setCreateBy(getUsername());
        return toAjax(techTrialTrackService.insertTechTrialTrack(techTrialTrack));
    }

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:edit')")
    @Log(title = "OE试制跟踪", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechTrialTrack techTrialTrack) {
        techTrialTrack.setUpdateBy(getUsername());
        return toAjax(techTrialTrackService.updateTechTrialTrack(techTrialTrack));
    }

    @PreAuthorize("@ss.hasPermi('tech:trialTrack:remove')")
    @Log(title = "OE试制跟踪", businessType = BusinessType.DELETE)
    @DeleteMapping("/{trackIds}")
    public AjaxResult remove(@PathVariable Long[] trackIds) {
        return toAjax(techTrialTrackService.deleteTechTrialTrackByTrackIds(trackIds));
    }

    /**
     * 清空OE试制跟踪（按搜索条件全选删除）
     */
    @PreAuthorize("@ss.hasPermi('tech:trialTrack:remove')")
    @Log(title = "OE试制跟踪", businessType = BusinessType.DELETE)
    @DeleteMapping("/cleanAll")
    public AjaxResult cleanAll(TechTrialTrack techTrialTrack) {
        int count = techTrialTrackService.cleanAllTracks(techTrialTrack);
        return success("成功删除 " + count + " 条试制跟踪记录");
    }

    @Log(title = "OE试制跟踪", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('tech:trialTrack:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(defaultValue = "0") int titleNum) throws Exception {
        ExcelUtil<TechTrialTrack> util = new ExcelUtil<TechTrialTrack>(TechTrialTrack.class);
        List<TechTrialTrack> list = util.importExcel(file.getInputStream(), titleNum);
        String message = techTrialTrackService.importTracks(list, updateSupport, getUsername());
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<TechTrialTrack> util = new ExcelUtil<TechTrialTrack>(TechTrialTrack.class);
        util.importTemplateExcel(response, "OE试制跟踪数据");
    }
}
