import request from '@/utils/request'

// 查询产品清单列表
export function listProduct(query) {
  return request({
    url: '/tech/product/list',
    method: 'get',
    params: query
  })
}

// 查询产品详细
export function getProduct(productId) {
  return request({
    url: '/tech/product/' + productId,
    method: 'get'
  })
}

// 新增产品
export function addProduct(data) {
  return request({
    url: '/tech/product',
    method: 'post',
    data: data
  })
}

// 修改产品
export function updateProduct(data) {
  return request({
    url: '/tech/product',
    method: 'put',
    data: data
  })
}

// 删除产品
export function delProduct(productId) {
  return request({
    url: '/tech/product/' + productId,
    method: 'delete'
  })
}

// 按搜索条件清空全部产品
export function cleanAllProducts(query) {
  return request({
    url: '/tech/product/cleanAll',
    method: 'delete',
    params: query
  })
}

// 轮毂识别：上传图片，返回相似产品列表（FormData 含 file 字段，不要手动设 Content-Type）
export function recognizeProduct(data) {
  return request({
    url: '/tech/product/recognize',
    method: 'post',
    data: data
  })
}
