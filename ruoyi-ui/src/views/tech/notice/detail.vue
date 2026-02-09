<template>
  <div class="app-container notice-detail">
    <div v-loading="loading">
      <!-- 顶部操作栏 -->
      <div class="top-toolbar">
        <span class="page-title"><i class="el-icon-document"></i> 产品试制通知单详情</span>
        <div class="toolbar-actions">
          <el-button type="primary" size="small" icon="el-icon-edit" @click="editMode = !editMode">
            {{ editMode ? '取消编辑' : '编辑工序' }}
          </el-button>
          <el-button v-if="editMode" type="success" size="small" icon="el-icon-check" @click="saveProcessData">保存</el-button>
          <el-button size="small" icon="el-icon-printer" @click="handlePrint">打印</el-button>
          <el-button size="small" icon="el-icon-back" @click="goBack">返回</el-button>
        </div>
      </div>

      <!-- 通知单基本信息 -->
      <div class="info-card">
        <div class="card-title">基本信息</div>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="通知单编号">{{ notice.noticeCode }}</el-descriptions-item>
          <el-descriptions-item label="轮型号">{{ notice.wheelCode }}</el-descriptions-item>
          <el-descriptions-item label="试制状态">
            <el-tag :type="getTrialStatusType(notice.trialStatus)" size="small">{{ notice.trialStatus || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="试制类型">{{ notice.trialType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开发类型">{{ notice.devType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ notice.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="表面状态">{{ notice.surfaceStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">
            <span class="responsible-name">{{ notice.responsible || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="尺寸规格">{{ notice.sizeSpec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="试制数量">{{ notice.trialQuantity || 0 }}</el-descriptions-item>
          <el-descriptions-item label="计划交样数量">{{ notice.sampleQuantity || 0 }}</el-descriptions-item>
          <el-descriptions-item label="工艺流程">
            <el-tag type="warning" size="small">{{ notice.craftProcess || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="试制开始时间">{{ formatDate(notice.trialStart) }}</el-descriptions-item>
          <el-descriptions-item label="送样时间">{{ formatDate(notice.sampleDate) }}</el-descriptions-item>
          <el-descriptions-item label="通知单状态">
            <el-tag :type="getStatusType(notice.status)" size="small" effect="dark">{{ notice.status || '待启动' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(notice.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <!-- 实验项目 -->
        <div class="experiment-section" v-if="notice.experimentItems">
          <span class="experiment-label">实验项目：</span>
          <el-tag v-for="item in experimentList" :key="item" size="small" type="info" style="margin-right:6px;margin-bottom:4px">
            {{ item }}
          </el-tag>
        </div>
        <!-- 备注 -->
        <div class="remark-section" v-if="notice.remark">
          <span class="remark-label">备注：</span>
          <span>{{ notice.remark }}</span>
        </div>
      </div>

      <!-- 工序详情表格 -->
      <div class="process-card">
        <div class="card-title">
          <i class="el-icon-s-operation"></i> 工序跟踪表
          <span class="process-hint">（固定工序：压铸 > 旋压 > 热处理 > X光 > 粗车 > 涂装 > 终检 > 精车 > 台架实验 > 性能实验 > 总结）</span>
        </div>
        <el-table :data="processDataList" border size="small" class="process-table" :row-class-name="processRowClass">
          <el-table-column label="序号" align="center" width="60" type="index" :index="indexMethod" />
          <el-table-column label="工序名称" align="center" prop="name" min-width="100">
            <template slot-scope="scope">
              <span class="process-name">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" min-width="100">
            <template slot-scope="scope">
              <el-select v-if="editMode" v-model="scope.row.status" size="mini" style="width:100%">
                <el-option label="待开始" value="待开始" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
                <el-option label="异常" value="异常" />
              </el-select>
              <el-tag v-else :type="getProcessStatusType(scope.row.status)" size="small">
                {{ scope.row.status || '待开始' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="负责人" align="center" prop="responsible" min-width="100">
            <template slot-scope="scope">
              <el-select v-if="editMode" v-model="scope.row.responsible" size="mini" filterable clearable placeholder="选择" style="width:100%">
                <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
              </el-select>
              <span v-else>{{ scope.row.responsible || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" align="center" prop="startDate" min-width="130">
            <template slot-scope="scope">
              <el-date-picker v-if="editMode" v-model="scope.row.startDate" type="date" value-format="yyyy-MM-dd" size="mini" style="width:100%" placeholder="选择" />
              <span v-else>{{ scope.row.startDate || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="完成日期" align="center" prop="endDate" min-width="130">
            <template slot-scope="scope">
              <el-date-picker v-if="editMode" v-model="scope.row.endDate" type="date" value-format="yyyy-MM-dd" size="mini" style="width:100%" placeholder="选择" />
              <span v-else>{{ scope.row.endDate || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结果" align="center" prop="result" min-width="100">
            <template slot-scope="scope">
              <el-select v-if="editMode" v-model="scope.row.result" size="mini" clearable style="width:100%">
                <el-option label="合格" value="合格" />
                <el-option label="不合格" value="不合格" />
                <el-option label="待检" value="待检" />
              </el-select>
              <span v-else :class="{'text-success': scope.row.result === '合格', 'text-danger': scope.row.result === '不合格'}">
                {{ scope.row.result || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center" prop="remark" min-width="150">
            <template slot-scope="scope">
              <el-input v-if="editMode" v-model="scope.row.remark" size="mini" placeholder="备注" />
              <span v-else>{{ scope.row.remark || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { getTrialNotice, updateTrialNotice } from "@/api/tech/trialNotice"
import { getAllUsers } from "@/api/tech/common"

// 固定工序列表
const FIXED_PROCESSES = ['压铸', '旋压', '热处理', 'X光', '粗车', '涂装', '终检', '精车', '台架实验', '性能实验', '总结']

export default {
  name: "TrialNoticeDetail",
  data() {
    return {
      loading: false,
      editMode: false,
      notice: {},
      processDataList: [],
      userList: [],
      noticeId: null
    }
  },
  computed: {
    experimentList() {
      if (!this.notice.experimentItems) return []
      return this.notice.experimentItems.split(',').filter(i => i.trim())
    }
  },
  created() {
    this.noticeId = this.$route.query.id
    if (this.noticeId) {
      this.loadDetail()
    }
    this.loadUserList()
  },
  methods: {
    loadUserList() {
      getAllUsers().then(response => {
        this.userList = response.rows || []
      })
    },
    loadDetail() {
      this.loading = true
      getTrialNotice(this.noticeId).then(response => {
        this.notice = response.data || {}
        this.parseProcessData()
        this.loading = false
      }).catch(() => {
        this.loading = false
        // 如果接口失败，初始化空的工序数据
        this.initEmptyProcessData()
      })
    },
    /** 解析工序数据JSON */
    parseProcessData() {
      if (this.notice.processData) {
        try {
          this.processDataList = JSON.parse(this.notice.processData)
          // 确保所有固定工序都存在
          this.ensureAllProcesses()
        } catch (e) {
          this.initEmptyProcessData()
        }
      } else {
        this.initEmptyProcessData()
      }
    },
    /** 初始化空工序数据 */
    initEmptyProcessData() {
      this.processDataList = FIXED_PROCESSES.map(name => ({
        name: name,
        status: '待开始',
        responsible: '',
        startDate: '',
        endDate: '',
        result: '',
        remark: ''
      }))
    },
    /** 确保所有固定工序都存在 */
    ensureAllProcesses() {
      const existingNames = this.processDataList.map(p => p.name)
      FIXED_PROCESSES.forEach(name => {
        if (!existingNames.includes(name)) {
          this.processDataList.push({
            name: name,
            status: '待开始',
            responsible: '',
            startDate: '',
            endDate: '',
            result: '',
            remark: ''
          })
        }
      })
      // 按固定顺序排序
      this.processDataList.sort((a, b) => {
        return FIXED_PROCESSES.indexOf(a.name) - FIXED_PROCESSES.indexOf(b.name)
      })
    },
    /** 保存工序数据 */
    saveProcessData() {
      const updateData = {
        noticeId: this.noticeId,
        processData: JSON.stringify(this.processDataList)
      }
      updateTrialNotice(updateData).then(() => {
        this.$modal.msgSuccess('工序数据保存成功')
        this.editMode = false
        this.loadDetail()
      })
    },
    formatDate(date) {
      if (!date) return '-'
      if (typeof date === 'string') return date.substring(0, 10)
      return '-'
    },
    getTrialStatusType(status) {
      const map = { '首次试制': 'primary', '重复试制': 'warning', '量产': 'success' }
      return map[status] || 'info'
    },
    getStatusType(status) {
      const map = { '待启动': 'info', '进行中': 'primary', '已完成': 'success', '已取消': 'danger' }
      return map[status] || 'info'
    },
    getProcessStatusType(status) {
      const map = { '待开始': 'info', '进行中': '', '已完成': 'success', '异常': 'danger' }
      return map[status] !== undefined ? map[status] : 'info'
    },
    processRowClass({ row }) {
      if (row.status === '已完成') return 'row-completed'
      if (row.status === '进行中') return 'row-active'
      if (row.status === '异常') return 'row-error'
      return ''
    },
    indexMethod(index) {
      return index + 1
    },
    goBack() {
      this.$router.push('/trial/notice')
    },
    handlePrint() {
      window.print()
    }
  }
}
</script>

<style scoped lang="scss">
.notice-detail {
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
  padding: 15px;
}

.top-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    
    i {
      margin-right: 8px;
      color: #409EFF;
    }
  }
  
  .toolbar-actions {
    display: flex;
    gap: 8px;
  }
}

.info-card,
.process-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  
  i {
    margin-right: 6px;
    color: #409EFF;
  }
  
  .process-hint {
    font-size: 12px;
    font-weight: normal;
    color: #909399;
    margin-left: 10px;
  }
}

.responsible-name {
  color: #409EFF;
  font-weight: 500;
}

.experiment-section {
  margin-top: 15px;
  padding-top: 10px;
  
  .experiment-label {
    font-weight: 600;
    color: #606266;
    margin-right: 10px;
  }
}

.remark-section {
  margin-top: 10px;
  color: #606266;
  
  .remark-label {
    font-weight: 600;
    margin-right: 10px;
  }
}

.process-name {
  font-weight: 600;
  color: #303133;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.process-table {
  ::v-deep th {
    background: #3d5a80 !important;
    color: #fff !important;
    font-weight: 600;
    padding: 10px 0;
  }
  
  ::v-deep .row-completed {
    background: #f0f9eb !important;
  }
  
  ::v-deep .row-active {
    background: #ecf5ff !important;
  }
  
  ::v-deep .row-error {
    background: #fef0f0 !important;
  }
}

/* 打印样式 */
@media print {
  .top-toolbar .toolbar-actions {
    display: none !important;
  }
  
  .notice-detail {
    background: #fff !important;
    padding: 0 !important;
  }
  
  .info-card,
  .process-card {
    box-shadow: none !important;
    border: 1px solid #eee;
  }
}
</style>
