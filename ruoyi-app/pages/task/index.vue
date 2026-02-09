<template>
  <view class="task-page">
    <!-- 顶部 -->
    <view class="header-bg">
      <view class="header-badge">试制任务</view>
      <view class="user-card">
        <view class="user-avatar">👤</view>
        <view class="user-info">
          <text class="user-name">{{ userName }}</text>
          <text class="user-roles">🔒 {{ userRoles }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <input
        v-model="searchText"
        placeholder="搜索任务、工序或日期..."
        class="search-input"
        confirm-type="search"
        @confirm="doSearch"
      />
    </view>

    <!-- 任务列表卡片 -->
    <view class="task-card">
      <view class="task-header">
        <text class="task-title">任务列表</text>
        <view class="tab-group">
          <view class="tab-item" :class="{ active: tab === 'doing' }" @tap="switchTab('doing')">
            进行中 {{ doingCount }}
          </view>
          <view class="tab-item" :class="{ active: tab === 'done' }" @tap="switchTab('done')">
            已完成 {{ doneCount }}
          </view>
        </view>
      </view>

      <view class="filter-info">
        <text class="filter-label">{{ tab === 'doing' ? '显示进行中任务' : '显示已完成任务' }}</text>
        <text class="filter-count">{{ filteredList.length }} 个任务</text>
      </view>

      <!-- 列表 -->
      <scroll-view scroll-y class="task-list" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
        <view
          v-for="item in filteredList"
          :key="item.noticeId"
          class="task-item"
          @tap="openDetail(item)"
        >
          <view class="item-icon">📋</view>
          <view class="item-info">
            <text class="item-name">{{ item.wheelCode || item.noticeCode }}-{{ item.craftProcess || '低压铸造' }}+{{ item.surfaceStatus || '全涂' }}</text>
            <text class="item-meta" v-if="item.trialStart">📅 {{ formatDate(item.trialStart) }}</text>
          </view>
          <view class="item-priority" :class="item.urgency === '紧急' ? 'danger' : item.urgency === '优先' ? 'warning' : 'normal'">
            {{ item.urgency || '正常' }}
          </view>
        </view>

        <view v-if="filteredList.length === 0 && !loading" class="empty">
          <text>暂无{{ tab === 'doing' ? '进行中' : '已完成' }}的任务</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部 -->
    <view class="footer-text">© 2026 任务状态管理系统 | 版本 3.0</view>
  </view>
</template>

<script>
import { listNotice } from '@/api/notice'

export default {
  data() {
    return {
      loading: false,
      refreshing: false,
      searchText: '',
      tab: 'doing',
      taskList: [],
      doingCount: 0,
      doneCount: 0
    }
  },
  computed: {
    userName() { return this.$store.state.name || '用户' },
    userRoles() { return '产品开发组-项目管理权限、产品开发组-试制管理权限' },
    filteredList() {
      let list = this.taskList.filter(i => {
        return this.tab === 'doing' ? i.status !== '已完成' : i.status === '已完成'
      })
      if (this.searchText) {
        const kw = this.searchText.toLowerCase()
        list = list.filter(i =>
          (i.wheelCode && i.wheelCode.toLowerCase().includes(kw)) ||
          (i.noticeCode && i.noticeCode.toLowerCase().includes(kw)) ||
          (i.responsible && i.responsible.toLowerCase().includes(kw))
        )
      }
      return list
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    formatDate(d) {
      if (!d) return ''
      return String(d).slice(0, 10)
    },
    loadData() {
      this.loading = true
      listNotice({ pageNum: 1, pageSize: 500 }).then(res => {
        this.taskList = res.rows || []
        this.countTasks()
      }).catch(() => {
        this.taskList = []
        this.countTasks()
      }).finally(() => {
        this.loading = false
        this.refreshing = false
      })
    },
    countTasks() {
      this.doingCount = this.taskList.filter(i => i.status !== '已完成').length
      this.doneCount = this.taskList.filter(i => i.status === '已完成').length
    },
    switchTab(t) { this.tab = t },
    doSearch() { /* computed自动过滤 */ },
    onRefresh() {
      this.refreshing = true
      this.loadData()
    },
    openDetail(item) {
      uni.navigateTo({ url: '/pages/task/detail?id=' + item.noticeId })
    }
  }
}
</script>

<style lang="scss" scoped>
.task-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4a3b8f 0%, #6c5bb3 35%, #f0f2f5 35%);
  padding-bottom: 30rpx;
}

.header-bg {
  padding: 30rpx 30rpx 20rpx;

  .header-badge {
    text-align: center;
    font-size: 28rpx;
    color: rgba(255,255,255,0.8);
    margin-bottom: 20rpx;
  }
}

.user-card {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.15);
  border-radius: 24rpx;
  padding: 24rpx;

  .user-avatar {
    width: 76rpx;
    height: 76rpx;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 38rpx;
    margin-right: 18rpx;
  }

  .user-name {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }

  .user-roles {
    display: block;
    font-size: 22rpx;
    color: rgba(255,255,255,0.7);
    margin-top: 4rpx;
  }
}

.search-bar {
  margin: 20rpx 30rpx;

  .search-input {
    background: #fff;
    border-radius: 50rpx;
    padding: 0 30rpx;
    height: 80rpx;
    font-size: 28rpx;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  }
}

.task-card {
  margin: 0 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;

  .task-title {
    font-size: 34rpx;
    font-weight: 700;
    color: #303133;
  }
}

.tab-group {
  display: flex;
  gap: 14rpx;

  .tab-item {
    padding: 10rpx 24rpx;
    border-radius: 50rpx;
    font-size: 24rpx;
    background: #f0f2f5;
    color: #606266;

    &.active {
      background: #6c5bb3;
      color: #fff;
    }
  }
}

.filter-info {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 10rpx;

  .filter-label { font-size: 24rpx; color: #409EFF; }
  .filter-count { font-size: 24rpx; color: #909399; }
}

.task-list {
  max-height: 800rpx;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }

  .item-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 14rpx;
    background: #e8f0fe;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    margin-right: 18rpx;
    flex-shrink: 0;
  }

  .item-info {
    flex: 1;
    min-width: 0;

    .item-name {
      display: block;
      font-size: 28rpx;
      font-weight: 500;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-meta {
      display: block;
      font-size: 22rpx;
      color: #909399;
      margin-top: 6rpx;
    }
  }

  .item-priority {
    padding: 6rpx 16rpx;
    border-radius: 8rpx;
    font-size: 22rpx;
    font-weight: 500;
    flex-shrink: 0;
    margin-left: 12rpx;

    &.normal { color: #67c23a; background: #f0f9eb; }
    &.warning { color: #e6a23c; background: #fdf6ec; }
    &.danger { color: #f56c6c; background: #fef0f0; }
  }
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #c0c4cc;
  font-size: 28rpx;
}

.footer-text {
  text-align: center;
  padding: 30rpx;
  font-size: 22rpx;
  color: #c0c4cc;
}
</style>
