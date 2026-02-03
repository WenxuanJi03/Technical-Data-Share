import request from '@/utils/request'

// 查询模具档案列表
export function listMold(query) {
  return request({
    url: '/tech/mold/list',
    method: 'get',
    params: query
  })
}

// 查询模具档案详细
export function getMold(moldId) {
  return request({
    url: '/tech/mold/' + moldId,
    method: 'get'
  })
}

// 新增模具档案
export function addMold(data) {
  return request({
    url: '/tech/mold',
    method: 'post',
    data: data
  })
}

// 修改模具档案
export function updateMold(data) {
  return request({
    url: '/tech/mold',
    method: 'put',
    data: data
  })
}

// 删除模具档案
export function delMold(moldId) {
  return request({
    url: '/tech/mold/' + moldId,
    method: 'delete'
  })
}
