import { get, post, put, del } from '@/utils/request'

// 查询技术文档列表
export function listDocument(query) {
  return get('/tech/document/list', query)
}

// 查询技术文档详细
export function getDocument(docId) {
  return get('/tech/document/' + docId)
}

// 查询项目列表（用于筛选）
export function listProject(query) {
  return get('/tech/project/list', query)
}
