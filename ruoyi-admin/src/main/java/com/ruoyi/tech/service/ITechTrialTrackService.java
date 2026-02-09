package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechTrialTrack;

public interface ITechTrialTrackService
{
    public TechTrialTrack selectTechTrialTrackByTrackId(Long trackId);
    public List<TechTrialTrack> selectTechTrialTrackList(TechTrialTrack techTrialTrack);
    public int insertTechTrialTrack(TechTrialTrack techTrialTrack);
    public int updateTechTrialTrack(TechTrialTrack techTrialTrack);
    public int deleteTechTrialTrackByTrackIds(Long[] trackIds);
    public int deleteTechTrialTrackByTrackId(Long trackId);
    public String importTracks(List<TechTrialTrack> trackList, boolean isUpdateSupport, String operName);
}
