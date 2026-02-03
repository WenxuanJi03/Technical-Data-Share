package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechChangeMapper;
import com.ruoyi.tech.domain.TechChange;
import com.ruoyi.tech.service.ITechChangeService;

/**
 * 变更申请Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechChangeServiceImpl implements ITechChangeService 
{
    @Autowired
    private TechChangeMapper techChangeMapper;

    /**
     * 查询变更申请
     * 
     * @param changeId 变更申请主键
     * @return 变更申请
     */
    @Override
    public TechChange selectTechChangeByChangeId(Long changeId)
    {
        return techChangeMapper.selectTechChangeByChangeId(changeId);
    }

    /**
     * 查询变更申请列表
     * 
     * @param techChange 变更申请
     * @return 变更申请
     */
    @Override
    public List<TechChange> selectTechChangeList(TechChange techChange)
    {
        return techChangeMapper.selectTechChangeList(techChange);
    }

    /**
     * 新增变更申请
     * 
     * @param techChange 变更申请
     * @return 结果
     */
    @Override
    public int insertTechChange(TechChange techChange)
    {
        techChange.setCreateTime(DateUtils.getNowDate());
        return techChangeMapper.insertTechChange(techChange);
    }

    /**
     * 修改变更申请
     * 
     * @param techChange 变更申请
     * @return 结果
     */
    @Override
    public int updateTechChange(TechChange techChange)
    {
        techChange.setUpdateTime(DateUtils.getNowDate());
        return techChangeMapper.updateTechChange(techChange);
    }

    /**
     * 批量删除变更申请
     * 
     * @param changeIds 需要删除的变更申请主键
     * @return 结果
     */
    @Override
    public int deleteTechChangeByChangeIds(Long[] changeIds)
    {
        return techChangeMapper.deleteTechChangeByChangeIds(changeIds);
    }

    /**
     * 删除变更申请信息
     * 
     * @param changeId 变更申请主键
     * @return 结果
     */
    @Override
    public int deleteTechChangeByChangeId(Long changeId)
    {
        return techChangeMapper.deleteTechChangeByChangeId(changeId);
    }
}
