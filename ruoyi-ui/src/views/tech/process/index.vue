<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="试制ID" prop="trialId">
        <el-input
          v-model="queryParams.trialId"
          placeholder="请输入试制ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="车间顺序" prop="workshopOrder">
        <el-input
          v-model="queryParams.workshopOrder"
          placeholder="请输入车间顺序"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="车间代码" prop="workshopCode">
        <el-input
          v-model="queryParams.workshopCode"
          placeholder="请输入车间代码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="车间名称" prop="workshopName">
        <el-input
          v-model="queryParams.workshopName"
          placeholder="请输入车间名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收人ID" prop="receiverId">
        <el-input
          v-model="queryParams.receiverId"
          placeholder="请输入接收人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收人姓名" prop="receiverName">
        <el-input
          v-model="queryParams.receiverName"
          placeholder="请输入接收人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="接收时间" prop="receiveTime">
        <el-date-picker clearable
          v-model="queryParams.receiveTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择接收时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="处理人ID" prop="handlerId">
        <el-input
          v-model="queryParams.handlerId"
          placeholder="请输入处理人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理人姓名" prop="handlerName">
        <el-input
          v-model="queryParams.handlerName"
          placeholder="请输入处理人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理时间" prop="handleTime">
        <el-date-picker clearable
          v-model="queryParams.handleTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择处理时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="工时(小时)" prop="workHours">
        <el-input
          v-model="queryParams.workHours"
          placeholder="请输入工时(小时)"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['tech:process:add']"
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
          v-hasPermi="['tech:process:edit']"
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
          v-hasPermi="['tech:process:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tech:process:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="processList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="流程ID" align="center" prop="processId" />
      <el-table-column label="试制ID" align="center" prop="trialId" />
      <el-table-column label="车间顺序" align="center" prop="workshopOrder" />
      <el-table-column label="车间代码" align="center" prop="workshopCode" />
      <el-table-column label="车间名称" align="center" prop="workshopName" />
      <el-table-column label="处理状态" align="center" prop="status" />
      <el-table-column label="接收人ID" align="center" prop="receiverId" />
      <el-table-column label="接收人姓名" align="center" prop="receiverName" />
      <el-table-column label="接收时间" align="center" prop="receiveTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.receiveTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="处理人ID" align="center" prop="handlerId" />
      <el-table-column label="处理人姓名" align="center" prop="handlerName" />
      <el-table-column label="处理时间" align="center" prop="handleTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.handleTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="工时(小时)" align="center" prop="workHours" />
      <el-table-column label="处理结果" align="center" prop="result" />
      <el-table-column label="问题记录" align="center" prop="problem" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tech:process:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tech:process:remove']"
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

    <!-- 添加或修改试制流程记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="试制ID" prop="trialId">
          <el-input v-model="form.trialId" placeholder="请输入试制ID" />
        </el-form-item>
        <el-form-item label="车间顺序" prop="workshopOrder">
          <el-input v-model="form.workshopOrder" placeholder="请输入车间顺序" />
        </el-form-item>
        <el-form-item label="车间代码" prop="workshopCode">
          <el-input v-model="form.workshopCode" placeholder="请输入车间代码" />
        </el-form-item>
        <el-form-item label="车间名称" prop="workshopName">
          <el-input v-model="form.workshopName" placeholder="请输入车间名称" />
        </el-form-item>
        <el-form-item label="接收人ID" prop="receiverId">
          <el-input v-model="form.receiverId" placeholder="请输入接收人ID" />
        </el-form-item>
        <el-form-item label="接收人姓名" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入接收人姓名" />
        </el-form-item>
        <el-form-item label="接收时间" prop="receiveTime">
          <el-date-picker clearable
            v-model="form.receiveTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择接收时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="处理人ID" prop="handlerId">
          <el-input v-model="form.handlerId" placeholder="请输入处理人ID" />
        </el-form-item>
        <el-form-item label="处理人姓名" prop="handlerName">
          <el-input v-model="form.handlerName" placeholder="请输入处理人姓名" />
        </el-form-item>
        <el-form-item label="处理时间" prop="handleTime">
          <el-date-picker clearable
            v-model="form.handleTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择处理时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="工时(小时)" prop="workHours">
          <el-input v-model="form.workHours" placeholder="请输入工时(小时)" />
        </el-form-item>
        <el-form-item label="处理结果" prop="result">
          <el-input v-model="form.result" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="问题记录" prop="problem">
          <el-input v-model="form.problem" type="textarea" placeholder="请输入内容" />
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
import { listProcess, getProcess, delProcess, addProcess, updateProcess } from "@/api/tech/process"

export default {
  name: "Process",
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
      // 试制流程记录表格数据
      processList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        trialId: null,
        workshopOrder: null,
        workshopCode: null,
        workshopName: null,
        status: null,
        receiverId: null,
        receiverName: null,
        receiveTime: null,
        handlerId: null,
        handlerName: null,
        handleTime: null,
        workHours: null,
        result: null,
        problem: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        trialId: [
          { required: true, message: "试制ID不能为空", trigger: "blur" }
        ],
        workshopOrder: [
          { required: true, message: "车间顺序不能为空", trigger: "blur" }
        ],
        workshopCode: [
          { required: true, message: "车间代码不能为空", trigger: "blur" }
        ],
        workshopName: [
          { required: true, message: "车间名称不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询试制流程记录列表 */
    getList() {
      this.loading = true
      listProcess(this.queryParams).then(response => {
        this.processList = response.rows
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
        processId: null,
        trialId: null,
        workshopOrder: null,
        workshopCode: null,
        workshopName: null,
        status: null,
        receiverId: null,
        receiverName: null,
        receiveTime: null,
        handlerId: null,
        handlerName: null,
        handleTime: null,
        workHours: null,
        result: null,
        problem: null,
        remark: null,
        createTime: null,
        updateTime: null
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
      this.ids = selection.map(item => item.processId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加试制流程记录"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const processId = row.processId || this.ids
      getProcess(processId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改试制流程记录"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.processId != null) {
            updateProcess(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addProcess(this.form).then(response => {
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
      const processIds = row.processId || this.ids
      this.$modal.confirm('是否确认删除试制流程记录编号为"' + processIds + '"的数据项？').then(function() {
        return delProcess(processIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/process/export', {
        ...this.queryParams
      }, `process_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
