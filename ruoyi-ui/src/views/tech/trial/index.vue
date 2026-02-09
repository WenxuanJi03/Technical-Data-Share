<template>
  <div class="app-container trial-dashboard">
    <!-- 顶部操作栏 -->
    <div class="top-toolbar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="输入轮型号、负责人..."
        prefix-icon="el-icon-search"
        class="search-input"
        clearable
        @keyup.enter.native="handleQuery"
      />
      <el-button type="primary" icon="el-icon-search" @click="handleQuery">查找</el-button>
      <div class="toolbar-buttons">
        <el-button type="warning" plain icon="el-icon-document" @click="goNoticeList">试制通知单</el-button>
        <el-button type="primary" plain icon="el-icon-setting" @click="goProcess">试制流程</el-button>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-area">
      <div class="stat-card completed">
        <div class="stat-icon"><i class="el-icon-circle-check"></i></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card in-trial">
        <div class="stat-icon"><i class="el-icon-loading"></i></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.inTrial }}</div>
          <div class="stat-label">试制中</div>
        </div>
      </div>
    </div>

    <!-- 工序统计表格 - 可手动编辑 -->
    <div class="process-stats-card">
      <div class="card-header">
        <span class="card-title"><i class="el-icon-s-data"></i> 工序统计数据</span>
        <div class="card-actions">
          <el-button v-if="!editingStats" size="small" type="primary" plain icon="el-icon-edit" @click="editingStats = true">编辑</el-button>
          <template v-else>
            <el-button size="small" type="success" icon="el-icon-check" @click="saveStats">保存</el-button>
            <el-button size="small" icon="el-icon-close" @click="cancelEditStats">取消</el-button>
          </template>
        </div>
      </div>
      <el-table :data="processStatsData" border size="small" class="stats-table">
        <el-table-column label="工序" align="center" prop="name" min-width="100">
          <template slot-scope="scope">
            <span class="process-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="计划数" align="center" prop="planned" min-width="80">
          <template slot-scope="scope">
            <el-input-number v-if="editingStats" v-model="scope.row.planned" :min="0" size="mini" controls-position="right" style="width:90px" />
            <span v-else>{{ scope.row.planned }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成数" align="center" prop="finished" min-width="80">
          <template slot-scope="scope">
            <el-input-number v-if="editingStats" v-model="scope.row.finished" :min="0" size="mini" controls-position="right" style="width:90px" />
            <span v-else :class="{'text-success': scope.row.finished >= scope.row.planned && scope.row.planned > 0}">{{ scope.row.finished }}</span>
          </template>
        </el-table-column>
        <el-table-column label="合格数" align="center" prop="qualified" min-width="80">
          <template slot-scope="scope">
            <el-input-number v-if="editingStats" v-model="scope.row.qualified" :min="0" size="mini" controls-position="right" style="width:90px" />
            <span v-else>{{ scope.row.qualified }}</span>
          </template>
        </el-table-column>
        <el-table-column label="不合格数" align="center" prop="unqualified" min-width="80">
          <template slot-scope="scope">
            <el-input-number v-if="editingStats" v-model="scope.row.unqualified" :min="0" size="mini" controls-position="right" style="width:90px" />
            <span v-else :class="{'text-danger': scope.row.unqualified > 0}">{{ scope.row.unqualified }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成率" align="center" min-width="100">
          <template slot-scope="scope">
            <el-progress 
              :percentage="calcPercent(scope.row.finished, scope.row.planned)" 
              :stroke-width="14"
              :text-inside="true"
              :color="getProgressColor(calcPercent(scope.row.finished, scope.row.planned))"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 试制列表表格 -->
    <div class="table-card">
      <div class="card-header">
        <span class="card-title"><i class="el-icon-s-order"></i> 试制模具列表</span>
        <div class="card-actions">
          <el-button type="primary" plain size="small" icon="el-icon-plus" @click="handleAdd">新增模具</el-button>
          <el-button type="success" plain size="small" icon="el-icon-download" @click="handleExport">导出</el-button>
        </div>
      </div>
      <el-table 
        v-loading="loading" 
        :data="trialList" 
        @row-click="handleRowClick"
        :row-class-name="tableRowClassName"
        highlight-current-row
        border
        size="small"
      >
        <el-table-column label="模具编号" align="center" prop="trialCode" min-width="120">
          <template slot-scope="scope">
            <span class="mold-code clickable" @click.stop="openDetail(scope.row)">{{ scope.row.trialCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" align="center" prop="priority" min-width="80">
          <template slot-scope="scope">
            <el-tag :type="getPriorityType(scope.row.priority)" size="small" effect="dark">
              {{ scope.row.priority || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" align="center" prop="planStartDate" min-width="100">
          <template slot-scope="scope">
            <span>{{ formatDate(scope.row.planStartDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="周期" align="center" prop="cycle" min-width="60">
          <template slot-scope="scope">
            <span>{{ scope.row.cycle || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="试制次数" align="center" prop="trialCount" min-width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.trialCount > 3" type="danger" size="mini">{{ scope.row.trialCount }}次</el-tag>
            <span v-else>{{ scope.row.trialCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="责任人" align="center" prop="initiatorName" min-width="80" />
        <el-table-column label="类型" align="center" prop="trialType" min-width="70">
          <template slot-scope="scope">
            <span>{{ scope.row.trialType || '新品' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" align="center" prop="status" min-width="90">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status || '待回厂' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后状态" align="center" prop="result" min-width="90">
          <template slot-scope="scope">
            <span>{{ scope.row.result || '无' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" min-width="120" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click.stop="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#f56c6c" @click.stop="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <!-- 添加或修改模具对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模具编号" prop="trialCode">
              <el-input v-model="form.trialCode" placeholder="请输入模具编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模具名称" prop="trialName">
              <el-input v-model="form.trialName" placeholder="请输入模具名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入关联项目" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择优先级" style="width:100%">
                <el-option label="紧急" value="紧急" />
                <el-option label="优先" value="优先" />
                <el-option label="正常" value="正常" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="trialType">
              <el-select v-model="form.trialType" placeholder="请选择类型" style="width:100%">
                <el-option label="新品" value="新品" />
                <el-option label="复制模" value="复制模" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任人" prop="initiatorName">
              <el-select v-model="form.initiatorName" placeholder="请选择责任人" filterable style="width:100%">
                <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周期(天)" prop="cycle">
              <el-input-number v-model="form.cycle" :min="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试制次数" prop="trialCount">
              <el-input-number v-model="form.trialCount" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="planStartDate">
              <el-date-picker v-model="form.planStartDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
                <el-option label="待回厂" value="待回厂" />
                <el-option label="试制中" value="试制中" />
                <el-option label="已完成" value="已完成" />
                <el-option label="暂停" value="暂停" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最后状态" prop="result">
              <el-select v-model="form.result" placeholder="请选择" style="width:100%">
                <el-option label="无" value="无" />
                <el-option label="试制合格" value="试制合格" />
                <el-option label="粗车" value="粗车" />
                <el-option label="涂装" value="涂装" />
                <el-option label="试制不合格" value="试制不合格" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="继续试制" prop="isWholeMold">
              <el-switch v-model="form.isWholeMold" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTrial, getTrial, delTrial, addTrial, updateTrial } from "@/api/tech/trial"
import { getAllUsers } from "@/api/tech/common"

export default {
  name: "Trial",
  data() {
    return {
      loading: true,
      total: 0,
      trialList: [],
      userList: [],
      title: "",
      open: false,
      selectedRow: null,
      editingStats: false,
      // 顶部统计
      stats: {
        completed: 487,
        inTrial: 30
      },
      // 工序统计数据（可手动编辑）
      processStatsData: [
        { name: '压铸', planned: 50, finished: 48, qualified: 46, unqualified: 2 },
        { name: '旋压', planned: 30, finished: 28, qualified: 27, unqualified: 1 },
        { name: '热处理', planned: 45, finished: 42, qualified: 40, unqualified: 2 },
        { name: 'X光', planned: 40, finished: 38, qualified: 37, unqualified: 1 },
        { name: '粗车', planned: 38, finished: 35, qualified: 34, unqualified: 1 },
        { name: '涂装', planned: 35, finished: 30, qualified: 29, unqualified: 1 },
        { name: '终检', planned: 30, finished: 25, qualified: 24, unqualified: 1 },
        { name: '精车', planned: 25, finished: 22, qualified: 21, unqualified: 1 },
        { name: '台架实验', planned: 20, finished: 15, qualified: 14, unqualified: 1 },
        { name: '性能实验', planned: 15, finished: 10, qualified: 9, unqualified: 1 },
        { name: '总结', planned: 10, finished: 8, qualified: 8, unqualified: 0 }
      ],
      processStatsBackup: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: null
      },
      form: {},
      rules: {
        trialCode: [{ required: true, message: "模具编号不能为空", trigger: "blur" }],
        trialName: [{ required: true, message: "模具名称不能为空", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
    this.loadUserList()
    this.loadProcessStats()
  },
  methods: {
    loadUserList() {
      getAllUsers().then(response => {
        this.userList = response.rows || []
      })
    },
    loadProcessStats() {
      // 从localStorage读取已保存的工序统计数据
      const saved = localStorage.getItem('trial_process_stats')
      if (saved) {
        try {
          this.processStatsData = JSON.parse(saved)
        } catch (e) { /* ignore */ }
      }
    },
    saveStats() {
      localStorage.setItem('trial_process_stats', JSON.stringify(this.processStatsData))
      this.editingStats = false
      this.$modal.msgSuccess('统计数据已保存')
    },
    cancelEditStats() {
      this.loadProcessStats()
      this.editingStats = false
    },
    calcPercent(finished, planned) {
      if (!planned || planned === 0) return 0
      return Math.min(100, Math.round((finished / planned) * 100))
    },
    getProgressColor(percent) {
      if (percent >= 80) return '#67c23a'
      if (percent >= 50) return '#e6a23c'
      return '#f56c6c'
    },
    getList() {
      this.loading = true
      listTrial(this.queryParams).then(response => {
        this.trialList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.trialList = []
        this.total = 0
        this.loading = false
      })
    },
    formatDate(date) {
      if (!date) return 'N/A'
      return date.substring(0, 10)
    },
    getPriorityType(priority) {
      const map = { '紧急': 'danger', '优先': 'warning', '正常': 'success' }
      return map[priority] || 'success'
    },
    getStatusType(status) {
      const map = { '待回厂': 'warning', '试制中': 'primary', '已完成': 'success', '暂停': 'info' }
      return map[status] || 'info'
    },
    tableRowClassName({ row }) {
      if (this.selectedRow && this.selectedRow.trialId === row.trialId) {
        return 'selected-row'
      }
      return ''
    },
    handleRowClick(row) {
      this.selectedRow = row
    },
    openDetail(row) {
      this.$modal.msgInfo(`查看模具详情: ${row.trialCode}`)
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    goNoticeList() {
      this.$router.push('/trial/notice')
    },
    goProcess() {
      this.$router.push('/trial/process')
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增模具"
    },
    handleUpdate(row) {
      this.reset()
      this.form = { ...row }
      this.open = true
      this.title = "编辑模具"
    },
    reset() {
      this.form = {
        trialId: null,
        trialCode: null,
        trialName: null,
        projectName: null,
        priority: '正常',
        trialType: '新品',
        initiatorName: null,
        cycle: 15,
        trialCount: 0,
        planStartDate: null,
        status: '待回厂',
        result: '无',
        isWholeMold: false,
        remark: null
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.trialId != null) {
            updateTrial(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTrial(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除模具编号为"' + row.trialCode + '"的数据项？').then(() => {
        return delTrial(row.trialId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('tech/trial/export', { ...this.queryParams }, `trial_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped lang="scss">
.trial-dashboard {
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
  padding: 15px;
}

.top-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  gap: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  
  .search-input {
    width: 280px;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
    margin-left: auto;
  }
}

/* 统计卡片区域 */
.stats-area {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 25px 30px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-right: 20px;
  }
  
  .stat-number {
    font-size: 36px;
    font-weight: bold;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 14px;
    margin-top: 4px;
  }
  
  &.completed {
    background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
    color: #fff;
    
    .stat-icon {
      background: rgba(255,255,255,0.2);
    }
  }
  
  &.in-trial {
    background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
    color: #fff;
    
    .stat-icon {
      background: rgba(255,255,255,0.2);
    }
  }
}

/* 卡片通用样式 */
.process-stats-card,
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    
    i {
      margin-right: 6px;
      color: #409EFF;
    }
  }
  
  .card-actions {
    display: flex;
    gap: 8px;
  }
}

.process-name {
  font-weight: 500;
}

.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.mold-code {
  font-weight: 500;
  color: #303133;
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      color: #409EFF;
    }
  }
}

::v-deep .el-table {
  border-radius: 6px;
  overflow: hidden;
  
  th {
    background: #3d5a80 !important;
    color: #fff !important;
    font-weight: 600;
    padding: 10px 0;
  }
  
  td {
    padding: 8px 0;
  }
  
  .selected-row {
    background: #e6f7ff !important;
  }
}

.stats-table {
  ::v-deep th {
    background: #f5f7fa !important;
    color: #303133 !important;
  }
}
</style>
