import request from '@/utils/request'

// 查询产品试制通知单列表
export function listTrialNotice(query) {
  return request({
    url: '/tech/notice/list',
    method: 'get',
    params: query
  })
}

// 查询产品试制通知单详细
export function getTrialNotice(noticeId) {
  return request({
    url: '/tech/notice/' + noticeId,
    method: 'get'
  })
}

// 新增产品试制通知单
export function addTrialNotice(data) {
  return request({
    url: '/tech/notice',
    method: 'post',
    data: data
  })
}

// 修改产品试制通知单
export function updateTrialNotice(data) {
  return request({
    url: '/tech/notice',
    method: 'put',
    data: data
  })
}

// 删除产品试制通知单
export function delTrialNotice(noticeId) {
  return request({
    url: '/tech/notice/' + noticeId,
    method: 'delete'
  })
}
