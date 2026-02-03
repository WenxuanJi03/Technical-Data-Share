<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="试制编号" prop="trialCode">
        <el-input
          v-model="queryParams.trialCode"
          placeholder="请输入试制编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="试制名称" prop="trialName">
        <el-input
          v-model="queryParams.trialName"
          placeholder="请输入试制名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联项目ID" prop="projectId">
        <el-input
          v-model="queryParams.projectId"
          placeholder="请输入关联项目ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联项目名称" prop="projectName">
        <el-input
          v-model="queryParams.projectName"
          placeholder="请输入关联项目名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="试制数量" prop="quantity">
        <el-input
          v-model="queryParams.quantity"
          placeholder="请输入试制数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当前车间" prop="currentWorkshop">
        <el-input
          v-model="queryParams.currentWorkshop"
          placeholder="请输入当前车间"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发起人ID" prop="initiatorId">
        <el-input
          v-model="queryParams.initiatorId"
          placeholder="请输入发起人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发起人姓名" prop="initiatorName">
        <el-input
          v-model="queryParams.initiatorName"
          placeholder="请输入发起人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="计划开始日期" prop="planStartDate">
        <el-date-picker clearable
          v-model="queryParams.planStartDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择计划开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="计划结束日期" prop="planEndDate">
        <el-date-picker clearable
          v-model="queryParams.planEndDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择计划结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="实际开始日期" prop="actualStartDate">
        <el-date-picker clearable
          v-model="queryParams.actualStartDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择实际开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="实际结束日期" prop="actualEndDate">
        <el-date-picker clearable
          v-model="queryParams.actualEndDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择实际结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['tech:trial:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['tech:trial:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['tech:trial:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tech:trial:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="trialList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="试制ID" align="center" prop="trialId" />
      <el-table-column label="试制编号" align="center" prop="trialCode" />
      <el-table-column label="试制名称" align="center" prop="trialName" />
      <el-table-column label="关联项目ID" align="center" prop="projectId" />
      <el-table-column label="关联项目名称" align="center" prop="projectName" />
      <el-table-column label="试制类型" align="center" prop="trialType" />
      <el-table-column label="试制数量" align="center" prop="quantity" />
      <el-table-column label="试制状态" align="center" prop="status" />
      <el-table-column label="当前车间" align="center" prop="currentWorkshop" />
      <el-table-column label="发起人ID" align="center" prop="initiatorId" />
      <el-table-column label="发起人姓名" align="center" prop="initiatorName" />
      <el-table-column label="计划开始日期" align="center" prop="planStartDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.planStartDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="计划结束日期" align="center" prop="planEndDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.planEndDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际开始日期" align="center" prop="actualStartDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.actualStartDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实际结束日期" align="center" prop="actualEndDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.actualEndDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="试制要求" align="center" prop="requirement" />
      <el-table-column label="试制结果" align="center" prop="result" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tech:trial:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tech:trial:remove']"
          >删除</el-button>
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

    <!-- 添加或修改试制任务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="试制编号" prop="trialCode">
          <el-input v-model="form.trialCode" placeholder="请输入试制编号" />
        </el-form-item>
        <el-form-item label="试制名称" prop="trialName">
          <el-input v-model="form.trialName" placeholder="请输入试制名称" />
        </el-form-item>
        <el-form-item label="关联项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入关联项目ID" />
        </el-form-item>
        <el-form-item label="关联项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入关联项目名称" />
        </el-form-item>
        <el-form-item label="试制数量" prop="quantity">
          <el-input v-model="form.quantity" placeholder="请输入试制数量" />
        </el-form-item>
        <el-form-item label="当前车间" prop="currentWorkshop">
          <el-input v-model="form.currentWorkshop" placeholder="请输入当前车间" />
        </el-form-item>
        <el-form-item label="发起人ID" prop="initiatorId">
          <el-input v-model="form.initiatorId" placeholder="请输入发起人ID" />
        </el-form-item>
        <el-form-item label="发起人姓名" prop="initiatorName">
          <el-input v-model="form.initiatorName" placeholder="请输入发起人姓名" />
        </el-form-item>
        <el-form-item label="计划开始日期" prop="planStartDate">
          <el-date-picker clearable
            v-model="form.planStartDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择计划开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="计划结束日期" prop="planEndDate">
          <el-date-picker clearable
            v-model="form.planEndDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择计划结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际开始日期" prop="actualStartDate">
          <el-date-picker clearable
            v-model="form.actualStartDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择实际开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际结束日期" prop="actualEndDate">
          <el-date-picker clearable
            v-model="form.actualEndDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择实际结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="试制要求" prop="requirement">
          <el-input v-model="form.requirement" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="试制结果" prop="result">
          <el-input v-model="form.result" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
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
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 试制任务表格数据
      trialList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        trialCode: null,
        trialName: null,
        projectId: null,
        projectName: null,
        trialType: null,
        quantity: null,
        status: null,
        currentWorkshop: null,
        initiatorId: null,
        initiatorName: null,
        planStartDate: null,
        planEndDate: null,
        actualStartDate: null,
        actualEndDate: null,
        requirement: null,
        result: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        trialCode: [
          { required: true, message: "试制编号不能为空", trigger: "blur" }
        ],
        trialName: [
          { required: true, message: "试制名称不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询试制任务列表 */
    getList() {
      this.loading = true
      listTrial(this.queryParams).then(response => {
        this.trialList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        trialId: null,
        trialCode: null,
        trialName: null,
        projectId: null,
        projectName: null,
        trialType: null,
        quantity: null,
        status: null,
        currentWorkshop: null,
        initiatorId: null,
        initiatorName: null,
        planStartDate: null,
        planEndDate: null,
        actualStartDate: null,
        actualEndDate: null,
        requirement: null,
        result: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        delFlag: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.trialId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加试制任务"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const trialId = row.trialId || this.ids
      getTrial(trialId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改试制任务"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.trialId != null) {
            updateTrial(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addTrial(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const trialIds = row.trialId || this.ids
      this.$modal.confirm('是否确认删除试制任务编号为"' + trialIds + '"的数据项？').then(function() {
        return delTrial(trialIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/trial/export', {
        ...this.queryParams
      }, `trial_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
