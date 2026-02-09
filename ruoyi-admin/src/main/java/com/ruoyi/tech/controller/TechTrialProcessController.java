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
import com.ruoyi.tech.domain.TechTrialProcess;
import com.ruoyi.tech.service.ITechTrialProcessService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 试制流程记录Controller
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@RestController
@RequestMapping("/tech/process")
public class TechTrialProcessController extends BaseController {
    @Autowired
    private ITechTrialProcessService techTrialProcessService;

    /**
     * 检查用户是否有逾期任务（截止日期<=今天 且 步骤未完成）
     */
    @GetMapping("/checkOverdue")
    public AjaxResult checkOverdue(@RequestParam(required = false) String userName) {
        if (userName == null || userName.isEmpty()) {
            return success(false);
        }
        return success(techTrialProcessService.checkUserOverdue(userName));
    }

    /**
     * 查询试制流程记录列表
     */
    @PreAuthorize("@ss.hasPermi('tech:process:list')")
    @GetMapping("/list")
    public TableDataInfo list(TechTrialProcess techTrialProcess) {
        startPage();
        List<TechTrialProcess> list = techTrialProcessService.selectTechTrialProcessList(techTrialProcess);
        return getDataTable(list);
    }

    /**
     * 导出试制流程记录列表
     */
    @PreAuthorize("@ss.hasPermi('tech:process:export')")
    @Log(title = "试制流程记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TechTrialProcess techTrialProcess) {
        List<TechTrialProcess> list = techTrialProcessService.selectTechTrialProcessList(techTrialProcess);
        ExcelUtil<TechTrialProcess> util = new ExcelUtil<TechTrialProcess>(TechTrialProcess.class);
        util.exportExcel(response, list, "试制流程记录数据");
    }

    /**
     * 获取试制流程记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('tech:process:query')")
    @GetMapping(value = "/{processId}")
    public AjaxResult getInfo(@PathVariable("processId") Long processId) {
        return success(techTrialProcessService.selectTechTrialProcessByProcessId(processId));
    }

    /**
     * 新增试制流程记录
     */
    @PreAuthorize("@ss.hasPermi('tech:process:add')")
    @Log(title = "试制流程记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TechTrialProcess techTrialProcess) {
        return toAjax(techTrialProcessService.insertTechTrialProcess(techTrialProcess));
    }

    /**
     * 修改试制流程记录
     */
    @PreAuthorize("@ss.hasPermi('tech:process:edit')")
    @Log(title = "试制流程记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TechTrialProcess techTrialProcess) {
        return toAjax(techTrialProcessService.updateTechTrialProcess(techTrialProcess));
    }

    /**
     * 删除试制流程记录
     */
    @PreAuthorize("@ss.hasPermi('tech:process:remove')")
    @Log(title = "试制流程记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{processIds}")
    public AjaxResult remove(@PathVariable Long[] processIds) {
        return toAjax(techTrialProcessService.deleteTechTrialProcessByProcessIds(processIds));
    }
}
