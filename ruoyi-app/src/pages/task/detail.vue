<template>
  <view class="detail-page">
    <!-- 顶部 -->
    <view class="header-bg">
      <view class="header-badge">移动端菜单</view>
      <view class="sys-title">
        <view class="title-left">
          <text class="title-main">试制信息录入系统</text>
          <text class="title-sub">试制监控与管理</text>
        </view>
        <view class="title-right">
          <text class="resp-name">{{ notice.responsible || '负责人' }}</text>
          <text class="resp-line">{{ notice.craftProcess || '生产线' }}</text>
        </view>
      </view>
    </view>

    <!-- 产品信息概览 -->
    <view class="info-card">
      <view class="card-bar">产品信息概览</view>
      <view class="product-section">
        <view class="product-img">
          <image v-if="notice.wheelImage" :src="notice.wheelImage" mode="aspectFill" class="wheel-img" />
          <view v-else class="img-placeholder">🔩</view>
        </view>
        <view class="product-grid">
          <view class="grid-row">
            <view class="grid-cell"><text class="cell-label">模具编号:</text> {{ notice.wheelCode || '-' }} <text :class="notice.urgency === '紧急' ? 'dot-red' : 'dot-green'">●</text> {{ notice.urgency || '正常' }}</view>
            <view class="grid-cell"><text class="cell-label">试制编号:</text> {{ notice.noticeCode || '-' }}</view>
          </view>
          <view class="grid-row">
            <view class="grid-cell"><text class="cell-label">试制类型:</text> {{ notice.trialType || '-' }} <text class="dot-orange">●</text></view>
            <view class="grid-cell"><text class="cell-label">涂装次数:</text> 1 <text class="dot-red">●</text> 表面状态: {{ notice.surfaceStatus || '-' }}</view>
          </view>
          <view class="grid-row">
            <view class="grid-cell"><text class="cell-label">当前工序:</text> {{ currentProcess }} <text class="dot-orange">●</text></view>
            <view class="grid-cell"><text class="cell-label">产品尺寸:</text> {{ notice.sizeSpec || '-' }}</view>
          </view>
          <view class="grid-row">
            <view class="grid-cell highlight"><text class="cell-label">工艺流程:</text> {{ notice.craftProcess || '-' }}+{{ notice.surfaceStatus || '-' }}</view>
            <view class="grid-cell"><text class="cell-label">#</text> 下转数: 无、试制数: {{ notice.trialQuantity || 0 }}、交样数: {{ notice.sampleQuantity || 0 }}</view>
          </view>
          <view class="grid-row">
            <view class="grid-cell"><text class="cell-label">试制目的:</text> {{ notice.devType || '新产品首次试制-小批量' }}</view>
            <view class="grid-cell notice-cell">
              <text class="cell-label">注意事项及特殊要求:</text>
              <text class="notice-text">{{ experimentText }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="action-row">
        <view class="action-btn blue" @tap="openFilesDialog">&#128196; 文件</view>
        <view class="action-btn orange" @tap="goBack">⬅ 返回</view>
      </view>
    </view>

    <!-- 已录入数据 -->
    <view class="data-card">
      <view class="data-header">
        <view>
          <text class="data-title">已录入数据</text>
          <text class="data-sub">最近录入记录</text>
        </view>
        <view class="data-actions">
          <view class="small-btn" @tap="copyData">📋 复制数据</view>
          <view class="small-btn" @tap="loadDetail">🔄 刷新</view>
        </view>
      </view>

      <view class="record-list">
        <view v-for="(proc, idx) in processDataList" :key="idx" class="record-row">
          <text class="record-label">{{ proc.name }}</text>
          <text class="record-value" :class="{ 'no-data': !proc.result || proc.result === '无' }">{{ proc.result || '无' }}</text>
        </view>
      </view>

      <view class="input-area">
        <button class="input-btn" @tap="showInputForm = true">✏ 输入</button>
      </view>
    </view>

    <!-- 数据录入弹窗 -->
    <view v-if="showInputForm" class="modal-mask" @tap.self="showInputForm = false">
      <view class="modal-content">
        <view class="modal-title">工序数据录入</view>
        <scroll-view scroll-y class="modal-scroll">
          <view v-for="proc in fixedProcesses" :key="proc" class="form-row">
            <text class="form-label">{{ proc }}</text>
            <picker :value="inputIndex[proc]" :range="resultOptions" @change="onPickerChange($event, proc)">
              <view class="form-picker">{{ inputForm[proc] || '请选择结果' }}</view>
            </picker>
          </view>
          <view class="form-row">
            <text class="form-label">备注</text>
            <input v-model="inputForm.remark" placeholder="输入备注" class="form-input" />
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @tap="showInputForm = false">取消</button>
          <button class="modal-btn confirm" @tap="submitInput">提交</button>
        </view>
      </view>
    </view>

    <!-- 文件弹窗 -->
    <view v-if="showFilesDialog" class="modal-mask" @tap.self="showFilesDialog = false">
      <view class="modal-content">
        <view class="modal-title">相关文件</view>
        <scroll-view scroll-y class="modal-scroll" v-if="relatedFiles.length > 0">
          <view v-for="(file, idx) in relatedFiles" :key="idx" class="file-item" @tap="openFile(file)">
            <view class="file-icon" :style="{ background: getFileColor(file.fileType) }">
              <text class="file-ext">{{ getFileExt(file.fileType) }}</text>
            </view>
            <view class="file-info">
              <text class="file-name">{{ file.docName }}</text>
              <text class="file-meta">{{ file.fileType ? file.fileType.toUpperCase() : '' }} {{ formatFileSize(file.fileSize) }}</text>
            </view>
            <view class="file-download">下载</view>
          </view>
        </scroll-view>
        <view v-else class="empty-files">&#128194; 暂无相关文件</view>
        <view class="modal-footer">
          <button class="modal-btn confirm" @tap="showFilesDialog = false">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getNotice, updateNotice } from '@/api/notice'
import { listDocument } from '@/api/document'
import config from '@/config/index'

const FIXED = ['压铸', '旋压', '热处理', 'X光', '粗车', '涂装', '终检', '精车', '台架实验', '性能实验', '总结']

export default {
  data() {
    return {
      notice: {},
      processDataList: [],
      fixedProcesses: FIXED,
      resultOptions: ['合格', '不合格', '无', '待检'],
      showInputForm: false,
      showFilesDialog: false,
      inputForm: {},
      inputIndex: {},
      relatedFiles: []
    }
  },
  computed: {
    experimentText() {
      return this.notice.experimentItems ? this.notice.experimentItems.replace(/,/g, ', ') : '无'
    },
    currentProcess() {
      const cur = this.processDataList.find(p => p.status === '进行中')
      if (cur) return cur.name
      const pen = this.processDataList.find(p => p.status === '待开始')
      return pen ? pen.name : '已完成'
    }
  },
  onLoad(options) {
    if (options.id) this.loadDetail(options.id)
    this.resetInput()
  },
  methods: {
    loadDetail(id) {
      const nid = id || this.notice.noticeId
      if (!nid) return
      uni.showLoading({ title: '加载中' })
      getNotice(nid).then(res => {
        this.notice = res.data || {}
        this.parseProcess()
      }).catch(() => {
        // 模拟数据
        this.notice = {
          noticeId: nid, wheelCode: '07125C67-M1', noticeCode: 'CD16-2025-0516',
          trialType: '新产品小批量', surfaceStatus: '全涂', craftProcess: '低压铸造',
          sizeSpec: '1770', trialQuantity: 200, sampleQuantity: 0, responsible: '王工 (生产主管)',
          urgency: '正常', devType: '新产品首次试制-小批量',
          experimentItems: '13度冲击,90度冲击,CASS中性盐雾,化学成分,弯曲疲劳,径向疲劳,机械性能,金相'
        }
        this.initEmpty()
      }).finally(() => {
        uni.hideLoading()
      })
    },
    parseProcess() {
      if (this.notice.processData) {
        try {
          this.processDataList = JSON.parse(this.notice.processData)
          this.ensureAll()
        } catch (e) { this.initEmpty() }
      } else { this.initEmpty() }
    },
    initEmpty() {
      this.processDataList = FIXED.map(n => ({ name: n, status: '待开始', result: '', remark: '' }))
    },
    ensureAll() {
      const exist = this.processDataList.map(p => p.name)
      FIXED.forEach(n => {
        if (!exist.includes(n)) this.processDataList.push({ name: n, status: '待开始', result: '', remark: '' })
      })
      this.processDataList.sort((a, b) => FIXED.indexOf(a.name) - FIXED.indexOf(b.name))
    },
    resetInput() {
      this.inputForm = {}
      this.inputIndex = {}
      FIXED.forEach(p => { this.inputForm[p] = ''; this.inputIndex[p] = -1 })
      this.inputForm.remark = ''
    },
    onPickerChange(e, proc) {
      const idx = e.detail.value
      this.$set(this.inputForm, proc, this.resultOptions[idx])
      this.$set(this.inputIndex, proc, idx)
    },
    submitInput() {
      FIXED.forEach(name => {
        if (this.inputForm[name]) {
          const proc = this.processDataList.find(p => p.name === name)
          if (proc) {
            proc.result = this.inputForm[name]
            if (this.inputForm[name] !== '无') proc.status = '已完成'
          }
        }
      })
      const data = { noticeId: this.notice.noticeId, processData: JSON.stringify(this.processDataList) }
      updateNotice(data).then(() => {
        uni.showToast({ title: '录入成功', icon: 'success' })
        this.showInputForm = false
        this.resetInput()
        this.loadDetail()
      }).catch(() => {
        uni.showToast({ title: '录入成功（本地）', icon: 'success' })
        this.showInputForm = false
        this.resetInput()
      })
    },
    openFilesDialog() {
      this.showFilesDialog = true
      this.loadRelatedFiles()
    },
    loadRelatedFiles() {
      listDocument({ pageNum: 1, pageSize: 100 }).then(res => {
        this.relatedFiles = (res.rows || []).filter(f => {
          if (!this.notice.wheelCode) return false
          return (f.docName && f.docName.includes(this.notice.wheelCode)) ||
                 (f.docCode && f.docCode.includes(this.notice.wheelCode)) ||
                 (f.projectName && this.notice.customerName && f.projectName.includes(this.notice.customerName))
        })
      }).catch(() => {
        this.relatedFiles = []
      })
    },
    openFile(file) {
      if (!file.filePath) {
        uni.showToast({ title: '暂无文件', icon: 'none' })
        return
      }
      uni.showLoading({ title: '准备下载...' })
      const url = config.baseUrl + '/common/download/resource?resource=' + encodeURIComponent(file.filePath)
      // #ifdef MP-WEIXIN
      uni.downloadFile({
        url: url,
        header: { 'Authorization': 'Bearer ' + uni.getStorageSync('token') },
        success: (res) => {
          if (res.statusCode === 200) {
            uni.openDocument({
              filePath: res.tempFilePath,
              showMenu: true,
              fail: () => { uni.showToast({ title: '无法打开此文件', icon: 'none' }) }
            })
          }
        },
        fail: () => { uni.showToast({ title: '下载失败', icon: 'none' }) },
        complete: () => { uni.hideLoading() }
      })
      // #endif
      // #ifdef H5
      window.open(url)
      uni.hideLoading()
      // #endif
    },
    getFileColor(type) {
      const t = (type || '').toLowerCase()
      const colors = { 'xlsx': '#217346', 'xls': '#217346', 'doc': '#2B579A', 'docx': '#2B579A', 'pdf': '#D32F2F', 'dwg': '#E65100' }
      return colors[t] || '#909399'
    },
    getFileExt(type) {
      return (type || '?').toUpperCase().substring(0, 4)
    },
    formatFileSize(size) {
      if (!size) return ''
      if (size < 1024) return size + 'B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
      return (size / (1024 * 1024)).toFixed(1) + 'MB'
    },
    copyData() {
      let txt = `模具编号: ${this.notice.wheelCode}\n试制编号: ${this.notice.noticeCode}\n`
      this.processDataList.forEach(p => { txt += `${p.name}: ${p.result || '无'}\n` })
      uni.setClipboardData({ data: txt, success: () => { uni.showToast({ title: '已复制' }) } })
    },
    goBack() { uni.navigateBack() }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #2d2452 0%, #3d3470 12%, #f0f2f5 12%);
  padding-bottom: 40rpx;
}

.header-bg {
  padding: 24rpx 30rpx 16rpx;

  .header-badge {
    text-align: center;
    font-size: 26rpx;
    color: rgba(255,255,255,0.6);
    margin-bottom: 12rpx;
  }
}

.sys-title {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .title-main { display: block; font-size: 30rpx; font-weight: 600; color: #fff; }
  .title-sub { display: block; font-size: 20rpx; color: rgba(255,255,255,0.5); }
  .resp-name { display: block; font-size: 24rpx; color: #fff; text-align: right; }
  .resp-line { display: block; font-size: 20rpx; color: rgba(255,255,255,0.5); text-align: right; }
}

.info-card, .data-card {
  margin: 16rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

.card-bar {
  font-size: 30rpx;
  font-weight: 700;
  color: #303133;
  padding-left: 16rpx;
  border-left: 6rpx solid #6c5bb3;
  margin-bottom: 20rpx;
}

.product-section {
  display: flex;
  gap: 16rpx;
}

.product-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;

  .wheel-img { width: 100%; height: 100%; }
  .img-placeholder {
    width: 100%; height: 100%;
    display: flex; align-items: center; justify-content: center;
    font-size: 48rpx;
  }
}

.product-grid {
  flex: 1;
  min-width: 0;
}

.grid-row {
  display: flex;
  gap: 10rpx;
  margin-bottom: 8rpx;
}

.grid-cell {
  flex: 1;
  font-size: 22rpx;
  color: #303133;
  line-height: 1.5;
  word-break: break-all;

  &.highlight {
    background: #e8f4fd;
    padding: 4rpx 10rpx;
    border-radius: 8rpx;
    color: #1890ff;
  }

  .cell-label { color: #909399; }
}

.notice-cell {
  flex-direction: column;
  .notice-text { display: block; font-size: 22rpx; line-height: 1.5; color: #303133; }
}

.dot-green { color: #52c41a; }
.dot-red { color: #f5222d; }
.dot-orange { color: #fa8c16; }

.action-row {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;

  .action-btn {
    padding: 12rpx 28rpx;
    border-radius: 12rpx;
    font-size: 26rpx;
    font-weight: 500;

    &.blue { background: #409EFF; color: #fff; }
    &.orange { background: #fa8c16; color: #fff; }
    &:active { opacity: 0.8; }
  }
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;

  .data-title { font-size: 30rpx; font-weight: 700; color: #303133; }
  .data-sub { display: block; font-size: 22rpx; color: #909399; }
}

.data-actions {
  display: flex;
  gap: 10rpx;

  .small-btn {
    font-size: 22rpx;
    padding: 8rpx 16rpx;
    background: #6c5bb3;
    color: #fff;
    border-radius: 8rpx;
    &:active { opacity: 0.8; }
  }
}

.record-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  background: #f8f9fd;
  padding: 16rpx;
  border-radius: 12rpx;
}

.record-row {
  width: calc(50% - 6rpx);

  .record-label { font-size: 22rpx; color: #909399; display: block; }
  .record-value {
    font-size: 26rpx; font-weight: 500; color: #303133;
    &.no-data { color: #c0c4cc; }
  }
}

.input-area {
  text-align: center;
  margin-top: 20rpx;

  .input-btn {
    display: inline-block;
    background: #6c5bb3;
    color: #fff;
    font-size: 28rpx;
    padding: 14rpx 48rpx;
    border-radius: 12rpx;
    border: none;
    &::after { border: none; }
  }
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 90%;
  max-height: 80vh;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
  margin-bottom: 20rpx;
  color: #303133;
}

.modal-scroll {
  max-height: 60vh;
}

.form-row {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  .form-label {
    width: 160rpx;
    font-size: 26rpx;
    color: #303133;
    flex-shrink: 0;
  }

  .form-picker {
    flex: 1;
    font-size: 26rpx;
    color: #606266;
    background: #f5f7fa;
    padding: 14rpx 20rpx;
    border-radius: 10rpx;
  }

  .form-input {
    flex: 1;
    font-size: 26rpx;
    background: #f5f7fa;
    padding: 14rpx 20rpx;
    border-radius: 10rpx;
  }
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 24rpx;

  .modal-btn {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;
    &::after { border: none; }

    &.cancel { background: #f0f2f5; color: #606266; }
    &.confirm { background: #6c5bb3; color: #fff; }
  }
}

.file-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }
}

.file-icon {
  width: 68rpx;
  height: 68rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.file-ext {
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
}

.file-info {
  flex: 1;
  min-width: 0;
  .file-name {
    display: block;
    font-size: 26rpx;
    font-weight: 500;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .file-meta {
    display: block;
    font-size: 22rpx;
    color: #909399;
    margin-top: 4rpx;
  }
}

.file-download {
  padding: 8rpx 20rpx;
  background: #6c5bb3;
  color: #fff;
  border-radius: 8rpx;
  font-size: 22rpx;
  flex-shrink: 0;
  margin-left: 12rpx;
}

.empty-files {
  text-align: center;
  padding: 60rpx 0;
  color: #c0c4cc;
  font-size: 28rpx;
}
</style>
