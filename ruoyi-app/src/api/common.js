import { get } from '@/utils/request'

// 获取用户列表
export function getAllUsers() {
  return get('/system/user/list', { pageNum: 1, pageSize: 1000 })
}
