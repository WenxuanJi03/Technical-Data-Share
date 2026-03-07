import config from '@/config/index'
import { getToken } from '@/utils/auth'

// 获取后端地址(支持动态切换)
function getBaseUrl() {
  const customUrl = uni.getStorageSync('customBaseUrl')
  return customUrl || config.baseUrl
}

// 轮毂图像识别:上传图片,返回相似产品列表
export function recognizeProduct(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    const uploadTask = uni.uploadFile({
      url: BASE_URL + '/tech/product/recognize',
      filePath: filePath,
      name: 'file',
      timeout: 120000,
      header: {
        Authorization: token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data)
          } else if (data.code === 401) {
            uni.showToast({ title: '登录已过期', icon: 'none' })
            setTimeout(() => uni.reLaunch({ url: '/pages/login/index' }), 1500)
            reject(data)
          } else {
            uni.showToast({ title: data.msg || '识别失败', icon: 'none' })
            reject(data)
          }
        } catch (e) {
          uni.showToast({ title: '解析响应失败', icon: 'none' })
          reject(e)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 文件上传(通用)
export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.uploadFile({
      url: BASE_URL + '/common/upload',
      filePath: filePath,
      name: 'file',
      timeout: 60000,
      header: {
        Authorization: token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data)
          } else {
            uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
            reject(data)
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        if (err && err.errMsg && err.errMsg.includes('timeout')) {
          uni.showToast({ title: '上传超时，请检查网络', icon: 'none' })
        } else {
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

// 动态获取 BASE_URL（每次调用都读取最新配置，避免登录前后值不一致）
export function getProductBaseUrl() {
  return getBaseUrl()
}

// 兼容旧引用：仍导出静态 BASE_URL，但组件应优先使用 getProductBaseUrl()
export const BASE_URL = getBaseUrl()
