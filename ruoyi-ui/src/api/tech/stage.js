import request from '@/utils/request'

// 查询项目阶段记录列表
export function listStage(query) {
  return request({
    url: '/tech/stage/list',
    method: 'get',
    params: query
  })
}

// 查询项目阶段记录详细
export function getStage(stageId) {
  return request({
    url: '/tech/stage/' + stageId,
    method: 'get'
  })
}

// 新增项目阶段记录
export function addStage(data) {
  return request({
    url: '/tech/stage',
    method: 'post',
    data: data
  })
}

// 修改项目阶段记录
export function updateStage(data) {
  return request({
    url: '/tech/stage',
    method: 'put',
    data: data
  })
}

// 删除项目阶段记录
export function delStage(stageId) {
  return request({
    url: '/tech/stage/' + stageId,
    method: 'delete'
  })
}
