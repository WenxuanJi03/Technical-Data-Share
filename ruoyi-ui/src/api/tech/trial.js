import request from '@/utils/request'

// 查询试制任务列表
export function listTrial(query) {
  return request({
    url: '/tech/trial/list',
    method: 'get',
    params: query
  })
}

// 查询试制任务详细
export function getTrial(trialId) {
  return request({
    url: '/tech/trial/' + trialId,
    method: 'get'
  })
}

// 新增试制任务
export function addTrial(data) {
  return request({
    url: '/tech/trial',
    method: 'post',
    data: data
  })
}

// 修改试制任务
export function updateTrial(data) {
  return request({
    url: '/tech/trial',
    method: 'put',
    data: data
  })
}

// 删除试制任务
export function delTrial(trialId) {
  return request({
    url: '/tech/trial/' + trialId,
    method: 'delete'
  })
}
