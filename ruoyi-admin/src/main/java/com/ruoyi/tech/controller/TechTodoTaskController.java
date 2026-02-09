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
import com.ruoyi.tech.domain.TechTodoTask;
import com.ruoyi.tech.service.ITechTodoTaskService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 待办任务Controller
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@RestController
@RequestMapping("/tech/todoTask")
public class TechTodoTaskController extends BaseController {
    @Autowired
    private ITechTodoTaskService techTodoTaskService;

    /**
     * 查询待办任务列表
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechTodoTask techTodoTask) {
        startPage();
        List<TechTodoTask> list = techTodoTaskService.selectTechTodoTaskList(techTodoTask);
        return getDataTable(list);
    }

    /**
     * 导出待办任务列表
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:export')")
    @Log(title = "待办任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechTodoTask techTodoTask) {
        List<TechTodoTask> list = techTodoTaskService.selectTechTodoTaskList(techTodoTask);
        ExcelUtil<TechTodoTask> util = new ExcelUtil<TechTodoTask>(TechTodoTask.class);
        util.exportExcel(response, list, "待办任务数据");
    }

    /**
     * 获取待办任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId) {
        return success(techTodoTaskService.selectTechTodoTaskByTaskId(taskId));
    }

    /**
     * 新增待办任务
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:add')")
    @Log(title = "待办任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechTodoTask techTodoTask) {
        return toAjax(techTodoTaskService.insertTechTodoTask(techTodoTask));
    }

    /**
     * 修改待办任务
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:edit')")
    @Log(title = "待办任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechTodoTask techTodoTask) {
        return toAjax(techTodoTaskService.updateTechTodoTask(techTodoTask));
    }

    /**
     * 删除待办任务
     */
    @PreAuthorize("@ss.hasPermi('tech:todoTask:remove')")
    @Log(title = "待办任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(techTodoTaskService.deleteTechTodoTaskByTaskIds(taskIds));
    }
}
