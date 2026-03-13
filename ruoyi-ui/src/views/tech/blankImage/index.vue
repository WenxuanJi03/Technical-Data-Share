<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="型号" prop="modelCode">
        <el-input v-model="queryParams.modelCode" placeholder="请输入型号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="版本" prop="version">
        <el-select v-model="queryParams.version" placeholder="全部" clearable>
          <el-option label="A" value="A" />
          <el-option label="B" value="B" />
          <el-option label="C" value="C" />
          <el-option label="D" value="D" />
          <el-option label="E" value="E" />
          <el-option label="F" value="F" />
          <el-option label="G" value="G" />
          <el-option label="H" value="H" />
          <el-option label="I" value="I" />
          <el-option label="J" value="J" />
          <el-option label="K" value="K" />
          <el-option label="L" value="L" />
          <el-option label="M" value="M" />
          <el-option label="N" value="N" />
          <el-option label="O" value="O" />
          <el-option label="P" value="P" />
          <el-option label="Q" value="Q" />
          <el-option label="R" value="R" />
          <el-option label="S" value="S" />
          <el-option label="T" value="T" />
          <el-option label="U" value="U" />
          <el-option label="V" value="V" />
          <el-option label="W" value="W" />
          <el-option label="X" value="X" />
          <el-option label="Y" value="Y" />
          <el-option label="Z" value="Z" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['tech:blankImage:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleBatchDelete" v-hasPermi="['tech:blankImage:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['tech:blankImage:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['tech:blankImage:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-picture" size="mini" @click="batchImageOpen = true" v-hasPermi="['tech:blankImage:edit']">批量上传图片</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :type="viewMode === 'card' ? 'primary' : 'default'" plain icon="el-icon-s-grid" size="mini" @click="viewMode = 'card'">卡片</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :type="viewMode === 'table' ? 'primary' : 'default'" plain icon="el-icon-s-unfold" size="mini" @click="viewMode = 'table'">表格</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 统计条 -->
    <div class="stats-bar" v-if="total > 0">
      <span class="stats-item">共 <b>{{ total }}</b> 条毛胚图</span>
      <el-divider direction="vertical"></el-divider>
      <span class="stats-item" v-if="queryParams.version">版本: {{ queryParams.version }}</span>
      <span class="stats-item" v-if="queryParams.version">版本: {{ queryParams.version }}</span>
      <el-divider direction="vertical"></el-divider>
      <el-checkbox v-model="selectAll" style="margin-left:8px">全选所有 {{ total }} 条毛胚图</el-checkbox>
      <el-button v-if="selectAll" type="danger" size="mini" icon="el-icon-delete" style="margin-left:12px" @click="handleCleanAll" v-hasPermi="['tech:blankImage:remove']">删除全部</el-button>
    </div>

    <!-- ★ 卡片视图（默认） -->
    <div v-if="viewMode === 'card'" v-loading="loading" class="card-container">
      <div
        v-for="item in blankList"
        :key="item.blankId"
        class="product-card"
        @click="showDetail(item)"
      >
        <!-- 图片区域 -->
        <div class="card-image">
          <img v-if="item.blankImage" :src="baseUrl + item.blankImage" class="wheel-img" />
          <div v-else class="no-image">
            <i class="el-icon-picture-outline"></i>
            <span>暂无图片</span>
          </div>
          <!-- 版本角标 -->
          <span v-if="item.version" class="status-badge badge-primary">{{ item.version }}</span>
        </div>
        <!-- 信息区域 -->
        <div class="card-body">
          <div class="wheel-code">{{ item.modelCode }}</div>
          <div class="card-info-row" v-if="item.releaseDate">
            <span class="info-label">下发</span>
            <span class="info-value">{{ item.releaseDate }}</span>
          </div>
        </div>
      </div>
      <!-- 空状态 -->
      <div v-if="!loading && blankList.length === 0" class="empty-card">
        <i class="el-icon-box" style="font-size:48px;color:#c0c4cc"></i>
        <p>暂无毛胚图数据，请先导入</p>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-if="viewMode === 'table'" v-loading="loading" :data="blankList" @selection-change="handleSelectionChange" border size="mini">
      <el-table-column type="selection" width="40" align="center" />
      <el-table-column label="型号" align="center" prop="modelCode" width="120">
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.modelCode }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="version" width="60">
        <template slot-scope="scope">
          <el-tag type="primary" size="mini">{{ scope.row.version || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下发时间" align="center" prop="releaseDate" width="120" />
      <el-table-column label="图片" align="center" width="100">
        <template slot-scope="scope">
          <img v-if="scope.row.blankImage" :src="baseUrl + scope.row.blankImage" style="width:60px;height:60px;object-fit:cover;border-radius:4px;cursor:pointer" @click="showDetail(scope.row)" />
          <span v-else style="color:#c0c4cc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="showDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['tech:blankImage:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['tech:blankImage:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" :page-sizes="[10, 20, 24, 30, 50]" @pagination="getList" />

    <!-- ★ 详情弹窗 ★ -->
    <el-dialog :title="detailData.modelCode || '毛胚图详情'" :visible.sync="detailVisible" width="80%" top="5vh" append-to-body center>
      <div class="detail-drawer" v-if="detailData.blankId">
        <!-- 大图 -->
        <div class="detail-image-wrap">
          <img v-if="detailData.blankImage" :src="baseUrl + detailData.blankImage" class="detail-image" />
          <div v-else class="detail-no-image">
            <i class="el-icon-picture-outline" style="font-size:60px"></i>
            <p>暂无毛胚图</p>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImageUpload"
              accept="image/*"
              v-hasPermi="['tech:blankImage:edit']"
            >
              <el-button size="small" type="primary" plain>上传图片</el-button>
            </el-upload>
          </div>
          <el-upload
            v-if="detailData.blankImage"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageUpload"
            accept="image/*"
            class="change-image-btn"
            v-hasPermi="['tech:blankImage:edit']"
          >
            <el-button size="mini" type="primary" plain icon="el-icon-camera">更换图片</el-button>
          </el-upload>
        </div>

        <!-- 基础信息 -->
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="型号">{{ detailData.modelCode }}</el-descriptions-item>
            <el-descriptions-item label="版本">
              <el-tag type="primary" size="mini">{{ detailData.version || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下发时间">{{ detailData.releaseDate || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button type="primary" size="small" icon="el-icon-edit" @click="handleUpdate(detailData)" v-hasPermi="['tech:blankImage:edit']">编辑</el-button>
          <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(detailData)" v-hasPermi="['tech:blankImage:remove']">删除</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="型号" prop="modelCode"><el-input v-model="form.modelCode" placeholder="如 00919F03" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本">
              <el-select v-model="form.version" style="width:100%">
                <el-option label="A" value="A" />
                <el-option label="B" value="B" />
                <el-option label="C" value="C" />
                <el-option label="D" value="D" />
                <el-option label="E" value="E" />
                <el-option label="F" value="F" />
                <el-option label="G" value="G" />
                <el-option label="H" value="H" />
                <el-option label="I" value="I" />
                <el-option label="J" value="J" />
                <el-option label="K" value="K" />
                <el-option label="L" value="L" />
                <el-option label="M" value="M" />
                <el-option label="N" value="N" />
                <el-option label="O" value="O" />
                <el-option label="P" value="P" />
                <el-option label="Q" value="Q" />
                <el-option label="R" value="R" />
                <el-option label="S" value="S" />
                <el-option label="T" value="T" />
                <el-option label="U" value="U" />
                <el-option label="V" value="V" />
                <el-option label="W" value="W" />
                <el-option label="X" value="X" />
                <el-option label="Y" value="Y" />
                <el-option label="Z" value="Z" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="下发时间"><el-input v-model="form.releaseDate" placeholder="如 2025/9/20" /></el-form-item>
        <el-form-item label="毛胚图">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleFormImageUpload"
            accept="image/*"
            list-type="picture"
          >
            <img v-if="form.blankImage" :src="baseUrl + form.blankImage" style="width:120px;height:120px;object-fit:cover;border-radius:8px" />
            <el-button v-else size="small" type="primary" plain icon="el-icon-upload">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入毛胚图数据" :visible.sync="importOpen" width="520px" append-to-body>
      <div style="margin-bottom:12px;padding:10px 14px;background:#e8f4fd;border-radius:4px;font-size:13px;color:#409EFF;line-height:1.8">
        上传你的 <b>毛胚图 Excel</b> 文件，系统会自动提取文字数据和 WPS 嵌入图片。
        <br/>如果数据不在第一个 Sheet，请确认列名包含「模号」「型号」「版本」「下发时间」。
      </div>

      <el-upload ref="importUpload" :limit="1" accept=".xlsx,.xls,.xlsm" :headers="uploadHeaders" :action="computedImportUrl" :disabled="importUploading" :on-progress="handleImportProgress" :on-success="handleImportSuccess" :on-error="handleImportError" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击选取</em></div>
      </el-upload>

      <div style="margin-top:12px;display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <el-checkbox v-model="importUpdateSupport">更新已有数据（按型号）</el-checkbox>
        <span style="font-size:12px;color:#909399">跳过前</span>
        <el-input-number v-model="importTitleNum" :min="0" :max="20" size="mini" style="width:100px" />
        <span style="font-size:12px;color:#909399">行</span>
      </div>

      <div slot="footer" style="display:flex;justify-content:space-between;align-items:center">
        <el-link type="primary" :underline="false" icon="el-icon-download" @click="handleImportTemplate">下载导入模板</el-link>
        <div>
          <el-button @click="importOpen = false">取消</el-button>
          <el-button type="primary" :loading="importUploading" @click="submitImport">开始导入</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 批量上传图片弹窗 -->
    <el-dialog title="批量上传毛胚图（智能导入）" :visible.sync="batchImageOpen" width="560px" append-to-body>
      <div style="margin-bottom:16px;padding:12px;background:#e8f4fd;border-radius:4px;font-size:13px;color:#409EFF;line-height:1.8">
        <b>操作说明：</b><br/>
        1. 将每张毛胚图用<b>型号命名</b>（如 <code>00919F03.jpg</code>）<br/>
        2. 选择全部图片，一次性上传<br/>
        3. 系统<b>自动根据文件名创建记录</b>（自动解析型号和模号）<br/>
        4. 如果记录已存在则更新图片<br/>
        <b style="color:#67C23A">✅ 这是从 WPS 导入毛胚图最简单的方式！</b>
      </div>
      <el-upload
        ref="batchImageUpload"
        :action="batchImageUrl"
        :headers="uploadHeaders"
        :auto-upload="false"
        :on-success="batchImageSuccess"
        :on-error="batchImageError"
        :file-list="batchImageFiles"
        multiple
        accept="image/*"
        list-type="picture"
        name="files"
      >
        <el-button size="small" type="primary" icon="el-icon-picture">选择图片（可多选）</el-button>
        <div slot="tip" class="el-upload__tip">图片文件名必须是型号，如 00919F03.jpg</div>
      </el-upload>
      <div slot="footer">
        <el-button @click="batchImageOpen = false">取消</el-button>
        <el-button type="primary" :loading="batchImageUploading" @click="submitBatchImages">开始匹配上传</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBlankImage, getBlankImage, delBlankImage, addBlankImage, updateBlankImage, cleanBlankImage } from "@/api/tech/blankImage"
import { getToken } from "@/utils/auth"

export default {
  name: "BlankImage",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      blankList: [],
      title: "",
      open: false,
      viewMode: "card",
      selectAll: false,
      // 详情抽屉
      detailVisible: false,
      detailData: {},
      // 导入
      importOpen: false,
      importUploading: false,
      importUpdateSupport: false,
      importTitleNum: 0,
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      batchImageUrl: process.env.VUE_APP_BASE_API + "/tech/blankImage/batchUploadImages",
      batchImageOpen: false,
      batchImageUploading: false,
      batchImageFiles: [],
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      baseUrl: process.env.VUE_APP_BASE_API,
      queryParams: { pageNum: 1, pageSize: 24, modelCode: null, moldNo: null, version: null },
      form: {},
      rules: { modelCode: [{ required: true, message: "型号不能为空", trigger: "blur" }] }
    }
  },
  computed: {
    computedImportUrl() {
      return process.env.VUE_APP_BASE_API + "/tech/blankImage/importData?updateSupport=" + this.importUpdateSupport + "&titleNum=" + this.importTitleNum
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listBlankImage(this.queryParams).then(res => {
        this.blankList = res.rows; this.total = res.total; this.loading = false
      })
    },
    showDetail(row) {
      getBlankImage(row.blankId).then(res => {
        this.detailData = res.data; this.detailVisible = true
      })
    },
    /** 图片上传成功（详情页） */
    handleImageUpload(res) {
      if (res.code === 200) {
        this.detailData.blankImage = res.fileName
        updateBlankImage({ blankId: this.detailData.blankId, blankImage: res.fileName }).then(() => {
          this.$modal.msgSuccess("图片上传成功")
          this.getList()
        })
      }
    },
    /** 图片上传成功（表单） */
    handleFormImageUpload(res) {
      if (res.code === 200) {
        this.$set(this.form, 'blankImage', res.fileName)
      }
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = { blankId: null, modelCode: null, moldNo: null, version: 'A', releaseDate: null, blankImage: null, remark: null }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(sel) { this.ids = sel.map(i => i.blankId); this.single = sel.length !== 1; this.multiple = !sel.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加毛胚图" },
    handleUpdate(row) {
      this.reset()
      getBlankImage(row.blankId).then(res => { this.form = res.data; this.open = true; this.title = "修改毛胚图"; this.detailVisible = false })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.blankId) {
            updateBlankImage(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addBlankImage(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除型号 "' + row.modelCode + '" ？').then(() => delBlankImage(row.blankId)).then(() => {
        this.getList(); this.detailVisible = false; this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$modal.confirm('确认删除选中的 ' + this.ids.length + ' 条毛胚图？').then(() => delBlankImage(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleCleanAll() {
      const q = { ...this.queryParams }; delete q.pageNum; delete q.pageSize;
      this.$modal.confirm('确认删除全部 ' + this.total + ' 条毛胚图？此操作不可恢复！').then(() => cleanBlankImage(q)).then(res => {
        this.selectAll = false; this.getList(); this.$modal.msgSuccess(res.msg || "删除成功")
      }).catch(() => {})
    },
    handleImport() { this.importOpen = true },
    handleImportTemplate() { this.download('tech/blankImage/importTemplate', {}, '毛胚图导入模板.xlsx') },
    submitImport() { this.$refs.importUpload.submit() },
    handleImportProgress() { this.importUploading = true },
    handleImportSuccess(res) {
      this.importUploading = false; this.$refs.importUpload.clearFiles()
      if (res.code === 200) { this.$modal.msgSuccess(res.msg || "导入成功"); this.importOpen = false; this.getList() }
      else { this.$modal.msgError(res.msg || "导入失败") }
    },
    handleImportError(err) {
      this.importUploading = false
      console.error('导入上传失败:', err)
      this.$modal.msgError("上传失败！可能原因：文件过大、网络超时、或后端处理异常。建议导入不含图片的纯文字Excel，再用'批量上传图片'功能补充图片。")
    },
    handleExport() { this.download('tech/blankImage/export', { ...this.queryParams }, '毛胚图.xlsx') },
    /** 批量图片上传 */
    submitBatchImages() {
      this.$refs.batchImageUpload.submit()
      this.batchImageUploading = true
    },
    batchImageSuccess(res) {
      this.batchImageUploading = false
      if (res.code === 200) {
        this.$modal.msgSuccess(res.msg || "上传匹配完成")
        this.batchImageOpen = false
        this.batchImageFiles = []
        this.$refs.batchImageUpload.clearFiles()
        this.getList()
      } else {
        this.$modal.msgError(res.msg || "上传失败")
      }
    },
    batchImageError() {
      this.batchImageUploading = false
      this.$modal.msgError("上传失败，请重试")
    }
  }
}
</script>

<style scoped lang="scss">
.stats-bar {
  padding: 8px 16px;
  background: #f8f9fc;
  border-radius: 4px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
  .stats-item b { color: #409EFF; }
}

/* ===== 卡片视图 ===== */
.card-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.25s;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
    border-color: #409EFF;
  }
}

.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.wheel-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.no-image {
  text-align: center;
  color: #c0c4cc;
  i { font-size: 48px; display: block; margin-bottom: 8px; }
  span { font-size: 13px; }
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  &.badge-primary { background: #ecf5ff; color: #409eff; }
}

.card-body {
  padding: 12px 14px 14px;
}

.wheel-code {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.card-info-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
}

.info-label {
  color: #909399;
  margin-right: 4px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  font-weight: 500;
}

.empty-card {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 0;
  color: #909399;
}

/* ===== 详情弹窗 ===== */
.detail-drawer {
  padding: 0 10px 10px;
}

.detail-image-wrap {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.detail-image {
  width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 12px;
  background: #f5f7fa;
}

.detail-no-image {
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  p { margin: 8px 0 12px; font-size: 14px; }
}

.change-image-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #409EFF;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>
