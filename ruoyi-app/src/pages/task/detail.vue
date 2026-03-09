<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">试制跟踪详情</text>
      <view style="width:100rpx"></view>
    </view>

    <scroll-view scroll-y class="page-body">
      <!-- 顶部概览 -->
      <view class="detail-header">
        <text class="dh-code">{{ detail.moldCode }}</text>
        <view class="dh-tags">
          <view v-if="detail.moldType" class="dtag dtag-warning">
            <text>{{ detail.moldType }}</text>
          </view>
          <view v-if="detail.surfaceStatus" class="dtag dtag-success">
            <text>{{ detail.surfaceStatus }}</text>
          </view>
          <view v-if="detail.machineType" class="dtag dtag-info">
            <text>{{ detail.machineType }}</text>
          </view>
          <view class="dtag" :class="detail.allProcessDone === '是' ? 'dtag-success' : 'dtag-info'">
            <text>{{ detail.allProcessDone === '是' ? '✓ 全序完成' : '● 进行中' }}</text>
          </view>
        </view>
      </view>

      <!-- 基础信息 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">基础信息</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">产品规格</text>
            <text class="desc-value">{{ detail.productSpec || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">上机次数</text>
            <text class="desc-value">{{ detail.machineCount != null ? detail.machineCount : '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">预计上机</text>
            <text class="desc-value">{{ detail.planMachineTime || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">模具类型</text>
            <text class="desc-value">{{ detail.moldType || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">表面状态</text>
            <text class="desc-value">{{ detail.surfaceStatus || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">上机类型</text>
            <text class="desc-value">{{ detail.machineType || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 压铸阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">压铸阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">上机日期</text>
            <text class="desc-value">{{ detail.hotMachineDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">机台</text>
            <text class="desc-value">{{ detail.hotMachineStation || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">保压时间</text>
            <text class="desc-value">{{ detail.roundKeepTime || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">测量数据</text>
            <text class="desc-value">{{ detail.hotCheckMeasureData || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">生产情况</text>
            <text class="desc-value">{{ detail.hotProduction || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">改善记录</text>
            <text class="desc-value">{{ detail.improveRecord || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">负责人</text>
            <text class="desc-value">{{ detail.hotImprovePerson || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 旋压阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">旋压阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">上机日期</text>
            <text class="desc-value">{{ detail.spinMachineDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">旋压机台</text>
            <text class="desc-value">{{ detail.spinMachineStation || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">生产情况</text>
            <text class="desc-value">{{ detail.spinProduction || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">改模记录</text>
            <text class="desc-value">{{ detail.moldModifyRecord || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">负责人</text>
            <text class="desc-value">{{ detail.spinImprovePerson || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 热处理阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">热处理阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">下转时间</text>
            <text class="desc-value">{{ detail.heatTransferTime || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">接收数量</text>
            <text class="desc-value">{{ detail.heatReceiveCount != null ? detail.heatReceiveCount : '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">下转数量</text>
            <text class="desc-value">{{ detail.heatTransferCount != null ? detail.heatTransferCount : '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 粗车阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">粗车阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">上机日期</text>
            <text class="desc-value">{{ detail.roughMachineDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">负责人</text>
            <text class="desc-value">{{ detail.roughImprovePerson || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">生产情况</text>
            <text class="desc-value">{{ detail.roughProduction || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">改善方案</text>
            <text class="desc-value">{{ detail.improvePlan || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 精车阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">精车阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">精车上机</text>
            <text class="desc-value">{{ detail.fineMachineDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">精车负责人</text>
            <text class="desc-value">{{ detail.fineImprovePerson || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">精车情况</text>
            <text class="desc-value">{{ detail.fineProduction || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 涂装阶段 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">涂装阶段</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">涂装上机</text>
            <text class="desc-value">{{ detail.paintMachineDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">涂装负责人</text>
            <text class="desc-value">{{ detail.paintImprovePerson || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">涂装情况</text>
            <text class="desc-value">{{ detail.paintProduction || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 实验/总结 -->
      <view class="detail-section">
        <view class="section-title-bar">
          <text class="section-title">实验 / 总结</text>
        </view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">冲击试验日</text>
            <text class="desc-value">{{ detail.impactTestDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">试验结果</text>
            <text class="desc-value">{{ detail.impactTestResult || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">完成日期</text>
            <text class="desc-value">{{ detail.completeDate || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">全序完成</text>
            <text class="desc-value">{{ detail.allProcessDone || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">实验说明</text>
            <text class="desc-value">{{ detail.testDescription || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">失效分析</text>
            <text class="desc-value">{{ detail.failAnalysis || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">生产总结</text>
            <text class="desc-value">{{ detail.productionSummary || '-' }}</text>
          </view>
          <view class="desc-item full">
            <text class="desc-label">改善措施</text>
            <text class="desc-value">{{ detail.improveMeasures || '-' }}</text>
          </view>
        </view>
      </view>

      <!-- 底部操作 -->
      <view class="detail-actions">
        <view class="detail-btn btn-edit" @tap="handleEdit">
          <text class="detail-btn-text">✏ 编辑</text>
        </view>
        <view class="detail-btn btn-delete" @tap="handleDelete">
          <text class="detail-btn-text">🗑 删除</text>
        </view>
      </view>

      <view style="height:80rpx"></view>
    </scroll-view>
  </view>
</template>

<script>
import { getTrialTrack, delTrialTrack } from '@/api/trialTrack'

export default {
  data() {
    return {
      trackId: null,
      detail: {}
    }
  },
  computed: {
    navBarStyle() {
      const info = uni.getSystemInfoSync()
      const statusBarHeight = info.statusBarHeight || 20
      return `padding-top: ${statusBarHeight}px`
    }
  },
  onLoad(options) {
    if (options.id) {
      this.trackId = options.id
      this.loadDetail()
    }
  },
  onShow() {
    // 每次显示时重新加载数据，因为可能从编辑页返回
    if (this.trackId) {
      this.loadDetail()
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    loadDetail() {
      uni.showLoading({ title: '加载中' })
      getTrialTrack(this.trackId).then(res => {
        this.detail = res.data || {}
      }).finally(() => {
        uni.hideLoading()
      })
    },
    handleEdit() {
      uni.navigateTo({
        url: `/pages/task/edit?id=${this.trackId}`
      })
    },
    handleDelete() {
      uni.showModal({
        title: '确认删除',
        content: '确认删除模号 "' + this.detail.moldCode + '" 的跟踪记录？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' })
            delTrialTrack(this.trackId).then(() => {
              uni.showToast({ title: '删除成功', icon: 'success' })
              uni.$emit('refreshTaskList')
              setTimeout(() => {
                uni.navigateBack()
              }, 1000)
            }).catch(() => {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }).finally(() => {
              uni.hideLoading()
            })
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
  padding-bottom: 14rpx;
  padding-left: 20rpx;
  padding-right: 20rpx;
}
.nav-back {
  width: 100rpx;
  .nav-back-icon {
    font-size: 56rpx;
    color: #fff;
  }
}
.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}
.page-body {
  flex: 1;
  height: 0;
  padding: 20rpx;
}

/* ===== 详情样式 ===== */
.detail-header {
  text-align: center;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}
.dh-code {
  font-size: 40rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16rpx;
}
.dh-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12rpx;
}
.dtag {
  padding: 6rpx 18rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 500;
  &.dtag-warning { background: #fdf6ec; color: #e6a23c; }
  &.dtag-success { background: #f0f9eb; color: #67c23a; }
  &.dtag-info { background: #ecf5ff; color: #409eff; }
}
.detail-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}
.section-title-bar {
  margin-bottom: 16rpx;
  padding-left: 14rpx;
  border-left: 6rpx solid #6c5bb3;
}
.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #303133;
}
.desc-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.desc-item {
  width: calc(50% - 6rpx);
  &.full { width: 100%; }
  .desc-label {
    display: block;
    font-size: 22rpx;
    color: #909399;
    margin-bottom: 4rpx;
  }
  .desc-value {
    display: block;
    font-size: 26rpx;
    color: #303133;
    word-break: break-all;
  }
}
.detail-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 0;
}
.detail-btn {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 16rpx;
  &:active { opacity: 0.8; }
  .detail-btn-text { font-size: 30rpx; font-weight: 600; }
  &.btn-edit {
    background: #6c5bb3;
    .detail-btn-text { color: #fff; }
  }
  &.btn-delete {
    background: #fff;
    border: 2rpx solid #f56c6c;
    .detail-btn-text { color: #f56c6c; }
  }
}
</style>
