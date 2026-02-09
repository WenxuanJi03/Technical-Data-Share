<template>
  <view class="page-container">
    <view class="page-header">
      <text class="page-title">新产品评审</text>
      <view class="back-btn" @tap="goBack">← 返回</view>
    </view>
    <view class="content-area">
      <view class="empty-state">
        <text class="empty-icon">✅</text>
        <text class="empty-text">新产品评审任务列表</text>
        <text class="empty-sub">评审功能开发中，敬请期待</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    goBack() { uni.navigateBack() }
  }
}
</script>

<style lang="scss" scoped>
.page-container { min-height: 100vh; background: #f0f2f5; }
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 80rpx 30rpx 30rpx; color: #fff;
  .page-title { font-size: 34rpx; font-weight: 600; }
  .back-btn { font-size: 28rpx; padding: 10rpx 20rpx; background: rgba(255,255,255,0.2); border-radius: 50rpx; }
}
.content-area { padding: 40rpx 24rpx; }
.empty-state {
  text-align: center; padding: 100rpx 0; background: #fff; border-radius: 24rpx;
  .empty-icon { display: block; font-size: 80rpx; margin-bottom: 20rpx; }
  .empty-text { display: block; font-size: 32rpx; font-weight: 600; color: #303133; margin-bottom: 10rpx; }
  .empty-sub { display: block; font-size: 26rpx; color: #909399; }
}
</style>
