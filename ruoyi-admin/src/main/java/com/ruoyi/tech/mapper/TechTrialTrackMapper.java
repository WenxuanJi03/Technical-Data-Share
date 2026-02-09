package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechTrialTrack;

public interface TechTrialTrackMapper
{
    public TechTrialTrack selectTechTrialTrackByTrackId(Long trackId);
    public List<TechTrialTrack> selectTechTrialTrackList(TechTrialTrack techTrialTrack);
    public int insertTechTrialTrack(TechTrialTrack techTrialTrack);
    public int updateTechTrialTrack(TechTrialTrack techTrialTrack);
    public int deleteTechTrialTrackByTrackId(Long trackId);
    public int deleteTechTrialTrackByTrackIds(Long[] trackIds);
}
