import request from '@/utils/request'

// 查询技术文档列表
export function listDocument(query) {
  return request({
    url: '/tech/document/list',
    method: 'get',
    params: query
  })
}

// 查询技术文档详细
export function getDocument(docId) {
  return request({
    url: '/tech/document/' + docId,
    method: 'get'
  })
}

// 新增技术文档
export function addDocument(data) {
  return request({
    url: '/tech/document',
    method: 'post',
    data: data
  })
}

// 修改技术文档
export function updateDocument(data) {
  return request({
    url: '/tech/document',
    method: 'put',
    data: data
  })
}

// 删除技术文档
export function delDocument(docId) {
  return request({
    url: '/tech/document/' + docId,
    method: 'delete'
  })
}

// 导入技术文档
export function importDocument(data) {
  return request({
    url: '/tech/document/importData',
    method: 'post',
    data: data
  })
}
