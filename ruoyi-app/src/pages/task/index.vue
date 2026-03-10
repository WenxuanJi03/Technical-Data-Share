<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">试制任务</text>
      <view class="nav-add"></view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索模号..."
        placeholder-class="search-placeholder"
        @input="onSearch"
        confirm-type="search"
      />
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <picker mode="selector" :range="moldTypeOptions" :value="moldTypeIndex" @change="onMoldTypeChange">
        <view class="filter-chip" :class="{ active: queryParams.moldType }">
          <text>{{ queryParams.moldType || '模具类型' }}</text>
          <text class="chip-arrow">▾</text>
        </view>
      </picker>
      <picker mode="selector" :range="surfaceOptions" :value="surfaceIndex" @change="onSurfaceChange">
        <view class="filter-chip" :class="{ active: queryParams.surfaceStatus }">
          <text>{{ queryParams.surfaceStatus || '表面状态' }}</text>
          <text class="chip-arrow">▾</text>
        </view>
      </picker>
      <picker mode="selector" :range="machineTypeOptions" :value="machineTypeIndex" @change="onMachineTypeChange">
        <view class="filter-chip" :class="{ active: queryParams.machineType }">
          <text>{{ queryParams.machineType || '上机类型' }}</text>
          <text class="chip-arrow">▾</text>
        </view>
      </picker>
      <picker mode="selector" :range="statusLabels" :value="statusIndex" @change="onStatusChange">
        <view class="filter-chip" :class="{ active: queryParams.allProcessDone }">
          <text>{{ statusDisplayText }}</text>
          <text class="chip-arrow">▾</text>
        </view>
      </picker>
      <view class="filter-chip reset" v-if="hasFilters" @tap="resetFilters">
        <text>重置</text>
      </view>
    </view>

    <!-- 统计栏 -->
    <view class="stats-bar" v-if="total > 0">
      <text class="stats-text">共 <text class="stats-num">{{ total }}</text> 条试制记录</text>
    </view>

    <!-- 卡片网格 -->
    <scroll-view scroll-y class="card-scroll" @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view v-if="trackList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无试制跟踪记录</text>
      </view>

      <view class="card-grid">
        <view
          v-for="item in trackList"
          :key="item.trackId"
          class="track-card"
          @tap="showDetail(item)"
        >
          <!-- 卡片顶部 -->
          <view class="card-top">
            <text class="mold-code">{{ item.moldCode }}</text>
            <view class="done-badge" :class="item.allProcessDone === '是' ? 'done-yes' : 'done-no'" @tap.stop="toggleStatus(item)">
              <text class="badge-text">{{ item.allProcessDone === '是' ? '✓ 已完成' : '● 进行中' }}</text>
            </view>
          </view>

          <!-- 标签 -->
          <view class="card-tags">
            <view v-if="item.moldType" class="tag tag-warning">
              <text class="tag-text">{{ item.moldType }}</text>
            </view>
            <view v-if="item.surfaceStatus" class="tag tag-success">
              <text class="tag-text">{{ item.surfaceStatus }}</text>
            </view>
            <view v-if="item.machineType" class="tag tag-info">
              <text class="tag-text">{{ item.machineType }}</text>
            </view>
          </view>

          <!-- 关键信息 -->
          <view class="card-meta">
            <view class="meta-row" v-if="item.productSpec">
              <text class="meta-l">轮型</text>
              <text class="meta-v">{{ item.productSpec }}</text>
            </view>
            <view class="meta-row" v-if="item.hotMachineDate || item.hotMachineStation">
              <text class="meta-l">压铸</text>
              <text class="meta-v">{{ item.hotMachineDate || '-' }} / {{ item.hotMachineStation || '-' }}</text>
            </view>
            <view class="meta-row" v-if="item.hotProduction">
              <text class="meta-l">生产</text>
              <text class="meta-v meta-ellipsis">{{ item.hotProduction }}</text>
            </view>
            <view class="meta-row" v-if="item.impactTestResult">
              <text class="meta-l">实验</text>
              <text class="meta-v">{{ item.impactTestResult }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="list-bottom" v-if="trackList.length > 0">
        <text class="list-bottom-text" v-if="noMore">—— 没有更多了 ——</text>
        <text class="list-bottom-text" v-else-if="loading">加载中...</text>
      </view>
    </scroll-view>


  </view>
</template>

<script>
import { listTrialTrack, getTrialTrack, updateTrialTrack, delTrialTrack } from '@/api/trialTrack'

export default {
  data() {
    return {
      loading: false,
      refreshing: false,
      noMore: false,
      total: 0,
      trackList: [],
      searchKeyword: '',
      searchTimer: null,
      // query params
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        moldCode: null,
        moldType: null,
        surfaceStatus: null,
        machineType: null,
        allProcessDone: null
      },
      // filter options
      moldTypeOptions: ['首模', '改模', '翻模'],
      moldTypeIndex: -1,
      surfaceOptions: ['精车', '全涂', '全涂装'],
      surfaceIndex: -1,
      machineTypeOptions: ['小批量', '量产'],
      machineTypeIndex: -1,
      statusLabels: ['进行中', '已完成'],
      statusValues: ['doing', 'done'],
      statusIndex: -1,
      statusIndex: -1
    }
  },
  computed: {
    navBarStyle() {
      const info = uni.getSystemInfoSync()
      const statusBarHeight = info.statusBarHeight || 20
      return `padding-top: ${statusBarHeight}px`
    },
    statusDisplayText() {
      if (!this.queryParams.allProcessDone) return '状态'
      return this.queryParams.allProcessDone === 'doing' ? '进行中' : '已完成'
    },
    hasFilters() {
      return this.queryParams.moldType || this.queryParams.surfaceStatus || this.queryParams.machineType || this.queryParams.allProcessDone || this.searchKeyword
    }
  },
  onLoad() {
    const now = new Date()
    this.currentDate = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0')
    uni.$on('refreshTaskList', this.reloadList)
    this.reloadList()
  },
  onUnload() {
    uni.$off('refreshTaskList', this.reloadList)
  },
  onShow() {
    // Only fetch if necessary
  },
  methods: {
    noop() {},
    goBack() {
      // It is a normal page now, we can use navigateBack
      uni.navigateBack()
    },
    // ===== Data =====
    loadData() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.searchKeyword) {
        params.moldCode = this.searchKeyword
      }
      // 清除 null/undefined/空字符串 的参数，防止后端查询异常
      Object.keys(params).forEach(k => {
        if (params[k] === null || params[k] === undefined || params[k] === '') {
          delete params[k]
        }
      })
      console.log('[试制任务] loadData params:', JSON.stringify(params))
      listTrialTrack(params).then(res => {
        console.log('[试制任务] loadData response:', JSON.stringify(res).substring(0, 500))
        const rows = res.rows || []
        if (this.queryParams.pageNum === 1) {
          this.trackList = rows
        } else {
          this.trackList = this.trackList.concat(rows)
        }
        this.total = res.total || 0
        this.noMore = this.trackList.length >= this.total
      }).catch((err) => {
        console.error('[试制任务] loadData error:', err)
        uni.showToast({ title: '加载失败: ' + (err.msg || '请检查网络'), icon: 'none', duration: 3000 })
        if (this.queryParams.pageNum === 1) this.trackList = []
      }).finally(() => {
        this.loading = false
        this.refreshing = false
      })
    },
    loadMore() {
      if (this.loading || this.noMore) return
      this.queryParams.pageNum++
      this.loadData()
    },
    onRefresh() {
      this.refreshing = true
      this.queryParams.pageNum = 1
      this.trackList = []
      this.loadData()
    },
    onSearch() {
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.queryParams.pageNum = 1
        this.trackList = []
        this.loadData()
      }, 400)
    },

    // ===== Filters =====
    onMoldTypeChange(e) {
      const idx = e.detail.value
      this.moldTypeIndex = idx
      this.queryParams.moldType = this.moldTypeOptions[idx]
      this.reloadList()
    },
    onSurfaceChange(e) {
      const idx = e.detail.value
      this.surfaceIndex = idx
      this.queryParams.surfaceStatus = this.surfaceOptions[idx]
      this.reloadList()
    },
    onMachineTypeChange(e) {
      const idx = e.detail.value
      this.machineTypeIndex = idx
      this.queryParams.machineType = this.machineTypeOptions[idx]
      this.reloadList()
    },
    onStatusChange(e) {
      const idx = e.detail.value
      this.statusIndex = idx
      this.queryParams.allProcessDone = this.statusValues[idx]
      this.reloadList()
    },
    resetFilters() {
      this.searchKeyword = ''
      this.moldTypeIndex = -1
      this.surfaceIndex = -1
      this.machineTypeIndex = -1
      this.statusIndex = -1
      this.queryParams.moldCode = null
      this.queryParams.moldType = null
      this.queryParams.surfaceStatus = null
      this.queryParams.machineType = null
      this.queryParams.allProcessDone = null
      this.reloadList()
    },
    reloadList() {
      this.queryParams.pageNum = 1
      this.trackList = []
      this.loadData()
    },
    
    toggleStatus(item) {
      const newStatus = item.allProcessDone === '是' ? '否' : '是'
      uni.showModal({
        title: '确认切换状态',
        content: `确认将该试制跟踪状态切换为"${newStatus === '是' ? '已完成' : '进行中'}"？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            updateTrialTrack({ trackId: item.trackId, allProcessDone: newStatus }).then(() => {
              uni.hideLoading()
              uni.showToast({ title: '状态流转成功', icon: 'success' })
              this.reloadList()
            }).catch(() => {
              uni.hideLoading()
            })
          }
        }
      })
    },

    // ===== Detail =====
    showDetail(item) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${item.trackId}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #4a3b8f 0%, #6c5bb3 220rpx, #f0f2f5 220rpx);
}

/* ===== 导航栏 ===== */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 30rpx;
  .nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
  .nav-back-icon { font-size: 52rpx; color: #fff; font-weight: 300; line-height: 1; }
  .nav-title {
    font-size: 34rpx;
    font-weight: 700;
    color: #fff;
  }
  .nav-add { width: 60rpx; height: 60rpx; }
}

/* ===== 搜索栏 ===== */
.search-bar {
  margin: 16rpx 24rpx 12rpx;
  .search-input {
    background: #e6f0ff;
    border: 2rpx solid #a8cfff;
    border-radius: 50rpx;
    padding: 0 30rpx;
    height: 72rpx;
    font-size: 28rpx;
    color: #1a73e8;
    box-shadow: 0 4rpx 16rpx rgba(26, 115, 232, 0.1);
  }
}

.search-placeholder {
  color: #8ab4f8;
}

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 0 24rpx 16rpx;
}
.filter-chip {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #e6f0ff;
  border: 2rpx solid #a8cfff;
  border-radius: 30rpx;
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  color: #1a73e8;
  &.active {
    background: #1a73e8;
    color: #fff;
    border-color: #1a73e8;
    font-weight: 600;
  }
  &.reset {
    background: #fef0f0;
    color: #f56c6c;
    border-color: #fbc4c4;
  }
  .chip-arrow { font-size: 20rpx; }
}

/* ===== 统计 ===== */
.stats-bar {
  padding: 0 30rpx 12rpx;
  .stats-text { font-size: 24rpx; color: #909399; }
  .stats-num { color: #6c5bb3; font-weight: 700; }
}

/* ===== 卡片滚动区 ===== */
.card-scroll {
  height: calc(100vh - 420rpx);
  padding: 0 10rpx;
  box-sizing: border-box; /* Fix for cards being cut off on the right */
}

/* ===== 卡片网格 ===== */
.card-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 16rpx 0;
}
.track-card {
  width: calc(50% - 6rpx);
  box-sizing: border-box;
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
  &:active { opacity: 0.85; transform: scale(0.98); }
}

/* 卡片顶部 */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10rpx;
  gap: 8rpx;
}
.mold-code {
  font-size: 30rpx;
  font-weight: 700;
  color: #303133;
  flex: 1;
  white-space: normal;
  overflow: visible;
  word-break: break-all;
}
.done-badge {
  flex-shrink: 0;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.1);
  transition: all 0.2s;
  &:active {
    transform: scale(0.95);
    opacity: 0.8;
  }
  .badge-text { font-size: 18rpx; font-weight: 500; }
  &.done-yes { background: #f0f9eb; border: 1rpx solid #e1f3d8; .badge-text { color: #67c23a; } }
  &.done-no { background: #ecf5ff; border: 1rpx solid #d9ecff; .badge-text { color: #409eff; } }
}

/* 标签 */
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  .tag-text { font-size: 20rpx; }
  &.tag-warning { background: #fdf6ec; .tag-text { color: #e6a23c; } }
  &.tag-success { background: #f0f9eb; .tag-text { color: #67c23a; } }
  &.tag-info { background: #ecf5ff; .tag-text { color: #409eff; } }
}

/* 卡片元数据 */
.card-meta {
  .meta-row {
    display: flex;
    margin-bottom: 6rpx;
  }
  .meta-l {
    font-size: 22rpx;
    color: #909399;
    width: 60rpx;
    flex-shrink: 0;
  }
  .meta-v {
    font-size: 22rpx;
    color: #606266;
    font-weight: 500;
  }
  .meta-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200rpx;
  }
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  .empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
  .empty-text { font-size: 28rpx; color: #c0c4cc; }
}
.list-bottom {
  text-align: center;
  padding: 30rpx 0;
  .list-bottom-text { font-size: 24rpx; color: #c0c4cc; }
}
</style>
