import { get, post, put } from '@/utils/request'

export function listTrialTrack(data) {
  return get('/tech/trialTrack/list', data)
}

export function addTrialTrack(data) {
  return post('/tech/trialTrack', data)
}

export function updateTrialTrack(data) {
  return put('/tech/trialTrack', data)
}
