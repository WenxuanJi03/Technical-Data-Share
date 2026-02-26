import config from '@/config/index'
import { getToken } from '@/utils/auth'

const BASE_URL = config.baseUrl

// 轮毂图像识别：上传图片，返回相似产品列表
export function recognizeProduct(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: BASE_URL + '/tech/product/recognize',
      filePath: filePath,
      name: 'file',
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

// 文件上传（通用）
export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: BASE_URL + '/common/upload',
      filePath: filePath,
      name: 'file',
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
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export { BASE_URL }
