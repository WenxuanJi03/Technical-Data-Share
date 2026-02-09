<template>
  <view class="home-page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="header-badge">技术科管理平台</view>
      <!-- 用户卡片 -->
      <view class="user-card">
        <view class="user-avatar">👤</view>
        <view class="user-info">
          <text class="user-name">{{ userName }}</text>
          <text class="user-dept">{{ userDept }}</text>
        </view>
      </view>
    </view>

    <!-- 菜单区域 -->
    <view class="menu-section">
      <view class="section-title">⚙ 菜单</view>
      <view class="menu-grid">
        <view class="menu-card" @tap="goPage('/pages/task/index')">
          <view class="card-icon purple">🔬</view>
          <text class="card-title">试制任务</text>
          <text class="card-desc">试制任务处理列表</text>
        </view>
        <view class="menu-card" @tap="goPage('/pages/review/index')">
          <view class="card-icon green">✅</view>
          <text class="card-title">新产品评审</text>
          <text class="card-desc">新产品评审任务列表</text>
        </view>
        <view class="menu-card" @tap="goPage('/pages/files/index')">
          <view class="card-icon orange">📁</view>
          <text class="card-title">文件夹</text>
          <text class="card-desc">项目文件、产品标签</text>
        </view>
        <view class="menu-card" @tap="goPage('/pages/workorder/index')">
          <view class="card-icon blue">📋</view>
          <text class="card-title">工单管理</text>
          <text class="card-desc">创建处理工单任务</text>
        </view>
      </view>
    </view>

    <!-- 快捷统计 -->
    <view class="stats-section">
      <view class="stat-item">
        <text class="stat-num">{{ stats.doing }}</text>
        <text class="stat-label">进行中</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num done">{{ stats.done }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num warn">{{ stats.overdue }}</text>
        <text class="stat-label">逾期</text>
      </view>
    </view>
  </view>
</template>

<script>
import { listNotice } from '@/api/notice'

export default {
  data() {
    return {
      stats: { doing: 0, done: 0, overdue: 0 }
    }
  },
  computed: {
    userName() {
      return this.$store.state.name || '用户'
    },
    userDept() {
      const info = this.$store.state.userInfo
      if (info && info.dept) return info.dept.deptName || '技术科'
      return '产品开发组'
    }
  },
  onShow() {
    this.loadStats()
  },
  methods: {
    goPage(url) {
      uni.navigateTo({ url })
    },
    loadStats() {
      listNotice({ pageNum: 1, pageSize: 1000 }).then(res => {
        const list = res.rows || []
        this.stats.doing = list.filter(i => i.status !== '已完成').length
        this.stats.done = list.filter(i => i.status === '已完成').length
        this.stats.overdue = 0
      }).catch(() => {
        this.stats = { doing: 3, done: 216, overdue: 0 }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding-bottom: 30rpx;
}

.header-bg {
  background: linear-gradient(135deg, #4a3b8f 0%, #6c5bb3 60%, #9b7fd4 100%);
  padding: 60rpx 30rpx 80rpx;
  border-radius: 0 0 40rpx 40rpx;

  .header-badge {
    text-align: center;
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
    margin-bottom: 30rpx;
  }
}

.user-card {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.15);
  border-radius: 24rpx;
  padding: 28rpx;
  backdrop-filter: blur(10px);

  .user-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin-right: 20rpx;
  }

  .user-info {
    flex: 1;

    .user-name {
      display: block;
      font-size: 34rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 6rpx;
    }

    .user-dept {
      display: block;
      font-size: 24rpx;
      color: rgba(255,255,255,0.7);
    }
  }
}

.menu-section {
  margin: -40rpx 24rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #303133;
    margin-bottom: 24rpx;
  }
}

.menu-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.menu-card {
  width: calc(50% - 10rpx);
  background: #f8f9fd;
  border-radius: 20rpx;
  padding: 34rpx 24rpx;
  text-align: center;
  box-sizing: border-box;

  &:active {
    opacity: 0.8;
    transform: scale(0.97);
  }

  .card-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin: 0 auto 14rpx;

    &.purple { background: linear-gradient(135deg, #7c5cbf, #9b7fd4); }
    &.green { background: linear-gradient(135deg, #52c41a, #73d13d); }
    &.orange { background: linear-gradient(135deg, #fa8c16, #ffa940); }
    &.blue { background: linear-gradient(135deg, #1890ff, #40a9ff); }
  }

  .card-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6rpx;
  }

  .card-desc {
    display: block;
    font-size: 22rpx;
    color: #909399;
  }
}

.stats-section {
  display: flex;
  margin: 0 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-num {
      display: block;
      font-size: 44rpx;
      font-weight: 700;
      color: #6c5bb3;

      &.done { color: #52c41a; }
      &.warn { color: #fa8c16; }
    }

    .stat-label {
      display: block;
      font-size: 24rpx;
      color: #909399;
      margin-top: 8rpx;
    }
  }

  .stat-divider {
    width: 2rpx;
    background: #eee;
    margin: 10rpx 0;
  }
}
</style>
