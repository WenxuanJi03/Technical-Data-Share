<template>
  <div class="app-container notice-container">
    <!-- 顶部操作栏 -->
    <div class="top-toolbar">
      <el-input
        v-model="queryParams.wheelCode"
        placeholder="输入轮型号搜索..."
        prefix-icon="el-icon-search"
        class="search-input"
        clearable
        @keyup.enter.native="handleQuery"
      />
      <el-select v-model="queryParams.status" placeholder="状态筛选" clearable style="width:120px" @change="handleQuery">
        <el-option label="待启动" value="待启动" />
        <el-option label="进行中" value="进行中" />
        <el-option label="已完成" value="已完成" />
        <el-option label="已取消" value="已取消" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="handleQuery">查找</el-button>
      <div class="toolbar-buttons">
        <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增通知单</el-button>
        <el-button type="success" plain icon="el-icon-download" @click="handleExport">导出</el-button>
      </div>
      <el-button type="info" plain icon="el-icon-back" @click="$router.push('/trial/task')">返回试制</el-button>
    </div>

    <!-- 通知单列表 -->
    <el-table 
      v-loading="loading" 
      :data="noticeList" 
      border
      highlight-current-row
      @row-click="handleRowClick"
    >
      <el-table-column label="通知单编号" align="center" prop="noticeCode" min-width="130">
        <template slot-scope="scope">
          <span class="notice-code clickable" @click.stop="openDetail(scope.row)">{{ scope.row.noticeCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="轮型号" align="center" prop="wheelCode" min-width="120" />
      <el-table-column label="试制状态" align="center" prop="trialStatus" min-width="90">
        <template slot-scope="scope">
          <el-tag :type="getTrialStatusType(scope.row.trialStatus)" size="small">
            {{ scope.row.trialStatus || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="试制类型" align="center" prop="trialType" min-width="90" />
      <el-table-column label="开发类型" align="center" prop="devType" min-width="90" />
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="100" show-overflow-tooltip />
      <el-table-column label="负责人" align="center" prop="responsible" min-width="80" />
      <el-table-column label="工艺流程" align="center" prop="craftProcess" min-width="100" />
      <el-table-column label="试制数量" align="center" prop="trialQuantity" min-width="80" />
      <el-table-column label="试制开始" align="center" prop="trialStart" min-width="100">
        <template slot-scope="scope">
          <span>{{ formatDate(scope.row.trialStart) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="送样时间" align="center" prop="sampleDate" min-width="100">
        <template slot-scope="scope">
          <span>{{ formatDate(scope.row.sampleDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" min-width="80">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small" effect="dark">
            {{ scope.row.status || '待启动' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" min-width="160" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click.stop="openDetail(scope.row)">详情</el-button>
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

    <!-- 新增/编辑通知单对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="850px" append-to-body :close-on-click-modal="false">
      <el-form ref="noticeForm" :model="form" :rules="formRules" label-width="120px" class="notice-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="轮型号" prop="wheelCode">
              <el-input v-model="form.wheelCode" placeholder="请输入轮型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通知单编号" prop="noticeCode">
              <el-input v-model="form.noticeCode" placeholder="自动生成或手动输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试制状态" prop="trialStatus">
              <el-select v-model="form.trialStatus" placeholder="请选择" style="width:100%">
                <el-option label="首次试制" value="首次试制" />
                <el-option label="重复试制" value="重复试制" />
                <el-option label="量产" value="量产" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试制类型" prop="trialType">
              <el-select v-model="form.trialType" placeholder="请选择" style="width:100%">
                <el-option label="新品开发" value="新品开发" />
                <el-option label="改进试制" value="改进试制" />
                <el-option label="客户变更" value="客户变更" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开发类型" prop="devType">
              <el-select v-model="form.devType" placeholder="请选择" style="width:100%">
                <el-option label="全新开发" value="全新开发" />
                <el-option label="平台化开发" value="平台化开发" />
                <el-option label="改型开发" value="改型开发" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="表面状态" prop="surfaceStatus">
              <el-select v-model="form.surfaceStatus" placeholder="请选择" style="width:100%">
                <el-option label="涂装" value="涂装" />
                <el-option label="电镀" value="电镀" />
                <el-option label="抛光" value="抛光" />
                <el-option label="机加" value="机加" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsible">
              <el-select v-model="form.responsible" placeholder="请选择负责人" filterable style="width:100%">
                <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="尺寸规格" prop="sizeSpec">
              <el-input v-model="form.sizeSpec" placeholder="如: 17x7.5J" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试制数量" prop="trialQuantity">
              <el-input-number v-model="form.trialQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划交样数量" prop="sampleQuantity">
              <el-input-number v-model="form.sampleQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工艺流程" prop="craftProcess">
              <el-select v-model="form.craftProcess" placeholder="请选择工艺流程" style="width:100%">
                <el-option label="低压铸造" value="低压铸造" />
                <el-option label="旋压铸造" value="旋压铸造" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试制开始时间" prop="trialStart">
              <el-date-picker v-model="form.trialStart" type="date" value-format="yyyy-MM-dd" placeholder="请选择" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="送样时间" prop="sampleDate">
              <el-date-picker v-model="form.sampleDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="实验项目" prop="experimentItems">
          <el-checkbox-group v-model="experimentList">
            <el-checkbox label="13度冲击">13度冲击</el-checkbox>
            <el-checkbox label="90度冲击">90度冲击</el-checkbox>
            <el-checkbox label="弯曲疲劳">弯曲疲劳</el-checkbox>
            <el-checkbox label="径向疲劳">径向疲劳</el-checkbox>
            <el-checkbox label="CASS中性盐雾">CASS中性盐雾</el-checkbox>
            <el-checkbox label="金相">金相</el-checkbox>
            <el-checkbox label="机械性能">机械性能</el-checkbox>
            <el-checkbox label="化学成分">化学成分</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTrialNotice, getTrialNotice, addTrialNotice, updateTrialNotice, delTrialNotice } from "@/api/tech/trialNotice"
import { getAllUsers } from "@/api/tech/common"

export default {
  name: "TrialNotice",
  data() {
    return {
      loading: true,
      submitLoading: false,
      total: 0,
      noticeList: [],
      userList: [],
      dialogTitle: '',
      dialogVisible: false,
      experimentList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        wheelCode: null,
        status: null
      },
      form: {},
      formRules: {
        wheelCode: [{ required: true, message: '轮型号不能为空', trigger: 'blur' }],
        trialStatus: [{ required: true, message: '请选择试制状态', trigger: 'change' }],
        trialType: [{ required: true, message: '请选择试制类型', trigger: 'change' }],
        responsible: [{ required: true, message: '请选择负责人', trigger: 'change' }],
        craftProcess: [{ required: true, message: '请选择工艺流程', trigger: 'change' }]
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
      listTrialNotice(this.queryParams).then(response => {
        this.noticeList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.noticeList = []
        this.total = 0
        this.loading = false
      })
    },
    formatDate(date) {
      if (!date) return '-'
      return date.substring(0, 10)
    },
    getTrialStatusType(status) {
      const map = { '首次试制': 'primary', '重复试制': 'warning', '量产': 'success' }
      return map[status] || 'info'
    },
    getStatusType(status) {
      const map = { '待启动': 'info', '进行中': 'primary', '已完成': 'success', '已取消': 'danger' }
      return map[status] || 'info'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleRowClick(row) {
      // 可以高亮选中行
    },
    resetForm() {
      this.form = {
        noticeId: null,
        noticeCode: '',
        wheelCode: '',
        trialStatus: '',
        trialType: '',
        devType: '',
        customerName: '',
        responsible: '',
        surfaceStatus: '',
        sizeSpec: '',
        urgency: '正常',
        trialQuantity: 0,
        sampleQuantity: 0,
        trialStart: null,
        sampleDate: null,
        craftProcess: '',
        experimentItems: '',
        wheelImage: '',
        processData: '',
        status: '待启动',
        remark: ''
      }
      this.experimentList = []
    },
    handleAdd() {
      this.resetForm()
      // 自动生成编号
      this.form.noticeCode = 'TN-' + new Date().getFullYear() + '-' + String(new Date().getMonth() + 1).padStart(2, '0') + '-' + String(Math.floor(Math.random() * 9000) + 1000)
      this.dialogTitle = '新增产品试制通知单'
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.noticeForm) this.$refs.noticeForm.clearValidate()
      })
    },
    handleUpdate(row) {
      this.resetForm()
      getTrialNotice(row.noticeId).then(response => {
        this.form = response.data || { ...row }
        this.experimentList = this.form.experimentItems ? this.form.experimentItems.split(',') : []
        this.dialogTitle = '编辑产品试制通知单'
        this.dialogVisible = true
      }).catch(() => {
        this.form = { ...row }
        this.experimentList = row.experimentItems ? row.experimentItems.split(',') : []
        this.dialogTitle = '编辑产品试制通知单'
        this.dialogVisible = true
      })
    },
    submitForm() {
      this.$refs.noticeForm.validate(valid => {
        if (valid) {
          this.submitLoading = true
          // 将实验项目数组转为逗号分隔字符串
          this.form.experimentItems = this.experimentList.join(',')
          
          if (this.form.noticeId != null) {
            updateTrialNotice(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.dialogVisible = false
              this.getList()
            }).finally(() => { this.submitLoading = false })
          } else {
            // 初始化工序数据
            this.form.processData = JSON.stringify(this.getInitProcessData())
            addTrialNotice(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.dialogVisible = false
              this.getList()
            }).finally(() => { this.submitLoading = false })
          }
        }
      })
    },
    /** 获取初始化工序数据 */
    getInitProcessData() {
      const processes = ['压铸', '旋压', '热处理', 'X光', '粗车', '涂装', '终检', '精车', '台架实验', '性能实验', '总结']
      return processes.map(name => ({
        name: name,
        status: '待开始',
        responsible: '',
        startDate: '',
        endDate: '',
        result: '',
        remark: ''
      }))
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除通知单编号为"' + row.noticeCode + '"的数据项？').then(() => {
        return delTrialNotice(row.noticeId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    openDetail(row) {
      this.$router.push({ path: '/trial/notice-detail', query: { id: row.noticeId } })
    },
    handleExport() {
      this.download('tech/notice/export', { ...this.queryParams }, `trial_notice_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped lang="scss">
.notice-container {
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
    width: 250px;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
    margin-left: auto;
  }
}

.notice-code {
  font-weight: 600;
  color: #409EFF;
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

::v-deep .el-table {
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  
  th {
    background: #3d5a80 !important;
    color: #fff !important;
    font-weight: 600;
    padding: 10px 0;
  }
  
  td {
    padding: 8px 0;
  }
}

.notice-form {
  ::v-deep .el-checkbox {
    margin-right: 15px;
    margin-bottom: 8px;
  }
}
</style>
