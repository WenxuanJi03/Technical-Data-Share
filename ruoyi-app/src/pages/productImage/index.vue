<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">&#x2039;</text>
      </view>
      <text class="nav-title">产品图</text>
      <view class="nav-placeholder"></view>
    </view>

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

    <view class="filter-bar">
      <view class="filter-chips">
        <picker mode="selector" :range="statusOptions" :value="statusIndex" @change="onStatusChange">
          <view class="filter-chip" :class="{ active: queryParams.productStatus }">
            <text>{{ queryParams.productStatus || '产品状态' }}</text>
            <text class="chip-arrow">&#x25BE;</text>
          </view>
        </picker>
        <view class="filter-chip" :class="{ active: queryParams.customer }" @tap="openFilterInput('customer')">
          <text>{{ queryParams.customer || '客户' }}</text>
          <text class="chip-arrow">&#x25BE;</text>
        </view>
        <view class="filter-chip" :class="{ active: queryParams.sizeSpec }" @tap="openFilterInput('sizeSpec')">
          <text>{{ queryParams.sizeSpec || '规格' }}</text>
          <text class="chip-arrow">&#x25BE;</text>
        </view>
        <view class="filter-chip" :class="{ active: queryParams.surfaceTreatment }" @tap="openFilterInput('surfaceTreatment')">
          <text>{{ queryParams.surfaceTreatment || '表面处理' }}</text>
          <text class="chip-arrow">&#x25BE;</text>
        </view>
        <view class="filter-chip reset" v-if="hasFilters" @tap="resetFilters">
          <text>重置</text>
        </view>
      </view>
      <view class="filter-add-btn" @tap="openAddForm">
        <text class="filter-add-icon">+</text>
      </view>
    </view>

    <view class="stats-bar" v-if="total > 0">
      <text class="stats-text">共 <text class="stats-num">{{ total }}</text> 款产品</text>
    </view>

    <scroll-view
      scroll-y
      class="card-scroll"
      @scrolltolower="loadMore"
      refresher-enabled
      @refresherrefresh="onRefresh"
      :refresher-triggered="refreshing"
    >
      <view v-if="productList.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无产品数据</text>
      </view>

      <view class="waterfall-grid" v-if="productList.length > 0">
        <view
          v-for="item in productList"
          :key="item.productId"
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
            <view v-else-if="item.frontImage && !item._imgErr" class="no-image">
              <text class="no-image-text">加载中...</text>
            </view>
            <view v-else class="no-image">
              <text class="no-image-text">暂无图片</text>
            </view>
            <view
              class="status-badge"
              v-if="item.productStatus"
              :class="item.productStatus === '量产' ? 'badge-success' : (item.productStatus.indexOf('试制') > -1 ? 'badge-warning' : (item.productStatus.indexOf('退市') > -1 || item.productStatus.indexOf('停产') > -1 ? 'badge-info' : (item.productStatus.indexOf('报废') > -1 ? 'badge-danger' : 'badge-default')))"
            >
              <text class="badge-text">{{ item.productStatus }}</text>
            </view>
          </view>
          <view class="card-body">
            <text class="wheel-code">{{ item.wheelCode }}</text>
            <view class="info-row">
              <text class="info-label">客户</text>
              <text class="info-value">{{ item.customer || '-' }}</text>
            </view>
            <view class="info-row" v-if="item.sizeSpec || item.offsetEt">
              <text class="info-label">规格</text>
              <text class="info-value">{{ item.sizeSpec || '-' }}</text>
              <text class="info-sep" v-if="item.offsetEt"> </text>
              <text class="info-label" v-if="item.offsetEt">ET</text>
              <text class="info-value" v-if="item.offsetEt">{{ item.offsetEt }}</text>
            </view>
            <view class="info-row" v-if="item.pcd || item.centerHole">
              <text class="info-label">PCD</text>
              <text class="info-value">{{ item.pcd || '-' }}</text>
              <text class="info-sep"> </text>
              <text class="info-label">孔</text>
              <text class="info-value">{{ item.centerHole || '-' }}</text>
            </view>
            <view class="info-row" v-if="item.surfaceTreatment">
              <text class="info-label">表面</text>
              <text class="info-value">{{ item.surfaceTreatment }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="list-bottom" v-if="productList.length > 0">
        <text class="list-bottom-text" v-if="noMore">—— 没有更多了 ——</text>
        <text class="list-bottom-text" v-else-if="loading">加载中...</text>
      </view>
    </scroll-view>

    <!-- 筛选输入弹窗 -->
    <view class="dialog-mask" v-if="filterInput.visible" @tap="filterInput.visible = false">
      <view class="dialog-box" @tap.stop>
        <text class="dialog-title">{{ filterInput.title }}</text>
        <input
          class="dialog-input"
          v-model="filterInput.value"
          :placeholder="filterInput.placeholder"
          :focus="filterInput.visible"
          @confirm="confirmFilterInput"
        />
        <view class="dialog-btns">
          <view class="dialog-btn cancel" @tap="clearFilterInput">清除</view>
          <view class="dialog-btn confirm" @tap="confirmFilterInput">确定</view>
        </view>
      </view>
    </view>

    <!-- 新增/编辑表单弹层 -->
    <view class="form-overlay" v-if="formVisible">
      <view class="form-wrap">
        <view class="form-header">
          <text class="form-header-cancel" @tap="closeForm">取消</text>
          <text class="form-header-title">新增产品</text>
          <text class="form-header-save" @tap="submitForm" :class="{ disabled: formLoading }">保存</text>
        </view>
        <scroll-view scroll-y class="form-body">
          <!-- 图片 -->
          <view class="form-section">
            <view class="form-section-title">正面图</view>
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
      </view>
    </view>
  </view>
</template>

<script>
import { listProduct, buildImageUrl, resolveImageUrl, addProduct, uploadFile } from '@/api/product'

export default {
  data() {
    return {
      loading: false,
      refreshing: false,
      noMore: false,
      total: 0,
      productList: [],
      searchKeyword: '',
      searchTimer: null,
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        wheelCode: null,
        customer: null,
        productStatus: null,
        sizeSpec: null,
        surfaceTreatment: null
      },
      statusOptions: ['量产', '试制/小批量', '退市/停产', '转移', '还回品/报废'],
      statusIndex: -1,
      filterInput: {
        visible: false,
        field: '',
        title: '',
        placeholder: '',
        value: ''
      },
      // 新增表单
      formVisible: false,
      formLoading: false,
      imageUploading: false,
      formStatusIndex: 0,
      formData: {}
    }
  },
  computed: {
    navBarStyle() {
      const sys = uni.getSystemInfoSync()
      return 'padding-top: ' + (sys.statusBarHeight || 20) + 'px'
    },
    hasFilters() {
      return this.queryParams.productStatus || this.queryParams.customer || this.queryParams.sizeSpec || this.queryParams.surfaceTreatment || this.searchKeyword
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
        params.wheelCode = this.searchKeyword
      } else if (this.queryParams.wheelCode) {
        params.wheelCode = this.queryParams.wheelCode
      }
      if (this.queryParams.customer) params.customer = this.queryParams.customer
      if (this.queryParams.productStatus) params.productStatus = this.queryParams.productStatus
      if (this.queryParams.sizeSpec) params.sizeSpec = this.queryParams.sizeSpec
      if (this.queryParams.surfaceTreatment) params.surfaceTreatment = this.queryParams.surfaceTreatment

      listProduct(params).then(function (res) {
        var rows = res.rows || []
        if (this.queryParams.pageNum === 1) {
          this.productList = rows
        } else {
          this.productList = this.productList.concat(rows)
        }
        this.total = res.total || 0
        this.noMore = this.productList.length >= this.total
        this.resolveImages(rows)
      }.bind(this)).catch(function () {
        uni.showToast({ title: '加载失败', icon: 'none' })
        if (this.queryParams.pageNum === 1) {
          this.productList = []
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
      this.productList = []
      this.loadData()
    },

    onSearch() {
      var self = this
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(function () {
        self.queryParams.pageNum = 1
        self.productList = []
        self.loadData()
      }, 400)
    },

    reloadList() {
      this.queryParams.pageNum = 1
      this.productList = []
      this.loadData()
    },

    onStatusChange(e) {
      this.statusIndex = e.detail.value
      this.queryParams.productStatus = this.statusOptions[this.statusIndex]
      this.reloadList()
    },

    openFilterInput(field) {
      var fieldMap = {
        customer: { title: '筛选客户', placeholder: '输入客户名称关键字' },
        sizeSpec: { title: '筛选规格', placeholder: '输入规格关键字，如 17x7.0' },
        surfaceTreatment: { title: '筛选表面处理', placeholder: '输入表面处理关键字，如 精车' }
      }
      var cfg = fieldMap[field]
      if (!cfg) return
      this.filterInput.title = cfg.title
      this.filterInput.placeholder = cfg.placeholder
      this.filterInput.value = this.queryParams[field] || ''
      this.filterInput.field = field
      this.filterInput.visible = true
    },

    confirmFilterInput() {
      var val = this.filterInput.value.trim()
      this.queryParams[this.filterInput.field] = val || null
      this.filterInput.visible = false
      this.reloadList()
    },

    clearFilterInput() {
      this.queryParams[this.filterInput.field] = null
      this.filterInput.value = ''
      this.filterInput.visible = false
      this.reloadList()
    },

    resetFilters() {
      this.searchKeyword = ''
      this.statusIndex = -1
      this.queryParams.productStatus = null
      this.queryParams.customer = null
      this.queryParams.sizeSpec = null
      this.queryParams.surfaceTreatment = null
      this.queryParams.wheelCode = null
      this.reloadList()
    },

    resolveImages(rows) {
      var self = this
      rows.forEach(function (item) {
        if (!item.frontImage) return
        resolveImageUrl(item.frontImage).then(function (localPath) {
          var target = self.productList.find(function (p) { return p.productId === item.productId })
          if (target) {
            self.$set(target, '_localImg', localPath)
          }
        })
      })
    },

    getImageUrl(path) {
      return buildImageUrl(path)
    },

    onImageError(item) {
      this.$set(item, '_imgErr', true)
    },

    showDetail(item) {
      uni.navigateTo({
        url: '/pages/productImage/detail?id=' + item.productId
      })
    },

    // ——— 新增表单 ———
    openAddForm() {
      this.formData = {
        productId: null,
        wheelCode: '',
        serialNo: null,
        frontImage: null,
        _localPreview: '',
        customer: '',
        shipLocation: '',
        productType: '',
        productSource: '',
        firstMoldNo: '',
        moldSource: '',
        productStatus: '量产',
        statusRemark: '',
        sizeSpec: '',
        offsetEt: '',
        pcd: '',
        centerHole: '',
        designWeight: null,
        surfaceTreatment: '',
        color: '',
        labelInfo: '',
        similarWheels: '',
        similarDiff: ''
      }
      this.formStatusIndex = 0
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
            self.formData._localPreview = ''
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

      addProduct(submitData).then(function () {
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
.filter-add-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
  margin-top: -4rpx;
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
  .status-badge {
    position: absolute;
    top: 10rpx;
    right: 10rpx;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
    .badge-text {
      font-size: 20rpx;
      font-weight: 500;
    }
    &.badge-success {
      background: rgba(240, 249, 235, 0.9);
      .badge-text { color: #67c23a; }
    }
    &.badge-warning {
      background: rgba(253, 246, 236, 0.9);
      .badge-text { color: #e6a23c; }
    }
    &.badge-info {
      background: rgba(244, 244, 245, 0.9);
      .badge-text { color: #909399; }
    }
    &.badge-danger {
      background: rgba(254, 240, 240, 0.9);
      .badge-text { color: #f56c6c; }
    }
    &.badge-default {
      background: rgba(236, 245, 255, 0.9);
      .badge-text { color: #409eff; }
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
    max-width: 120rpx;
  }
  .info-sep {
    font-size: 22rpx;
    color: #dcdfe6;
    margin: 0 6rpx;
    flex-shrink: 0;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  .empty-text {
    font-size: 28rpx;
    color: #c0c4cc;
  }
}

.list-bottom {
  text-align: center;
  padding: 30rpx 0 60rpx;
  .list-bottom-text {
    font-size: 24rpx;
    color: #c0c4cc;
  }
}

/* 筛选弹窗 */
.dialog-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.dialog-box {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.15);
}
.dialog-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 24rpx;
  text-align: center;
}
.dialog-input {
  background: #f5f7fa;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  padding: 0 24rpx;
  height: 80rpx;
  font-size: 28rpx;
  color: #303133;
  margin-bottom: 30rpx;
}
.dialog-btns {
  display: flex;
  gap: 20rpx;
}
.dialog-btn {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  &.cancel {
    background: #f5f7fa;
    color: #909399;
  }
  &.confirm {
    background: #6c5bb3;
    color: #fff;
  }
}

/* 新增表单弹层 */
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
  padding: 60rpx 30rpx 20rpx;
  flex-shrink: 0;
}
.form-header-cancel {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  padding: 8rpx 0;
}
.form-header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
.form-header-save {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  padding: 8rpx 28rpx;
  border-radius: 30rpx;
  &.disabled { opacity: 0.5; }
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
