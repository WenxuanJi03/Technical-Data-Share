import { checkOverdue } from '@/api/tech/trialProcess'

const state = {
  hasOverdue: false
}

const mutations = {
  SET_HAS_OVERDUE: (state, hasOverdue) => {
    state.hasOverdue = hasOverdue
  }
}

const actions = {
  // 检查当前用户是否有逾期任务
  checkUserOverdue({ commit, rootGetters }) {
    return new Promise((resolve) => {
      // 使用用户昵称（nickName）而不是用户名（name），因为负责人存的是昵称
      const nickName = rootGetters.nickName
      console.log('检查逾期任务，当前用户昵称:', nickName)
      if (nickName) {
        checkOverdue(nickName).then(response => {
          const hasOverdue = response.data === true
          console.log('逾期检查结果:', hasOverdue)
          commit('SET_HAS_OVERDUE', hasOverdue)
          resolve(hasOverdue)
        }).catch((err) => {
          console.error('检查逾期任务失败:', err)
          commit('SET_HAS_OVERDUE', false)
          resolve(false)
        })
      } else {
        console.log('未获取到用户昵称')
        resolve(false)
      }
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
