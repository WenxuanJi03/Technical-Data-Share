<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="文档名称" prop="docName">
        <el-input
          v-model="queryParams.docName"
          placeholder="请输入文档名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文档类型" prop="docType">
        <el-select v-model="queryParams.docType" placeholder="全部类型" clearable>
          <el-option label="设计文档" value="design" />
          <el-option label="工艺文档" value="process" />
          <el-option label="质量文档" value="quality" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="关联项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="全部项目" clearable filterable>
          <el-option
            v-for="p in projectList"
            :key="p.projectId"
            :label="p.projectName"
            :value="p.projectId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="全部格式" clearable>
          <el-option label="Excel" value="xlsx" />
          <el-option label="Word" value="docx" />
          <el-option label="PDF" value="pdf" />
          <el-option label="WPS文字" value="wps" />
          <el-option label="WPS表格" value="et" />
          <el-option label="CAD图纸" value="dwg" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
          <el-option label="草稿" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="已归档" value="2" />
        </el-select>
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
          type="info"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['tech:document:add']"
        >导入</el-button>
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
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="文档编号" align="center" prop="docCode" width="140" />
      <el-table-column label="文档名称" align="center" prop="docName" min-width="200" show-overflow-tooltip />
      <el-table-column label="文档类型" align="center" prop="docType" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.docType === 'design'" type="primary" size="small">设计</el-tag>
          <el-tag v-else-if="scope.row.docType === 'process'" type="success" size="small">工艺</el-tag>
          <el-tag v-else-if="scope.row.docType === 'quality'" type="warning" size="small">质量</el-tag>
          <el-tag v-else size="small">其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联项目" align="center" prop="projectName" width="160" show-overflow-tooltip />
      <el-table-column label="版本" align="center" prop="version" width="70" />
      <el-table-column label="文件格式" align="center" prop="fileType" width="90">
        <template slot-scope="scope">
          <span :style="{ color: getFileColor(scope.row.fileType) }">
            {{ getFileIcon(scope.row.fileType) }} {{ (scope.row.fileType || '').toUpperCase() }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize" width="100">
        <template slot-scope="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="info" size="small">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="success" size="small">已发布</el-tag>
          <el-tag v-else-if="scope.row.status === '2'" type="warning" size="small">已归档</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="作者" align="center" prop="authorName" width="90" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-download"
            @click="handleDownload(scope.row)"
            v-if="scope.row.filePath"
          >下载</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-if="scope.row.filePath"
          >预览</el-button>
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
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文档名称" prop="docName">
              <el-input v-model="form.docName" placeholder="请输入文档名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文档编号" prop="docCode">
              <el-input v-model="form.docCode" placeholder="请输入文档编号（选填）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文档类型" prop="docType">
              <el-select v-model="form.docType" placeholder="请选择" style="width: 100%">
                <el-option label="设计文档" value="design" />
                <el-option label="工艺文档" value="process" />
                <el-option label="质量文档" value="quality" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择关联项目" clearable filterable style="width: 100%" @change="onProjectChange">
                <el-option
                  v-for="p in projectList"
                  :key="p.projectId"
                  :label="p.projectName"
                  :value="p.projectId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="如：V1.0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
                <el-option label="草稿" value="0" />
                <el-option label="已发布" value="1" />
                <el-option label="已归档" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上传文件" prop="filePath">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :file-list="uploadFileList"
            :limit="1"
            :on-exceed="handleExceed"
            :on-remove="handleRemoveFile"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">
              支持 Excel/Word/PDF/WPS/CAD 等格式，单个文件不超过50MB
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入技术文档数据" :visible.sync="importOpen" width="400px" append-to-body>
      <el-upload
        ref="importUpload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="uploadHeaders"
        :action="importUrl"
        :disabled="importUploading"
        :on-progress="handleImportProgress"
        :on-success="handleImportSuccess"
        :auto-upload="false"
      >
        <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
        <el-button style="margin-left: 10px;" size="small" type="success" :loading="importUploading" @click="submitImport">开始导入</el-button>
        <div slot="tip" class="el-upload__tip">
          <el-checkbox v-model="importUpdateSupport" /> 是否更新已存在的数据
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline;" @click="handleImportTemplate">下载模板</el-link>
        </div>
      </el-upload>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog title="文件预览" :visible.sync="previewOpen" width="80%" top="5vh" append-to-body>
      <iframe v-if="previewUrl" :src="previewUrl" style="width: 100%; height: 75vh; border: none;"></iframe>
      <div v-else style="text-align: center; padding: 40px; color: #909399;">
        该文件格式暂不支持在线预览，请下载后查看
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDocument, getDocument, delDocument, addDocument, updateDocument } from "@/api/tech/document"
import { listProject } from "@/api/tech/project"
import { getToken } from "@/utils/auth"

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
      // 项目列表
      projectList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 导入弹窗
      importOpen: false,
      importUploading: false,
      importUpdateSupport: false,
      // 预览弹窗
      previewOpen: false,
      previewUrl: "",
      // 上传文件列表
      uploadFileList: [],
      // 上传地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      importUrl: process.env.VUE_APP_BASE_API + "/tech/document/importData",
      // 上传请求头
      uploadHeaders: {
        Authorization: "Bearer " + getToken()
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        docCode: null,
        docName: null,
        docType: null,
        projectId: null,
        fileType: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        docName: [
          { required: true, message: "文档名称不能为空", trigger: "blur" }
        ],
        docType: [
          { required: true, message: "请选择文档类型", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getList()
    this.getProjectList()
  },
  methods: {
    /** 查询项目列表（用于下拉选择） */
    getProjectList() {
      listProject({ pageNum: 1, pageSize: 500 }).then(response => {
        this.projectList = response.rows || []
      })
    },
    /** 查询技术文档列表 */
    getList() {
      this.loading = true
      listDocument(this.queryParams).then(response => {
        this.documentList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 项目选择变化 */
    onProjectChange(val) {
      const proj = this.projectList.find(p => p.projectId === val)
      if (proj) {
        this.form.projectName = proj.projectName
      } else {
        this.form.projectName = null
      }
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
        version: "V1.0",
        filePath: null,
        fileSize: null,
        fileType: null,
        status: "0",
        authorId: null,
        authorName: null,
        remark: null
      }
      this.uploadFileList = []
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
      this.single = selection.length !== 1
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
        if (this.form.filePath) {
          this.uploadFileList = [{ name: this.getFileName(this.form.filePath), url: this.form.filePath }]
        }
        this.open = true
        this.title = "修改技术文档"
      })
    },
    /** 上传前校验 */
    beforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isLt50M) {
        this.$modal.msgError("上传文件大小不能超过 50MB!")
        return false
      }
      return true
    },
    /** 上传成功回调 */
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.form.filePath = response.fileName
        this.form.fileSize = file.size
        // 提取文件扩展名
        const name = file.name
        const ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase()
        this.form.fileType = ext
        this.$modal.msgSuccess("文件上传成功")
      } else {
        this.$modal.msgError(response.msg || "文件上传失败")
      }
    },
    /** 上传失败 */
    handleUploadError() {
      this.$modal.msgError("文件上传失败，请重试")
    },
    /** 文件数量超限 */
    handleExceed() {
      this.$modal.msgError("每个文档只能上传一个文件，请先删除已有文件")
    },
    /** 移除文件 */
    handleRemoveFile() {
      this.form.filePath = null
      this.form.fileSize = null
      this.form.fileType = null
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
      this.$modal.confirm('是否确认删除所选的技术文档？').then(function() {
        return delDocument(docIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 下载文件 */
    handleDownload(row) {
      if (row.filePath) {
        const baseUrl = process.env.VUE_APP_BASE_API
        window.open(baseUrl + "/common/download/resource?resource=" + encodeURIComponent(row.filePath))
      }
    },
    /** 预览文件 */
    handlePreview(row) {
      if (!row.filePath) return
      const ext = (row.fileType || '').toLowerCase()
      if (['pdf', 'jpg', 'jpeg', 'png', 'gif'].includes(ext)) {
        // PDF和图片可以直接预览
        this.previewUrl = process.env.VUE_APP_BASE_API + row.filePath
        this.previewOpen = true
      } else {
        // 其他格式提示下载
        this.$modal.confirm('该文件格式暂不支持在线预览，是否下载后查看？').then(() => {
          this.handleDownload(row)
        }).catch(() => {})
      }
    },
    /** 导入按钮操作 */
    handleImport() {
      this.importOpen = true
    },
    /** 下载导入模板 */
    handleImportTemplate() {
      this.download('tech/document/importTemplate', {}, '技术文档导入模板_' + new Date().getTime() + '.xlsx')
    },
    /** 开始导入 */
    submitImport() {
      this.$refs.importUpload.submit()
    },
    /** 导入进度 */
    handleImportProgress() {
      this.importUploading = true
    },
    /** 导入成功 */
    handleImportSuccess(response) {
      this.importUploading = false
      this.$refs.importUpload.clearFiles()
      if (response.code === 200) {
        this.$modal.msgSuccess(response.msg || "导入成功")
        this.importOpen = false
        this.getList()
      } else {
        this.$modal.msgError(response.msg || "导入失败")
      }
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('tech/document/export', {
        ...this.queryParams
      }, `技术文档_${new Date().getTime()}.xlsx`)
    },
    /** 文件类型图标 */
    getFileIcon(type) {
      const t = (type || '').toLowerCase()
      const icons = {
        'xlsx': '📊', 'xls': '📊', 'et': '📊',
        'doc': '📝', 'docx': '📝', 'wps': '📝',
        'pdf': '📕',
        'dwg': '📐', 'dxf': '📐',
        'ppt': '📙', 'pptx': '📙', 'dps': '📙',
        'jpg': '🖼', 'jpeg': '🖼', 'png': '🖼',
      }
      return icons[t] || '📄'
    },
    /** 文件类型颜色 */
    getFileColor(type) {
      const t = (type || '').toLowerCase()
      const colors = {
        'xlsx': '#217346', 'xls': '#217346', 'et': '#217346',
        'doc': '#2B579A', 'docx': '#2B579A', 'wps': '#2B579A',
        'pdf': '#D32F2F',
        'dwg': '#E65100', 'dxf': '#E65100',
      }
      return colors[t] || '#606266'
    },
    /** 格式化文件大小 */
    formatFileSize(size) {
      if (!size) return '-'
      if (size < 1024) return size + ' B'
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
      return (size / (1024 * 1024)).toFixed(1) + ' MB'
    },
    /** 获取文件名 */
    getFileName(path) {
      if (!path) return ''
      return path.substring(path.lastIndexOf('/') + 1)
    }
  }
}
</script>
