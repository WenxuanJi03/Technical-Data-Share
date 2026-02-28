package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechTrialTrackMapper;
import com.ruoyi.tech.domain.TechTrialTrack;
import com.ruoyi.tech.service.ITechTrialTrackService;

@Service
public class TechTrialTrackServiceImpl implements ITechTrialTrackService {
    private static final Logger log = LoggerFactory.getLogger(TechTrialTrackServiceImpl.class);

    @Autowired
    private TechTrialTrackMapper techTrialTrackMapper;

    @Override
    public TechTrialTrack selectTechTrialTrackByTrackId(Long trackId) {
        return techTrialTrackMapper.selectTechTrialTrackByTrackId(trackId);
    }

    @Override
    public List<TechTrialTrack> selectTechTrialTrackList(TechTrialTrack t) {
        return techTrialTrackMapper.selectTechTrialTrackList(t);
    }

    @Override
    public int insertTechTrialTrack(TechTrialTrack t) {
        t.setCreateTime(DateUtils.getNowDate());
        return techTrialTrackMapper.insertTechTrialTrack(t);
    }

    @Override
    public int updateTechTrialTrack(TechTrialTrack t) {
        t.setUpdateTime(DateUtils.getNowDate());
        return techTrialTrackMapper.updateTechTrialTrack(t);
    }

    @Override
    public int deleteTechTrialTrackByTrackIds(Long[] trackIds) {
        return techTrialTrackMapper.deleteTechTrialTrackByTrackIds(trackIds);
    }

    @Override
    public int deleteTechTrialTrackByTrackId(Long trackId) {
        return techTrialTrackMapper.deleteTechTrialTrackByTrackId(trackId);
    }

    @Override
    public String importTracks(List<TechTrialTrack> trackList, boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(trackList) || trackList.size() == 0) {
            throw new ServiceException("导入数据不能为空！");
        }
        log.info("========== OE试制跟踪导入开始 ==========");
        log.info("Excel解析数据总行数: {}", trackList.size());
        log.info("更新模式: {}", isUpdateSupport ? "开启（更新已有记录）" : "关闭（跳过已有记录）");
        
        int successNum = 0;
        int failureNum = 0;
        int skipNum = 0;
        int emptyMoldNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        // 记录本批次已处理的模号，避免同一文件内重复模号多次插入
        java.util.Set<String> processedCodes = new java.util.HashSet<>();
        for (TechTrialTrack track : trackList) {
            try {
                if (track == null || StringUtils.isEmpty(track.getMoldCode())) {
                    emptyMoldNum++;
                    continue;
                }
                String code = track.getMoldCode().trim();
                // 同一批次内跳过已处理的模号
                if (processedCodes.contains(code)) {
                    skipNum++;
                    log.debug("跳过重复模号: {}", code);
                    continue;
                }
                processedCodes.add(code);
                track.setMoldCode(code);
                track.setCreateBy(operName);
                track.setCreateTime(DateUtils.getNowDate());
                // 以模号判重
                TechTrialTrack query = new TechTrialTrack();
                query.setMoldCode(code);
                List<TechTrialTrack> existList = techTrialTrackMapper.selectTechTrialTrackList(query);
                if (existList != null && existList.size() > 0) {
                    if (isUpdateSupport) {
                        TechTrialTrack exist = existList.get(0);
                        track.setTrackId(exist.getTrackId());
                        track.setUpdateBy(operName);
                        track.setUpdateTime(DateUtils.getNowDate());
                        techTrialTrackMapper.updateTechTrialTrack(track);
                        successNum++;
                        log.info("更新成功: 模号 {} (ID: {})", code, exist.getTrackId());
                        successMsg.append("<br/>").append(successNum).append("、模号 ").append(code)
                                .append(" 更新成功");
                    } else {
                        failureNum++;
                        log.warn("模号已存在跳过: {}", code);
                        failureMsg.append("<br/>").append(failureNum).append("、模号 ").append(code)
                                .append(" 已存在");
                    }
                } else {
                    techTrialTrackMapper.insertTechTrialTrack(track);
                    successNum++;
                    log.info("新增成功: 模号 {}", code);
                    successMsg.append("<br/>").append(successNum).append("、模号 ").append(code)
                            .append(" 导入成功");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、模号 " + (track != null ? track.getMoldCode() : "未知") + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error("导入失败: 模号 {} - {}", (track != null ? track.getMoldCode() : "未知"), e.getMessage(), e);
            }
        }
        log.info("========== OE试制跟踪导入结果统计 ==========");
        log.info("Excel总行数: {}", trackList.size());
        log.info("空模号跳过: {} 条", emptyMoldNum);
        log.info("文件内重复跳过: {} 条", skipNum);
        log.info("成功处理: {} 条", successNum);
        log.info("失败/已存在: {} 条", failureNum);
        log.info("==========================================");
        
        StringBuilder result = new StringBuilder();
        result.append("【导入统计】Excel共").append(trackList.size()).append("行数据。");
        if (emptyMoldNum > 0) {
            result.append("空模号跳过 ").append(emptyMoldNum).append(" 条。");
        }
        if (skipNum > 0) {
            result.append("文件内重复跳过 ").append(skipNum).append(" 条。");
        }
        if (successNum > 0) {
            result.append("成功导入 ").append(successNum).append(" 条。");
        }
        if (failureNum > 0) {
            result.append("失败/已存在 ").append(failureNum).append(" 条。");
        }
        if (successNum == 0 && failureNum == 0) {
            throw new ServiceException("未找到可导入的数据，请检查Excel表头是否匹配。");
        }
        if (successNum == 0 && failureNum > 0) {
            throw new ServiceException("导入失败！共 " + failureNum + " 条已存在，请勾选\"更新已有记录\"后重试。" + failureMsg);
        }
        return result.toString();
    }

    @Override
    public int cleanAllTracks(TechTrialTrack query) {
        List<TechTrialTrack> list = techTrialTrackMapper.selectTechTrialTrackList(query);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Long[] ids = list.stream().map(TechTrialTrack::getTrackId).toArray(Long[]::new);
        return techTrialTrackMapper.deleteTechTrialTrackByTrackIds(ids);
    }
}
