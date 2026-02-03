package com.ruoyi.tech.mapper;

import java.util.List;
import com.ruoyi.tech.domain.TechProjectMember;

/**
 * 项目成员Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface TechProjectMemberMapper 
{
    /**
     * 查询项目成员
     * 
     * @param id 项目成员主键
     * @return 项目成员
     */
    public TechProjectMember selectTechProjectMemberById(Long id);

    /**
     * 查询项目成员列表
     * 
     * @param techProjectMember 项目成员
     * @return 项目成员集合
     */
    public List<TechProjectMember> selectTechProjectMemberList(TechProjectMember techProjectMember);

    /**
     * 新增项目成员
     * 
     * @param techProjectMember 项目成员
     * @return 结果
     */
    public int insertTechProjectMember(TechProjectMember techProjectMember);

    /**
     * 修改项目成员
     * 
     * @param techProjectMember 项目成员
     * @return 结果
     */
    public int updateTechProjectMember(TechProjectMember techProjectMember);

    /**
     * 删除项目成员
     * 
     * @param id 项目成员主键
     * @return 结果
     */
    public int deleteTechProjectMemberById(Long id);

    /**
     * 批量删除项目成员
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTechProjectMemberByIds(Long[] ids);
}
