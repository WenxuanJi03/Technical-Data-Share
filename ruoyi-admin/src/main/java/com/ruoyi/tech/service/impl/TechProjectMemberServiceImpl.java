package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechProjectMemberMapper;
import com.ruoyi.tech.domain.TechProjectMember;
import com.ruoyi.tech.service.ITechProjectMemberService;

/**
 * 项目成员Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechProjectMemberServiceImpl implements ITechProjectMemberService 
{
    @Autowired
    private TechProjectMemberMapper techProjectMemberMapper;

    /**
     * 查询项目成员
     * 
     * @param id 项目成员主键
     * @return 项目成员
     */
    @Override
    public TechProjectMember selectTechProjectMemberById(Long id)
    {
        return techProjectMemberMapper.selectTechProjectMemberById(id);
    }

    /**
     * 查询项目成员列表
     * 
     * @param techProjectMember 项目成员
     * @return 项目成员
     */
    @Override
    public List<TechProjectMember> selectTechProjectMemberList(TechProjectMember techProjectMember)
    {
        return techProjectMemberMapper.selectTechProjectMemberList(techProjectMember);
    }

    /**
     * 新增项目成员
     * 
     * @param techProjectMember 项目成员
     * @return 结果
     */
    @Override
    public int insertTechProjectMember(TechProjectMember techProjectMember)
    {
        techProjectMember.setCreateTime(DateUtils.getNowDate());
        return techProjectMemberMapper.insertTechProjectMember(techProjectMember);
    }

    /**
     * 修改项目成员
     * 
     * @param techProjectMember 项目成员
     * @return 结果
     */
    @Override
    public int updateTechProjectMember(TechProjectMember techProjectMember)
    {
        return techProjectMemberMapper.updateTechProjectMember(techProjectMember);
    }

    /**
     * 批量删除项目成员
     * 
     * @param ids 需要删除的项目成员主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectMemberByIds(Long[] ids)
    {
        return techProjectMemberMapper.deleteTechProjectMemberByIds(ids);
    }

    /**
     * 删除项目成员信息
     * 
     * @param id 项目成员主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectMemberById(Long id)
    {
        return techProjectMemberMapper.deleteTechProjectMemberById(id);
    }
}
