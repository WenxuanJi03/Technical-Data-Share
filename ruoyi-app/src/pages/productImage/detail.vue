<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">&#x2039;</text>
      </view>
      <text class="nav-title">产品详情</text>
      <view style="width: 60rpx"></view>
    </view>

    <scroll-view scroll-y class="page-body">
      <!-- 产品图片 -->
      <view class="image-section" v-if="localImageUrl && !imgErr">
        <image
          :src="localImageUrl"
          mode="aspectFit"
          class="main-image"
          @tap="previewImage"
          @error="imgErr = true"
        ></image>
      </view>
      <view class="image-section no-img" v-else-if="detail.frontImage && !localImageUrl">
        <text class="no-img-text">图片加载中...</text>
      </view>
      <view class="image-section no-img" v-else>
        <text class="no-img-text">暂无产品图片</text>
      </view>

      <!-- 顶部概览 -->
      <view class="detail-header">
        <text class="dh-code">{{ detail.wheelCode || '-' }}</text>
        <view class="dh-tags">
          <view v-if="detail.productStatus" class="dtag"
            :class="detail.productStatus === '量产' ? 'dtag-success' : (detail.productStatus.indexOf('试制') > -1 ? 'dtag-warning' : 'dtag-info')">
            <text>{{ detail.productStatus }}</text>
          </view>
          <view v-if="detail.productType" class="dtag dtag-default">
            <text>{{ detail.productType }}</text>
          </view>
          <view v-if="detail.surfaceTreatment" class="dtag dtag-default">
            <text>{{ detail.surfaceTreatment }}</text>
          </view>
        </view>
      </view>

      <!-- 基础信息 -->
      <view class="detail-section">
        <view class="section-title-bar"><text class="section-title">基础信息</text></view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">客户</text>
            <text class="desc-value">{{ detail.customer || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">发货地</text>
            <text class="desc-value">{{ detail.shipLocation || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">产品类型</text>
            <text class="desc-value">{{ detail.productType || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">产品来源</text>
            <text class="desc-value">{{ detail.productSource || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">首模号</text>
            <text class="desc-value">{{ detail.firstMoldNo || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">首模来源</text>
            <text class="desc-value">{{ detail.moldSource || '-' }}</text>
          </view>
          <view class="desc-item full" v-if="detail.statusRemark">
            <text class="desc-label">状态备注</text>
            <text class="desc-value">{{ detail.statusRemark }}</text>
          </view>
        </view>
      </view>

      <!-- 规格参数 -->
      <view class="detail-section">
        <view class="section-title-bar"><text class="section-title">规格参数</text></view>
        <view class="desc-grid">
          <view class="desc-item">
            <text class="desc-label">规格</text>
            <text class="desc-value">{{ detail.sizeSpec || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">偏距ET</text>
            <text class="desc-value">{{ detail.offsetEt || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">PCD</text>
            <text class="desc-value">{{ detail.pcd || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">中心孔</text>
            <text class="desc-value">{{ detail.centerHole || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">设计单重</text>
            <text class="desc-value">{{ detail.designWeight ? detail.designWeight + 'kg' : '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">表面处理</text>
            <text class="desc-value">{{ detail.surfaceTreatment || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">颜色</text>
            <text class="desc-value">{{ detail.color || '-' }}</text>
          </view>
          <view class="desc-item">
            <text class="desc-label">标签</text>
            <text class="desc-value">{{ detail.labelInfo || '-' }}</text>
          </view>
          <view class="desc-item full" v-if="detail.similarWheels">
            <text class="desc-label">相似轮型</text>
            <text class="desc-value">{{ detail.similarWheels }}</text>
          </view>
          <view class="desc-item full" v-if="detail.similarDiff">
            <text class="desc-label">差异点</text>
            <text class="desc-value">{{ detail.similarDiff }}</text>
          </view>
        </view>
      </view>

      <!-- 准备阶段 -->
      <view class="detail-section" v-if="hasPrepData">
        <view class="section-title-bar"><text class="section-title">准备阶段</text></view>
        <view class="desc-grid">
          <view class="desc-item" v-if="detail.transferTime">
            <text class="desc-label">转移时间</text>
            <text class="desc-value">{{ detail.transferTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.internalEvalTime">
            <text class="desc-label">内评时间</text>
            <text class="desc-value">{{ detail.internalEvalTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.qualityCheckTime">
            <text class="desc-label">工检清单</text>
            <text class="desc-value">{{ detail.qualityCheckTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.moldOpenTime">
            <text class="desc-label">开模时间</text>
            <text class="desc-value">{{ detail.moldOpenTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.qualityArrivalTime">
            <text class="desc-label">工检到厂</text>
            <text class="desc-value">{{ detail.qualityArrivalTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.sampleWheelTime">
            <text class="desc-label">样轮到厂</text>
            <text class="desc-value">{{ detail.sampleWheelTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.paintArrivalTime">
            <text class="desc-label">油漆到厂</text>
            <text class="desc-value">{{ detail.paintArrivalTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.drawingIssueTime">
            <text class="desc-label">图纸签发</text>
            <text class="desc-value">{{ detail.drawingIssueTime }}</text>
          </view>
        </view>
      </view>

      <!-- 产品开发 -->
      <view class="detail-section" v-if="hasDevData">
        <view class="section-title-bar"><text class="section-title">产品开发</text></view>
        <view class="desc-grid">
          <view class="desc-item" v-if="detail.feaResult">
            <text class="desc-label">FEA结果</text>
            <text class="desc-value">{{ detail.feaResult }}</text>
          </view>
          <view class="desc-item" v-if="detail.firstMoldArrival">
            <text class="desc-label">首模到厂</text>
            <text class="desc-value">{{ detail.firstMoldArrival }}</text>
          </view>
          <view class="desc-item" v-if="detail.firstMachineTime">
            <text class="desc-label">首上机</text>
            <text class="desc-value">{{ detail.firstMachineTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.sampleSubmitTime">
            <text class="desc-label">送样时间</text>
            <text class="desc-value">{{ detail.sampleSubmitTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.samplePassTime">
            <text class="desc-label">样件合格</text>
            <text class="desc-value">{{ detail.samplePassTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.trialSummaryTime">
            <text class="desc-label">试制总结</text>
            <text class="desc-value">{{ detail.trialSummaryTime }}</text>
          </view>
          <view class="desc-item full" v-if="detail.sampleHistory">
            <text class="desc-label">送样履历</text>
            <text class="desc-value">{{ detail.sampleHistory }}</text>
          </view>
        </view>
      </view>

      <!-- 量产阶段 -->
      <view class="detail-section" v-if="hasProdData">
        <view class="section-title-bar"><text class="section-title">量产阶段</text></view>
        <view class="desc-grid">
          <view class="desc-item full" v-if="detail.trialSituation">
            <text class="desc-label">试制情况</text>
            <text class="desc-value">{{ detail.trialSituation }}</text>
          </view>
          <view class="desc-item" v-if="detail.batchSummaryTime">
            <text class="desc-label">小批量总结</text>
            <text class="desc-value">{{ detail.batchSummaryTime }}</text>
          </view>
          <view class="desc-item full" v-if="detail.batchSituation">
            <text class="desc-label">小批量情况</text>
            <text class="desc-value">{{ detail.batchSituation }}</text>
          </view>
          <view class="desc-item" v-if="detail.massProdTime">
            <text class="desc-label">量产时间</text>
            <text class="desc-value">{{ detail.massProdTime }}</text>
          </view>
          <view class="desc-item" v-if="detail.impactDeliveryTime">
            <text class="desc-label">影响交付</text>
            <text class="desc-value">{{ detail.impactDeliveryTime }}</text>
          </view>
          <view class="desc-item full" v-if="detail.impactDeliveryItem">
            <text class="desc-label">交付事项</text>
            <text class="desc-value">{{ detail.impactDeliveryItem }}</text>
          </view>
        </view>
      </view>

      <!-- 持续改进 -->
      <view class="detail-section" v-if="hasChangeData">
        <view class="section-title-bar"><text class="section-title">持续改进</text></view>
        <view class="desc-grid">
          <view class="desc-item" v-if="detail.latestChangeTime">
            <text class="desc-label">变更关闭</text>
            <text class="desc-value">{{ detail.latestChangeTime }}</text>
          </view>
          <view class="desc-item full" v-if="detail.latestChangeContent">
            <text class="desc-label">变更内容</text>
            <text class="desc-value">{{ detail.latestChangeContent }}</text>
          </view>
          <view class="desc-item full" v-if="detail.controlPoints">
            <text class="desc-label">控制要点</text>
            <text class="desc-value">{{ detail.controlPoints }}</text>
          </view>
        </view>
      </view>

      <view style="height: 180rpx"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="detail.productId">
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
          <text class="form-header-title">编辑产品</text>
          <view style="width: 60rpx;"></view>
        </view>
        <scroll-view scroll-y class="form-body">
          <!-- 图片 -->
          <view class="form-section">
            <view class="form-section-title">正面图</view>
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
              <text class="form-label"><text class="required">*</text>轮型号</text>
              <input class="form-input" v-model="formData.wheelCode" placeholder="如 05115C09" />
            </view>
            <view class="form-item">
              <text class="form-label">序号</text>
              <input class="form-input" v-model="formData.serialNo" type="number" placeholder="排序序号" />
            </view>
            <view class="form-item">
              <text class="form-label">客户</text>
              <input class="form-input" v-model="formData.customer" placeholder="如 丰田" />
            </view>
            <view class="form-item">
              <text class="form-label">发货地</text>
              <input class="form-input" v-model="formData.shipLocation" placeholder="如 广州" />
            </view>
            <view class="form-item">
              <text class="form-label">产品类型</text>
              <input class="form-input" v-model="formData.productType" placeholder="外部调入/常规转移等" />
            </view>
            <view class="form-item">
              <text class="form-label">产品来源</text>
              <input class="form-input" v-model="formData.productSource" placeholder="散装调拨/集团转移等" />
            </view>
            <view class="form-item">
              <text class="form-label">首模号</text>
              <input class="form-input" v-model="formData.firstMoldNo" placeholder="首模号" />
            </view>
            <view class="form-item">
              <text class="form-label">首模来源</text>
              <input class="form-input" v-model="formData.moldSource" placeholder="首模来源" />
            </view>
            <view class="form-item form-item-picker">
              <text class="form-label">产品状态</text>
              <picker mode="selector" :range="statusOptions" :value="formStatusIndex" @change="onFormStatusChange">
                <view class="form-picker-value">
                  <text>{{ formData.productStatus || '请选择' }}</text>
                  <text class="form-picker-arrow">&#x25BE;</text>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">状态备注</text>
              <input class="form-input" v-model="formData.statusRemark" placeholder="状态备注" />
            </view>
          </view>

          <!-- 规格参数 -->
          <view class="form-section">
            <view class="form-section-title">规格参数</view>
            <view class="form-item">
              <text class="form-label">规格</text>
              <input class="form-input" v-model="formData.sizeSpec" placeholder="如 1775" />
            </view>
            <view class="form-item">
              <text class="form-label">偏距ET</text>
              <input class="form-input" v-model="formData.offsetEt" placeholder="如 ET45" />
            </view>
            <view class="form-item">
              <text class="form-label">PCD</text>
              <input class="form-input" v-model="formData.pcd" placeholder="如 5-114.3" />
            </view>
            <view class="form-item">
              <text class="form-label">中心孔</text>
              <input class="form-input" v-model="formData.centerHole" placeholder="如 O60" />
            </view>
            <view class="form-item">
              <text class="form-label">设计单重</text>
              <input class="form-input" v-model="formData.designWeight" type="digit" placeholder="单位 kg" />
            </view>
            <view class="form-item">
              <text class="form-label">表面处理</text>
              <input class="form-input" v-model="formData.surfaceTreatment" placeholder="精车/全涂装等" />
            </view>
            <view class="form-item">
              <text class="form-label">颜色</text>
              <input class="form-input" v-model="formData.color" placeholder="如 11BK19" />
            </view>
            <view class="form-item">
              <text class="form-label">标签</text>
              <input class="form-input" v-model="formData.labelInfo" placeholder="标签信息" />
            </view>
          </view>

          <!-- 相似产品 -->
          <view class="form-section">
            <view class="form-section-title">相似产品</view>
            <view class="form-item">
              <text class="form-label">相似轮型</text>
              <input class="form-input" v-model="formData.similarWheels" placeholder="多个用逗号分隔" />
            </view>
            <view class="form-item form-item-textarea">
              <text class="form-label">差异点</text>
              <textarea class="form-textarea" v-model="formData.similarDiff" placeholder="差异说明" auto-height></textarea>
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
import { getProduct, buildImageUrl, resolveImageUrl, updateProduct, delProduct, uploadFile } from '@/api/product'

export default {
  data() {
    return {
      productId: null,
      detail: {},
      imgErr: false,
      localImageUrl: '',
      // 编辑表单
      formVisible: false,
      formLoading: false,
      imageUploading: false,
      formData: {},
      formStatusIndex: 0,
      statusOptions: ['量产', '试制/小批量', '退市/停产', '转移', '还回品/报废']
    }
  },
  computed: {
    navBarStyle() {
      var sys = uni.getSystemInfoSync()
      return 'padding-top: ' + (sys.statusBarHeight || 20) + 'px'
    },
    hasPrepData() {
      var d = this.detail
      return d.transferTime || d.internalEvalTime || d.qualityCheckTime ||
        d.moldOpenTime || d.qualityArrivalTime || d.sampleWheelTime ||
        d.paintArrivalTime || d.drawingIssueTime
    },
    hasDevData() {
      var d = this.detail
      return d.feaResult || d.firstMoldArrival || d.firstMachineTime ||
        d.sampleSubmitTime || d.samplePassTime || d.trialSummaryTime || d.sampleHistory
    },
    hasProdData() {
      var d = this.detail
      return d.trialSituation || d.batchSummaryTime || d.batchSituation ||
        d.massProdTime || d.impactDeliveryTime || d.impactDeliveryItem
    },
    hasChangeData() {
      var d = this.detail
      return d.latestChangeTime || d.latestChangeContent || d.controlPoints
    }
  },
  onLoad(options) {
    if (options.id) {
      this.productId = options.id
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
      getProduct(this.productId).then(function (res) {
        this.detail = res.data || {}
        this.imgErr = false
        this.localImageUrl = ''
        if (this.detail.frontImage) {
          resolveImageUrl(this.detail.frontImage).then(function (localPath) {
            this.localImageUrl = localPath
          }.bind(this))
        }
      }.bind(this)).catch(function () {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }).finally(function () {
        uni.hideLoading()
      })
    },

    getImageUrl(path) {
      return buildImageUrl(path)
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
        productId: d.productId,
        wheelCode: d.wheelCode || '',
        serialNo: d.serialNo || null,
        frontImage: d.frontImage || null,
        _localPreview: this.localImageUrl || '',
        customer: d.customer || '',
        shipLocation: d.shipLocation || '',
        productType: d.productType || '',
        productSource: d.productSource || '',
        firstMoldNo: d.firstMoldNo || '',
        moldSource: d.moldSource || '',
        productStatus: d.productStatus || '量产',
        statusRemark: d.statusRemark || '',
        sizeSpec: d.sizeSpec || '',
        offsetEt: d.offsetEt || '',
        pcd: d.pcd || '',
        centerHole: d.centerHole || '',
        designWeight: d.designWeight || null,
        surfaceTreatment: d.surfaceTreatment || '',
        color: d.color || '',
        labelInfo: d.labelInfo || '',
        similarWheels: d.similarWheels || '',
        similarDiff: d.similarDiff || ''
      }
      var idx = this.statusOptions.indexOf(d.productStatus)
      this.formStatusIndex = idx >= 0 ? idx : 0
      this.formVisible = true
    },

    closeForm() {
      this.formVisible = false
    },

    onFormStatusChange(e) {
      this.formStatusIndex = e.detail.value
      this.formData.productStatus = this.statusOptions[this.formStatusIndex]
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
            self.formData.frontImage = data.fileName
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
      if (!this.formData.wheelCode || !this.formData.wheelCode.trim()) {
        uni.showToast({ title: '请填写轮型号', icon: 'none' })
        return
      }
      this.formLoading = true
      var submitData = Object.assign({}, this.formData)
      delete submitData._localPreview

      updateProduct(submitData).then(function () {
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
        content: '确认删除产品 "' + (this.detail.wheelCode || '') + '"？此操作不可恢复',
        confirmColor: '#f56c6c',
        success: function (res) {
          if (res.confirm) {
            uni.showLoading({ title: '删除中' })
            delProduct(self.detail.productId).then(function () {
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
  &.dtag-success { background: #f0f9eb; color: #67c23a; }
  &.dtag-warning { background: #fdf6ec; color: #e6a23c; }
  &.dtag-info { background: #ecf5ff; color: #409eff; }
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
  &.full { width: 100%; }
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
  padding: 60rpx 24rpx 20rpx;
  flex-shrink: 0;
}
.form-header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-header-back-icon {
  font-size: 56rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
.form-header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
.form-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}
.form-footer-btn {
  flex: 1;
  height: 96rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { opacity: 0.8; }
  &.disabled { opacity: 0.6; }
}
.form-footer-cancel {
  background: #f5f7fa;
  border: 2rpx solid #dcdfe6;
}
.form-footer-save {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
}
.form-footer-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  .form-footer-save & {
    color: #fff;
  }
}
.form-footer-cancel .form-footer-btn-text {
  color: #606266;
}
.form-footer-save .form-footer-btn-text {
  color: #fff;
}
.form-body {
  flex: 1;
  min-height: 0;
}
.form-section {
  background: #fff;
  margin: 20rpx 20rpx 0;
  border-radius: 20rpx;
  padding: 0 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.form-section-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #6c5bb3;
  padding: 20rpx 0 12rpx;
  border-bottom: 2rpx solid #f5f7fa;
  margin-bottom: 4rpx;
}
.form-item {
  display: flex;
  align-items: center;
  min-height: 88rpx;
  border-bottom: 1rpx solid #f5f7fa;
  &:last-child { border-bottom: none; }
}
.form-item-textarea {
  align-items: flex-start;
  padding: 16rpx 0;
}
.form-item-picker {
  cursor: pointer;
}
.form-label {
  font-size: 26rpx;
  color: #606266;
  width: 140rpx;
  flex-shrink: 0;
  .required {
    color: #f56c6c;
    margin-right: 4rpx;
  }
}
.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #303133;
  height: 88rpx;
  text-align: right;
}
.form-textarea {
  flex: 1;
  font-size: 28rpx;
  color: #303133;
  text-align: right;
  min-height: 80rpx;
  width: 100%;
}
.form-picker-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  font-size: 28rpx;
  color: #303133;
}
.form-picker-arrow {
  font-size: 22rpx;
  color: #909399;
}
.form-image-row {
  width: 200rpx;
  height: 200rpx;
  margin: 16rpx auto;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
  border: 2rpx dashed #d0d0d0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-preview-img {
  width: 100%;
  height: 100%;
}
.form-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}
.form-image-icon {
  font-size: 64rpx;
  opacity: 0.5;
}
.form-image-text {
  font-size: 22rpx;
  color: #c0c4cc;
}
.form-image-uploading {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-image-uploading-text {
  font-size: 24rpx;
  color: #fff;
}
</style>
