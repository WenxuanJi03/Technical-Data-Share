package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechTrialNoticeMapper;
import com.ruoyi.tech.domain.TechTrialNotice;
import com.ruoyi.tech.service.ITechTrialNoticeService;

/**
 * 产品试制通知单Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class TechTrialNoticeServiceImpl implements ITechTrialNoticeService 
{
    private static final Logger log = LoggerFactory.getLogger(TechTrialNoticeServiceImpl.class);

    @Autowired
    private TechTrialNoticeMapper techTrialNoticeMapper;

    @Override
    public TechTrialNotice selectTechTrialNoticeByNoticeId(Long noticeId)
    {
        return techTrialNoticeMapper.selectTechTrialNoticeByNoticeId(noticeId);
    }

    @Override
    public List<TechTrialNotice> selectTechTrialNoticeList(TechTrialNotice techTrialNotice)
    {
        return techTrialNoticeMapper.selectTechTrialNoticeList(techTrialNotice);
    }

    @Override
    public int insertTechTrialNotice(TechTrialNotice techTrialNotice)
    {
        techTrialNotice.setCreateTime(DateUtils.getNowDate());
        return techTrialNoticeMapper.insertTechTrialNotice(techTrialNotice);
    }

    @Override
    public int updateTechTrialNotice(TechTrialNotice techTrialNotice)
    {
        techTrialNotice.setUpdateTime(DateUtils.getNowDate());
        return techTrialNoticeMapper.updateTechTrialNotice(techTrialNotice);
    }

    @Override
    public int deleteTechTrialNoticeByNoticeIds(Long[] noticeIds)
    {
        return techTrialNoticeMapper.deleteTechTrialNoticeByNoticeIds(noticeIds);
    }

    @Override
    public int deleteTechTrialNoticeByNoticeId(Long noticeId)
    {
        return techTrialNoticeMapper.deleteTechTrialNoticeByNoticeId(noticeId);
    }

    /**
     * 导入产品试制通知单数据
     */
    @Override
    public String importNotices(List<TechTrialNotice> noticeList, boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(noticeList) || noticeList.size() == 0)
        {
            throw new ServiceException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (TechTrialNotice notice : noticeList)
        {
            try
            {
                if (StringUtils.isEmpty(notice.getNoticeCode()) && StringUtils.isEmpty(notice.getWheelCode()))
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、通知单编号和轮型号均为空，跳过");
                    continue;
                }
                notice.setCreateBy(operName);
                notice.setCreateTime(DateUtils.getNowDate());
                // 检查是否已存在
                if (StringUtils.isNotEmpty(notice.getNoticeCode()))
                {
                    TechTrialNotice query = new TechTrialNotice();
                    query.setNoticeCode(notice.getNoticeCode());
                    List<TechTrialNotice> existList = techTrialNoticeMapper.selectTechTrialNoticeList(query);
                    if (existList != null && existList.size() > 0)
                    {
                        if (isUpdateSupport)
                        {
                            TechTrialNotice exist = existList.get(0);
                            notice.setNoticeId(exist.getNoticeId());
                            notice.setUpdateBy(operName);
                            notice.setUpdateTime(DateUtils.getNowDate());
                            techTrialNoticeMapper.updateTechTrialNotice(notice);
                            successNum++;
                            successMsg.append("<br/>").append(successNum).append("、通知单 ").append(notice.getNoticeCode()).append(" 更新成功");
                        }
                        else
                        {
                            failureNum++;
                            failureMsg.append("<br/>").append(failureNum).append("、通知单编号 ").append(notice.getNoticeCode()).append(" 已存在");
                        }
                        continue;
                    }
                }
                techTrialNoticeMapper.insertTechTrialNotice(notice);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、通知单 ").append(notice.getNoticeCode()).append(" 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、通知单 " + notice.getNoticeCode() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
