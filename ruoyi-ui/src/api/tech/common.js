import request from '@/utils/request'

// 获取所有用户（简化版，用于发起人/负责人下拉选择）
// 使用 /tech/common/users，任何已登录用户均可访问，无需 system:user:list 权限
export function getAllUsers() {
  return request({
    url: '/tech/common/users',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000 }
  })
}

// 验证用户是否存在（同样改用公共接口）
export function checkUserExists(nickName) {
  return request({
    url: '/tech/common/users',
    method: 'get',
    params: { nickName: nickName, pageNum: 1, pageSize: 1 }
  })
}
