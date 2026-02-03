package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechChange;

/**
 * 变更申请Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechChangeService 
{
    /**
     * 查询变更申请
     * 
     * @param changeId 变更申请主键
     * @return 变更申请
     */
    public TechChange selectTechChangeByChangeId(Long changeId);

    /**
     * 查询变更申请列表
     * 
     * @param techChange 变更申请
     * @return 变更申请集合
     */
    public List<TechChange> selectTechChangeList(TechChange techChange);

    /**
     * 新增变更申请
     * 
     * @param techChange 变更申请
     * @return 结果
     */
    public int insertTechChange(TechChange techChange);

    /**
     * 修改变更申请
     * 
     * @param techChange 变更申请
     * @return 结果
     */
    public int updateTechChange(TechChange techChange);

    /**
     * 批量删除变更申请
     * 
     * @param changeIds 需要删除的变更申请主键集合
     * @return 结果
     */
    public int deleteTechChangeByChangeIds(Long[] changeIds);

    /**
     * 删除变更申请信息
     * 
     * @param changeId 变更申请主键
     * @return 结果
     */
    public int deleteTechChangeByChangeId(Long changeId);
}
