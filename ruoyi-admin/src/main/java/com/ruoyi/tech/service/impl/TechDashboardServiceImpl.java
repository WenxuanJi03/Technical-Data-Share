package com.ruoyi.tech.service.impl;

import com.ruoyi.tech.domain.TechProduct;
import com.ruoyi.tech.domain.TechTrialTrack;
import com.ruoyi.tech.mapper.TechProductMapper;
import com.ruoyi.tech.mapper.TechTrialTrackMapper;
import com.ruoyi.tech.service.ITechDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 技术看板统计 Service实现
 * 
 * @author ruoyi
 */
@Service
public class TechDashboardServiceImpl implements ITechDashboardService {

    @Autowired
    private TechTrialTrackMapper trialTrackMapper;

    @Autowired
    private TechProductMapper productMapper;

    @Override
    public Map<String, Object> getTrialTrackStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有试制跟踪记录
        List<TechTrialTrack> allTracks = trialTrackMapper.selectTechTrialTrackList(new TechTrialTrack());
        
        // 统计总数
        result.put("total", allTracks.size());
        
        // 统计模具类型
        Map<String, Long> moldTypeStats = allTracks.stream()
            .filter(t -> t.getMoldType() != null && !t.getMoldType().trim().isEmpty())
            .collect(Collectors.groupingBy(TechTrialTrack::getMoldType, Collectors.counting()));
        result.put("moldTypeStats", convertToStatsList(moldTypeStats));
        result.put("moldTypeTotal", moldTypeStats.size());
        
        // 统计表面状态
        Map<String, Long> surfaceStats = allTracks.stream()
            .filter(t -> t.getSurfaceStatus() != null && !t.getSurfaceStatus().trim().isEmpty())
            .collect(Collectors.groupingBy(TechTrialTrack::getSurfaceStatus, Collectors.counting()));
        result.put("surfaceStatusStats", convertToStatsList(surfaceStats));
        result.put("surfaceStatusTotal", surfaceStats.size());
        
        // 统计上机类型
        Map<String, Long> machineTypeStats = allTracks.stream()
            .filter(t -> t.getMachineType() != null && !t.getMachineType().trim().isEmpty())
            .collect(Collectors.groupingBy(TechTrialTrack::getMachineType, Collectors.counting()));
        result.put("machineTypeStats", convertToStatsList(machineTypeStats));
        result.put("machineTypeTotal", machineTypeStats.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getProductStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有产品
        List<TechProduct> allProducts = productMapper.selectTechProductList(new TechProduct());
        
        // 统计总数
        result.put("total", allProducts.size());
        
        // 统计产品状态
        Map<String, Long> statusStats = allProducts.stream()
            .filter(p -> p.getProductStatus() != null && !p.getProductStatus().trim().isEmpty())
            .collect(Collectors.groupingBy(TechProduct::getProductStatus, Collectors.counting()));
        result.put("productStatusStats", convertToStatsList(statusStats));
        
        // 统计客户
        Map<String, Long> customerStats = allProducts.stream()
            .filter(p -> p.getCustomer() != null && !p.getCustomer().trim().isEmpty())
            .collect(Collectors.groupingBy(TechProduct::getCustomer, Collectors.counting()));
        result.put("customerStats", convertToStatsList(customerStats));
        result.put("customerTotal", customerStats.size());
        
        // 统计规格（需要排序）
        Map<String, Long> specStats = allProducts.stream()
            .filter(p -> p.getSizeSpec() != null && !p.getSizeSpec().trim().isEmpty())
            .collect(Collectors.groupingBy(TechProduct::getSizeSpec, Collectors.counting()));
        result.put("sizeSpecStats", convertToSortedSpecList(specStats));
        result.put("sizeSpecTotal", specStats.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getAllDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("trialTrack", getTrialTrackStats());
        result.put("product", getProductStats());
        return result;
    }

    /**
     * 将统计Map转换为列表格式
     */
    private List<Map<String, Object>> convertToStatsList(Map<String, Long> statsMap) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statsMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            list.add(item);
        }
        // 按数量降序排序
        list.sort((a, b) -> Long.compare((Long)b.get("value"), (Long)a.get("value")));
        return list;
    }

    /**
     * 将规格统计转换为排序后的列表（按规格数值从小到大）
     */
    private List<Map<String, Object>> convertToSortedSpecList(Map<String, Long> statsMap) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statsMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            // 尝试提取数字进行排序
            item.put("sortKey", extractNumber(entry.getKey()));
            list.add(item);
        }
        // 按规格数值排序
        list.sort(Comparator.comparing(m -> (Double)m.get("sortKey")));
        // 移除sortKey
        list.forEach(m -> m.remove("sortKey"));
        return list;
    }

    /**
     * 从规格字符串中提取数字用于排序
     */
    private Double extractNumber(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // 提取字符串中的第一个数字
            String numStr = spec.replaceAll("[^0-9.]", "");
            if (!numStr.isEmpty()) {
                return Double.parseDouble(numStr);
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
        }
        return 0.0;
    }
}
