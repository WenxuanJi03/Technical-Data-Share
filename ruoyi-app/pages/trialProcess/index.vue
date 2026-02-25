<template>
  <view class="page">
    <scroll-view scroll-y class="list">
      <view v-for="item in list" :key="item.processId" class="card">
        <view class="card-head">
          <text class="code">{{ item.projectCode }}</text>
          <text class="initiator">发起人：{{ item.initiator }}</text>
        </view>
        <view class="steps">
          <view v-for="(step, idx) in getSteps(item)" :key="idx" class="step">
            <text class="step-name">{{ step.name }}</text>
            <text class="step-status">{{ formatStepStatus(step.status) }}</text>
          </view>
        </view>
        <view class="card-foot">
          <text class="time">创建时间：{{ item.createTime || '-' }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { listTrialProcess } from '@/api/trialProcess'

export default {
  data() {
    return {
      list: []
    }
  },
  onShow() {
    this.loadList()
  },
  methods: {
    loadList() {
      listTrialProcess({ pageNum: 1, pageSize: 100 }).then(res => {
        this.list = res.rows || []
      })
    },
    getSteps(item) {
      return [
        { name: '基础信息',      status: item.step1Status },
        { name: '热工阶段',      status: item.step2Status },
        { name: '旋压阶段',      status: item.step3Status },
        { name: '粗车阶段',      status: item.step4Status },
        { name: '精车/涂装阶段',  status: item.step5Status },
        { name: '实验/总结',      status: item.step6Status }
      ]
    },
    formatStepStatus(s) {
      if (s === 'done') return '已完成'
      if (s === 'active') return '进行中'
      return '未开始'
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20rpx;
}
.list {
  height: 100vh;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.code {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}
.initiator {
  font-size: 24rpx;
  color: #909399;
}
.steps {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 12rpx;
}
.step {
  width: 50%;
  padding: 6rpx 0;
  font-size: 22rpx;
  color: #606266;
}
.step-name {
  margin-right: 8rpx;
}
.step-status {
  color: #909399;
}
.card-foot {
  font-size: 22rpx;
  color: #c0c4cc;
}
</style>

