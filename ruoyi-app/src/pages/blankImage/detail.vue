<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">&#x2039;</text>
      </view>
      <text class="nav-title">毛胚图详情</text>
      <view style="width: 60rpx"></view>
    </view>

    <scroll-view scroll-y class="page-body">
      <!-- 毛胚图图片 -->
      <view class="image-section" v-if="localImageUrl && !imgErr">
        <image
          :src="localImageUrl"
          mode="aspectFit"
          class="main-image"
          @tap="previewImage"
          @error="imgErr = true"
        ></image>
      </view>
      <view class="image-section no-img" v-else-if="detail.blankImage && !localImageUrl">
        <text class="no-img-text">图片加载中...</text>
      </view>
      <view class="image-section no-img" v-else>
        <text class="no-img-text">暂无毛胚图片</text>
      </view>

      <!-- 顶部概览 -->
      <view class="detail-header">
        <text class="dh-code">{{ detail.modelCode || '-' }}</text>
        <view class="dh-tags">
          <view v-if="detail.version" class="dtag dtag-purple">
            <text>版本 {{ detail.version }}</text>
          </view>
          <view v-if="detail.moldNo" class="dtag dtag-default">
            <text>模号 {{ detail.moldNo }}</text>
          </view>
        </view>
      </view>

      <!-- 基础信息 -->
      <view class="detail-section">
        <view class="section-title-bar"><text class="section-title">基础信息</text></view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">模号</text>
            <text class="desc-value">{{ detail.moldNo || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">型号</text>
            <text class="desc-value">{{ detail.modelCode || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">版本</text>
            <text class="desc-value">{{ detail.version || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">下发时间</text>
            <text class="desc-value">{{ detail.releaseDate || '-' }}</text>
          </view>
        </view>
      </view>

      <view style="height: 180rpx"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="detail.blankId">
      <view class="action-btn action-edit" @tap="openEditForm">
        <text class="action-btn-icon">✏️</text>
        <text class="action-btn-text">编辑</text>
      </view>
      <view class="action-btn action-delete" @tap="handleDelete">
        <text class="action-btn-icon">🗑️</text>
        <text class="action-btn-text">删除</text>
      </view>
    </view>

    <!-- 编辑表单弹层 -->
    <view class="form-overlay" v-if="formVisible">
      <view class="form-wrap">
        <view class="form-header">
          <view class="form-header-back" @tap="closeForm">
            <text class="form-header-back-icon">&#x2039;</text>
          </view>
          <text class="form-header-title">编辑毛胚图</text>
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
                <text class="form-image-text">点击更换图片</text>
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
  </view>
</template>

<script>
import { getBlankImage, buildImageUrl, resolveImageUrl, updateBlankImage, delBlankImage, uploadFile } from '@/api/blankImage'

export default {
  data() {
    return {
      blankId: null,
      detail: {},
      imgErr: false,
      localImageUrl: '',
      // 编辑表单
      formVisible: false,
      formLoading: false,
      imageUploading: false,
      formData: {},
      formVersionIndex: 0,
      versionOptions: ['A', 'B', 'C']
    }
  },
  computed: {
    navBarStyle() {
      var sys = uni.getSystemInfoSync()
      return 'padding-top: ' + (sys.statusBarHeight || 20) + 'px'
    }
  },
  onLoad(options) {
    if (options.id) {
      this.blankId = options.id
      this.loadDetail()
    }
  },
  onBackPress() {
    if (this.formVisible) {
      this.closeForm()
      return true
    }
    return false
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },

    loadDetail() {
      uni.showLoading({ title: '加载中' })
      getBlankImage(this.blankId).then(function (res) {
        this.detail = res.data || {}
        this.imgErr = false
        this.localImageUrl = ''
        if (this.detail.blankImage) {
          resolveImageUrl(this.detail.blankImage).then(function (localPath) {
            this.localImageUrl = localPath
          }.bind(this))
        }
      }.bind(this)).catch(function () {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }).finally(function () {
        uni.hideLoading()
      })
    },

    previewImage() {
      if (this.localImageUrl) {
        uni.previewImage({
          urls: [this.localImageUrl]
        })
      }
    },

    // ——— 编辑 ———
    openEditForm() {
      var d = this.detail
      this.formData = {
        blankId: d.blankId,
        modelCode: d.modelCode || '',
        moldNo: d.moldNo || '',
        version: d.version || 'A',
        releaseDate: d.releaseDate || '',
        blankImage: d.blankImage || null,
        _localPreview: this.localImageUrl || ''
      }
      var idx = this.versionOptions.indexOf(d.version)
      this.formVersionIndex = idx >= 0 ? idx : 0
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
            self.formData._localPreview = self.localImageUrl || ''
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

      updateBlankImage(submitData).then(function () {
        uni.showToast({ title: '保存成功', icon: 'success' })
        this.formVisible = false
        this.loadDetail()
      }.bind(this)).catch(function () {
      }).finally(function () {
        this.formLoading = false
      }.bind(this))
    },

    // ——— 删除 ———
    handleDelete() {
      var self = this
      uni.showModal({
        title: '确认删除',
        content: '确认删除毛胚图 "' + (this.detail.modelCode || '') + '"？此操作不可恢复',
        confirmColor: '#f56c6c',
        success: function (res) {
          if (res.confirm) {
            uni.showLoading({ title: '删除中' })
            delBlankImage(self.detail.blankId).then(function () {
              uni.hideLoading()
              uni.showToast({ title: '删除成功', icon: 'success' })
              setTimeout(function () {
                uni.navigateBack()
              }, 800)
            }).catch(function () {
              uni.hideLoading()
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
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
  flex-shrink: 0;
}
.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .nav-back-icon {
    font-size: 56rpx;
    color: #fff;
    font-weight: 300;
  }
}
.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

.page-body {
  flex: 1;
  min-height: 0;
  padding: 20rpx;
}

.image-section {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  .main-image {
    width: 100%;
    height: 500rpx;
  }
  &.no-img {
    height: 200rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    .no-img-text {
      font-size: 28rpx;
      color: #c0c4cc;
    }
  }
}

.detail-header {
  text-align: center;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.dh-code {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16rpx;
}
.dh-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12rpx;
}
.dtag {
  padding: 6rpx 18rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 500;
  &.dtag-purple { background: #f0ecff; color: #6c5bb3; }
  &.dtag-default { background: #f4f4f5; color: #909399; }
}

.detail-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.section-title-bar {
  margin-bottom: 16rpx;
  padding-left: 14rpx;
  border-left: 6rpx solid #6c5bb3;
}
.section-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #303133;
}
.desc-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.desc-item {
  width: calc(50% - 6rpx);
  .desc-label {
    display: block;
    font-size: 22rpx;
    color: #909399;
    margin-bottom: 4rpx;
  }
  .desc-value {
    display: block;
    font-size: 26rpx;
    color: #303133;
    word-break: break-all;
  }
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.08);
  gap: 20rpx;
  z-index: 100;
}
.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  &:active { opacity: 0.8; }
}
.action-edit {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
}
.action-delete {
  background: linear-gradient(135deg, #e65c5c, #f56c6c);
}
.action-btn-icon {
  font-size: 32rpx;
}
.action-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}

/* 编辑表单弹层 */
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
  .form-picker-arrow { font-size: 24rpx; color: #c0c4cc; }
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
.form-preview-img { width: 100%; height: 100%; }
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
</style>
