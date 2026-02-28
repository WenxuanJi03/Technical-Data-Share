package com.ruoyi.tech.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.tech.service.ITechDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 技术看板统计 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/tech/dashboard")
public class TechDashboardController extends BaseController {

    @Autowired
    private ITechDashboardService dashboardService;

    /**
     * 获取OE试制跟踪表统计数据
     */
    @GetMapping("/trialTrackStats")
    public AjaxResult getTrialTrackStats() {
        Map<String, Object> stats = dashboardService.getTrialTrackStats();
        return success(stats);
    }

    /**
     * 获取产品清单统计数据
     */
    @GetMapping("/productStats")
    public AjaxResult getProductStats() {
        Map<String, Object> stats = dashboardService.getProductStats();
        return success(stats);
    }

    /**
     * 获取所有看板数据（一次性返回）
     */
    @GetMapping("/allStats")
    public AjaxResult getAllStats() {
        Map<String, Object> result = dashboardService.getAllDashboardStats();
        return success(result);
    }
}
