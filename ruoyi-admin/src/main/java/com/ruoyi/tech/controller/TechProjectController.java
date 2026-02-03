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
import com.ruoyi.tech.domain.TechProject;
import com.ruoyi.tech.service.ITechProjectService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 项目Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/project")
public class TechProjectController extends BaseController
{
    @Autowired
    private ITechProjectService techProjectService;

    /**
     * 查询项目列表
     */
    @PreAuthorize("@ss.hasPermi('tech:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechProject techProject)
    {
        startPage();
        List<TechProject> list = techProjectService.selectTechProjectList(techProject);
        return getDataTable(list);
    }

    /**
     * 导出项目列表
     */
    @PreAuthorize("@ss.hasPermi('tech:project:export')")
    @Log(title = "项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechProject techProject)
    {
        List<TechProject> list = techProjectService.selectTechProjectList(techProject);
        ExcelUtil<TechProject> util = new ExcelUtil<TechProject>(TechProject.class);
        util.exportExcel(response, list, "项目数据");
    }

    /**
     * 获取项目详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:project:query')")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId)
    {
        return success(techProjectService.selectTechProjectByProjectId(projectId));
    }

    /**
     * 新增项目
     */
    @PreAuthorize("@ss.hasPermi('tech:project:add')")
    @Log(title = "项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechProject techProject)
    {
        return toAjax(techProjectService.insertTechProject(techProject));
    }

    /**
     * 修改项目
     */
    @PreAuthorize("@ss.hasPermi('tech:project:edit')")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechProject techProject)
    {
        return toAjax(techProjectService.updateTechProject(techProject));
    }

    /**
     * 删除项目
     */
    @PreAuthorize("@ss.hasPermi('tech:project:remove')")
    @Log(title = "项目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(techProjectService.deleteTechProjectByProjectIds(projectIds));
    }
}
