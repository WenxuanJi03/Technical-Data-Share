import request from '@/utils/request'

// 查询毛胚图列表
export function listBlankImage(query) {
  return request({
    url: '/tech/blankImage/list',
    method: 'get',
    params: query
  })
}

// 查询毛胚图详细
export function getBlankImage(blankId) {
  return request({
    url: '/tech/blankImage/' + blankId,
    method: 'get'
  })
}

// 新增毛胚图
export function addBlankImage(data) {
  return request({
    url: '/tech/blankImage',
    method: 'post',
    data: data
  })
}

// 修改毛胚图
export function updateBlankImage(data) {
  return request({
    url: '/tech/blankImage',
    method: 'put',
    data: data
  })
}

// 删除毛胚图
export function delBlankImage(blankId) {
  return request({
    url: '/tech/blankImage/' + blankId,
    method: 'delete'
  })
}

// 清空毛胚图（全选删除）
export function cleanBlankImage(query) {
  return request({
    url: '/tech/blankImage/cleanAll',
    method: 'delete',
    params: query
  })
}
