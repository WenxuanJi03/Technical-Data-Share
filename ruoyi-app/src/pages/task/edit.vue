<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">{{ editForm.trackId ? '编辑试制跟踪' : '新增试制跟踪' }}</text>
      <view style="width:100rpx"></view>
    </view>

    <scroll-view scroll-y class="page-body">
      <!-- 基础信息 -->
      <view class="edit-section">
        <text class="edit-section-title">基础信息</text>
        <view class="edit-form-item">
          <text class="edit-label">模号 *</text>
          <input class="edit-input" v-model="editForm.moldCode" placeholder="请输入模号" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">产品规格</text>
          <input class="edit-input" v-model="editForm.productSpec" placeholder="请输入产品规格" />
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">上机次数</text>
            <input class="edit-input" type="number" v-model="editForm.machineCount" placeholder="次数" />
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">模具类型</text>
            <input class="edit-input" v-model="editForm.moldType" placeholder="首模/改模" />
          </view>
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">表面状态</text>
            <input class="edit-input" v-model="editForm.surfaceStatus" placeholder="精车/全涂" />
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">上机类型</text>
            <input class="edit-input" v-model="editForm.machineType" placeholder="小批量/量产" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">预计上机时间</text>
          <picker mode="date" :value="editForm.planMachineTime || currentDate" @change="onEditDateChange('planMachineTime', $event)">
            <view class="edit-input date-picker">
              <text :style="{ color: editForm.planMachineTime ? '#303133' : '#999' }">{{ editForm.planMachineTime || '选择日期' }}</text>
              <text class="picker-arrow">▾</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 压铸阶段 -->
      <view class="edit-section">
        <text class="edit-section-title">压铸阶段</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">压铸上机</text>
            <picker mode="date" :value="editForm.hotMachineDate || currentDate" @change="onEditDateChange('hotMachineDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.hotMachineDate ? '#303133' : '#999' }">{{ editForm.hotMachineDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">机台</text>
            <input class="edit-input" v-model="editForm.hotMachineStation" placeholder="机台编号" />
          </view>
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">保压时间</text>
            <input class="edit-input" v-model="editForm.roundKeepTime" placeholder="保压时间" />
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">负责人</text>
            <input class="edit-input" v-model="editForm.hotImprovePerson" placeholder="负责人" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">测量数据</text>
          <input class="edit-input" v-model="editForm.hotCheckMeasureData" placeholder="检查站首件测量数据" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">生产情况</text>
          <textarea class="edit-textarea" v-model="editForm.hotProduction" placeholder="请输入生产情况" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">改善记录</text>
          <textarea class="edit-textarea" v-model="editForm.improveRecord" placeholder="请输入改善记录" />
        </view>
      </view>

      <!-- 旋压阶段 -->
      <view class="edit-section">
        <text class="edit-section-title">旋压阶段</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">旋压上机</text>
            <picker mode="date" :value="editForm.spinMachineDate || currentDate" @change="onEditDateChange('spinMachineDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.spinMachineDate ? '#303133' : '#999' }">{{ editForm.spinMachineDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">旋压机台</text>
            <input class="edit-input" v-model="editForm.spinMachineStation" placeholder="机台编号" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">负责人</text>
          <input class="edit-input" v-model="editForm.spinImprovePerson" placeholder="负责人姓名" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">生产情况</text>
          <textarea class="edit-textarea" v-model="editForm.spinProduction" placeholder="请输入生产情况" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">改模记录</text>
          <textarea class="edit-textarea" v-model="editForm.moldModifyRecord" placeholder="请输入改模记录" />
        </view>
      </view>

      <!-- 热处理阶段 -->
      <view class="edit-section">
        <text class="edit-section-title">热处理阶段</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">下转时间</text>
            <picker mode="date" :value="editForm.heatTransferTime || currentDate" @change="onEditDateChange('heatTransferTime', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.heatTransferTime ? '#303133' : '#999' }">{{ editForm.heatTransferTime || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">接收数量</text>
            <input class="edit-input" type="number" v-model="editForm.heatReceiveCount" placeholder="数量" />
          </view>
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">下转数量</text>
            <input class="edit-input" type="number" v-model="editForm.heatTransferCount" placeholder="数量" />
          </view>
        </view>
      </view>

      <!-- 粗车阶段 -->
      <view class="edit-section">
        <text class="edit-section-title">粗车阶段</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">粗车上机</text>
            <picker mode="date" :value="editForm.roughMachineDate || currentDate" @change="onEditDateChange('roughMachineDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.roughMachineDate ? '#303133' : '#999' }">{{ editForm.roughMachineDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">负责人</text>
            <input class="edit-input" v-model="editForm.roughImprovePerson" placeholder="负责人姓名" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">生产情况</text>
          <textarea class="edit-textarea" v-model="editForm.roughProduction" placeholder="请输入生产情况" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">改善方案</text>
          <textarea class="edit-textarea" v-model="editForm.improvePlan" placeholder="请输入改善方案" />
        </view>
      </view>

      <!-- 精车 / 涂装阶段 -->
      <view class="edit-section">
        <text class="edit-section-title">精车 / 涂装阶段</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">精车上机</text>
            <picker mode="date" :value="editForm.fineMachineDate || currentDate" @change="onEditDateChange('fineMachineDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.fineMachineDate ? '#303133' : '#999' }">{{ editForm.fineMachineDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">精车负责人</text>
            <input class="edit-input" v-model="editForm.fineImprovePerson" placeholder="负责人" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">精车情况</text>
          <textarea class="edit-textarea" v-model="editForm.fineProduction" placeholder="请输入精车生产情况" />
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">涂装上机</text>
            <picker mode="date" :value="editForm.paintMachineDate || currentDate" @change="onEditDateChange('paintMachineDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.paintMachineDate ? '#303133' : '#999' }">{{ editForm.paintMachineDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">涂装负责人</text>
            <input class="edit-input" v-model="editForm.paintImprovePerson" placeholder="负责人" />
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">涂装情况</text>
          <textarea class="edit-textarea" v-model="editForm.paintProduction" placeholder="请输入涂装生产情况" />
        </view>
      </view>

      <!-- 实验/总结 -->
      <view class="edit-section">
        <text class="edit-section-title">实验 / 总结</text>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">冲击试验日期</text>
            <picker mode="date" :value="editForm.impactTestDate || currentDate" @change="onEditDateChange('impactTestDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.impactTestDate ? '#303133' : '#999' }">{{ editForm.impactTestDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">冲击结果</text>
            <input class="edit-input" v-model="editForm.impactTestResult" placeholder="试验结果" />
          </view>
        </view>
        <view class="edit-form-row">
          <view class="edit-form-item half">
            <text class="edit-label">生产完成日期</text>
            <picker mode="date" :value="editForm.completeDate || currentDate" @change="onEditDateChange('completeDate', $event)">
              <view class="edit-input date-picker">
                <text :style="{ color: editForm.completeDate ? '#303133' : '#999' }">{{ editForm.completeDate || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
          <view class="edit-form-item half">
            <text class="edit-label">全序完成</text>
            <picker mode="selector" :range="['否', '是']" :value="editForm.allProcessDone === '是' ? 1 : 0" @change="onAllProcessDoneChange">
              <view class="edit-input date-picker">
                <text>{{ editForm.allProcessDone || '否' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
          </view>
        </view>
        <view class="edit-form-item">
          <text class="edit-label">实验说明</text>
          <textarea class="edit-textarea" v-model="editForm.testDescription" placeholder="请输入实验说明" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">失效分析</text>
          <textarea class="edit-textarea" v-model="editForm.failAnalysis" placeholder="请输入失效分析" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">生产总结</text>
          <textarea class="edit-textarea" v-model="editForm.productionSummary" placeholder="请输入生产总结" />
        </view>
        <view class="edit-form-item">
          <text class="edit-label">改善措施</text>
          <textarea class="edit-textarea" v-model="editForm.improveMeasures" placeholder="请输入改善措施" />
        </view>
      </view>

      <view class="bottom-action-fixed">
        <view class="btn-save-bottom" @tap="submitEdit">
          <text class="btn-save-text">{{ editLoading ? '保存中...' : '保存' }}</text>
        </view>
      </view>
      <view style="height:120rpx"></view>
    </scroll-view>
  </view>
</template>

<script>
import { getTrialTrack, updateTrialTrack } from '@/api/trialTrack'

export default {
  data() {
    return {
      trackId: null,
      editLoading: false,
      editForm: {},
      currentDate: ''
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
    const now = new Date()
    this.currentDate = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0')
    
    if (options.id) {
      this.trackId = options.id
      this.loadData()
    }
  },
  methods: {
    goBack() {
      if (this.editLoading) return
      uni.navigateBack()
    },
    loadData() {
      uni.showLoading({ title: '加载中' })
      getTrialTrack(this.trackId).then(res => {
        this.editForm = res.data || {}
      }).catch(() => {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }).finally(() => {
        uni.hideLoading()
      })
    },
    onEditDateChange(field, e) {
      this.$set(this.editForm, field, e.detail.value)
    },
    onAllProcessDoneChange(e) {
      this.editForm.allProcessDone = e.detail.value === 1 ? '是' : '否'
    },
    submitEdit() {
      if (!this.editForm.moldCode) {
        uni.showToast({ title: '模号不能为空', icon: 'none' })
        return
      }
      this.editLoading = true
      updateTrialTrack(this.editForm).then(() => {
        uni.showToast({ title: '保存成功', icon: 'success' })
        uni.$emit('refreshTaskList')
        setTimeout(() => {
          uni.navigateBack()
        }, 1000)
      }).catch(() => {
        uni.showToast({ title: '保存失败', icon: 'none' })
      }).finally(() => {
        this.editLoading = false
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

/* ===== 编辑表单 ===== */
.edit-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
}
.edit-section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 20rpx;
  padding-left: 14rpx;
  border-left: 6rpx solid #6c5bb3;
}
.edit-form-item {
  margin-bottom: 20rpx;
  &.half { flex: 1; min-width: 0; }
}
.edit-form-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 0;
}
.edit-label {
  display: block;
  font-size: 24rpx;
  color: #606266;
  margin-bottom: 8rpx;
}
.edit-input {
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 24rpx 20rpx;
  font-size: 28rpx;
  line-height: 40rpx;
  min-height: 90rpx;
  color: #303133;
  width: 100%;
  box-sizing: border-box;
  &.date-picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
.edit-textarea {
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 24rpx 20rpx;
  font-size: 28rpx;
  line-height: 40rpx;
  color: #303133;
  width: 100%;
  min-height: 160rpx;
  box-sizing: border-box;
}
.picker-arrow {
  font-size: 24rpx;
  color: #909399;
}
.bottom-action-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 40rpx;
  background: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
  z-index: 100;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
.btn-save-bottom {
  width: 100%;
  height: 88rpx;
  background: #07c160;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
  &:active { opacity: 0.85; }
}
</style>
