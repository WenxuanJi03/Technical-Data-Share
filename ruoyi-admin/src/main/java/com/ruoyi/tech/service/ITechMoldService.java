package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechMold;

/**
 * 模具档案Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechMoldService 
{
    /**
     * 查询模具档案
     * 
     * @param moldId 模具档案主键
     * @return 模具档案
     */
    public TechMold selectTechMoldByMoldId(Long moldId);

    /**
     * 查询模具档案列表
     * 
     * @param techMold 模具档案
     * @return 模具档案集合
     */
    public List<TechMold> selectTechMoldList(TechMold techMold);

    /**
     * 新增模具档案
     * 
     * @param techMold 模具档案
     * @return 结果
     */
    public int insertTechMold(TechMold techMold);

    /**
     * 修改模具档案
     * 
     * @param techMold 模具档案
     * @return 结果
     */
    public int updateTechMold(TechMold techMold);

    /**
     * 批量删除模具档案
     * 
     * @param moldIds 需要删除的模具档案主键集合
     * @return 结果
     */
    public int deleteTechMoldByMoldIds(Long[] moldIds);

    /**
     * 删除模具档案信息
     * 
     * @param moldId 模具档案主键
     * @return 结果
     */
    public int deleteTechMoldByMoldId(Long moldId);
}
