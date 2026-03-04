package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.tech.domain.TechTrialProcess;
import com.ruoyi.tech.mapper.TechTrialProcessMapper;
import com.ruoyi.tech.mapper.TechTrialTrackMapper;
import com.ruoyi.tech.domain.TechTrialTrack;
import com.ruoyi.tech.service.ITechTrialTrackService;

@Service
public class TechTrialTrackServiceImpl implements ITechTrialTrackService {
    private static final Logger log = LoggerFactory.getLogger(TechTrialTrackServiceImpl.class);
    private static final String TRACK_DONE = "是";
    private static final String PROCESS_DONE = "done";
    private static final String PROCESS_ACTIVE = "active";
    private static final String PROCESS_PENDING = "pending";
    private static final String SYNC_DESCRIPTION = "由OE试制跟踪自动同步创建";

    @Autowired
    private TechTrialTrackMapper techTrialTrackMapper;

    @Autowired
    private TechTrialProcessMapper techTrialProcessMapper;

    @Override
    public TechTrialTrack selectTechTrialTrackByTrackId(Long trackId) {
        return techTrialTrackMapper.selectTechTrialTrackByTrackId(trackId);
    }

    @Override
    public List<TechTrialTrack> selectTechTrialTrackList(TechTrialTrack t) {
        return techTrialTrackMapper.selectTechTrialTrackList(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTechTrialTrack(TechTrialTrack t) {
        normalizeMoldCode(t);
        t.setCreateTime(DateUtils.getNowDate());
        int rows = techTrialTrackMapper.insertTechTrialTrack(t);
        if (rows > 0) {
            syncTrialProcess(t, null);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTechTrialTrack(TechTrialTrack t) {
        TechTrialTrack beforeUpdate = null;
        if (t.getTrackId() != null) {
            beforeUpdate = techTrialTrackMapper.selectTechTrialTrackByTrackId(t.getTrackId());
        }
        normalizeMoldCode(t);
        t.setUpdateTime(DateUtils.getNowDate());
        int rows = techTrialTrackMapper.updateTechTrialTrack(t);
        if (rows > 0 && t.getTrackId() != null) {
            TechTrialTrack latest = techTrialTrackMapper.selectTechTrialTrackByTrackId(t.getTrackId());
            if (latest != null) {
                String oldMoldCode = beforeUpdate == null ? null : beforeUpdate.getMoldCode();
                syncTrialProcess(latest, oldMoldCode);
            }
        }
        return rows;
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
                        syncTrialProcess(track, exist.getMoldCode());
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
                    syncTrialProcess(track, null);
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

    private void normalizeMoldCode(TechTrialTrack techTrialTrack) {
        if (techTrialTrack == null || techTrialTrack.getMoldCode() == null) {
            return;
        }
        techTrialTrack.setMoldCode(techTrialTrack.getMoldCode().trim());
    }

    private void syncTrialProcess(TechTrialTrack techTrialTrack, String oldMoldCode) {
        String moldCode = StringUtils.trimToNull(techTrialTrack.getMoldCode());
        if (moldCode == null) {
            return;
        }

        TechTrialProcess existed = techTrialProcessMapper.selectTechTrialProcessByMoldCode(moldCode);
        if (existed == null) {
            String oldCode = StringUtils.trimToNull(oldMoldCode);
            if (oldCode != null && !StringUtils.equals(oldCode, moldCode)) {
                existed = techTrialProcessMapper.selectTechTrialProcessByMoldCode(oldCode);
            }
        }

        if (existed == null) {
            TechTrialProcess createData = new TechTrialProcess();
            createData.setMoldCode(moldCode);
            createData.setInitiator(resolveOperator(techTrialTrack));
            createData.setDescription(SYNC_DESCRIPTION);
            applyDefaultStatus(createData);
            if (isTrackDone(techTrialTrack)) {
                applyDoneStatus(createData);
            }
            createData.setCreateBy(resolveOperator(techTrialTrack));
            createData.setCreateTime(DateUtils.getNowDate());
            techTrialProcessMapper.insertTechTrialProcess(createData);
            return;
        }

        TechTrialProcess updateData = new TechTrialProcess();
        updateData.setProcessId(existed.getProcessId());
        updateData.setMoldCode(moldCode);
        updateData.setUpdateBy(resolveOperator(techTrialTrack));
        updateData.setUpdateTime(DateUtils.getNowDate());
        if (isTrackDone(techTrialTrack) && !isProcessDone(existed)) {
            applyDoneStatus(updateData);
        }
        techTrialProcessMapper.updateTechTrialProcess(updateData);
    }

    private boolean isTrackDone(TechTrialTrack techTrialTrack) {
        return StringUtils.equals(TRACK_DONE, StringUtils.trimToEmpty(techTrialTrack.getAllProcessDone()));
    }

    private boolean isProcessDone(TechTrialProcess techTrialProcess) {
        return isDoneStatus(techTrialProcess.getStep1Status())
                && isDoneStatus(techTrialProcess.getStep2Status())
                && isDoneStatus(techTrialProcess.getStep3Status())
                && isDoneStatus(techTrialProcess.getStep4Status())
                && isDoneStatus(techTrialProcess.getStep5Status())
                && isDoneStatus(techTrialProcess.getStep6Status())
                && isDoneStatus(techTrialProcess.getStep7Status())
                && isDoneStatus(techTrialProcess.getStep8Status());
    }

    private boolean isDoneStatus(String status) {
        return StringUtils.equalsIgnoreCase(PROCESS_DONE, StringUtils.trimToEmpty(status));
    }

    private void applyDefaultStatus(TechTrialProcess techTrialProcess) {
        techTrialProcess.setStatus(PROCESS_ACTIVE);
        techTrialProcess.setStep1Status(PROCESS_ACTIVE);
        techTrialProcess.setStep2Status(PROCESS_PENDING);
        techTrialProcess.setStep3Status(PROCESS_PENDING);
        techTrialProcess.setStep4Status(PROCESS_PENDING);
        techTrialProcess.setStep5Status(PROCESS_PENDING);
        techTrialProcess.setStep6Status(PROCESS_PENDING);
        techTrialProcess.setStep7Status(PROCESS_PENDING);
        techTrialProcess.setStep8Status(PROCESS_PENDING);
    }

    private void applyDoneStatus(TechTrialProcess techTrialProcess) {
        techTrialProcess.setStatus(PROCESS_DONE);
        techTrialProcess.setStep1Status(PROCESS_DONE);
        techTrialProcess.setStep2Status(PROCESS_DONE);
        techTrialProcess.setStep3Status(PROCESS_DONE);
        techTrialProcess.setStep4Status(PROCESS_DONE);
        techTrialProcess.setStep5Status(PROCESS_DONE);
        techTrialProcess.setStep6Status(PROCESS_DONE);
        techTrialProcess.setStep7Status(PROCESS_DONE);
        techTrialProcess.setStep8Status(PROCESS_DONE);
    }

    private String resolveOperator(TechTrialTrack techTrialTrack) {
        String operator = StringUtils.trimToNull(techTrialTrack.getUpdateBy());
        if (operator != null) {
            return operator;
        }
        operator = StringUtils.trimToNull(techTrialTrack.getCreateBy());
        return operator == null ? "system" : operator;
    }
}
