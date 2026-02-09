const TOKEN_KEY = 'token'
const USER_KEY = 'userInfo'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getUserInfo() {
  const data = uni.getStorageSync(USER_KEY)
  return data ? JSON.parse(data) : null
}

export function setUserInfo(info) {
  uni.setStorageSync(USER_KEY, JSON.stringify(info))
}

export function removeUserInfo() {
  uni.removeStorageSync(USER_KEY)
}

export function clearAll() {
  removeToken()
  removeUserInfo()
}
