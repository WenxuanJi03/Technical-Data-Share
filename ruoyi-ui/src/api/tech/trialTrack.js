import request from '@/utils/request'

export function listTrialTrack(query) {
  return request({ url: '/tech/trialTrack/list', method: 'get', params: query })
}
export function getTrialTrack(trackId) {
  return request({ url: '/tech/trialTrack/' + trackId, method: 'get' })
}
export function addTrialTrack(data) {
  return request({ url: '/tech/trialTrack', method: 'post', data })
}
export function updateTrialTrack(data) {
  return request({ url: '/tech/trialTrack', method: 'put', data })
}
export function delTrialTrack(trackId) {
  return request({ url: '/tech/trialTrack/' + trackId, method: 'delete' })
}
export function cleanAllTrialTracks(query) {
  return request({ url: '/tech/trialTrack/cleanAll', method: 'delete', params: query })
}
