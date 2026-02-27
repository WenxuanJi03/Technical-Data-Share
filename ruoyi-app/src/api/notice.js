import { get, post, put, del } from '@/utils/request'

// 查询通知单列表
export function listNotice(query) {
  return get('/tech/notice/list', query)
}

// 查询通知单详细
export function getNotice(noticeId) {
  return get('/tech/notice/' + noticeId)
}

// 新增通知单
export function addNotice(data) {
  return post('/tech/notice', data)
}

// 修改通知单
export function updateNotice(data) {
  return put('/tech/notice', data)
}

// 删除通知单
export function delNotice(noticeId) {
  return del('/tech/notice/' + noticeId)
}
