import { get, post, put, del } from '@/utils/request'

// 查询试制任务列表
export function listTrial(query) {
  return get('/tech/trial/list', query)
}

// 查询试制任务详细
export function getTrial(trialId) {
  return get('/tech/trial/' + trialId)
}

// 新增试制任务
export function addTrial(data) {
  return post('/tech/trial', data)
}

// 修改试制任务
export function updateTrial(data) {
  return put('/tech/trial', data)
}

// 删除试制任务
export function delTrial(trialId) {
  return del('/tech/trial/' + trialId)
}
