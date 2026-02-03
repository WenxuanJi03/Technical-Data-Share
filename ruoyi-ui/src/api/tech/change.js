import request from '@/utils/request'

// 查询变更申请列表
export function listChange(query) {
  return request({
    url: '/tech/change/list',
    method: 'get',
    params: query
  })
}

// 查询变更申请详细
export function getChange(changeId) {
  return request({
    url: '/tech/change/' + changeId,
    method: 'get'
  })
}

// 新增变更申请
export function addChange(data) {
  return request({
    url: '/tech/change',
    method: 'post',
    data: data
  })
}

// 修改变更申请
export function updateChange(data) {
  return request({
    url: '/tech/change',
    method: 'put',
    data: data
  })
}

// 删除变更申请
export function delChange(changeId) {
  return request({
    url: '/tech/change/' + changeId,
    method: 'delete'
  })
}
