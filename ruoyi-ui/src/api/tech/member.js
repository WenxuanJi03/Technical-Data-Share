import request from '@/utils/request'

// 查询项目成员列表
export function listMember(query) {
  return request({
    url: '/tech/member/list',
    method: 'get',
    params: query
  })
}

// 查询项目成员详细
export function getMember(id) {
  return request({
    url: '/tech/member/' + id,
    method: 'get'
  })
}

// 新增项目成员
export function addMember(data) {
  return request({
    url: '/tech/member',
    method: 'post',
    data: data
  })
}

// 修改项目成员
export function updateMember(data) {
  return request({
    url: '/tech/member',
    method: 'put',
    data: data
  })
}

// 删除项目成员
export function delMember(id) {
  return request({
    url: '/tech/member/' + id,
    method: 'delete'
  })
}
