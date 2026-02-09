package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechTrialNotice;

/**
 * 产品试制通知单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
public interface TechTrialNoticeMapper 
{
    /**
     * 查询产品试制通知单
     * 
     * @param noticeId 通知单主键
     * @return 产品试制通知单
     */
    public TechTrialNotice selectTechTrialNoticeByNoticeId(Long noticeId);

    /**
     * 查询产品试制通知单列表
     * 
     * @param techTrialNotice 产品试制通知单
     * @return 产品试制通知单集合
     */
    public List<TechTrialNotice> selectTechTrialNoticeList(TechTrialNotice techTrialNotice);

    /**
     * 新增产品试制通知单
     * 
     * @param techTrialNotice 产品试制通知单
     * @return 结果
     */
    public int insertTechTrialNotice(TechTrialNotice techTrialNotice);

    /**
     * 修改产品试制通知单
     * 
     * @param techTrialNotice 产品试制通知单
     * @return 结果
     */
    public int updateTechTrialNotice(TechTrialNotice techTrialNotice);

    /**
     * 删除产品试制通知单
     * 
     * @param noticeId 通知单主键
     * @return 结果
     */
    public int deleteTechTrialNoticeByNoticeId(Long noticeId);

    /**
     * 批量删除产品试制通知单
     * 
     * @param noticeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTechTrialNoticeByNoticeIds(Long[] noticeIds);
}
