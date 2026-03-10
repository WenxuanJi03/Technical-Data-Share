import config from '@/config/index'
import { getToken } from '@/utils/auth'

// 获取后端地址(支持动态切换)
function getBaseUrl() {
  const customUrl = uni.getStorageSync('customBaseUrl')
  return customUrl || config.baseUrl
}

// 获取产品清单列表
export function listProduct(query) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/product/list',
      method: 'GET',
      data: query,
      header: {
        Authorization: token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({ title: res.data.msg || '获取列表失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 获取产品详情
export function getProduct(productId) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/product/' + productId,
      method: 'GET',
      header: {
        Authorization: token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({ title: res.data.msg || '获取详情失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
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

// 新增产品
export function addProduct(data) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/product',
      method: 'POST',
      data: data,
      header: {
        Authorization: token ? 'Bearer ' + token : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({ title: res.data.msg || '新增失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 修改产品
export function updateProduct(data) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/product',
      method: 'PUT',
      data: data,
      header: {
        Authorization: token ? 'Bearer ' + token : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({ title: res.data.msg || '修改失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 删除产品
export function delProduct(productId) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/product/' + productId,
      method: 'DELETE',
      header: {
        Authorization: token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data)
        } else {
          uni.showToast({ title: res.data.msg || '删除失败', icon: 'none' })
          reject(res.data)
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

/**
 * 拼接图片完整 URL，供 <image src> 使用。
 * 保证 base 与 path 之间只有一个 /，避免后端返回 "profile/upload/xxx" 时拼成错址。
 * 真机调试时 baseUrl 请使用电脑局域网 IP（如 http://192.168.1.66:8080），手机无法解析 JWX.local。
 */
export function buildImageUrl(path) {
  if (!path) return ''
  if (path.indexOf('http') === 0) return path
  const base = getBaseUrl().replace(/\/$/, '')
  const p = (path + '').replace(/^\//, '')
  return p ? base + '/' + p : base
}

/**
 * 将远程图片下载到本地临时文件，返回可直接用于 <image src> 的本地路径。
 * 真机调试时 <image> 组件的原生加载器可能无法访问 HTTP 局域网地址，
 * 而 uni.downloadFile 遵守"不校验合法域名"设置，下载后的本地文件则无此限制。
 */
const _imgCache = {}

export function resolveImageUrl(path) {
  return new Promise(function (resolve) {
    if (!path) return resolve('')
    var url = buildImageUrl(path)
    if (!url) return resolve('')
    if (_imgCache[url]) return resolve(_imgCache[url])

    uni.downloadFile({
      url: url,
      header: {
        Authorization: getToken() ? 'Bearer ' + getToken() : ''
      },
      success: function (res) {
        if (res.statusCode === 200 && res.tempFilePath) {
          _imgCache[url] = res.tempFilePath
          resolve(res.tempFilePath)
        } else {
          resolve(url)
        }
      },
      fail: function () {
        resolve(url)
      }
    })
  })
}

// 兼容旧引用：仍导出静态 BASE_URL，但组件应优先使用 getProductBaseUrl()
export const BASE_URL = getBaseUrl()
