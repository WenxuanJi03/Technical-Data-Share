<script>
export default {
  onLaunch() {
    console.log('App Launch')
    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
page {
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 28rpx;
  color: #303133;
}

.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

/* 强制提升 vConsole 层级，防止被遮挡 (兼容各版本及 H5 组件) */
#__vconsole {
  z-index: 9999999 !important;
}
</style>
