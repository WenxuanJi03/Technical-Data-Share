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
        int successNum = 0;
        int failureNum = 0;
        int skipNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        // 记录本批次已处理的模号，避免同一文件内重复模号多次插入
        java.util.Set<String> processedCodes = new java.util.HashSet<>();
        for (TechTrialTrack track : trackList) {
            try {
                if (track == null || StringUtils.isEmpty(track.getMoldCode())) {
                    continue;
                }
                String code = track.getMoldCode().trim();
                // 同一批次内跳过已处理的模号
                if (processedCodes.contains(code)) {
                    skipNum++;
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
                        successMsg.append("<br/>").append(successNum).append("、模号 ").append(code)
                                .append(" 更新成功");
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、模号 ").append(code)
                                .append(" 已存在");
                    }
                } else {
                    techTrialTrackMapper.insertTechTrialTrack(track);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、模号 ").append(code)
                            .append(" 导入成功");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、模号 " + (track != null ? track.getMoldCode() : "未知") + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        StringBuilder result = new StringBuilder();
        if (successNum > 0) {
            result.append("成功导入 ").append(successNum).append(" 条。");
        }
        if (skipNum > 0) {
            result.append("跳过 ").append(skipNum).append(" 条重复行。");
        }
        if (failureNum > 0) {
            result.append("失败 ").append(failureNum).append(" 条（已存在）。");
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
