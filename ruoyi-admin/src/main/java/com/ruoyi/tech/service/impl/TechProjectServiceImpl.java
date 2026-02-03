package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechProjectMapper;
import com.ruoyi.tech.domain.TechProject;
import com.ruoyi.tech.service.ITechProjectService;

/**
 * 项目Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
@Service
public class TechProjectServiceImpl implements ITechProjectService 
{
    @Autowired
    private TechProjectMapper techProjectMapper;

    /**
     * 查询项目
     * 
     * @param projectId 项目主键
     * @return 项目
     */
    @Override
    public TechProject selectTechProjectByProjectId(Long projectId)
    {
        return techProjectMapper.selectTechProjectByProjectId(projectId);
    }

    /**
     * 查询项目列表
     * 
     * @param techProject 项目
     * @return 项目
     */
    @Override
    public List<TechProject> selectTechProjectList(TechProject techProject)
    {
        return techProjectMapper.selectTechProjectList(techProject);
    }

    /**
     * 新增项目
     * 
     * @param techProject 项目
     * @return 结果
     */
    @Override
    public int insertTechProject(TechProject techProject)
    {
        techProject.setCreateTime(DateUtils.getNowDate());
        return techProjectMapper.insertTechProject(techProject);
    }

    /**
     * 修改项目
     * 
     * @param techProject 项目
     * @return 结果
     */
    @Override
    public int updateTechProject(TechProject techProject)
    {
        techProject.setUpdateTime(DateUtils.getNowDate());
        return techProjectMapper.updateTechProject(techProject);
    }

    /**
     * 批量删除项目
     * 
     * @param projectIds 需要删除的项目主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectByProjectIds(Long[] projectIds)
    {
        return techProjectMapper.deleteTechProjectByProjectIds(projectIds);
    }

    /**
     * 删除项目信息
     * 
     * @param projectId 项目主键
     * @return 结果
     */
    @Override
    public int deleteTechProjectByProjectId(Long projectId)
    {
        return techProjectMapper.deleteTechProjectByProjectId(projectId);
    }
}
