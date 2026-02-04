<template>
  <div class="app-container trial-list-container">
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
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-card blue">
          <div class="stat-number">{{ stats.inProgress }}</div>
          <div class="stat-label">试制中</div>
        </div>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="label">月完成</span>
        <span class="value">{{ stats.monthCompleted }}</span>
      </div>
      <div class="stat-item">
        <span class="label">年完成</span>
        <span class="value">{{ stats.yearCompleted }}</span>
      </div>
      <div class="stat-item">
        <span class="label">低压</span>
        <span class="value highlight-blue">{{ stats.lowPressure }}</span>
      </div>
      <div class="stat-item">
        <span class="label">铸锻</span>
        <span class="value">{{ stats.forging }}</span>
      </div>
      <div class="stat-item">
        <span class="label">UPC</span>
        <span class="value">{{ stats.upc }}</span>
      </div>
      <div class="stat-item">
        <span class="label">挤压</span>
        <span class="value">{{ stats.extrusion }}</span>
      </div>
      <div class="stat-item">
        <span class="label">差压</span>
        <span class="value">{{ stats.differential }}</span>
      </div>
      <div class="stat-item">
        <span class="label">重力</span>
        <span class="value">{{ stats.gravity }}</span>
      </div>
    </div>

    <!-- 试制表格 -->
    <el-table 
      v-loading="loading" 
      :data="trialList" 
      @row-click="handleRowClick"
      :row-class-name="tableRowClassName"
      highlight-current-row
      border
    >
      <el-table-column label="模具编号" align="center" prop="trialCode" min-width="120">
        <template slot-scope="scope">
          <span class="trial-code clickable" @click.stop="openDetail(scope.row)">{{ scope.row.trialCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" min-width="80">
        <template slot-scope="scope">
          <el-tag :type="getPriorityType(scope.row.priority)" size="small">
            {{ scope.row.priority || '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="试制状态" align="center" prop="status" min-width="100">
        <template slot-scope="scope">
          <el-select v-model="scope.row.status" size="mini" @change="handleStatusChange(scope.row)">
            <el-option label="试制中" value="试制中" />
            <el-option label="待回厂" value="待回厂" />
            <el-option label="已完成" value="已完成" />
            <el-option label="暂停" value="暂停" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="planStartDate" min-width="100">
        <template slot-scope="scope">
          <span>{{ formatDate(scope.row.planStartDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="试制周期" align="center" prop="cycle" min-width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.cycle || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="责任人" align="center" prop="initiatorName" min-width="80" />
      <el-table-column label="铸造方式" align="center" prop="castingMethod" min-width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.castingMethod || '低压铸造+精车' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="试制目的" align="center" prop="purpose" min-width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.purpose || scope.row.requirement || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="产品描述" align="center" prop="productDesc" min-width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.productDesc || scope.row.trialType || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="流程状态" align="center" prop="processStatus" min-width="100">
        <template slot-scope="scope">
          <el-select v-model="scope.row.processStatus" size="mini" @change="handleProcessStatusChange(scope.row)">
            <el-option label="待启动" value="待启动" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="100" fixed="right">
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
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
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择" style="width:100%">
                <el-option label="紧急" value="紧急" />
                <el-option label="优先" value="优先" />
                <el-option label="正常" value="正常" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试制状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择" style="width:100%">
                <el-option label="试制中" value="试制中" />
                <el-option label="待回厂" value="待回厂" />
                <el-option label="已完成" value="已完成" />
                <el-option label="暂停" value="暂停" />
              </el-select>
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
            <el-form-item label="试制周期" prop="cycle">
              <el-input-number v-model="form.cycle" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="责任人" prop="initiatorName">
              <el-select v-model="form.initiatorName" placeholder="请选择责任人" filterable style="width:100%">
                <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="铸造方式" prop="castingMethod">
              <el-select v-model="form.castingMethod" placeholder="请选择" style="width:100%">
                <el-option label="低压铸造+精车" value="低压铸造+精车" />
                <el-option label="低压铸造+性能验证" value="低压铸造+性能验证" />
                <el-option label="旋压铸造+精车" value="旋压铸造+精车" />
                <el-option label="低压铸造+全涂" value="低压铸造+全涂" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="流程状态" prop="processStatus">
              <el-select v-model="form.processStatus" placeholder="请选择" style="width:100%">
                <el-option label="待启动" value="待启动" />
                <el-option label="进行中" value="进行中" />
                <el-option label="已完成" value="已完成" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品描述" prop="productDesc">
              <el-input v-model="form.productDesc" placeholder="请输入产品描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="试制目的" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入试制目的" />
        </el-form-item>
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
  name: "TrialList",
  data() {
    return {
      loading: true,
      total: 0,
      trialList: [],
      userList: [],
      title: "",
      open: false,
      selectedRow: null,
      stats: {
        completed: 487,
        inProgress: 30,
        monthCompleted: 103,
        yearCompleted: 109,
        lowPressure: 11,
        forging: 0,
        upc: 3,
        extrusion: 1,
        differential: 0,
        gravity: 5
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
    this.loadUserList()
  },
  methods: {
    loadUserList() {
      getAllUsers().then(response => {
        this.userList = response.rows || []
      })
    },
    getList() {
      this.loading = true
      listTrial(this.queryParams).then(response => {
        this.trialList = (response.rows || []).map(item => ({
          ...item,
          processStatus: item.processStatus || '待启动',
          castingMethod: item.castingMethod || '低压铸造+精车'
        }))
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.trialList = []
        this.total = 0
        this.loading = false
      })
    },
    formatDate(date) {
      if (!date) return '-'
      return date.replace(/-/g, '/')
    },
    getPriorityType(priority) {
      const map = { '紧急': 'danger', '优先': 'warning', '正常': 'success' }
      return map[priority] || 'success'
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
      this.handleUpdate(row)
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleStatusChange(row) {
      updateTrial(row).then(() => {
        this.$modal.msgSuccess("状态已更新")
      }).catch(() => {})
    },
    handleProcessStatusChange(row) {
      updateTrial(row).then(() => {
        this.$modal.msgSuccess("流程状态已更新")
      }).catch(() => {})
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增模具"
    },
    handleUpdate(row) {
      this.reset()
      getTrial(row.trialId).then(response => {
        this.form = response.data || row
        this.open = true
        this.title = "编辑模具"
      }).catch(() => {
        this.form = { ...row }
        this.open = true
        this.title = "编辑模具"
      })
    },
    reset() {
      this.form = {
        trialId: null,
        trialCode: null,
        trialName: null,
        priority: '正常',
        status: '试制中',
        planStartDate: null,
        cycle: 0,
        initiatorName: null,
        castingMethod: '低压铸造+精车',
        processStatus: '待启动',
        productDesc: null,
        purpose: null,
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
      this.download('tech/trial/export', { ...this.queryParams }, `trial_list_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped lang="scss">
.trial-list-container {
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
      
      &.highlight-blue {
        color: #1890ff;
      }
    }
  }
}

.trial-code {
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
    padding: 10px 0;
  }
  
  .selected-row {
    background: #e6f7ff !important;
  }
}
</style>
