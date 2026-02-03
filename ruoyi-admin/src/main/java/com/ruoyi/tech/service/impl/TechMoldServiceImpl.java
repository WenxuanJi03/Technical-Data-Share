package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechMoldMapper;
import com.ruoyi.tech.domain.TechMold;
import com.ruoyi.tech.service.ITechMoldService;

/**
 * 模具档案Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechMoldServiceImpl implements ITechMoldService 
{
    @Autowired
    private TechMoldMapper techMoldMapper;

    /**
     * 查询模具档案
     * 
     * @param moldId 模具档案主键
     * @return 模具档案
     */
    @Override
    public TechMold selectTechMoldByMoldId(Long moldId)
    {
        return techMoldMapper.selectTechMoldByMoldId(moldId);
    }

    /**
     * 查询模具档案列表
     * 
     * @param techMold 模具档案
     * @return 模具档案
     */
    @Override
    public List<TechMold> selectTechMoldList(TechMold techMold)
    {
        return techMoldMapper.selectTechMoldList(techMold);
    }

    /**
     * 新增模具档案
     * 
     * @param techMold 模具档案
     * @return 结果
     */
    @Override
    public int insertTechMold(TechMold techMold)
    {
        techMold.setCreateTime(DateUtils.getNowDate());
        return techMoldMapper.insertTechMold(techMold);
    }

    /**
     * 修改模具档案
     * 
     * @param techMold 模具档案
     * @return 结果
     */
    @Override
    public int updateTechMold(TechMold techMold)
    {
        techMold.setUpdateTime(DateUtils.getNowDate());
        return techMoldMapper.updateTechMold(techMold);
    }

    /**
     * 批量删除模具档案
     * 
     * @param moldIds 需要删除的模具档案主键
     * @return 结果
     */
    @Override
    public int deleteTechMoldByMoldIds(Long[] moldIds)
    {
        return techMoldMapper.deleteTechMoldByMoldIds(moldIds);
    }

    /**
     * 删除模具档案信息
     * 
     * @param moldId 模具档案主键
     * @return 结果
     */
    @Override
    public int deleteTechMoldByMoldId(Long moldId)
    {
        return techMoldMapper.deleteTechMoldByMoldId(moldId);
    }
}
