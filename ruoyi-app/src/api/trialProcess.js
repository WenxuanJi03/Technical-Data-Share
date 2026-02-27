import { get, post, put, del } from '@/utils/request'

export function listTrialProcess(data) {
  return get('/tech/process/list', data)
}

export function addTrialProcess(data) {
  return post('/tech/process', data)
}

export function updateTrialProcess(data) {
  return put('/tech/process', data)
}

export function delTrialProcess(processId) {
  return del('/tech/process/' + processId)
}

