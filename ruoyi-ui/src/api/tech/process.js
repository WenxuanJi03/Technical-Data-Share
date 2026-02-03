import request from '@/utils/request'

// 查询试制流程记录列表
export function listProcess(query) {
  return request({
    url: '/tech/process/list',
    method: 'get',
    params: query
  })
}

// 查询试制流程记录详细
export function getProcess(processId) {
  return request({
    url: '/tech/process/' + processId,
    method: 'get'
  })
}

// 新增试制流程记录
export function addProcess(data) {
  return request({
    url: '/tech/process',
    method: 'post',
    data: data
  })
}

// 修改试制流程记录
export function updateProcess(data) {
  return request({
    url: '/tech/process',
    method: 'put',
    data: data
  })
}

// 删除试制流程记录
export function delProcess(processId) {
  return request({
    url: '/tech/process/' + processId,
    method: 'delete'
  })
}
