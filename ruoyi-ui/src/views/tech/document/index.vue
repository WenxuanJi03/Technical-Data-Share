<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文档编号" prop="docCode">
        <el-input
          v-model="queryParams.docCode"
          placeholder="请输入文档编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文档名称" prop="docName">
        <el-input
          v-model="queryParams.docName"
          placeholder="请输入文档名称"
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
      <el-form-item label="版本号" prop="version">
        <el-input
          v-model="queryParams.version"
          placeholder="请输入版本号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件大小(字节)" prop="fileSize">
        <el-input
          v-model="queryParams.fileSize"
          placeholder="请输入文件大小(字节)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作者ID" prop="authorId">
        <el-input
          v-model="queryParams.authorId"
          placeholder="请输入作者ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="作者姓名" prop="authorName">
        <el-input
          v-model="queryParams.authorName"
          placeholder="请输入作者姓名"
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
          v-hasPermi="['tech:document:add']"
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
          v-hasPermi="['tech:document:edit']"
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
          v-hasPermi="['tech:document:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['tech:document:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="documentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文档ID" align="center" prop="docId" />
      <el-table-column label="文档编号" align="center" prop="docCode" />
      <el-table-column label="文档名称" align="center" prop="docName" />
      <el-table-column label="文档类型" align="center" prop="docType" />
      <el-table-column label="关联项目ID" align="center" prop="projectId" />
      <el-table-column label="关联项目名称" align="center" prop="projectName" />
      <el-table-column label="版本号" align="center" prop="version" />
      <el-table-column label="文件路径" align="center" prop="filePath" />
      <el-table-column label="文件大小(字节)" align="center" prop="fileSize" />
      <el-table-column label="文件类型(pdf/doc/xls等)" align="center" prop="fileType" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="作者ID" align="center" prop="authorId" />
      <el-table-column label="作者姓名" align="center" prop="authorName" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['tech:document:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['tech:document:remove']"
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

    <!-- 添加或修改技术文档对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="文档编号" prop="docCode">
          <el-input v-model="form.docCode" placeholder="请输入文档编号" />
        </el-form-item>
        <el-form-item label="文档名称" prop="docName">
          <el-input v-model="form.docName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="关联项目ID" prop="projectId">
          <el-input v-model="form.projectId" placeholder="请输入关联项目ID" />
        </el-form-item>
        <el-form-item label="关联项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入关联项目名称" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="form.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="文件路径" prop="filePath">
          <el-input v-model="form.filePath" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="文件大小(字节)" prop="fileSize">
          <el-input v-model="form.fileSize" placeholder="请输入文件大小(字节)" />
        </el-form-item>
        <el-form-item label="作者ID" prop="authorId">
          <el-input v-model="form.authorId" placeholder="请输入作者ID" />
        </el-form-item>
        <el-form-item label="作者姓名" prop="authorName">
          <el-input v-model="form.authorName" placeholder="请输入作者姓名" />
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
import { listDocument, getDocument, delDocument, addDocument, updateDocument } from "@/api/tech/document"

export default {
  name: "Document",
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
      // 技术文档表格数据
      documentList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        docCode: null,
        docName: null,
        docType: null,
        projectId: null,
        projectName: null,
        version: null,
        filePath: null,
        fileSize: null,
        fileType: null,
        status: null,
        authorId: null,
        authorName: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        docName: [
          { required: true, message: "文档名称不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询技术文档列表 */
    getList() {
      this.loading = true
      listDocument(this.queryParams).then(response => {
        this.documentList = response.rows
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
        docId: null,
        docCode: null,
        docName: null,
        docType: null,
        projectId: null,
        projectName: null,
        version: null,
        filePath: null,
        fileSize: null,
        fileType: null,
        status: null,
        authorId: null,
        authorName: null,
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
      this.ids = selection.map(item => item.docId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加技术文档"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const docId = row.docId || this.ids
      getDocument(docId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改技术文档"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.docId != null) {
            updateDocument(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDocument(this.form).then(response => {
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
      const docIds = row.docId || this.ids
      this.$modal.confirm('是否确认删除技术文档编号为"' + docIds + '"的数据项？').then(function() {
        return delDocument(docIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/document/export', {
        ...this.queryParams
      }, `document_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
