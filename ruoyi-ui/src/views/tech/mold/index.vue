<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模具编号" prop="moldCode">
        <el-input
          v-model="queryParams.moldCode"
          placeholder="请输入模具编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模具名称" prop="moldName">
        <el-input
          v-model="queryParams.moldName"
          placeholder="请输入模具名称"
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
      <el-form-item label="产品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入产品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="规格型号" prop="specification">
        <el-input
          v-model="queryParams.specification"
          placeholder="请输入规格型号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模具材料" prop="material">
        <el-input
          v-model="queryParams.material"
          placeholder="请输入模具材料"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商" prop="supplier">
        <el-input
          v-model="queryParams.supplier"
          placeholder="请输入供应商"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="存放位置" prop="location">
        <el-input
          v-model="queryParams.location"
          placeholder="请输入存放位置"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设计寿命(次)" prop="designLife">
        <el-input
          v-model="queryParams.designLife"
          placeholder="请输入设计寿命(次)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当前使用次数" prop="currentCount">
        <el-input
          v-model="queryParams.currentCount"
          placeholder="请输入当前使用次数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="制造日期" prop="manufactureDate">
        <el-date-picker clearable
          v-model="queryParams.manufactureDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择制造日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="验收日期" prop="acceptanceDate">
        <el-date-picker clearable
          v-model="queryParams.acceptanceDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择验收日期">
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
          v-hasPermi="['tech:mold:add']"
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
          v-hasPermi="['tech:mold:edit']"
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
          v-hasPermi="['tech:mold:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tech:mold:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="moldList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模具ID" align="center" prop="moldId" />
      <el-table-column label="模具编号" align="center" prop="moldCode" />
      <el-table-column label="模具名称" align="center" prop="moldName" />
      <el-table-column label="模具类型" align="center" prop="moldType" />
      <el-table-column label="关联项目ID" align="center" prop="projectId" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="规格型号" align="center" prop="specification" />
      <el-table-column label="模具材料" align="center" prop="material" />
      <el-table-column label="供应商" align="center" prop="supplier" />
      <el-table-column label="模具状态" align="center" prop="status" />
      <el-table-column label="存放位置" align="center" prop="location" />
      <el-table-column label="设计寿命(次)" align="center" prop="designLife" />
      <el-table-column label="当前使用次数" align="center" prop="currentCount" />
      <el-table-column label="制造日期" align="center" prop="manufactureDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.manufactureDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="验收日期" align="center" prop="acceptanceDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.acceptanceDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tech:mold:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tech:mold:remove']"
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

    <!-- 添加或修改模具档案对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="模具编号" prop="moldCode">
          <el-input v-model="form.moldCode" placeholder="请输入模具编号" />
        </el-form-item>
        <el-form-item label="模具名称" prop="moldName">
          <el-input v-model="form.moldName" placeholder="请输入模具名称" />
        </el-form-item>
        <el-form-item label="关联项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入关联项目ID" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="规格型号" prop="specification">
          <el-input v-model="form.specification" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="模具材料" prop="material">
          <el-input v-model="form.material" placeholder="请输入模具材料" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入存放位置" />
        </el-form-item>
        <el-form-item label="设计寿命(次)" prop="designLife">
          <el-input v-model="form.designLife" placeholder="请输入设计寿命(次)" />
        </el-form-item>
        <el-form-item label="当前使用次数" prop="currentCount">
          <el-input v-model="form.currentCount" placeholder="请输入当前使用次数" />
        </el-form-item>
        <el-form-item label="制造日期" prop="manufactureDate">
          <el-date-picker clearable
            v-model="form.manufactureDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择制造日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="验收日期" prop="acceptanceDate">
          <el-date-picker clearable
            v-model="form.acceptanceDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择验收日期">
          </el-date-picker>
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
import { listMold, getMold, delMold, addMold, updateMold } from "@/api/tech/mold"

export default {
  name: "Mold",
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
      // 模具档案表格数据
      moldList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        moldCode: null,
        moldName: null,
        moldType: null,
        projectId: null,
        productName: null,
        specification: null,
        material: null,
        supplier: null,
        status: null,
        location: null,
        designLife: null,
        currentCount: null,
        manufactureDate: null,
        acceptanceDate: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        moldCode: [
          { required: true, message: "模具编号不能为空", trigger: "blur" }
        ],
        moldName: [
          { required: true, message: "模具名称不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询模具档案列表 */
    getList() {
      this.loading = true
      listMold(this.queryParams).then(response => {
        this.moldList = response.rows
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
        moldId: null,
        moldCode: null,
        moldName: null,
        moldType: null,
        projectId: null,
        productName: null,
        specification: null,
        material: null,
        supplier: null,
        status: null,
        location: null,
        designLife: null,
        currentCount: null,
        manufactureDate: null,
        acceptanceDate: null,
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
      this.ids = selection.map(item => item.moldId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加模具档案"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const moldId = row.moldId || this.ids
      getMold(moldId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改模具档案"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.moldId != null) {
            updateMold(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addMold(this.form).then(response => {
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
      const moldIds = row.moldId || this.ids
      this.$modal.confirm('是否确认删除模具档案编号为"' + moldIds + '"的数据项？').then(function() {
        return delMold(moldIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/mold/export', {
        ...this.queryParams
      }, `mold_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
