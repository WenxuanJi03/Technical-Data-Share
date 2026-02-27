import { getToken } from './auth'
import config from '@/config/index'

// 获取后端地址(支持动态切换)
function getBaseUrl() {
  // 优先使用用户在登录页选择的服务器
  const customUrl = uni.getStorageSync('customBaseUrl')
  if (customUrl) {
    return customUrl
  }
  // 否则使用配置文件默认值
  return config.baseUrl
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    // 动态获取BASE_URL
    const BASE_URL = getBaseUrl()

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: 30000,
      success: (res) => {
        const { statusCode, data } = res
        if (statusCode === 200) {
          // 业务层成功
          if (data.code === 200) {
            resolve(data)
          } else if (data.code === 401) {
            // Token过期，跳转登录
            uni.showToast({ title: '登录已过期', icon: 'none' })
            setTimeout(() => {
              uni.reLaunch({ url: '/pages/login/index' })
            }, 1500)
            reject(data)
          } else {
            // 业务错误
            uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
            reject(data)
          }
        } else if (statusCode === 401) {
          uni.reLaunch({ url: '/pages/login/index' })
          reject(res)
        } else {
          uni.showToast({ title: '网络错误 ' + statusCode, icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 快捷方法
export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

// 导出BASE_URL供其他组件使用(如文件下载)
export function BASE_URL() {
  return getBaseUrl()
}

export default request
