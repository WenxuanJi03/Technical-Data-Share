import { get } from '@/utils/request'

// 获取用户列表（发起人/负责人下拉选择用）
// 使用 /tech/common/users，任何已登录用户均可访问，无需 system:user:list 权限
export function getAllUsers() {
  return get('/tech/common/users', { pageNum: 1, pageSize: 1000 })
}
