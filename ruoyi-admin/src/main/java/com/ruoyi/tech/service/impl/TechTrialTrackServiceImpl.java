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
public class TechTrialTrackServiceImpl implements ITechTrialTrackService
{
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
    public String importTracks(List<TechTrialTrack> trackList, boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(trackList) || trackList.size() == 0) {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (TechTrialTrack track : trackList)
        {
            try {
                if (StringUtils.isEmpty(track.getMoldCode())) {
                    continue;
                }
                track.setCreateBy(operName);
                track.setCreateTime(DateUtils.getNowDate());
                // 以模号判重
                TechTrialTrack query = new TechTrialTrack();
                query.setMoldCode(track.getMoldCode());
                List<TechTrialTrack> existList = techTrialTrackMapper.selectTechTrialTrackList(query);
                if (existList != null && existList.size() > 0) {
                    if (isUpdateSupport) {
                        TechTrialTrack exist = existList.get(0);
                        track.setTrackId(exist.getTrackId());
                        track.setUpdateBy(operName);
                        track.setUpdateTime(DateUtils.getNowDate());
                        techTrialTrackMapper.updateTechTrialTrack(track);
                        successNum++;
                        successMsg.append("<br/>").append(successNum).append("、模号 ").append(track.getMoldCode()).append(" 更新成功");
                    } else {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、模号 ").append(track.getMoldCode()).append(" 已存在");
                    }
                } else {
                    techTrialTrackMapper.insertTechTrialTrack(track);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、模号 ").append(track.getMoldCode()).append(" 导入成功");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、模号 " + track.getMoldCode() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "导入失败！共 " + failureNum + " 条错误：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, "导入成功！共 " + successNum + " 条：");
        return successMsg.toString();
    }
}
