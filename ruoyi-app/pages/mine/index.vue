<template>
  <view class="mine-page">
    <view class="header-bg">
      <view class="user-section">
        <view class="avatar">👤</view>
        <text class="user-name">{{ userName }}</text>
        <text class="user-role">{{ userRole }}</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @tap="showAbout">
        <text class="menu-icon">ℹ️</text>
        <text class="menu-text">关于系统</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @tap="clearCache">
        <text class="menu-icon">🗑</text>
        <text class="menu-text">清除缓存</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item logout" @tap="handleLogout">
        <text class="menu-icon">🚪</text>
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="version-info">
      <text>技术管理系统 v1.0.0</text>
    </view>
  </view>
</template>

<script>
export default {
  computed: {
    userName() {
      return this.$store.state.name || '用户'
    },
    userRole() {
      const roles = this.$store.state.roles || []
      return roles.length > 0 ? roles.join('、') : '普通用户'
    }
  },
  methods: {
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('Logout').then(() => {
              uni.reLaunch({ url: '/pages/login/index' })
            })
          }
        }
      })
    },
    showAbout() {
      uni.showModal({
        title: '关于系统',
        content: '技术管理系统 v1.0.0\n基于RuoYi + uni-app开发',
        showCancel: false
      })
    },
    clearCache() {
      uni.showModal({
        title: '提示',
        content: '确定清除缓存吗？',
        success: (res) => {
          if (res.confirm) {
            uni.clearStorageSync()
            uni.showToast({ title: '缓存已清除', icon: 'success' })
            setTimeout(() => {
              uni.reLaunch({ url: '/pages/login/index' })
            }, 1500)
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.header-bg {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 80rpx 30rpx 60rpx;
  border-radius: 0 0 40rpx 40rpx;
}

.user-section {
  display: flex;
  flex-direction: column;
  align-items: center;

  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 60rpx;
    margin-bottom: 16rpx;
  }

  .user-name {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8rpx;
  }

  .user-role {
    font-size: 24rpx;
    color: rgba(255,255,255,0.7);
  }
}

.menu-list {
  margin: 30rpx 24rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 28rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
  &:active { background: #f5f7fa; }

  .menu-icon {
    font-size: 36rpx;
    margin-right: 20rpx;
  }

  .menu-text {
    flex: 1;
    font-size: 30rpx;
    color: #303133;
  }

  .menu-arrow {
    font-size: 32rpx;
    color: #c0c4cc;
  }

  &.logout .menu-text {
    color: #f56c6c;
  }
}

.version-info {
  text-align: center;
  padding: 40rpx;
  font-size: 24rpx;
  color: #c0c4cc;
}
</style>
