package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechWorkshopMapper;
import com.ruoyi.tech.domain.TechWorkshop;
import com.ruoyi.tech.service.ITechWorkshopService;

/**
 * 车间Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechWorkshopServiceImpl implements ITechWorkshopService 
{
    @Autowired
    private TechWorkshopMapper techWorkshopMapper;

    /**
     * 查询车间
     * 
     * @param workshopId 车间主键
     * @return 车间
     */
    @Override
    public TechWorkshop selectTechWorkshopByWorkshopId(Long workshopId)
    {
        return techWorkshopMapper.selectTechWorkshopByWorkshopId(workshopId);
    }

    /**
     * 查询车间列表
     * 
     * @param techWorkshop 车间
     * @return 车间
     */
    @Override
    public List<TechWorkshop> selectTechWorkshopList(TechWorkshop techWorkshop)
    {
        return techWorkshopMapper.selectTechWorkshopList(techWorkshop);
    }

    /**
     * 新增车间
     * 
     * @param techWorkshop 车间
     * @return 结果
     */
    @Override
    public int insertTechWorkshop(TechWorkshop techWorkshop)
    {
        techWorkshop.setCreateTime(DateUtils.getNowDate());
        return techWorkshopMapper.insertTechWorkshop(techWorkshop);
    }

    /**
     * 修改车间
     * 
     * @param techWorkshop 车间
     * @return 结果
     */
    @Override
    public int updateTechWorkshop(TechWorkshop techWorkshop)
    {
        return techWorkshopMapper.updateTechWorkshop(techWorkshop);
    }

    /**
     * 批量删除车间
     * 
     * @param workshopIds 需要删除的车间主键
     * @return 结果
     */
    @Override
    public int deleteTechWorkshopByWorkshopIds(Long[] workshopIds)
    {
        return techWorkshopMapper.deleteTechWorkshopByWorkshopIds(workshopIds);
    }

    /**
     * 删除车间信息
     * 
     * @param workshopId 车间主键
     * @return 结果
     */
    @Override
    public int deleteTechWorkshopByWorkshopId(Long workshopId)
    {
        return techWorkshopMapper.deleteTechWorkshopByWorkshopId(workshopId);
    }
}
