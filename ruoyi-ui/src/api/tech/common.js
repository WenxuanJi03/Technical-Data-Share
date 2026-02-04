import request from '@/utils/request'

// 获取用户列表（用于下拉选择）
export function getUserList(query) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: query
  })
}

// 获取所有用户（简化版，用于选择）
export function getAllUsers() {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000 }
  })
}

// 验证用户是否存在
export function checkUserExists(nickName) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: { nickName: nickName, pageNum: 1, pageSize: 1 }
  })
}
