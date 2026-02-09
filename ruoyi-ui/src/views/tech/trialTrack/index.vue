<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="模号" prop="moldCode">
        <el-input v-model="queryParams.moldCode" placeholder="请输入模号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模具类型" prop="moldType">
        <el-select v-model="queryParams.moldType" placeholder="全部" clearable>
          <el-option label="首模" value="首模" /><el-option label="改模" value="改模" /><el-option label="翻模" value="翻模" />
        </el-select>
      </el-form-item>
      <el-form-item label="表面状态" prop="surfaceStatus">
        <el-select v-model="queryParams.surfaceStatus" placeholder="全部" clearable>
          <el-option label="精车" value="精车" /><el-option label="全涂" value="全涂" /><el-option label="全涂装" value="全涂装" />
        </el-select>
      </el-form-item>
      <el-form-item label="上机类型" prop="machineType">
        <el-select v-model="queryParams.machineType" placeholder="全部" clearable>
          <el-option label="小批量" value="小批量" /><el-option label="量产" value="量产" />
        </el-select>
      </el-form-item>
      <el-form-item label="完成状态" prop="allProcessDone">
        <el-select v-model="queryParams.allProcessDone" placeholder="全部" clearable>
          <el-option label="是" value="是" /><el-option label="否" value="否" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['tech:trialTrack:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="el-icon-upload2" size="mini" @click="importOpen = true" v-hasPermi="['tech:trialTrack:import']">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['tech:trialTrack:export']">导出</el-button></el-col>
      <el-col :span="1.5">
        <el-button :type="viewMode==='card'?'primary':'default'" plain icon="el-icon-s-grid" size="mini" @click="viewMode='card'">卡片</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :type="viewMode==='table'?'primary':'default'" plain icon="el-icon-s-unfold" size="mini" @click="viewMode='table'">表格</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="stats-bar" v-if="total > 0">
      共 <b>{{ total }}</b> 条试制记录
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode==='card'" v-loading="loading" class="card-grid">
      <div v-for="item in trackList" :key="item.trackId" class="track-card" @click="showDetail(item)">
        <div class="card-top">
          <span class="mold-code">{{ item.moldCode }}</span>
          <span class="done-badge" :class="item.allProcessDone==='是'?'done-yes':'done-no'">
            {{ item.allProcessDone === '是' ? '已完成' : '进行中' }}
          </span>
        </div>
        <div class="card-tags">
          <el-tag v-if="item.moldType" size="mini" effect="plain" type="warning">{{ item.moldType }}</el-tag>
          <el-tag v-if="item.surfaceStatus" size="mini" effect="plain" type="success">{{ item.surfaceStatus }}</el-tag>
          <el-tag v-if="item.machineType" size="mini" effect="plain">{{ item.machineType }}</el-tag>
        </div>
        <div class="card-meta">
          <div class="meta-row" v-if="item.machineCount"><span class="meta-l">上机次数</span><span class="meta-v">{{ item.machineCount }} 次</span></div>
          <div class="meta-row" v-if="item.hotMachineDate"><span class="meta-l">热工上机</span><span class="meta-v">{{ item.hotMachineDate }}</span></div>
          <div class="meta-row" v-if="item.productSpec"><span class="meta-l">规格</span><span class="meta-v">{{ item.productSpec }}</span></div>
          <div class="meta-row" v-if="item.hotProduction"><span class="meta-l">热工情况</span><span class="meta-v truncate">{{ item.hotProduction }}</span></div>
        </div>
      </div>
      <div v-if="!loading && trackList.length===0" class="empty-card">
        <i class="el-icon-box" style="font-size:48px;color:#c0c4cc"></i>
        <p>暂无数据，请先导入WPS导出的Excel</p>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-if="viewMode==='table'" v-loading="loading" :data="trackList" border size="mini">
      <el-table-column label="模号" prop="moldCode" width="130">
        <template slot-scope="s"><el-link type="primary" @click="showDetail(s.row)">{{ s.row.moldCode }}</el-link></template>
      </el-table-column>
      <el-table-column label="模具类型" prop="moldType" width="85" align="center" />
      <el-table-column label="表面状态" prop="surfaceStatus" width="85" align="center" />
      <el-table-column label="上机类型" prop="machineType" width="85" align="center" />
      <el-table-column label="上机次数" prop="machineCount" width="75" align="center" />
      <el-table-column label="热工上机" prop="hotMachineDate" width="110" align="center" />
      <el-table-column label="保圆时间" prop="roundKeepTime" width="80" align="center" />
      <el-table-column label="热工生产情况" prop="hotProduction" min-width="180" show-overflow-tooltip />
      <el-table-column label="完成" prop="allProcessDone" width="60" align="center">
        <template slot-scope="s">
          <el-tag :type="s.row.allProcessDone==='是'?'success':'info'" size="mini">{{ s.row.allProcessDone || '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template slot-scope="s">
          <el-button size="mini" type="text" icon="el-icon-view" @click="showDetail(s.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(s.row)" v-hasPermi="['tech:trialTrack:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 详情抽屉 -->
    <el-drawer :title="detail.moldCode || '试制跟踪详情'" :visible.sync="detailVisible" size="560px" direction="rtl">
      <div class="detail-drawer" v-if="detail.trackId">
        <!-- 顶部概览 -->
        <div class="detail-header">
          <div class="dh-code">{{ detail.moldCode }}</div>
          <div class="dh-tags">
            <el-tag v-if="detail.moldType" effect="dark" size="small" type="warning">{{ detail.moldType }}</el-tag>
            <el-tag v-if="detail.surfaceStatus" effect="dark" size="small" type="success">{{ detail.surfaceStatus }}</el-tag>
            <el-tag v-if="detail.machineType" effect="dark" size="small">{{ detail.machineType }}</el-tag>
            <el-tag v-if="detail.allProcessDone==='是'" effect="dark" size="small" type="success">全序完成</el-tag>
            <el-tag v-else effect="dark" size="small" type="info">进行中</el-tag>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="产品规格">{{ detail.productSpec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="上机次数">{{ detail.machineCount || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预计上机">{{ detail.planMachineTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 热工阶段 -->
        <div class="detail-section">
          <div class="section-title">热工阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="上机日期">{{ detail.hotMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保圆时间">{{ detail.roundKeepTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产情况" :span="2">{{ detail.hotProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="改善记录" :span="2">{{ detail.improveRecord || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.hotImprovePerson || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 旋压阶段 -->
        <div class="detail-section">
          <div class="section-title">旋压阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="上机日期">{{ detail.spinMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="旋压机台">{{ detail.spinMachineStation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产情况" :span="2">{{ detail.spinProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="改模记录" :span="2">{{ detail.moldModifyRecord || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.spinImprovePerson || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 粗车阶段 -->
        <div class="detail-section">
          <div class="section-title">粗车阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="上机日期">{{ detail.roughMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.roughImprovePerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产情况" :span="2">{{ detail.roughProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="改善方案" :span="2">{{ detail.improvePlan || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 精车+涂装 -->
        <div class="detail-section">
          <div class="section-title">精车 / 涂装阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="精车上机">{{ detail.fineMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="精车情况">{{ detail.fineProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="涂装上机">{{ detail.paintMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="涂装情况">{{ detail.paintProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="涂装负责人">{{ detail.paintImprovePerson || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 实验/总结 -->
        <div class="detail-section">
          <div class="section-title">实验 / 总结</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="冲击试验日">{{ detail.impactTestDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="试验结果">{{ detail.impactTestResult || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成日期">{{ detail.completeDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="全序完成">{{ detail.allProcessDone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="失效溯源" :span="2">{{ detail.failProductTrace || '-' }}</el-descriptions-item>
            <el-descriptions-item label="失效分析" :span="2">{{ detail.failAnalysis || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产总结" :span="2">{{ detail.productionSummary || '-' }}</el-descriptions-item>
            <el-descriptions-item label="改善措施" :span="2">{{ detail.improveMeasures || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经验教训" :span="2">{{ detail.lessonsLearned || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-actions">
          <el-button type="primary" size="small" icon="el-icon-edit" @click="handleUpdate(detail)" v-hasPermi="['tech:trialTrack:edit']">编辑</el-button>
          <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(detail)" v-hasPermi="['tech:trialTrack:remove']">删除</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="formTitle" :visible.sync="formOpen" width="780px" append-to-body top="3vh">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="模号" prop="moldCode"><el-input v-model="form.moldCode" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="产品规格"><el-input v-model="form.productSpec" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="上机次数"><el-input-number v-model="form.machineCount" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="模具类型"><el-input v-model="form.moldType" placeholder="首模/改模" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="表面状态"><el-input v-model="form.surfaceStatus" placeholder="精车/全涂" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="上机类型"><el-input v-model="form.machineType" placeholder="小批量/量产" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">热工阶段</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="热工上机"><el-input v-model="form.hotMachineDate" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="保圆时间"><el-input v-model="form.roundKeepTime" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="负责人"><el-input v-model="form.hotImprovePerson" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="生产情况"><el-input v-model="form.hotProduction" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="改善记录"><el-input v-model="form.improveRecord" type="textarea" :rows="2" /></el-form-item>

        <el-divider content-position="left">旋压 / 粗车 / 精车 / 涂装</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="旋压上机"><el-input v-model="form.spinMachineDate" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="粗车上机"><el-input v-model="form.roughMachineDate" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="精车上机"><el-input v-model="form.fineMachineDate" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="涂装上机"><el-input v-model="form.paintMachineDate" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="完成日期"><el-input v-model="form.completeDate" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="全序完成">
              <el-select v-model="form.allProcessDone" style="width:100%">
                <el-option label="是" value="是" /><el-option label="否" value="否" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="formOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog title="导入OE试制跟踪数据" :visible.sync="importOpen" width="520px" append-to-body>
      <el-radio-group v-model="importMode" style="margin-bottom:16px;width:100%">
        <el-radio-button label="raw">直接导入WPS导出的Excel</el-radio-button>
        <el-radio-button label="template">使用标准模板</el-radio-button>
      </el-radio-group>
      <div v-if="importMode==='raw'" style="margin-bottom:12px;padding:10px 14px;background:#e8f4fd;border-radius:4px;font-size:13px;color:#409EFF;">
        在 WPS 在线表格中点 <b>"导出数据"</b> 保存为 Excel，然后直接上传。
      </div>
      <div v-else style="margin-bottom:12px;padding:10px 14px;background:#fdf6ec;border-radius:4px;font-size:13px;color:#E6A23C;">
        先<el-link type="primary" :underline="false" @click="downloadTemplate">下载模板</el-link>，按模板填入后上传。
      </div>
      <el-upload ref="importUpload" :limit="1" accept=".xlsx,.xls,.xlsm" :headers="headers" :action="importUrl" :disabled="uploading" :on-progress="()=>{uploading=true}" :on-success="importSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">拖到此处或<em>点击选取</em></div>
      </el-upload>
      <div style="margin-top:12px;display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <el-checkbox v-model="updateSupport">更新已有记录（按模号）</el-checkbox>
        <span style="font-size:12px;color:#909399">跳过前</span>
        <el-input-number v-model="titleNum" :min="0" :max="20" size="mini" style="width:100px" />
        <span style="font-size:12px;color:#909399">行</span>
      </div>
      <div slot="footer" style="display:flex;justify-content:space-between;align-items:center">
        <el-link v-if="importMode==='template'" type="primary" :underline="false" icon="el-icon-download" @click="downloadTemplate">下载模板</el-link>
        <span v-else></span>
        <div><el-button @click="importOpen=false">取消</el-button><el-button type="primary" :loading="uploading" @click="$refs.importUpload.submit()">开始导入</el-button></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTrialTrack, getTrialTrack, addTrialTrack, updateTrialTrack, delTrialTrack } from "@/api/tech/trialTrack"
import { getToken } from "@/utils/auth"

export default {
  name: "TrialTrack",
  data() {
    return {
      loading: true, trackList: [], total: 0, viewMode: "card", showSearch: true,
      queryParams: { pageNum: 1, pageSize: 20, moldCode: null, moldType: null, surfaceStatus: null, machineType: null, allProcessDone: null },
      // 详情
      detailVisible: false, detail: {},
      // 表单
      formOpen: false, formTitle: "", form: {},
      rules: { moldCode: [{ required: true, message: "模号不能为空", trigger: "blur" }] },
      // 导入
      importOpen: false, uploading: false, updateSupport: false, importMode: "raw", titleNum: 1,
      headers: { Authorization: "Bearer " + getToken() },
    }
  },
  computed: {
    importUrl() { return process.env.VUE_APP_BASE_API + "/tech/trialTrack/importData?titleNum=" + this.titleNum }
  },
  watch: {
    importMode(v) { this.titleNum = v === 'raw' ? 1 : 0 }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listTrialTrack(this.queryParams).then(r => { this.trackList = r.rows; this.total = r.total; this.loading = false })
    },
    showDetail(row) {
      getTrialTrack(row.trackId).then(r => { this.detail = r.data; this.detailVisible = true })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleAdd() {
      this.form = { moldCode: null, productSpec: null, moldType: null, surfaceStatus: null, machineType: null, machineCount: null, allProcessDone: '否' }
      this.formTitle = "新增试制跟踪"; this.formOpen = true
    },
    handleUpdate(row) {
      getTrialTrack(row.trackId).then(r => { this.form = r.data; this.formTitle = "修改试制跟踪"; this.formOpen = true; this.detailVisible = false })
    },
    submitForm() {
      this.$refs.form.validate(v => {
        if (!v) return
        if (this.form.trackId) {
          updateTrialTrack(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.formOpen = false; this.getList() })
        } else {
          addTrialTrack(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.formOpen = false; this.getList() })
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除模号 "' + row.moldCode + '" ？').then(() => delTrialTrack(row.trackId)).then(() => {
        this.getList(); this.detailVisible = false; this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    downloadTemplate() { this.download('tech/trialTrack/importTemplate', {}, 'OE试制跟踪导入模板.xlsx') },
    importSuccess(res) {
      this.uploading = false; this.$refs.importUpload.clearFiles()
      if (res.code === 200) { this.$modal.msgSuccess(res.msg); this.importOpen = false; this.getList() }
      else { this.$modal.msgError(res.msg || "导入失败") }
    },
    handleExport() { this.download('tech/trialTrack/export', { ...this.queryParams }, 'OE试制跟踪.xlsx') }
  }
}
</script>

<style scoped lang="scss">
.stats-bar { padding:8px 16px; background:#f8f9fc; border-radius:4px; margin-bottom:12px; font-size:13px; color:#606266; b { color:#409EFF; } }

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  min-height: 200px;
}
.track-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); border-color: #409EFF; }
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.mold-code { font-size: 17px; font-weight: 700; color: #303133; }
.done-badge {
  font-size: 12px; padding: 2px 10px; border-radius: 10px; font-weight: 500;
  &.done-yes { background: #f0f9eb; color: #67c23a; }
  &.done-no { background: #ecf5ff; color: #409eff; }
}
.card-tags { display: flex; gap: 6px; margin-bottom: 10px; flex-wrap: wrap; }
.card-meta { font-size: 12px; }
.meta-row { display: flex; margin-bottom: 4px; }
.meta-l { color: #909399; width: 70px; flex-shrink: 0; }
.meta-v { color: #606266; font-weight: 500; }
.truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 160px; display: inline-block; }
.empty-card { grid-column: 1/-1; text-align: center; padding: 80px 0; color: #909399; }

.detail-drawer { padding: 0 20px 20px; }
.detail-header { text-align: center; margin-bottom: 20px; padding: 16px; background: #f8f9fc; border-radius: 10px; }
.dh-code { font-size: 22px; font-weight: 700; color: #303133; margin-bottom: 10px; }
.dh-tags { display: flex; gap: 6px; justify-content: center; flex-wrap: wrap; }
.detail-section { margin-bottom: 14px; }
.section-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 8px; padding-left: 10px; border-left: 3px solid #409EFF; }
.detail-actions { text-align: center; padding: 16px 0; border-top: 1px solid #ebeef5; margin-top: 8px; }
</style>
