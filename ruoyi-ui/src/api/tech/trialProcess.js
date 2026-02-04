import request from '@/utils/request'

// 查询试制流程列表
export function listTrialProcess(query) {
  return request({
    url: '/tech/trialProcess/list',
    method: 'get',
    params: query
  })
}

// 检查用户是否有逾期任务
export function checkOverdue(userName) {
  return request({
    url: '/tech/trialProcess/checkOverdue',
    method: 'get',
    params: { userName }
  })
}

// 查询试制流程详细
export function getTrialProcess(processId) {
  return request({
    url: '/tech/trialProcess/' + processId,
    method: 'get'
  })
}

// 新增试制流程
export function addTrialProcess(data) {
  return request({
    url: '/tech/trialProcess',
    method: 'post',
    data: data
  })
}

// 修改试制流程
export function updateTrialProcess(data) {
  return request({
    url: '/tech/trialProcess',
    method: 'put',
    data: data
  })
}

// 删除试制流程
export function delTrialProcess(processId) {
  return request({
    url: '/tech/trialProcess/' + processId,
    method: 'delete'
  })
}
