package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechProject;

/**
 * 项目Service接口
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public interface ITechProjectService 
{
    /**
     * 查询项目
     * 
     * @param projectId 项目主键
     * @return 项目
     */
    public TechProject selectTechProjectByProjectId(Long projectId);

    /**
     * 查询项目列表
     * 
     * @param techProject 项目
     * @return 项目集合
     */
    public List<TechProject> selectTechProjectList(TechProject techProject);

    /**
     * 新增项目
     * 
     * @param techProject 项目
     * @return 结果
     */
    public int insertTechProject(TechProject techProject);

    /**
     * 修改项目
     * 
     * @param techProject 项目
     * @return 结果
     */
    public int updateTechProject(TechProject techProject);

    /**
     * 批量删除项目
     * 
     * @param projectIds 需要删除的项目主键集合
     * @return 结果
     */
    public int deleteTechProjectByProjectIds(Long[] projectIds);

    /**
     * 删除项目信息
     * 
     * @param projectId 项目主键
     * @return 结果
     */
    public int deleteTechProjectByProjectId(Long projectId);
}
