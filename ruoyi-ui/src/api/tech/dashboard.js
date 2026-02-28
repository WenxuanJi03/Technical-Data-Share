import request from '@/utils/request'

// 获取OE试制跟踪统计数据
export function getTrialTrackStats() {
  return request({
    url: '/tech/dashboard/trialTrackStats',
    method: 'get'
  })
}

// 获取产品清单统计数据
export function getProductStats() {
  return request({
    url: '/tech/dashboard/productStats',
    method: 'get'
  })
}

// 获取所有看板数据
export function getAllStats() {
  return request({
    url: '/tech/dashboard/allStats',
    method: 'get'
  })
}
