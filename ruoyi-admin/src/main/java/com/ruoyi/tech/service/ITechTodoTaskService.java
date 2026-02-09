package com.ruoyi.tech.service;

import java.util.List;
import com.ruoyi.tech.domain.TechTodoTask;

/**
 * 待办任务Service接口
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
public interface ITechTodoTaskService 
{
    /**
     * 查询待办任务
     */
    public TechTodoTask selectTechTodoTaskByTaskId(Long taskId);

    /**
     * 查询待办任务列表
     */
    public List<TechTodoTask> selectTechTodoTaskList(TechTodoTask techTodoTask);

    /**
     * 新增待办任务
     */
    public int insertTechTodoTask(TechTodoTask techTodoTask);

    /**
     * 修改待办任务
     */
    public int updateTechTodoTask(TechTodoTask techTodoTask);

    /**
     * 批量删除待办任务
     */
    public int deleteTechTodoTaskByTaskIds(Long[] taskIds);

    /**
     * 删除待办任务
     */
    public int deleteTechTodoTaskByTaskId(Long taskId);
}
