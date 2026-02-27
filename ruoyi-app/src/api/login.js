import { get, post } from '@/utils/request'

// 获取验证码
export function captchaImage() {
  return get('/captchaImage')
}

// 登录
export function login(data) {
  return post('/login', data)
}

// 获取用户信息
export function getInfo() {
  return get('/getInfo')
}

// 退出登录
export function logout() {
  return post('/logout')
}
