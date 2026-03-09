import { get, post, put, del } from '@/utils/request'

export function listTrialTrack(data) {
  return get('/tech/trialTrack/list', data)
}

export function getTrialTrack(trackId) {
  return get('/tech/trialTrack/' + trackId)
}

export function addTrialTrack(data) {
  return post('/tech/trialTrack', data)
}

export function updateTrialTrack(data) {
  return put('/tech/trialTrack', data)
}

export function delTrialTrack(trackId) {
  return del('/tech/trialTrack/' + trackId)
}
