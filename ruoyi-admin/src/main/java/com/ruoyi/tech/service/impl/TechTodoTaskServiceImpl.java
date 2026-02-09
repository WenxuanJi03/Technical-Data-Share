package com.ruoyi.tech.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.tech.mapper.TechTodoTaskMapper;
import com.ruoyi.tech.domain.TechTodoTask;
import com.ruoyi.tech.service.ITechTodoTaskService;

/**
 * 待办任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class TechTodoTaskServiceImpl implements ITechTodoTaskService 
{
    @Autowired
    private TechTodoTaskMapper techTodoTaskMapper;

    @Override
    public TechTodoTask selectTechTodoTaskByTaskId(Long taskId)
    {
        return techTodoTaskMapper.selectTechTodoTaskByTaskId(taskId);
    }

    @Override
    public List<TechTodoTask> selectTechTodoTaskList(TechTodoTask techTodoTask)
    {
        return techTodoTaskMapper.selectTechTodoTaskList(techTodoTask);
    }

    @Override
    public int insertTechTodoTask(TechTodoTask techTodoTask)
    {
        techTodoTask.setCreateTime(DateUtils.getNowDate());
        return techTodoTaskMapper.insertTechTodoTask(techTodoTask);
    }

    @Override
    public int updateTechTodoTask(TechTodoTask techTodoTask)
    {
        techTodoTask.setUpdateTime(DateUtils.getNowDate());
        return techTodoTaskMapper.updateTechTodoTask(techTodoTask);
    }

    @Override
    public int deleteTechTodoTaskByTaskIds(Long[] taskIds)
    {
        return techTodoTaskMapper.deleteTechTodoTaskByTaskIds(taskIds);
    }

    @Override
    public int deleteTechTodoTaskByTaskId(Long taskId)
    {
        return techTodoTaskMapper.deleteTechTodoTaskByTaskId(taskId);
    }
}
