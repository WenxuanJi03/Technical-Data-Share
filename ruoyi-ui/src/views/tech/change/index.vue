<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="变更编号" prop="changeCode">
        <el-input
          v-model="queryParams.changeCode"
          placeholder="请输入变更编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="变更标题" prop="changeTitle">
        <el-input
          v-model="queryParams.changeTitle"
          placeholder="请输入变更标题"
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
      <el-form-item label="紧急程度" prop="urgency">
        <el-input
          v-model="queryParams.urgency"
          placeholder="请输入紧急程度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人ID" prop="applicantId">
        <el-input
          v-model="queryParams.applicantId"
          placeholder="请输入申请人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人姓名" prop="applicantName">
        <el-input
          v-model="queryParams.applicantName"
          placeholder="请输入申请人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请时间" prop="applyTime">
        <el-date-picker clearable
          v-model="queryParams.applyTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择申请时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="审批人ID" prop="approverId">
        <el-input
          v-model="queryParams.approverId"
          placeholder="请输入审批人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审批人姓名" prop="approverName">
        <el-input
          v-model="queryParams.approverName"
          placeholder="请输入审批人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审批时间" prop="approveTime">
        <el-date-picker clearable
          v-model="queryParams.approveTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择审批时间">
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
          v-hasPermi="['tech:change:add']"
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
          v-hasPermi="['tech:change:edit']"
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
          v-hasPermi="['tech:change:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tech:change:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="changeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="变更ID" align="center" prop="changeId" />
      <el-table-column label="变更编号" align="center" prop="changeCode" />
      <el-table-column label="变更标题" align="center" prop="changeTitle" />
      <el-table-column label="变更类型" align="center" prop="changeType" />
      <el-table-column label="关联项目ID" align="center" prop="projectId" />
      <el-table-column label="关联项目名称" align="center" prop="projectName" />
      <el-table-column label="变更状态" align="center" prop="status" />
      <el-table-column label="紧急程度" align="center" prop="urgency" />
      <el-table-column label="变更原因" align="center" prop="changeReason" />
      <el-table-column label="变更内容" align="center" prop="changeContent" />
      <el-table-column label="变更影响" align="center" prop="changeEffect" />
      <el-table-column label="申请人ID" align="center" prop="applicantId" />
      <el-table-column label="申请人姓名" align="center" prop="applicantName" />
      <el-table-column label="申请时间" align="center" prop="applyTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.applyTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批人ID" align="center" prop="approverId" />
      <el-table-column label="审批人姓名" align="center" prop="approverName" />
      <el-table-column label="审批时间" align="center" prop="approveTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.approveTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" align="center" prop="approveOpinion" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tech:change:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tech:change:remove']"
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

    <!-- 添加或修改变更申请对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="变更编号" prop="changeCode">
          <el-input v-model="form.changeCode" placeholder="请输入变更编号" />
        </el-form-item>
        <el-form-item label="变更标题" prop="changeTitle">
          <el-input v-model="form.changeTitle" placeholder="请输入变更标题" />
        </el-form-item>
        <el-form-item label="关联项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入关联项目ID" />
        </el-form-item>
        <el-form-item label="关联项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入关联项目名称" />
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-input v-model="form.urgency" placeholder="请输入紧急程度" />
        </el-form-item>
        <el-form-item label="变更原因" prop="changeReason">
          <el-input v-model="form.changeReason" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="变更内容">
          <editor v-model="form.changeContent" :min-height="192"/>
        </el-form-item>
        <el-form-item label="变更影响" prop="changeEffect">
          <el-input v-model="form.changeEffect" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="申请人ID" prop="applicantId">
          <el-input v-model="form.applicantId" placeholder="请输入申请人ID" />
        </el-form-item>
        <el-form-item label="申请人姓名" prop="applicantName">
          <el-input v-model="form.applicantName" placeholder="请输入申请人姓名" />
        </el-form-item>
        <el-form-item label="申请时间" prop="applyTime">
          <el-date-picker clearable
            v-model="form.applyTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择申请时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="审批人ID" prop="approverId">
          <el-input v-model="form.approverId" placeholder="请输入审批人ID" />
        </el-form-item>
        <el-form-item label="审批人姓名" prop="approverName">
          <el-input v-model="form.approverName" placeholder="请输入审批人姓名" />
        </el-form-item>
        <el-form-item label="审批时间" prop="approveTime">
          <el-date-picker clearable
            v-model="form.approveTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择审批时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="审批意见" prop="approveOpinion">
          <el-input v-model="form.approveOpinion" type="textarea" placeholder="请输入内容" />
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
import { listChange, getChange, delChange, addChange, updateChange } from "@/api/tech/change"

export default {
  name: "Change",
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
      // 变更申请表格数据
      changeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        changeCode: null,
        changeTitle: null,
        changeType: null,
        projectId: null,
        projectName: null,
        status: null,
        urgency: null,
        changeReason: null,
        changeContent: null,
        changeEffect: null,
        applicantId: null,
        applicantName: null,
        applyTime: null,
        approverId: null,
        approverName: null,
        approveTime: null,
        approveOpinion: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        changeCode: [
          { required: true, message: "变更编号不能为空", trigger: "blur" }
        ],
        changeTitle: [
          { required: true, message: "变更标题不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询变更申请列表 */
    getList() {
      this.loading = true
      listChange(this.queryParams).then(response => {
        this.changeList = response.rows
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
        changeId: null,
        changeCode: null,
        changeTitle: null,
        changeType: null,
        projectId: null,
        projectName: null,
        status: null,
        urgency: null,
        changeReason: null,
        changeContent: null,
        changeEffect: null,
        applicantId: null,
        applicantName: null,
        applyTime: null,
        approverId: null,
        approverName: null,
        approveTime: null,
        approveOpinion: null,
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
      this.ids = selection.map(item => item.changeId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加变更申请"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const changeId = row.changeId || this.ids
      getChange(changeId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改变更申请"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.changeId != null) {
            updateChange(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addChange(this.form).then(response => {
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
      const changeIds = row.changeId || this.ids
      this.$modal.confirm('是否确认删除变更申请编号为"' + changeIds + '"的数据项？').then(function() {
        return delChange(changeIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/change/export', {
        ...this.queryParams
      }, `change_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
