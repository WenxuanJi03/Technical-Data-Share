<template>
  <view class="page-container">
    <!-- 顶部 -->
    <view class="header-bg">
      <view class="header-inner">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon">&#8592;</text>
        </view>
        <text class="page-title">技术文档</text>
        <view class="header-right">
          <text class="doc-count">{{ total }} 份文档</text>
        </view>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-bar">
        <input
          v-model="searchText"
          placeholder="搜索文档名称..."
          class="search-input"
          confirm-type="search"
          @confirm="doSearch"
        />
        <view class="search-btn" @tap="doSearch">搜索</view>
      </view>
    </view>

    <!-- 分类筛选 -->
    <view class="filter-section">
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-tags">
          <view
            class="filter-tag"
            :class="{ active: activeType === '' }"
            @tap="filterByType('')"
          >全部</view>
          <view
            class="filter-tag"
            :class="{ active: activeType === 'design' }"
            @tap="filterByType('design')"
          >设计</view>
          <view
            class="filter-tag"
            :class="{ active: activeType === 'process' }"
            @tap="filterByType('process')"
          >工艺</view>
          <view
            class="filter-tag"
            :class="{ active: activeType === 'quality' }"
            @tap="filterByType('quality')"
          >质量</view>
          <view
            class="filter-tag"
            :class="{ active: activeType === 'other' }"
            @tap="filterByType('other')"
          >其他</view>
        </view>
      </scroll-view>
    </view>

    <!-- 文档列表 -->
    <scroll-view
      scroll-y
      class="doc-list"
      refresher-enabled
      @refresherrefresh="onRefresh"
      :refresher-triggered="refreshing"
      @scrolltolower="loadMore"
    >
      <view v-for="doc in docList" :key="doc.docId" class="doc-card" @tap="showDocDetail(doc)">
        <view class="doc-icon" :style="{ background: getFileColor(doc.fileType) }">
          <text class="icon-text">{{ getFileExt(doc.fileType) }}</text>
        </view>
        <view class="doc-info">
          <text class="doc-name">{{ doc.docName }}</text>
          <view class="doc-meta">
            <text class="meta-tag" v-if="doc.projectName">{{ doc.projectName }}</text>
            <text class="meta-type">{{ getTypeName(doc.docType) }}</text>
            <text class="meta-size">{{ formatSize(doc.fileSize) }}</text>
          </view>
          <text class="doc-date">{{ formatDate(doc.createTime) }} | {{ doc.authorName || '未知' }}</text>
        </view>
        <view class="doc-actions">
          <view class="action-btn download" @tap.stop="downloadFile(doc)" v-if="doc.filePath">
            <text class="action-text">下载</text>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loading" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-if="!loading && noMore && docList.length > 0" class="loading-more">
        <text>没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && docList.length === 0" class="empty-state">
        <text class="empty-icon">&#128196;</text>
        <text class="empty-text">暂无技术文档</text>
        <text class="empty-sub">文档上传后将在此处显示</text>
      </view>
    </scroll-view>

    <!-- 文档详情弹窗 -->
    <view v-if="showDetail" class="modal-mask" @tap.self="showDetail = false">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">文档详情</text>
          <view class="modal-close" @tap="showDetail = false">X</view>
        </view>
        <view class="detail-body" v-if="currentDoc">
          <view class="detail-row">
            <text class="detail-label">文档名称</text>
            <text class="detail-value">{{ currentDoc.docName }}</text>
          </view>
          <view class="detail-row" v-if="currentDoc.docCode">
            <text class="detail-label">文档编号</text>
            <text class="detail-value">{{ currentDoc.docCode }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">文档类型</text>
            <text class="detail-value">{{ getTypeName(currentDoc.docType) }}</text>
          </view>
          <view class="detail-row" v-if="currentDoc.projectName">
            <text class="detail-label">关联项目</text>
            <text class="detail-value">{{ currentDoc.projectName }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">版本号</text>
            <text class="detail-value">{{ currentDoc.version || 'V1.0' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">文件格式</text>
            <text class="detail-value">{{ (currentDoc.fileType || '').toUpperCase() }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">文件大小</text>
            <text class="detail-value">{{ formatSize(currentDoc.fileSize) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">作者</text>
            <text class="detail-value">{{ currentDoc.authorName || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">创建时间</text>
            <text class="detail-value">{{ formatDate(currentDoc.createTime) }}</text>
          </view>
          <view class="detail-row" v-if="currentDoc.remark">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ currentDoc.remark }}</text>
          </view>
        </view>
        <view class="modal-footer" v-if="currentDoc && currentDoc.filePath">
          <button class="modal-btn preview" @tap="previewFile(currentDoc)">预览</button>
          <button class="modal-btn download" @tap="downloadFile(currentDoc)">下载文件</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listDocument } from '@/api/document'
import { getProductBaseUrl } from '@/api/product'

export default {
  data() {
    return {
      loading: false,
      refreshing: false,
      searchText: '',
      activeType: '',
      docList: [],
      total: 0,
      pageNum: 1,
      pageSize: 15,
      noMore: false,
      showDetail: false,
      currentDoc: null
    }
  },
  onShow() {
    this.resetAndLoad()
  },
  methods: {
    resetAndLoad() {
      this.pageNum = 1
      this.docList = []
      this.noMore = false
      this.loadData()
    },
    loadData() {
      if (this.loading) return
      this.loading = true
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
      }
      if (this.searchText) {
        params.docName = this.searchText
      }
      if (this.activeType) {
        params.docType = this.activeType
      }
      listDocument(params).then(res => {
        const rows = res.rows || []
        if (this.pageNum === 1) {
          this.docList = rows
        } else {
          this.docList = this.docList.concat(rows)
        }
        this.total = res.total || 0
        this.noMore = this.docList.length >= this.total
      }).catch(() => {
        if (this.pageNum === 1) {
          this.docList = []
          this.total = 0
        }
      }).finally(() => {
        this.loading = false
        this.refreshing = false
      })
    },
    loadMore() {
      if (this.noMore || this.loading) return
      this.pageNum++
      this.loadData()
    },
    onRefresh() {
      this.refreshing = true
      this.resetAndLoad()
    },
    doSearch() {
      this.resetAndLoad()
    },
    filterByType(type) {
      this.activeType = type
      this.resetAndLoad()
    },
    showDocDetail(doc) {
      this.currentDoc = doc
      this.showDetail = true
    },
    downloadFile(doc) {
      if (!doc.filePath) {
        uni.showToast({ title: '暂无文件', icon: 'none' })
        return
      }
      uni.showLoading({ title: '准备下载...' })
      const url = getProductBaseUrl() + '/common/download/resource?resource=' + encodeURIComponent(doc.filePath)
      // #ifdef MP-WEIXIN
      uni.downloadFile({
        url: url,
        header: {
          'Authorization': 'Bearer ' + uni.getStorageSync('token')
        },
        success: (res) => {
          if (res.statusCode === 200) {
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              success: () => {},
              fail: () => {
                uni.showToast({ title: '无法打开此文件类型', icon: 'none' })
              }
            })
          }
        },
        fail: () => {
          uni.showToast({ title: '下载失败', icon: 'none' })
        },
        complete: () => {
          uni.hideLoading()
        }
      })
      // #endif
      // #ifdef H5
      window.open(url)
      uni.hideLoading()
      // #endif
    },
    previewFile(doc) {
      this.downloadFile(doc)
    },
    getFileExt(type) {
      return (type || '?').toUpperCase().substring(0, 4)
    },
    getFileColor(type) {
      const t = (type || '').toLowerCase()
      const colors = {
        'xlsx': '#217346', 'xls': '#217346', 'et': '#217346',
        'doc': '#2B579A', 'docx': '#2B579A', 'wps': '#2B579A',
        'pdf': '#D32F2F',
        'dwg': '#E65100', 'dxf': '#E65100',
        'ppt': '#D04423', 'pptx': '#D04423', 'dps': '#D04423',
      }
      return colors[t] || '#909399'
    },
    getTypeName(type) {
      const map = { 'design': '设计', 'process': '工艺', 'quality': '质量', 'other': '其他' }
      return map[type] || '文档'
    },
    formatSize(size) {
      if (!size) return ''
      if (size < 1024) return size + 'B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
      return (size / (1024 * 1024)).toFixed(1) + 'MB'
    },
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.header-bg {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 0 0 20rpx;
}

.header-inner {
  display: flex;
  align-items: center;
  padding: 80rpx 30rpx 10rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  margin-right: 16rpx;
}

.back-icon {
  color: #fff;
  font-size: 32rpx;
}

.page-title {
  flex: 1;
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.header-right .doc-count {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  background: rgba(255,255,255,0.15);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.search-section {
  padding: 0 24rpx;
  margin-top: -10rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 0 10rpx 0 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}

.search-input {
  flex: 1;
  height: 76rpx;
  font-size: 28rpx;
}

.search-btn {
  padding: 14rpx 28rpx;
  background: #6c5bb3;
  color: #fff;
  border-radius: 40rpx;
  font-size: 26rpx;
}

.filter-section {
  padding: 20rpx 24rpx 0;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-tags {
  display: flex;
  gap: 16rpx;
}

.filter-tag {
  display: inline-block;
  padding: 10rpx 28rpx;
  background: #fff;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #606266;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);

  &.active {
    background: #6c5bb3;
    color: #fff;
  }
}

.doc-list {
  flex: 1;
  padding: 20rpx 24rpx;
  height: calc(100vh - 380rpx);
}

.doc-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);

  &:active {
    opacity: 0.85;
  }
}

.doc-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.icon-text {
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 8rpx;
  flex-wrap: wrap;
}

.meta-tag {
  font-size: 20rpx;
  color: #6c5bb3;
  background: #f0edf8;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-type {
  font-size: 20rpx;
  color: #909399;
}

.meta-size {
  font-size: 20rpx;
  color: #c0c4cc;
}

.doc-date {
  display: block;
  font-size: 20rpx;
  color: #c0c4cc;
  margin-top: 4rpx;
}

.doc-actions {
  flex-shrink: 0;
  margin-left: 12rpx;
}

.action-btn {
  padding: 10rpx 24rpx;
  border-radius: 10rpx;
  font-size: 24rpx;
  font-weight: 500;

  &.download {
    background: #6c5bb3;
    color: #fff;
  }

  .action-text {
    color: inherit;
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx;
  color: #c0c4cc;
  font-size: 24rpx;
}

.empty-state {
  text-align: center;
  padding: 120rpx 0;

  .empty-icon {
    display: block;
    font-size: 100rpx;
    margin-bottom: 20rpx;
  }

  .empty-text {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10rpx;
  }

  .empty-sub {
    display: block;
    font-size: 26rpx;
    color: #909399;
  }
}

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.modal-content {
  width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 30rpx;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #303133;
}

.modal-close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 28rpx;
  color: #909399;
}

.detail-body {
  padding: 20rpx 0;
  max-height: 50vh;
  overflow-y: auto;
}

.detail-row {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.detail-label {
  width: 160rpx;
  font-size: 26rpx;
  color: #909399;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 26rpx;
  color: #303133;
  word-break: break-all;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding-top: 20rpx;

  .modal-btn {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;

    &::after { border: none; }

    &.preview {
      background: #f0f2f5;
      color: #606266;
    }

    &.download {
      background: #6c5bb3;
      color: #fff;
    }
  }
}
</style>
