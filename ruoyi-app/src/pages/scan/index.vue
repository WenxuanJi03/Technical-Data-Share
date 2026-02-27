<template>
  <view class="scan-page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">轮毂识别</text>
      <view style="width:60rpx"></view>
    </view>

    <!-- 预览区域 -->
    <view class="scan-preview">
      <view class="preview-frame">
        <view class="preview-circle">
          <image
            v-if="previewUrl"
            :src="previewUrl"
            class="preview-image"
            mode="aspectFit"
          />
          <view v-else class="preview-placeholder">
            <text class="placeholder-icon">📷</text>
            <text class="placeholder-text">将轮毂置于圆形区域内</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮区 -->
    <view class="scan-actions">
      <view
        class="scan-btn"
        :class="{ 'scan-btn-loading': recognizing }"
        @tap="chooseImage"
      >
        <text class="scan-btn-icon">{{ recognizing ? '' : '📸' }}</text>
        <text class="scan-btn-text">{{ recognizing ? '识别中...' : '拍照 / 选择图片' }}</text>
      </view>
      <text class="scan-tips">建议使用正面清晰图片，避免强烈反光和遮挡</text>
    </view>

    <!-- 识别结果 -->
    <view class="result-section" v-if="results.length > 0">
      <view class="result-header">
        <text class="result-title">识别结果</text>
        <text class="result-count">共 {{ results.length }} 条候选</text>
      </view>
      <view class="result-list">
        <view
          v-for="item in results"
          :key="item.productId"
          class="result-item"
          @tap="previewResultImage(item)"
        >
          <view class="result-thumb">
            <image
              v-if="item.frontImage"
              :src="baseUrl + item.frontImage"
              mode="aspectFill"
              class="thumb-img"
            />
            <view v-else class="thumb-placeholder">
              <text class="thumb-placeholder-icon">🖼</text>
            </view>
          </view>
          <view class="result-info">
            <view class="result-top">
              <text class="wheel-code">{{ item.wheelCode }}</text>
              <text class="score-badge" v-if="item.similarityScore != null">
                {{ (item.similarityScore * 100).toFixed(1) }}%
              </text>
            </view>
            <view class="result-row">
              <text class="r-label">客户</text>
              <text class="r-value">{{ item.customer || '-' }}</text>
            </view>
            <view class="result-row">
              <text class="r-label">规格</text>
              <text class="r-value">{{ item.sizeSpec || '-' }}</text>
              <text class="r-label ml">PCD</text>
              <text class="r-value">{{ item.pcd || '-' }}</text>
            </view>
            <view class="result-row" v-if="item.surfaceStatus">
              <text class="r-label">表面</text>
              <text class="r-value">{{ item.surfaceStatus }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-section" v-else-if="!recognizing">
      <text class="empty-icon">🔍</text>
      <text class="empty-text">拍照或选择一张轮毂图片开始识别</text>
    </view>

    <!-- 识别中遮罩 -->
    <view class="recognizing-mask" v-if="recognizing">
      <view class="recognizing-box">
        <view class="spin-circle"></view>
        <text class="recognizing-text">正在识别中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { recognizeProduct } from '@/api/product'
import config from '@/config/index'

export default {
  name: 'WheelScan',
  data() {
    return {
      previewUrl: '',
      recognizing: false,
      results: [],
      baseUrl: config.baseUrl
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    chooseImage() {
      if (this.recognizing) return
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const filePath = res.tempFilePaths[0]
          this.previewUrl = filePath
          this.startRecognize(filePath)
        }
      })
    },
    startRecognize(filePath) {
      this.recognizing = true
      this.results = []
      recognizeProduct(filePath)
        .then(res => {
          this.results = res.data || []
          if (!this.results.length) {
            uni.showToast({ title: '未识别到相似轮型', icon: 'none' })
          }
        })
        .catch(() => {})
        .finally(() => {
          this.recognizing = false
        })
    },
    previewResultImage(item) {
      if (item.frontImage) {
        uni.previewImage({
          urls: [this.baseUrl + item.frontImage],
          current: this.baseUrl + item.frontImage
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.scan-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding-bottom: 40rpx;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f 0%, #6c5bb3 100%);
  padding: 100rpx 30rpx 30rpx;
}
.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-back-icon {
  font-size: 52rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

/* 预览区 */
.scan-preview {
  padding: 30rpx 40rpx 20rpx;
  display: flex;
  justify-content: center;
}
.preview-frame {
  width: 520rpx;
  height: 520rpx;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
}
.preview-circle {
  width: 460rpx;
  height: 460rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx dashed rgba(108, 91, 179, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9ff;
}
.preview-image {
  width: 100%;
  height: 100%;
}
.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}
.placeholder-icon {
  font-size: 80rpx;
  opacity: 0.5;
}
.placeholder-text {
  font-size: 26rpx;
  color: #c0c4cc;
}

/* 操作按钮 */
.scan-actions {
  padding: 24rpx 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}
.scan-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 6rpx 20rpx rgba(108, 91, 179, 0.4);
  &:active { opacity: 0.85; transform: scale(0.98); }
}
.scan-btn-loading {
  background: #909399;
  box-shadow: none;
}
.scan-btn-icon { font-size: 36rpx; }
.scan-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}
.scan-tips {
  font-size: 22rpx;
  color: #909399;
  text-align: center;
}

/* 识别结果 */
.result-section {
  margin: 30rpx 24rpx 0;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}
.result-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}
.result-count {
  font-size: 22rpx;
  color: #909399;
}
.result-list {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}
.result-item {
  display: flex;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  &:last-child { border-bottom: none; }
  &:active { background: #f8f9ff; }
}
.result-thumb {
  width: 110rpx;
  height: 110rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f5f7fa;
  margin-right: 20rpx;
  flex-shrink: 0;
}
.thumb-img {
  width: 100%;
  height: 100%;
}
.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.thumb-placeholder-icon { font-size: 44rpx; }

.result-info { flex: 1; }
.result-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}
.wheel-code {
  font-size: 30rpx;
  font-weight: 700;
  color: #303133;
}
.score-badge {
  font-size: 22rpx;
  color: #fff;
  background: #67c23a;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}
.result-row {
  display: flex;
  align-items: center;
  margin-top: 6rpx;
}
.r-label {
  font-size: 22rpx;
  color: #909399;
  margin-right: 6rpx;
  flex-shrink: 0;
}
.r-value {
  font-size: 22rpx;
  color: #606266;
  margin-right: 10rpx;
}
.ml { margin-left: 12rpx; }

/* 空状态 */
.empty-section {
  padding: 80rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}
.empty-icon { font-size: 80rpx; opacity: 0.4; }
.empty-text { font-size: 26rpx; color: #c0c4cc; }

/* 识别中遮罩 */
.recognizing-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.recognizing-box {
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}
.spin-circle {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid #f0f0f0;
  border-top-color: #6c5bb3;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.recognizing-text {
  font-size: 28rpx;
  color: #606266;
}
</style>
