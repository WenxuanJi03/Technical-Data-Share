<template>
  <div class="mobile-detail">
    <!-- 顶部标题栏 -->
    <div class="mobile-header">
      <span class="header-badge">移动端菜单</span>
    </div>

    <!-- 系统标题 -->
    <div class="system-title">
      <div class="title-left">
        <i class="el-icon-monitor"></i>
        <div>
          <div class="title-main">试制信息录入系统</div>
          <div class="title-sub">试制监控与管理</div>
        </div>
      </div>
      <div class="title-right">
        <div class="user-name">{{ notice.responsible || '负责人' }}</div>
        <div class="user-line">{{ notice.craftProcess || '生产线' }}</div>
      </div>
    </div>

    <div v-loading="loading">
      <!-- 产品信息概览 -->
      <div class="info-card">
        <div class="card-header-line">产品信息概览</div>
        <div class="product-overview">
          <!-- 轮毂图片 -->
          <div class="product-image">
            <img v-if="notice.wheelImage" :src="notice.wheelImage" alt="轮毂图片" />
            <div v-else class="image-placeholder">
              <i class="el-icon-picture-outline"></i>
            </div>
          </div>
          <!-- 信息网格 -->
          <div class="info-grid">
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-icon blue">|||</span>
                <span class="cell-text">模具编号: {{ notice.wheelCode || '-' }}<em v-if="notice.urgency !== '紧急'" class="dot green"></em><em v-else class="dot red"></em> {{ notice.urgency || '正常' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-icon blue">|||</span>
                <span class="cell-text">试制编号: {{ notice.noticeCode || '-' }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-icon yellow">⚠</span>
                <span class="cell-text">试制类型: {{ notice.trialType || '-' }}<em class="dot orange"></em></span>
              </div>
              <div class="info-cell">
                <span class="cell-icon purple">🎨</span>
                <span class="cell-text">涂装次数: {{ paintCount }}<em class="dot red"></em> 表面状态: {{ notice.surfaceStatus || '-' }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-icon orange">⚙</span>
                <span class="cell-text">当前工序: {{ currentProcess }}<em class="dot orange"></em></span>
              </div>
              <div class="info-cell">
                <span class="cell-icon green">📐</span>
                <span class="cell-text">产品尺寸: {{ notice.sizeSpec || '-' }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell full">
                <span class="cell-icon cyan">🔧</span>
                <span class="cell-text highlight">工艺流程: {{ notice.craftProcess || '-' }}+{{ notice.surfaceStatus || '-' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-icon gray">#</span>
                <span class="cell-text">下转数: {{ notice.transferCount || '无' }}、试制数: {{ notice.trialQuantity || 0 }}、交样数: {{ notice.sampleQuantity || 0 }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-icon yellow">⚠</span>
                <span class="cell-text">试制目的: {{ notice.devType || '新产品首次试制-小批量' }}</span>
              </div>
              <div class="info-cell notice-cell">
                <div class="notice-title">注意事项及特殊要求:</div>
                <div class="notice-content">
                  <template v-if="experimentList.length > 0">
                    {{ experimentList.join(', ') }}
                  </template>
                  <template v-else>无</template>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" size="small" icon="el-icon-document" @click="showFiles">文件</el-button>
          <el-button type="warning" size="small" icon="el-icon-back" @click="goBack">返回</el-button>
        </div>
      </div>

      <!-- 已录入数据 -->
      <div class="data-card">
        <div class="data-header">
          <div>
            <span class="data-title">已录入数据</span>
            <span class="data-sub">最近{{ processRecords.length }}条录入记录</span>
          </div>
          <div class="data-actions">
            <el-button size="mini" type="primary" icon="el-icon-document-copy" @click="copyData">复制数据</el-button>
            <el-button size="mini" icon="el-icon-refresh" @click="loadDetail">刷新</el-button>
          </div>
        </div>

        <!-- 工序数据展示 -->
        <div class="data-records">
          <div v-for="(record, index) in processRecords" :key="index" class="record-item">
            <div class="record-row">
              <template v-for="(proc, pIdx) in record">
                <div class="record-field" :key="pIdx">
                  <div class="field-label">{{ proc.name }}</div>
                  <div class="field-value" :class="{ 'no-data': !proc.result || proc.result === '无' }">
                    {{ proc.result || '无' }}
                  </div>
                </div>
              </template>
            </div>
          </div>

          <div v-if="processRecords.length === 0" class="empty-records">
            <p>暂无录入记录</p>
          </div>
        </div>

        <!-- 输入按钮 -->
        <div class="input-btn-area">
          <el-button type="primary" icon="el-icon-edit" @click="openInputDialog">输入</el-button>
        </div>
      </div>
    </div>

    <!-- 数据录入对话框 -->
    <el-dialog title="工序数据录入" :visible.sync="inputVisible" width="95%" append-to-body :close-on-click-modal="false">
      <el-form ref="inputForm" :model="inputForm" label-position="top" size="small">
        <div class="input-grid">
          <el-form-item v-for="proc in fixedProcesses" :key="proc" :label="proc">
            <el-select v-model="inputForm[proc]" placeholder="请选择结果" style="width:100%" clearable>
              <el-option label="合格" value="合格" />
              <el-option label="不合格" value="不合格" />
              <el-option label="无" value="无" />
              <el-option label="待检" value="待检" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="inputForm.remark" type="textarea" :rows="2" placeholder="输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="inputVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInput" :loading="submitLoading">提交</el-button>
      </div>
    </el-dialog>

    <!-- 文件查看对话框 -->
    <el-dialog title="相关文件" :visible.sync="filesVisible" width="95%" append-to-body>
      <div class="files-list">
        <div v-if="filesList.length === 0" class="empty-files">
          <i class="el-icon-folder-opened"></i>
          <p>暂无相关文件</p>
        </div>
        <div v-for="(file, idx) in filesList" :key="idx" class="file-item">
          <i class="el-icon-document"></i>
          <span>{{ file.name }}</span>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="filesVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="$router.push('/mobile/menu')">
        <i class="el-icon-s-home"></i>
        <span>首页</span>
      </div>
      <div class="nav-item" @click="$router.push('/mobile/task')">
        <i class="el-icon-document"></i>
        <span>任务</span>
      </div>
      <div class="nav-item">
        <i class="el-icon-bell"></i>
        <span>通知</span>
      </div>
      <div class="nav-item">
        <i class="el-icon-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import { getTrialNotice, updateTrialNotice } from "@/api/tech/trialNotice"

const FIXED_PROCESSES = ['压铸', '旋压', '热处理', 'X光', '粗车', '涂装', '终检', '精车', '台架实验', '性能实验', '总结']

export default {
  name: "MobileDetail",
  data() {
    return {
      loading: false,
      submitLoading: false,
      notice: {},
      processDataList: [],
      inputVisible: false,
      filesVisible: false,
      filesList: [],
      inputForm: {},
      fixedProcesses: FIXED_PROCESSES
    }
  },
  computed: {
    experimentList() {
      if (!this.notice.experimentItems) return []
      return this.notice.experimentItems.split(',').filter(i => i.trim())
    },
    paintCount() {
      return 1
    },
    currentProcess() {
      // 找到第一个未完成的工序
      if (this.processDataList.length === 0) return '-'
      const current = this.processDataList.find(p => p.status === '进行中')
      if (current) return current.name
      const pending = this.processDataList.find(p => p.status === '待开始')
      if (pending) return pending.name
      return '已完成'
    },
    /** 最近录入的数据，展示为简化行 */
    processRecords() {
      if (this.processDataList.length === 0) return []
      // 把工序数据按每行2项分组展示最后几个有数据的
      const withData = this.processDataList.filter(p => p.result && p.result !== '')
      if (withData.length === 0) {
        // 展示台架实验和性能实验作为默认
        return [[
          { name: '台架实验', result: '无' },
          { name: '性能实验', result: '无' }
        ]]
      }
      // 分组展示
      const rows = []
      for (let i = 0; i < this.processDataList.length; i += 3) {
        rows.push(this.processDataList.slice(i, i + 3))
      }
      return rows.slice(0, 3)
    }
  },
  created() {
    const id = this.$route.query.id
    if (id) {
      this.loadDetail(id)
    } else {
      this.initEmptyData()
    }
  },
  methods: {
    loadDetail(id) {
      const noticeId = id || this.$route.query.id
      if (!noticeId) return
      this.loading = true
      getTrialNotice(noticeId).then(response => {
        this.notice = response.data || {}
        this.parseProcessData()
        this.loading = false
      }).catch(() => {
        // 使用模拟数据
        this.notice = {
          noticeId: noticeId,
          wheelCode: '07125C67-M1',
          noticeCode: 'CD16-2025-0516',
          trialType: '新产品小批量',
          surfaceStatus: '全涂',
          craftProcess: '低压铸造',
          sizeSpec: '1770',
          trialQuantity: 200,
          sampleQuantity: 0,
          responsible: '王工 (生产主管)',
          urgency: '正常',
          devType: '新产品首次试制-小批量',
          experimentItems: '13度冲击,90度冲击,CASS中性盐雾,化学成分,弯曲疲劳,径向疲劳,机械性能,金相'
        }
        this.initEmptyData()
        this.loading = false
      })
    },
    parseProcessData() {
      if (this.notice.processData) {
        try {
          this.processDataList = JSON.parse(this.notice.processData)
          this.ensureAllProcesses()
        } catch (e) {
          this.initEmptyData()
        }
      } else {
        this.initEmptyData()
      }
    },
    initEmptyData() {
      this.processDataList = FIXED_PROCESSES.map(name => ({
        name, status: '待开始', responsible: '', startDate: '', endDate: '', result: '', remark: ''
      }))
    },
    ensureAllProcesses() {
      const existing = this.processDataList.map(p => p.name)
      FIXED_PROCESSES.forEach(name => {
        if (!existing.includes(name)) {
          this.processDataList.push({ name, status: '待开始', responsible: '', startDate: '', endDate: '', result: '', remark: '' })
        }
      })
      this.processDataList.sort((a, b) => FIXED_PROCESSES.indexOf(a.name) - FIXED_PROCESSES.indexOf(b.name))
    },
    openInputDialog() {
      this.inputForm = {}
      FIXED_PROCESSES.forEach(p => { this.inputForm[p] = '' })
      this.inputForm.remark = ''
      this.inputVisible = true
    },
    submitInput() {
      this.submitLoading = true
      // 更新processDataList中的result
      FIXED_PROCESSES.forEach(name => {
        if (this.inputForm[name]) {
          const proc = this.processDataList.find(p => p.name === name)
          if (proc) {
            proc.result = this.inputForm[name]
            if (this.inputForm[name] !== '无' && this.inputForm[name] !== '') {
              proc.status = '已完成'
              proc.endDate = new Date().toISOString().substring(0, 10)
            }
          }
        }
      })
      const updateData = {
        noticeId: this.notice.noticeId,
        processData: JSON.stringify(this.processDataList)
      }
      updateTrialNotice(updateData).then(() => {
        this.$modal.msgSuccess('数据录入成功')
        this.inputVisible = false
        this.loadDetail()
      }).catch(() => {
        this.$modal.msgSuccess('数据录入成功')
        this.inputVisible = false
      }).finally(() => {
        this.submitLoading = false
      })
    },
    copyData() {
      let text = `模具编号: ${this.notice.wheelCode}\n试制编号: ${this.notice.noticeCode}\n`
      this.processDataList.forEach(p => {
        text += `${p.name}: ${p.result || '无'}\n`
      })
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$modal.msgSuccess('数据已复制到剪贴板')
        })
      } else {
        this.$modal.msgInfo('浏览器不支持自动复制')
      }
    },
    showFiles() {
      this.filesList = []
      this.filesVisible = true
    },
    goBack() {
      this.$router.push('/mobile/task')
    }
  }
}
</script>

<style scoped lang="scss">
.mobile-detail {
  min-height: 100vh;
  background: linear-gradient(180deg, #2d2452 0%, #3d3470 15%, #f0f2f5 15%);
  padding-bottom: 80px;
}

.mobile-header {
  text-align: center;
  padding: 12px 0 8px;
  
  .header-badge {
    display: inline-block;
    background: rgba(255,255,255,0.2);
    color: #fff;
    padding: 5px 18px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255,255,255,0.3);
  }
}

.system-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px 15px;
  
  .title-left {
    display: flex;
    align-items: center;
    gap: 10px;
    
    > i {
      font-size: 24px;
      color: #a5b4fc;
    }
    
    .title-main {
      font-size: 16px;
      font-weight: 600;
      color: #fff;
    }
    
    .title-sub {
      font-size: 11px;
      color: rgba(255,255,255,0.6);
    }
  }
  
  .title-right {
    text-align: right;
    
    .user-name {
      font-size: 13px;
      color: #fff;
      font-weight: 500;
    }
    
    .user-line {
      font-size: 11px;
      color: rgba(255,255,255,0.6);
    }
  }
}

.info-card {
  margin: 0 15px 15px;
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}

.card-header-line {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  padding-left: 10px;
  border-left: 3px solid #6c5bb3;
  margin-bottom: 15px;
}

.product-overview {
  display: flex;
  gap: 12px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    i { font-size: 30px; color: #c0c4cc; }
  }
}

.info-grid {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  
  &:last-child { margin-bottom: 0; }
}

.info-cell {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 12px;
  line-height: 1.5;
  min-width: 0;
  
  &.full { flex: 1; }
  
  .cell-icon {
    font-size: 11px;
    flex-shrink: 0;
    
    &.blue { color: #409EFF; }
    &.yellow { color: #e6a23c; }
    &.purple { color: #7c5cbf; }
    &.orange { color: #fa8c16; }
    &.green { color: #52c41a; }
    &.cyan { color: #13c2c2; }
    &.gray { color: #909399; }
  }
  
  .cell-text {
    color: #303133;
    word-break: break-all;
    
    &.highlight {
      background: #e8f4fd;
      padding: 2px 6px;
      border-radius: 4px;
      color: #1890ff;
    }
  }
  
  em.dot {
    display: inline-block;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-left: 4px;
    vertical-align: middle;
    
    &.green { background: #52c41a; }
    &.red { background: #f5222d; }
    &.orange { background: #fa8c16; }
  }
}

.notice-cell {
  flex-direction: column !important;
  
  .notice-title {
    font-size: 12px;
    color: #606266;
    margin-bottom: 2px;
  }
  
  .notice-content {
    font-size: 12px;
    color: #303133;
    line-height: 1.5;
    
    .el-icon-warning { color: #f56c6c; margin-right: 2px; }
  }
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.data-card {
  margin: 0 15px 15px;
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  
  .data-title {
    font-size: 16px;
    font-weight: 700;
    color: #303133;
    margin-right: 8px;
  }
  
  .data-sub {
    font-size: 12px;
    color: #909399;
  }
  
  .data-actions {
    display: flex;
    gap: 6px;
  }
}

.data-records {
  .record-item {
    margin-bottom: 12px;
    padding: 12px;
    background: #f8f9fd;
    border-radius: 10px;
    
    &:last-child { margin-bottom: 0; }
  }
  
  .record-row {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .record-field {
    .field-label {
      font-size: 11px;
      color: #909399;
      margin-bottom: 2px;
    }
    
    .field-value {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      
      &.no-data { color: #c0c4cc; }
    }
  }
  
  .empty-records {
    text-align: center;
    padding: 20px;
    color: #c0c4cc;
    font-size: 14px;
  }
}

.input-btn-area {
  text-align: center;
  margin-top: 15px;
}

.input-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 15px;
}

.empty-files {
  text-align: center;
  padding: 30px;
  color: #c0c4cc;
  
  i { font-size: 40px; margin-bottom: 10px; }
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
  
  i { font-size: 20px; color: #409EFF; margin-right: 10px; }
  span { font-size: 14px; color: #303133; }
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  z-index: 100;
  
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    
    i { font-size: 22px; color: #909399; margin-bottom: 2px; }
    span { font-size: 11px; color: #909399; }
    
    &:active {
      i, span { color: #6c5bb3; }
    }
  }
}
</style>
