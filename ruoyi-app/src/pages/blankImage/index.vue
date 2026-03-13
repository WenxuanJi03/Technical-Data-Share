<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">&#x2039;</text>
      </view>
      <text class="nav-title">毛胚图</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="search-bar">
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索型号..."
        placeholder-class="search-placeholder"
        @input="onSearch"
        confirm-type="search"
      />
    </view>

    <view class="filter-bar">
      <view class="filter-chips">
        <picker mode="selector" :range="versionOptions" :value="versionIndex" @change="onVersionChange">
          <view class="filter-chip" :class="{ active: queryParams.version }">
            <text>{{ queryParams.version || '版本' }}</text>
            <text class="chip-arrow">&#x25BE;</text>
          </view>
        </picker>
        <view class="filter-chip reset" v-if="hasFilters" @tap="resetFilters">
          <text>重置</text>
        </view>
      </view>
      <view class="filter-add-btn text-btn" @tap="openAddForm">
        <text class="filter-add-text">新增</text>
      </view>
    </view>

    <view class="stats-bar" v-if="total > 0">
      <text class="stats-text">共 <text class="stats-num">{{ total }}</text> 条毛胚图</text>
    </view>

    <scroll-view
      scroll-y
      class="card-scroll"
      @scrolltolower="loadMore"
      refresher-enabled
      @refresherrefresh="onRefresh"
      :refresher-triggered="refreshing"
    >
      <view v-if="dataList.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无毛胚图数据</text>
      </view>

      <view class="waterfall-grid" v-if="dataList.length > 0">
        <view
          v-for="item in dataList"
          :key="item.blankId"
          class="product-card"
          @tap="showDetail(item)"
        >
          <view class="card-image">
            <image
              v-if="item._localImg && !item._imgErr"
              :src="item._localImg"
              mode="aspectFill"
              class="wheel-img"
              lazy-load
              @error="onImageError(item)"
            ></image>
            <view v-else-if="item.blankImage && !item._imgErr" class="no-image">
              <text class="no-image-text">加载中...</text>
            </view>
            <view v-else class="no-image">
              <text class="no-image-text">暂无图片</text>
            </view>
            <view class="version-badge" v-if="item.version">
              <text class="badge-text">{{ item.version }}</text>
            </view>
          </view>
          <view class="card-body">
            <text class="wheel-code">{{ item.modelCode }}</text>
            <view class="info-row" v-if="item.releaseDate">
              <text class="info-label">下发</text>
              <text class="info-value">{{ item.releaseDate }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="list-bottom" v-if="dataList.length > 0">
        <text class="list-bottom-text" v-if="noMore">—— 没有更多了 ——</text>
        <text class="list-bottom-text" v-else-if="loading">加载中...</text>
      </view>
    </scroll-view>

    <!-- 新增表单弹层 -->
    <page-container :show="formVisible" @clickoverlay="closeForm" @afterleave="closeForm" position="right" :overlay="false" custom-style="z-index: 1000; width: 100%; height: 100%;">
      <view class="form-overlay" v-if="formVisible">
        <view class="form-wrap">
          <view class="form-header">
            <view class="form-header-back" @tap="closeForm">
              <text class="form-header-back-icon">&#x2039;</text>
            </view>
            <text class="form-header-title">新增毛胚图</text>
            <view style="width: 60rpx;"></view>
          </view>
          <scroll-view scroll-y class="form-body">
            <!-- 图片 -->
            <view class="form-section">
              <view class="form-section-title">毛胚图</view>
              <view class="form-image-row" @tap="handleImageChoose">
                <image v-if="formData._localPreview" :src="formData._localPreview" class="form-preview-img" mode="aspectFill"></image>
                <view v-else class="form-image-placeholder">
                  <text class="form-image-icon">📷</text>
                  <text class="form-image-text">点击上传图片</text>
                </view>
                <view v-if="imageUploading" class="form-image-uploading">
                  <text class="form-image-uploading-text">上传中...</text>
                </view>
              </view>
            </view>

            <!-- 基础信息 -->
            <view class="form-section">
              <view class="form-section-title">基础信息</view>
              <view class="form-item">
                <text class="form-label"><text class="required">*</text>型号</text>
                <input class="form-input" v-model="formData.modelCode" placeholder="如 00919F03" />
              </view>
              <view class="form-item">
                <text class="form-label">模号</text>
                <input class="form-input" v-model="formData.moldNo" placeholder="如 009" />
              </view>
              <view class="form-item form-item-picker">
                <text class="form-label">版本</text>
                <picker mode="selector" :range="versionOptions" :value="formVersionIndex" @change="onFormVersionChange">
                  <view class="form-picker-value">
                    <text>{{ formData.version || '请选择' }}</text>
                    <text class="form-picker-arrow">&#x25BE;</text>
                  </view>
                </picker>
              </view>
              <view class="form-item">
                <text class="form-label">下发时间</text>
                <input class="form-input" v-model="formData.releaseDate" placeholder="如 2025/9/20" />
              </view>
            </view>

            <view style="height: 60rpx;"></view>
          </scroll-view>

          <!-- 表单底部按钮栏 -->
          <view class="form-footer">
            <view class="form-footer-btn form-footer-cancel" @tap="closeForm">
              <text class="form-footer-btn-text">取消</text>
            </view>
            <view class="form-footer-btn form-footer-save" @tap="submitForm" :class="{ disabled: formLoading }">
              <text class="form-footer-btn-text">{{ formLoading ? '保存中...' : '保存' }}</text>
            </view>
          </view>
        </view>
      </view>
    </page-container>
  </view>
</template>

<script>
import { listBlankImage, buildImageUrl, resolveImageUrl, addBlankImage, uploadFile } from '@/api/blankImage'

export default {
  data() {
    return {
      loading: false,
      refreshing: false,
      noMore: false,
      total: 0,
      dataList: [],
      searchKeyword: '',
      searchTimer: null,
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        modelCode: null,
        version: null
      },
      versionOptions: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
      versionIndex: -1,
      // 新增表单
      formVisible: false,
      formLoading: false,
      imageUploading: false,
      formVersionIndex: 0,
      formData: {}
    }
  },
  computed: {
    navBarStyle() {
      var sys = uni.getSystemInfoSync()
      return 'padding-top: ' + (sys.statusBarHeight || 20) + 'px'
    },
    hasFilters() {
      return this.queryParams.version || this.searchKeyword
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },

    loadData() {
      this.loading = true
      var params = {}
      params.pageNum = this.queryParams.pageNum
      params.pageSize = this.queryParams.pageSize
      if (this.searchKeyword) {
        params.modelCode = this.searchKeyword
      } else if (this.queryParams.modelCode) {
        params.modelCode = this.queryParams.modelCode
      }
      if (this.queryParams.version) params.version = this.queryParams.version

      listBlankImage(params).then(function (res) {
        var rows = res.rows || []
        if (this.queryParams.pageNum === 1) {
          this.dataList = rows
        } else {
          this.dataList = this.dataList.concat(rows)
        }
        this.total = res.total || 0
        this.noMore = this.dataList.length >= this.total
        this.resolveImages(rows)
      }.bind(this)).catch(function () {
        uni.showToast({ title: '加载失败', icon: 'none' })
        if (this.queryParams.pageNum === 1) {
          this.dataList = []
        }
      }.bind(this)).finally(function () {
        this.loading = false
        this.refreshing = false
      }.bind(this))
    },

    loadMore() {
      if (this.loading || this.noMore) return
      this.queryParams.pageNum++
      this.loadData()
    },

    onRefresh() {
      this.refreshing = true
      this.queryParams.pageNum = 1
      this.dataList = []
      this.loadData()
    },

    onSearch() {
      var self = this
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(function () {
        self.queryParams.pageNum = 1
        self.dataList = []
        self.loadData()
      }, 400)
    },

    reloadList() {
      this.queryParams.pageNum = 1
      this.dataList = []
      this.loadData()
    },

    onVersionChange(e) {
      this.versionIndex = e.detail.value
      this.queryParams.version = this.versionOptions[this.versionIndex]
      this.reloadList()
    },

    resetFilters() {
      this.searchKeyword = ''
      this.versionIndex = -1
      this.queryParams.version = null
      this.queryParams.modelCode = null
      this.reloadList()
    },

    resolveImages(rows) {
      var self = this
      rows.forEach(function (item) {
        if (!item.blankImage) return
        resolveImageUrl(item.blankImage).then(function (localPath) {
          var target = self.dataList.find(function (p) { return p.blankId === item.blankId })
          if (target) {
            self.$set(target, '_localImg', localPath)
          }
        })
      })
    },

    onImageError(item) {
      this.$set(item, '_imgErr', true)
    },

    showDetail(item) {
      uni.navigateTo({
        url: '/pages/blankImage/detail?id=' + item.blankId
      })
    },

    // ——— 新增表单 ———
    openAddForm() {
      this.formData = {
        blankId: null,
        modelCode: '',
        moldNo: '',
        version: 'A',
        releaseDate: '',
        blankImage: null,
        _localPreview: ''
      }
      this.formVersionIndex = 0
      this.formVisible = true
    },

    closeForm() {
      this.formVisible = false
    },

    onFormVersionChange(e) {
      this.formVersionIndex = e.detail.value
      this.formData.version = this.versionOptions[this.formVersionIndex]
    },

    handleImageChoose() {
      if (this.imageUploading) return
      var self = this
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: function (res) {
          var filePath = res.tempFilePaths[0]
          self.formData._localPreview = filePath
          self.imageUploading = true
          uploadFile(filePath).then(function (data) {
            self.formData.blankImage = data.fileName
          }).catch(function () {
            uni.showToast({ title: '图片上传失败', icon: 'none' })
            self.formData._localPreview = ''
          }).finally(function () {
            self.imageUploading = false
          })
        }
      })
    },

    submitForm() {
      if (this.formLoading || this.imageUploading) return
      if (!this.formData.modelCode || !this.formData.modelCode.trim()) {
        uni.showToast({ title: '请填写型号', icon: 'none' })
        return
      }
      this.formLoading = true
      var submitData = Object.assign({}, this.formData)
      delete submitData._localPreview

      addBlankImage(submitData).then(function () {
        uni.showToast({ title: '新增成功', icon: 'success' })
        this.formVisible = false
        this.reloadList()
      }.bind(this)).catch(function () {
      }).finally(function () {
        this.formLoading = false
      }.bind(this))
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  background: linear-gradient(180deg, #4a3b8f 0%, #6c5bb3 220rpx, #f0f2f5 220rpx);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 30rpx;
  flex-shrink: 0;
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
    font-weight: 700;
    color: #fff;
  }
  .nav-placeholder {
    width: 60rpx;
    height: 60rpx;
  }
}

.search-bar {
  margin: 16rpx 24rpx 12rpx;
  flex-shrink: 0;
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

.filter-bar {
  display: flex;
  align-items: flex-start;
  padding: 0 24rpx 12rpx;
  gap: 12rpx;
  flex-shrink: 0;
}
.filter-chips {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  min-width: 0;
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
  .chip-arrow {
    font-size: 20rpx;
  }
}
.filter-add-btn {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(108, 91, 179, 0.4);
  &:active { opacity: 0.8; }
}
.filter-add-btn.text-btn {
  width: 120rpx;
  border-radius: 36rpx;
}
.filter-add-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 500;
}

.stats-bar {
  padding: 0 30rpx 10rpx;
  flex-shrink: 0;
  .stats-text {
    font-size: 24rpx;
    color: #909399;
  }
  .stats-num {
    color: #6c5bb3;
    font-weight: 700;
  }
}

.card-scroll {
  flex: 1;
  min-height: 0;
  padding: 0 12rpx;
  box-sizing: border-box;
}

.waterfall-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding-bottom: 20rpx;
}

.product-card {
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-image {
  width: 100%;
  height: 260rpx;
  background: #f5f7fa;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  .wheel-img {
    width: 100%;
    height: 100%;
  }
  .no-image {
    display: flex;
    align-items: center;
    justify-content: center;
    .no-image-text {
      font-size: 24rpx;
      color: #c0c4cc;
    }
  }
  .version-badge {
    position: absolute;
    top: 10rpx;
    right: 10rpx;
    padding: 2rpx 16rpx;
    border-radius: 6rpx;
    background: rgba(108, 91, 179, 0.85);
    .badge-text {
      font-size: 22rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

.card-body {
  padding: 14rpx;
  .wheel-code {
    font-size: 26rpx;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8rpx;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.info-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 4rpx;
  flex-wrap: nowrap;
  .info-label {
    font-size: 22rpx;
    color: #909399;
    margin-right: 4rpx;
    flex-shrink: 0;
  }
  .info-value {
    font-size: 22rpx;
    color: #606266;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.empty-state {
  text-align: center;
  padding: 120rpx 0;
  .empty-text {
    font-size: 28rpx;
    color: #c0c4cc;
  }
}
.list-bottom {
  text-align: center;
  padding: 20rpx 0 40rpx;
  .list-bottom-text {
    font-size: 24rpx;
    color: #c0c4cc;
  }
}

/* 新增表单 */
.form-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: #f0f2f5;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}
.form-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 14rpx 20rpx;
  padding-top: calc(14rpx + env(safe-area-inset-top));
}
.form-header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .form-header-back-icon { font-size: 56rpx; color: #fff; font-weight: 300; }
}
.form-header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}
.form-footer {
  display: flex;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  gap: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.06);
}
.form-footer-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &.disabled { opacity: 0.5; }
  &:active { opacity: 0.85; }
}
.form-footer-cancel { background: #f5f7fa; }
.form-footer-save { background: linear-gradient(135deg, #4a3b8f, #6c5bb3); }
.form-footer-btn-text { font-size: 30rpx; font-weight: 600; }
.form-footer-cancel .form-footer-btn-text { color: #606266; }
.form-footer-save .form-footer-btn-text { color: #fff; }
.form-body {
  flex: 1;
  min-height: 0;
  padding: 20rpx;
}
.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.form-section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16rpx;
  padding-left: 14rpx;
  border-left: 6rpx solid #6c5bb3;
}
.form-item {
  margin-bottom: 20rpx;
  .form-label {
    font-size: 26rpx;
    color: #606266;
    margin-bottom: 8rpx;
    display: block;
    .required { color: #f56c6c; margin-right: 4rpx; }
  }
  .form-input {
    background: #f5f7fa;
    border-radius: 12rpx;
    padding: 0 20rpx;
    height: 72rpx;
    font-size: 28rpx;
  }
}
.form-item-picker {
  .form-picker-value {
    background: #f5f7fa;
    border-radius: 12rpx;
    padding: 0 20rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 28rpx;
    color: #303133;
  }
  .form-picker-arrow {
    font-size: 24rpx;
    color: #c0c4cc;
  }
}
.form-image-row {
  height: 300rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.form-preview-img {
  width: 100%;
  height: 100%;
}
.form-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  .form-image-icon { font-size: 60rpx; }
  .form-image-text { font-size: 24rpx; color: #c0c4cc; margin-top: 10rpx; }
}
.form-image-uploading {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  .form-image-uploading-text { color: #fff; font-size: 28rpx; }
}
</style>
