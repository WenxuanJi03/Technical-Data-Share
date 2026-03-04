package com.ruoyi.tech.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.tech.domain.TechTrialProcess;
import com.ruoyi.tech.domain.TechTrialTrack;
import com.ruoyi.tech.mapper.TechTrialProcessMapper;
import com.ruoyi.tech.mapper.TechTrialTrackMapper;
import com.ruoyi.tech.service.ITechTrialProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trial process service.
 */
@Service
public class TechTrialProcessServiceImpl implements ITechTrialProcessService {
    private static final Logger log = LoggerFactory.getLogger(TechTrialProcessServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PROCESS_DONE = "done";
    private static final String TRACK_DONE = "\u662f";
    private static final String TRACK_UNDONE = "\u5426";

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>();

    static {
        IMAGE_EXTENSIONS.add("jpg");
        IMAGE_EXTENSIONS.add("jpeg");
        IMAGE_EXTENSIONS.add("png");
        IMAGE_EXTENSIONS.add("gif");
        IMAGE_EXTENSIONS.add("bmp");
        IMAGE_EXTENSIONS.add("webp");
        IMAGE_EXTENSIONS.add("heic");
        IMAGE_EXTENSIONS.add("heif");
    }

    @Autowired
    private TechTrialProcessMapper techTrialProcessMapper;

    @Autowired
    private TechTrialTrackMapper techTrialTrackMapper;

    @Override
    public boolean checkUserOverdue(String userName) {
        return techTrialProcessMapper.checkUserOverdue(userName) > 0;
    }

    @Override
    public TechTrialProcess selectTechTrialProcessByProcessId(Long processId) {
        return techTrialProcessMapper.selectTechTrialProcessByProcessId(processId);
    }

    @Override
    public List<TechTrialProcess> selectTechTrialProcessList(TechTrialProcess techTrialProcess) {
        return techTrialProcessMapper.selectTechTrialProcessList(techTrialProcess);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTechTrialProcess(TechTrialProcess techTrialProcess) {
        normalizeProcessFields(techTrialProcess);
        techTrialProcess.setCreateTime(DateUtils.getNowDate());
        int rows = techTrialProcessMapper.insertTechTrialProcess(techTrialProcess);
        if (rows > 0) {
            syncTrialTrack(techTrialProcess, null);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTechTrialProcess(TechTrialProcess techTrialProcess) {
        TechTrialProcess beforeUpdate = null;
        if (techTrialProcess.getProcessId() != null) {
            beforeUpdate = techTrialProcessMapper.selectTechTrialProcessByProcessId(techTrialProcess.getProcessId());
        }

        normalizeProcessFields(techTrialProcess);
        techTrialProcess.setUpdateTime(DateUtils.getNowDate());
        int rows = techTrialProcessMapper.updateTechTrialProcess(techTrialProcess);
        if (rows > 0 && techTrialProcess.getProcessId() != null) {
            TechTrialProcess latest = techTrialProcessMapper.selectTechTrialProcessByProcessId(techTrialProcess.getProcessId());
            if (latest != null) {
                String oldMoldCode = beforeUpdate == null ? null : beforeUpdate.getMoldCode();
                syncTrialTrack(latest, oldMoldCode);
            }
        }
        return rows;
    }

    @Override
    public int deleteTechTrialProcessByProcessIds(Long[] processIds) {
        return techTrialProcessMapper.deleteTechTrialProcessByProcessIds(processIds);
    }

    @Override
    public int deleteTechTrialProcessByProcessId(Long processId) {
        return techTrialProcessMapper.deleteTechTrialProcessByProcessId(processId);
    }

    private void normalizeProcessFields(TechTrialProcess techTrialProcess) {
        if (techTrialProcess == null) {
            return;
        }
        if (techTrialProcess.getMoldCode() != null) {
            techTrialProcess.setMoldCode(techTrialProcess.getMoldCode().trim());
        }
        if (techTrialProcess.getMoldType() != null) {
            techTrialProcess.setMoldType(techTrialProcess.getMoldType().trim());
        }
        if (techTrialProcess.getSurfaceStatus() != null) {
            techTrialProcess.setSurfaceStatus(techTrialProcess.getSurfaceStatus().trim());
        }
        if (techTrialProcess.getMachineType() != null) {
            techTrialProcess.setMachineType(techTrialProcess.getMachineType().trim());
        }
    }

    private void syncTrialTrack(TechTrialProcess techTrialProcess, String oldMoldCode) {
        String moldCode = StringUtils.trimToNull(techTrialProcess.getMoldCode());
        if (moldCode == null) {
            return;
        }

        TechTrialTrack existed = techTrialTrackMapper.selectTechTrialTrackByMoldCode(moldCode);
        if (existed == null) {
            String oldCode = StringUtils.trimToNull(oldMoldCode);
            if (oldCode != null && !StringUtils.equals(oldCode, moldCode)) {
                existed = techTrialTrackMapper.selectTechTrialTrackByMoldCode(oldCode);
            }
        }

        TechTrialTrack syncData = new TechTrialTrack();
        syncData.setMoldCode(moldCode);
        syncData.setMoldType(techTrialProcess.getMoldType());
        syncData.setSurfaceStatus(techTrialProcess.getSurfaceStatus());
        syncData.setMachineType(techTrialProcess.getMachineType());
        syncData.setAllProcessDone(isAllStepDone(techTrialProcess) ? TRACK_DONE : TRACK_UNDONE);

        // Sync uploaded process images into OE image fields by phase.
        String hotImages = extractImageUrls(moldCode, techTrialProcess.getStep2Files());
        if (hotImages != null) {
            syncData.setHotCheckMeasureImage(hotImages);
        }
        String spinImages = extractImageUrls(moldCode, techTrialProcess.getStep3Files());
        if (spinImages != null) {
            syncData.setSpinFrontDistanceImage(spinImages);
        }
        String heatImages = extractImageUrls(moldCode, techTrialProcess.getStep4Files());
        if (heatImages != null) {
            syncData.setHeatFlowSheetImage(heatImages);
        }
        String paintImages = extractImageUrls(moldCode, techTrialProcess.getStep7Files());
        if (paintImages != null) {
            syncData.setPaintFlowSheetImage(paintImages);
        }

        if (existed == null) {
            syncData.setCreateBy(resolveOperator(techTrialProcess));
            syncData.setCreateTime(DateUtils.getNowDate());
            techTrialTrackMapper.insertTechTrialTrack(syncData);
            return;
        }

        syncData.setTrackId(existed.getTrackId());
        syncData.setUpdateBy(resolveOperator(techTrialProcess));
        syncData.setUpdateTime(DateUtils.getNowDate());
        techTrialTrackMapper.updateTechTrialTrack(syncData);
    }

    private boolean isAllStepDone(TechTrialProcess techTrialProcess) {
        return isDone(techTrialProcess.getStep1Status())
                && isDone(techTrialProcess.getStep2Status())
                && isDone(techTrialProcess.getStep3Status())
                && isDone(techTrialProcess.getStep4Status())
                && isDone(techTrialProcess.getStep5Status())
                && isDone(techTrialProcess.getStep6Status())
                && isDone(techTrialProcess.getStep7Status())
                && isDone(techTrialProcess.getStep8Status());
    }

    private boolean isDone(String status) {
        return StringUtils.equalsIgnoreCase(PROCESS_DONE, StringUtils.trimToEmpty(status));
    }

    private String resolveOperator(TechTrialProcess techTrialProcess) {
        String operator = StringUtils.trimToNull(techTrialProcess.getUpdateBy());
        if (operator != null) {
            return operator;
        }
        operator = StringUtils.trimToNull(techTrialProcess.getCreateBy());
        if (operator != null) {
            return operator;
        }
        operator = StringUtils.trimToNull(techTrialProcess.getInitiator());
        return operator == null ? "system" : operator;
    }

    private String extractImageUrls(String moldCode, String filesJson) {
        if (filesJson == null) {
            return "";
        }

        String trimmed = filesJson.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(trimmed);
            if (root == null || !root.isArray()) {
                return "";
            }
            List<String> urls = new ArrayList<>();
            for (JsonNode node : root) {
                if (node == null) {
                    continue;
                }
                String url = StringUtils.trimToEmpty(node.path("url").asText(""));
                if (url.isEmpty()) {
                    continue;
                }
                String fileName = StringUtils.trimToEmpty(node.path("name").asText(""));
                if (isImageFile(fileName, url)) {
                    urls.add(url);
                }
            }
            return String.join(",", urls);
        } catch (Exception ex) {
            log.warn("Failed to parse step files JSON, moldCode={}", moldCode, ex);
            return null;
        }
    }

    private boolean isImageFile(String fileName, String url) {
        String ext = resolveExtension(fileName);
        if (ext == null) {
            ext = resolveExtension(url);
        }
        return ext != null && IMAGE_EXTENSIONS.contains(ext);
    }

    private String resolveExtension(String value) {
        String safeValue = StringUtils.trimToEmpty(value);
        if (safeValue.isEmpty()) {
            return null;
        }

        int queryPos = safeValue.indexOf('?');
        if (queryPos >= 0) {
            safeValue = safeValue.substring(0, queryPos);
        }

        int dotPos = safeValue.lastIndexOf('.');
        if (dotPos < 0 || dotPos >= safeValue.length() - 1) {
            return null;
        }
        return safeValue.substring(dotPos + 1).toLowerCase(Locale.ROOT);
    }
}
