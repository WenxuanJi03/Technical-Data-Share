import config from '@/config/index'
import { getToken } from '@/utils/auth'

// 获取后端地址(支持动态切换)
function getBaseUrl() {
  const customUrl = uni.getStorageSync('customBaseUrl')
  return customUrl || config.baseUrl
}

// 获取毛胚图列表
export function listBlankImage(query) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/blankImage/list',
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

// 获取毛胚图详情
export function getBlankImage(blankId) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/blankImage/' + blankId,
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

// 新增毛胚图
export function addBlankImage(data) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/blankImage',
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

// 修改毛胚图
export function updateBlankImage(data) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/blankImage',
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

// 删除毛胚图
export function delBlankImage(blankId) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const BASE_URL = getBaseUrl()
    uni.request({
      url: BASE_URL + '/tech/blankImage/' + blankId,
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
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * 拼接图片完整 URL
 */
export function buildImageUrl(path) {
  if (!path) return ''
  if (path.indexOf('http') === 0) return path
  const base = getBaseUrl().replace(/\/$/, '')
  const p = (path + '').replace(/^\//, '')
  return p ? base + '/' + p : base
}

/**
 * 将远程图片下载到本地临时文件
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
