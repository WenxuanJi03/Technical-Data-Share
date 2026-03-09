<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">{{ currentStep.name || '' }} · 阶段意见</text>
      <view class="nav-add"></view>
    </view>
    
    <scroll-view scroll-y class="fullpage-body">
      <view class="comment-history-section">
        <view v-if="historyComments.length > 0">
          <view v-for="(item, cIdx) in historyComments" :key="cIdx" class="comment-card">
            <view class="comment-card-header">
              <text class="comment-user">{{ item.user }}</text>
              <text class="comment-time">{{ formatChineseDateTime(item.time) }}</text>
            </view>
            <view v-if="editCommentIdx === cIdx" class="comment-edit-area">
              <textarea class="comment-textarea" v-model="editCommentText" :placeholder="'编辑意见...'" />
              <view class="comment-edit-actions">
                <view class="edit-cancel-btn" @tap="cancelEditComment">取消</view>
                <view class="edit-save-btn" @tap="saveEditComment(cIdx)">保存</view>
              </view>
            </view>
            <view v-else class="comment-content">{{ item.content }}</view>
            <view class="comment-card-actions" v-if="editCommentIdx !== cIdx && item.user === currentUserName">
              <view class="comment-action-btn" @tap="editComment(cIdx)">
                <text class="comment-action-text">编辑</text>
              </view>
              <view class="comment-action-btn" @tap="deleteComment(cIdx)">
                <text class="comment-action-text danger">删除</text>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="comment-history-empty">
          <text class="history-empty-text">暂无历史意见</text>
        </view>
      </view>
      <!-- Bottom spacing -->
      <view style="height: 300rpx"></view>
    </scroll-view>

    <!-- 意见输入区 -->
    <view class="bottom-action-fixed">
      <view class="comment-input-section">
        <textarea
          class="comment-textarea"
          v-model="commentText"
          placeholder="请输入意见，多行输入"
          maxlength="500"
        />
        <view class="comment-submit-row">
          <text class="comment-char-count">{{ commentText.length }}/500</text>
          <view class="comment-submit-btn" @tap="submitComment">
            <text class="comment-submit-text">提交意见</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { updateTrialProcess } from '@/api/trialProcess'

export default {
  data() {
    return {
      statusBarHeight: 44,
      capsuleRightPadding: 104,
      
      currentProcess: null,
      currentStep: {},
      currentStepIndex: 0,
      
      commentText: '',
      historyComments: [],
      editCommentIdx: -1,
      editCommentText: ''
    }
  },
  computed: {
    currentUserName() {
      return this.$store.state.name || ''
    },
    navBarStyle() {
      return {
        paddingTop: this.statusBarHeight + 'px',
        paddingRight: this.capsuleRightPadding + 'px',
        paddingBottom: '16px',
        paddingLeft: '16px'
      }
    }
  },
  onLoad(options) {
    try {
      const sysInfo = uni.getSystemInfoSync()
      this.statusBarHeight = sysInfo.statusBarHeight || 44
      // #ifdef MP-WEIXIN
      try {
        const capsule = wx.getMenuButtonBoundingClientRect()
        this.capsuleRightPadding = sysInfo.windowWidth - capsule.left + 8
      } catch (e) {
        this.capsuleRightPadding = 104
      }
      // #endif
    } catch (e) {}

    const eventChannel = this.getOpenerEventChannel()
    if (eventChannel && eventChannel.on) {
      eventChannel.on('acceptDataFromOpenerPage', (data) => {
        this.initData(data.data, parseInt(options.stepIndex))
      })
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    initData(process, index) {
      this.currentProcess = process
      this.currentStepIndex = index
      
      const steps = [
        { name: '基础信息' }, { name: '压铸阶段' }, { name: '旋压阶段' }, { name: '热处理阶段' },
        { name: '粗车阶段' }, { name: '精车阶段' }, { name: '涂装阶段' }, { name: '实验/总结' }
      ]
      this.currentStep = steps[index]
      
      this.commentText = ''
      this.editCommentIdx = -1
      this.editCommentText = ''
      
      const key = `step${index + 1}Comments`
      try {
        this.historyComments = process[key] ? JSON.parse(process[key]) : []
      } catch (e) {
        this.historyComments = []
      }
    },
    formatChineseDateTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const year = d.getFullYear()
      const month = d.getMonth() + 1
      const day = d.getDate()
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      return `${year}年${month}月${day}日 ${hours}:${minutes}`
    },
    submitComment() {
      if (!this.commentText.trim()) {
        uni.showToast({ title: '请输入意见内容', icon: 'none' })
        return
      }
      const key = `step${this.currentStepIndex + 1}Comments`
      const newComment = {
        user: this.currentUserName || '用户',
        time: new Date().toISOString(),
        content: this.commentText
      }
      const comments = [...this.historyComments, newComment]
      const updateData = {
        processId: this.currentProcess.processId,
        [key]: JSON.stringify(comments)
      }
      uni.showLoading({ title: '保存中...' })
      updateTrialProcess(updateData).then(() => {
        uni.hideLoading()
        uni.showToast({ title: '意见已提交', icon: 'success' })
        this.historyComments = comments
        this.commentText = ''
        uni.$emit('refreshProcessList')
      }).catch(() => {
        uni.hideLoading()
      })
    },
    editComment(cIdx) {
      this.editCommentIdx = cIdx
      this.editCommentText = this.historyComments[cIdx].content
    },
    cancelEditComment() {
      this.editCommentIdx = -1
      this.editCommentText = ''
    },
    saveEditComment(cIdx) {
      if (!this.editCommentText.trim()) {
        uni.showToast({ title: '内容不能为空', icon: 'none' })
        return
      }
      const key = `step${this.currentStepIndex + 1}Comments`
      this.historyComments[cIdx].content = this.editCommentText
      this.historyComments[cIdx].time = new Date().toISOString()
      this.historyComments[cIdx].edited = true
      
      const updateData = {
        processId: this.currentProcess.processId,
        [key]: JSON.stringify(this.historyComments)
      }
      uni.showLoading({ title: '保存中...' })
      updateTrialProcess(updateData).then(() => {
        uni.hideLoading()
        uni.showToast({ title: '修改成功', icon: 'success' })
        this.editCommentIdx = -1
        this.editCommentText = ''
        uni.$emit('refreshProcessList')
      }).catch(() => {
        uni.hideLoading()
      })
    },
    deleteComment(cIdx) {
      uni.showModal({
        title: '确认删除',
        content: '确认删除此条意见？',
        success: (res) => {
          if (res.confirm) {
            const key = `step${this.currentStepIndex + 1}Comments`
            this.historyComments.splice(cIdx, 1)
            const updateData = {
              processId: this.currentProcess.processId,
              [key]: JSON.stringify(this.historyComments)
            }
            uni.showLoading({ title: '删除中...' })
            updateTrialProcess(updateData).then(() => {
              uni.hideLoading()
              uni.showToast({ title: '删除成功', icon: 'success' })
              uni.$emit('refreshProcessList')
            }).catch(() => { uni.hideLoading() })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 88rpx 208rpx 16rpx 30rpx;
  flex-shrink: 0;
}
.nav-back {
  width: 60rpx; height: 60rpx;
  display: flex; align-items: center; justify-content: center;
}
.nav-back-icon { font-size: 52rpx; color: #fff; font-weight: 300; line-height: 1; }
.nav-title { font-size: 34rpx; font-weight: 600; color: #fff; }
.nav-add { width: 60rpx; height: 60rpx; }

.fullpage-body { flex: 1; overflow-y: auto; }

.comment-history-section {
  margin: 20rpx 24rpx;
}
.comment-card {
  background: #fff; border-radius: 16rpx; padding: 20rpx 24rpx; margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.comment-card-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 12rpx; }
.comment-user { font-size: 24rpx; color: #409eff; font-weight: 600; flex: 1; }
.comment-time { font-size: 20rpx; color: #c0c4cc; }
.comment-content { font-size: 26rpx; color: #303133; line-height: 1.6; margin-bottom: 12rpx; }
.comment-edit-area { margin-bottom: 12rpx; }
.comment-edit-actions { display: flex; gap: 16rpx; justify-content: flex-end; }
.edit-cancel-btn { padding: 8rpx 20rpx; border-radius: 12rpx; background: #f5f7fa; font-size: 24rpx; color: #606266; }
.edit-save-btn { padding: 8rpx 20rpx; border-radius: 12rpx; background: #6c5bb3; font-size: 24rpx; color: #fff; }
.comment-card-actions {
  display: flex; gap: 20rpx; justify-content: flex-end; border-top: 1rpx solid #f5f5f5; padding-top: 12rpx;
}
.comment-action-text { font-size: 22rpx; color: #909399; }
.comment-action-text.danger { color: #f56c6c; }
.comment-history-empty { padding: 40rpx 24rpx; text-align: center; }
.history-empty-text { font-size: 26rpx; color: #c0c4cc; }

.bottom-action-fixed {
  position: fixed; bottom: 0; left: 0; right: 0; padding: 20rpx 40rpx;
  background: #fff; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05); z-index: 100;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
.comment-input-section { margin: 0; }
.comment-textarea {
  width: 100%; border: 2rpx solid #ebeef5; border-radius: 12rpx; padding: 16rpx;
  font-size: 26rpx; color: #303133; background: #f8f9fc; box-sizing: border-box;
  min-height: 160rpx; margin-bottom: 16rpx;
}
.comment-submit-row { display: flex; align-items: center; justify-content: space-between; }
.comment-char-count { font-size: 22rpx; color: #c0c4cc; }
.comment-submit-btn {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3); border-radius: 30rpx; padding: 12rpx 30rpx;
}
.comment-submit-text { font-size: 26rpx; color: #fff; font-weight: 600; }
</style>
