package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechWorkshop;

/**
 * 车间Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechWorkshopService 
{
    /**
     * 查询车间
     * 
     * @param workshopId 车间主键
     * @return 车间
     */
    public TechWorkshop selectTechWorkshopByWorkshopId(Long workshopId);

    /**
     * 查询车间列表
     * 
     * @param techWorkshop 车间
     * @return 车间集合
     */
    public List<TechWorkshop> selectTechWorkshopList(TechWorkshop techWorkshop);

    /**
     * 新增车间
     * 
     * @param techWorkshop 车间
     * @return 结果
     */
    public int insertTechWorkshop(TechWorkshop techWorkshop);

    /**
     * 修改车间
     * 
     * @param techWorkshop 车间
     * @return 结果
     */
    public int updateTechWorkshop(TechWorkshop techWorkshop);

    /**
     * 批量删除车间
     * 
     * @param workshopIds 需要删除的车间主键集合
     * @return 结果
     */
    public int deleteTechWorkshopByWorkshopIds(Long[] workshopIds);

    /**
     * 删除车间信息
     * 
     * @param workshopId 车间主键
     * @return 结果
     */
    public int deleteTechWorkshopByWorkshopId(Long workshopId);
}
