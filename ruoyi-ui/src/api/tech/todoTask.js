import request from '@/utils/request'

// 查询待办任务列表
export function listTodoTask(query) {
  return request({
    url: '/tech/todoTask/list',
    method: 'get',
    params: query
  })
}

// 查询待办任务详细
export function getTodoTask(taskId) {
  return request({
    url: '/tech/todoTask/' + taskId,
    method: 'get'
  })
}

// 新增待办任务
export function addTodoTask(data) {
  return request({
    url: '/tech/todoTask',
    method: 'post',
    data: data
  })
}

// 修改待办任务
export function updateTodoTask(data) {
  return request({
    url: '/tech/todoTask',
    method: 'put',
    data: data
  })
}

// 删除待办任务
export function delTodoTask(taskId) {
  return request({
    url: '/tech/todoTask/' + taskId,
    method: 'delete'
  })
}
