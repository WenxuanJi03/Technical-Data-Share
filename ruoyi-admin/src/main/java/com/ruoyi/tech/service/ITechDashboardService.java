package com.ruoyi.tech.service;

import java.util.Map;

/**
 * 技术看板统计 Service接口
 * 
 * @author ruoyi
 */
public interface ITechDashboardService {
    
    /**
     * 获取OE试制跟踪表统计数据
     * @return 统计数据Map
     */
    Map<String, Object> getTrialTrackStats();
    
    /**
     * 获取产品清单统计数据
     * @return 统计数据Map
     */
    Map<String, Object> getProductStats();
    
    /**
     * 获取所有看板统计数据
     * @return 所有统计数据
     */
    Map<String, Object> getAllDashboardStats();
}
