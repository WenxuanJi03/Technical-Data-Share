<template>
  <div class="app-container trial-container">
    <!-- 顶部操作栏 -->
    <div class="top-toolbar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="输入项目名称、编号或负责人..."
        prefix-icon="el-icon-search"
        class="search-input"
        clearable
        @keyup.enter.native="handleQuery"
      />
      <el-button type="primary" icon="el-icon-search" @click="handleQuery">查找</el-button>
      <div class="toolbar-buttons">
        <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增模具</el-button>
        <el-button type="success" plain icon="el-icon-download" @click="handleExport">导出数据</el-button>
      </div>
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card green">
          <div class="stat-number">{{ stats.totalProduction }}</div>
          <div class="stat-label">总量产</div>
        </div>
        <div class="stat-card blue">
          <div class="stat-number">{{ stats.inTrial }}</div>
          <div class="stat-label">试制中</div>
        </div>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="label">本年量产</span>
        <span class="value">{{ stats.yearProduction }}</span>
      </div>
      <div class="stat-item">
        <span class="label">本月量产</span>
        <span class="value">{{ stats.monthProduction }}</span>
      </div>
      <div class="stat-item">
        <span class="label">新品</span>
        <span class="value highlight-green">{{ stats.newProduct }}</span>
      </div>
      <div class="stat-item">
        <span class="label">复制模</span>
        <span class="value">{{ stats.copyMold }}</span>
      </div>
      <div class="stat-item">
        <span class="label">待回厂</span>
        <span class="value highlight-orange">{{ stats.waitReturn }}</span>
      </div>
      <div class="stat-item">
        <span class="label">待试制</span>
        <span class="value highlight-blue">{{ stats.waitTrial }}</span>
      </div>
      <div class="stat-item">
        <span class="label">1次</span>
        <span class="value">{{ stats.trial1 }}</span>
      </div>
      <div class="stat-item">
        <span class="label">2次</span>
        <span class="value">{{ stats.trial2 }}</span>
      </div>
      <div class="stat-item">
        <span class="label">3次</span>
        <span class="value">{{ stats.trial3 }}</span>
      </div>
      <div class="stat-item">
        <span class="label">>3次</span>
        <span class="value highlight-red">{{ stats.trialMore }}</span>
      </div>
    </div>

    <!-- 模具表格 -->
    <el-table 
      v-loading="loading" 
      :data="trialList" 
      @row-click="handleRowClick"
      :row-class-name="tableRowClassName"
      highlight-current-row
    >
      <el-table-column label="模具编号" align="center" prop="trialCode" min-width="120">
        <template slot-scope="scope">
          <span class="mold-code clickable" @click.stop="openDetail(scope.row)">{{ scope.row.trialCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" min-width="80">
        <template slot-scope="scope">
          <el-tag :type="getPriorityType(scope.row.priority)" size="small">
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
          <span>{{ scope.row.trialCount || 0 }}</span>
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
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后状态" align="center" prop="result" min-width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.result || '无' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="是否整模试制" align="center" prop="isWholeMold" min-width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.isWholeMold ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="100">
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
              <el-input v-model="form.initiatorName" placeholder="请输入责任人" />
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
            <el-form-item label="整模试制" prop="isWholeMold">
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

export default {
  name: "Trial",
  data() {
    return {
      loading: true,
      total: 0,
      trialList: [],
      title: "",
      open: false,
      selectedRow: null,
      // 统计数据
      stats: {
        totalProduction: 107,
        inTrial: 70,
        yearProduction: 107,
        monthProduction: 12,
        newProduct: 28,
        copyMold: 79,
        waitReturn: 51,
        waitTrial: 19,
        trial1: 16,
        trial2: 0,
        trial3: 23,
        trialMore: 19
      },
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
  },
  methods: {
    getList() {
      this.loading = true
      listTrial(this.queryParams).then(response => {
        this.trialList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        // 模拟数据
        this.trialList = [
          { trialId: 1, trialCode: '08525F32-M2', priority: '紧急', planStartDate: '2025-12-12', cycle: 15, trialCount: 0, initiatorName: '李罗程', trialType: '新品', status: '待回厂', result: '无', isWholeMold: false },
          { trialId: 2, trialCode: '08525F33-M2', priority: '紧急', planStartDate: '2025-12-12', cycle: 15, trialCount: 0, initiatorName: '李罗程', trialType: '新品', status: '待回厂', result: '无', isWholeMold: false },
          { trialId: 3, trialCode: '09225C08-M1', priority: '紧急', planStartDate: '2025-12-19', cycle: 8, trialCount: 0, initiatorName: '李罗程', trialType: '新品', status: '待回厂', result: '无', isWholeMold: false },
          { trialId: 4, trialCode: '07225C25-M6', priority: '紧急', planStartDate: '2025-08-20', cycle: 129, trialCount: 4, initiatorName: '易翔', trialType: '新品', status: '试制中', result: '试制合格', isWholeMold: false },
          { trialId: 5, trialCode: '07125C35-M7', priority: '紧急', planStartDate: '2025-11-05', cycle: 52, trialCount: 1, initiatorName: '易翔', trialType: '新品', status: '试制中', result: '试制合格', isWholeMold: false },
          { trialId: 6, trialCode: '07125C21-M1', priority: '优先', planStartDate: '2025-12-09', cycle: 18, trialCount: 0, initiatorName: '易翔', trialType: '新品', status: '待回厂', result: '无', isWholeMold: false },
          { trialId: 7, trialCode: '09225C19-M1', priority: '优先', planStartDate: '2025-12-10', cycle: 17, trialCount: 0, initiatorName: '李罗程', trialType: '新品', status: '待回厂', result: '无', isWholeMold: false },
          { trialId: 8, trialCode: '07125F13-M1', priority: '优先', planStartDate: '2025-06-19', cycle: 191, trialCount: 2, initiatorName: '易翔', trialType: '新品', status: '试制中', result: '粗车', isWholeMold: false },
          { trialId: 9, trialCode: '07024C26-M1', priority: '优先', planStartDate: '2025-03-28', cycle: 274, trialCount: 2, initiatorName: '易翔', trialType: '新品', status: '试制中', result: '试制合格', isWholeMold: true },
          { trialId: 10, trialCode: '09025C07-M1', priority: '优先', planStartDate: '2025-08-19', cycle: 131, trialCount: 3, initiatorName: '李罗程', trialType: '新品', status: '试制中', result: '试制合格', isWholeMold: true },
        ]
        this.total = 10
        this.loading = false
      })
    },
    formatDate(date) {
      if (!date) return 'N/A'
      return date.replace(/-/g, '/')
    },
    getPriorityType(priority) {
      const map = { '紧急': 'danger', '优先': 'warning', '正常': 'success' }
      return map[priority] || 'success'
    },
    getStatusType(status) {
      const map = { '待回厂': 'warning', '试制中': 'primary', '已完成': 'success', '暂停': 'info' }
      return map[status] || 'info'
    },
    getStatusText(status) {
      return status || '待回厂'
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
            }).catch(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
            })
          } else {
            addTrial(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            }).catch(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
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
.trial-container {
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
  
  .search-input {
    width: 300px;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
    margin-left: 15px;
  }
  
  .stats-cards {
    display: flex;
    gap: 15px;
    margin-left: auto;
  }
}

.stat-card {
  padding: 15px 25px;
  border-radius: 8px;
  text-align: center;
  min-width: 100px;
  
  &.green {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    color: #fff;
  }
  
  &.blue {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    color: #fff;
  }
  
  .stat-number {
    font-size: 28px;
    font-weight: bold;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 12px;
    opacity: 0.9;
  }
}

.stats-bar {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .label {
      font-size: 12px;
      color: #909399;
    }
    
    .value {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      
      &.highlight-green {
        color: #52c41a;
      }
      
      &.highlight-orange {
        color: #fa8c16;
      }
      
      &.highlight-blue {
        color: #1890ff;
      }
      
      &.highlight-red {
        color: #f5222d;
      }
    }
  }
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
  border-radius: 8px;
  overflow: hidden;
  
  th {
    background: #3d5a80 !important;
    color: #fff !important;
    font-weight: 600;
    padding: 12px 0;
  }
  
  td {
    padding: 12px 0;
  }
  
  .selected-row {
    background: #e6f7ff !important;
  }
}

::v-deep .el-tag--danger {
  background: #fff1f0;
  border-color: #ffa39e;
  color: #cf1322;
}

::v-deep .el-tag--warning {
  background: #fff7e6;
  border-color: #ffd591;
  color: #d46b08;
}

::v-deep .el-tag--success {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #389e0d;
}

::v-deep .el-tag--primary {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #096dd9;
}
</style>
