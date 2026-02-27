import Vue from 'vue'
import Vuex from 'vuex'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo, clearAll } from '@/utils/auth'
import { login, getInfo, logout } from '@/api/login'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    token: getToken(),
    userInfo: getUserInfo(),
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
    },
    SET_USER_INFO(state, info) {
      state.userInfo = info
      state.name = info.nickName || info.userName || ''
      state.avatar = info.avatar || ''
    },
    SET_ROLES(state, roles) {
      state.roles = roles
    },
    SET_PERMISSIONS(state, permissions) {
      state.permissions = permissions
    },
    CLEAR(state) {
      state.token = ''
      state.userInfo = null
      state.name = ''
      state.avatar = ''
      state.roles = []
      state.permissions = []
    }
  },
  actions: {
    // 登录
    Login({ commit }, loginData) {
      return new Promise((resolve, reject) => {
        login(loginData).then(res => {
          const token = res.token
          setToken(token)
          commit('SET_TOKEN', token)
          resolve(res)
        }).catch(err => {
          reject(err)
        })
      })
    },
    // 获取用户信息
    GetInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.user || {}
          setUserInfo(user)
          commit('SET_USER_INFO', user)
          commit('SET_ROLES', res.roles || [])
          commit('SET_PERMISSIONS', res.permissions || [])
          resolve(res)
        }).catch(err => {
          reject(err)
        })
      })
    },
    // 退出
    Logout({ commit }) {
      return new Promise((resolve) => {
        logout().catch(() => {}).finally(() => {
          clearAll()
          commit('CLEAR')
          resolve()
        })
      })
    }
  }
})

export default store
