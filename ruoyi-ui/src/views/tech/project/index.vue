<template>
  <div class="app-container project-container">
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
      <div class="toolbar-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增项目</el-button>
        <el-button type="success" plain icon="el-icon-download" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <!-- 项目表格 -->
    <el-table 
      v-loading="loading" 
      :data="projectList" 
      @row-click="handleRowClick"
      :row-class-name="tableRowClassName"
      highlight-current-row
    >
      <el-table-column label="项目名称" align="left" prop="projectCode" min-width="120">
        <template slot-scope="scope">
          <span class="project-code clickable" @click.stop="openDetail(scope.row)">{{ scope.row.projectCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="80">
        <template slot-scope="scope">
          <el-tag :type="getPriorityType(scope.row.priority)" size="small">
            {{ scope.row.priority || '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" align="center" prop="startDate" width="110">
        <template slot-scope="scope">
          <span>{{ formatDate(scope.row.startDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="责任人" align="center" prop="managerName" width="80" />
      <el-table-column label="进度" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="getProgressType(scope.row.progress)" size="small" effect="plain">
            {{ scope.row.progress || 0 }}%
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="流程实况" align="left" min-width="280">
        <template slot-scope="scope">
          <div class="process-tags">
            <el-tag 
              v-for="(stage, index) in getProcessStages(scope.row)" 
              :key="index"
              :type="stage.type"
              size="mini"
              class="process-tag"
            >
              {{ stage.name }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="80">
        <template slot-scope="scope">
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

    <!-- 项目详情弹窗 -->
    <el-dialog 
      :visible.sync="detailOpen" 
      width="1100px" 
      :close-on-click-modal="false"
      custom-class="project-detail-dialog"
      :show-close="true"
    >
      <div class="detail-container">
        <!-- 左侧：产品图片和时间线 -->
        <div class="left-panel">
          <div class="image-section">
            <div class="section-header">
              <span>产品图片</span>
              <el-button size="mini" plain>填充模式</el-button>
            </div>
            <div class="image-box">
              <div class="image-label">正面图</div>
              <el-upload
                class="image-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleFrontImageChange"
              >
                <img v-if="detailForm.frontImage" :src="detailForm.frontImage" class="product-image">
                <i v-else class="el-icon-plus image-uploader-icon"></i>
              </el-upload>
            </div>
            <div class="image-box">
              <div class="image-label">反面图</div>
              <el-upload
                class="image-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
                :on-change="handleBackImageChange"
              >
                <img v-if="detailForm.backImage" :src="detailForm.backImage" class="product-image">
                <i v-else class="el-icon-plus image-uploader-icon"></i>
              </el-upload>
            </div>
          </div>
          
          <!-- 时间线 -->
          <div class="timeline-section">
            <div class="timeline-item">
              <span class="timeline-label">申请时间</span>
              <el-date-picker v-model="detailForm.applyDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">立项时间</span>
              <el-date-picker v-model="detailForm.approvalDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">模具时间</span>
              <el-date-picker v-model="detailForm.moldDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">试制时间</span>
              <el-date-picker v-model="detailForm.trialDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">首件送样</span>
              <el-date-picker v-model="detailForm.sampleDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">自检实验</span>
              <el-date-picker v-model="detailForm.testDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
            <div class="timeline-item">
              <span class="timeline-label">SOP时间</span>
              <el-date-picker v-model="detailForm.sopDate" type="date" value-format="yyyy/MM/dd" size="small" />
            </div>
          </div>
        </div>

        <!-- 右侧：表单信息 -->
        <div class="right-panel">
          <!-- 基本信息 -->
          <div class="info-section">
            <div class="section-title"><i class="el-icon-info"></i> 基本信息</div>
            <el-row :gutter="16">
              <el-col :span="6">
                <div class="form-item">
                  <label>戴卡部件号 <span class="required">*</span></label>
                  <el-input v-model="detailForm.projectCode" placeholder="请输入" />
                  <div class="form-tip">请输入完整的戴卡部件编号</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>项目状态 <span class="required">*</span></label>
                  <el-select v-model="detailForm.status" placeholder="请选择" style="width:100%">
                    <el-option label="评审中" value="评审中" />
                    <el-option label="进行中" value="进行中" />
                    <el-option label="已完成" value="已完成" />
                    <el-option label="暂停" value="暂停" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>客户名称</label>
                  <el-input v-model="detailForm.customer" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>责任人</label>
                  <el-input v-model="detailForm.managerName" placeholder="请输入" />
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="6">
                <div class="form-item">
                  <label>类型 <span class="required">*</span></label>
                  <el-select v-model="detailForm.projectType" placeholder="请选择" style="width:100%">
                    <el-option label="定点开发" value="定点开发" />
                    <el-option label="样件开发" value="样件开发" />
                    <el-option label="量产变更" value="量产变更" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>首模</label>
                  <el-input v-model="detailForm.firstMold" placeholder="如M1" />
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>流程编号</label>
                  <el-input v-model="detailForm.processCode" placeholder="自动生成" disabled />
                  <div class="form-tip">无需填写，系统自动生成</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>优先级</label>
                  <el-select v-model="detailForm.priority" placeholder="请选择" style="width:100%">
                    <el-option label="正常" value="正常" />
                    <el-option label="紧急" value="紧急" />
                    <el-option label="特急" value="特急" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 产品信息 -->
          <div class="info-section">
            <div class="section-title"><i class="el-icon-goods"></i> 产品信息</div>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="form-item">
                  <label>平台/车型</label>
                  <el-input v-model="detailForm.platform" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>产品尺寸</label>
                  <el-input v-model="detailForm.productSize" placeholder="格式: 直径x厚度" />
                  <div class="form-tip">格式: 直径x厚度 (如: 200x50)</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>ET值</label>
                  <el-input v-model="detailForm.etValue" placeholder="请输入" />
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="form-item">
                  <label>PCD</label>
                  <el-input v-model="detailForm.pcd" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>成型工艺</label>
                  <el-select v-model="detailForm.moldingProcess" placeholder="请选择" style="width:100%">
                    <el-option label="低压铸造" value="低压铸造" />
                    <el-option label="重力铸造" value="重力铸造" />
                    <el-option label="锻造" value="锻造" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>表面状态</label>
                  <el-select v-model="detailForm.surfaceState" placeholder="请选择" style="width:100%">
                    <el-option label="精车" value="精车" />
                    <el-option label="全涂装" value="全涂装" />
                    <el-option label="抛光" value="抛光" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="form-item">
                  <label>重量 (kg)</label>
                  <el-input-number v-model="detailForm.weight" :precision="2" :step="0.1" style="width:100%" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>客户部件号</label>
                  <el-input v-model="detailForm.customerPartNo" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>合同年量</label>
                  <el-input v-model="detailForm.annualVolume" placeholder="请输入" />
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 其他信息 -->
          <div class="info-section">
            <div class="section-title"><i class="el-icon-more"></i> 其他信息</div>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="form-item">
                  <label>上线地点</label>
                  <el-input v-model="detailForm.location" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>颜色代码</label>
                  <el-input v-model="detailForm.colorCode" placeholder="请输入" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>配套件</label>
                  <el-select v-model="detailForm.accessories" placeholder="请选择" style="width:100%">
                    <el-option label="否" value="否" />
                    <el-option label="是" value="是" />
                  </el-select>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="form-item">
                  <label>装配方式</label>
                  <el-select v-model="detailForm.assemblyMethod" placeholder="请选择状态" style="width:100%">
                    <el-option label="螺栓" value="螺栓" />
                    <el-option label="螺母" value="螺母" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>涂装工艺</label>
                  <el-input v-model="detailForm.paintProcess" placeholder="请输入" />
                  <div class="form-tip">如有特殊工艺要求，请在此注明</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="form-item">
                  <label>共模信息</label>
                  <el-input v-model="detailForm.sharedMold" placeholder="请输入" />
                  <div class="form-tip">如有输入轮型号，多个则用逗号隔开</div>
                </div>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <div class="form-item">
                  <label>项目补充描述</label>
                  <el-input v-model="detailForm.description" type="textarea" :rows="2" placeholder="请输入项目描述..." />
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="dialog-footer">
        <el-button type="danger" plain icon="el-icon-delete" @click="handleDeleteDetail">删除项目</el-button>
        <div class="right-buttons">
          <el-button type="warning" icon="el-icon-s-check" @click="handleReview">项目评审</el-button>
          <el-button plain icon="el-icon-printer" @click="handlePrint">打印预览</el-button>
          <el-button type="primary" icon="el-icon-check" @click="saveDetail">保存项目</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 简单新增对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" placeholder="请输入项目编号" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择" style="width:100%">
            <el-option label="正常" value="正常" />
            <el-option label="紧急" value="紧急" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="managerName">
          <el-input v-model="form.managerName" placeholder="请输入负责人" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listProject, getProject, delProject, addProject, updateProject } from "@/api/tech/project"

export default {
  name: "Project",
  data() {
    return {
      loading: true,
      total: 0,
      projectList: [],
      title: "",
      open: false,
      detailOpen: false,
      selectedRow: null,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        keyword: null,
      },
      form: {},
      detailForm: {
        projectId: null,
        projectCode: '',
        projectName: '',
        projectType: '定点开发',
        customer: '',
        managerName: '',
        status: '评审中',
        priority: '正常',
        firstMold: '',
        processCode: '',
        platform: '',
        productSize: '',
        etValue: '',
        pcd: '',
        moldingProcess: '低压铸造',
        surfaceState: '精车',
        weight: null,
        customerPartNo: '',
        annualVolume: '',
        location: '',
        colorCode: '',
        accessories: '否',
        assemblyMethod: '',
        paintProcess: '',
        sharedMold: '',
        description: '',
        frontImage: '',
        backImage: '',
        applyDate: null,
        approvalDate: null,
        moldDate: null,
        trialDate: null,
        sampleDate: null,
        testDate: null,
        sopDate: null,
      },
      rules: {
        projectCode: [{ required: true, message: "项目编号不能为空", trigger: "blur" }],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listProject(this.queryParams).then(response => {
        this.projectList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatDate(date) {
      if (!date) return 'N/A'
      return date.substring(0, 10).replace(/-/g, '/')
    },
    getPriorityType(priority) {
      const map = { '紧急': 'danger', '特急': 'danger', '正常': 'success' }
      return map[priority] || 'success'
    },
    getProgressType(progress) {
      if (progress >= 100) return 'success'
      if (progress >= 50) return 'warning'
      return 'info'
    },
    getStatusType(status) {
      const map = { '评审中': 'primary', '进行中': 'success', '已完成': 'info', '暂停': 'warning' }
      return map[status] || 'primary'
    },
    getStatusText(status) {
      return status || '评审中'
    },
    getProcessStages(row) {
      const stages = []
      const progress = row.progress || 0
      const stageConfig = [
        { name: '项目评审完成', key: 'review' },
        { name: '技术科', key: 'tech' },
        { name: '热工科', key: 'heat' },
        { name: '机加科', key: 'machine' },
        { name: '质量科', key: 'quality' },
        { name: '涂装科', key: 'paint' },
        { name: '生产计划科', key: 'plan' },
      ]
      stageConfig.forEach((stage, index) => {
        if (progress > index * 14) {
          stages.push({ name: stage.name, type: 'success' })
        }
      })
      if (stages.length === 0) {
        stages.push({ name: '项目评审完成', type: 'warning' })
      }
      return stages
    },
    tableRowClassName({ row }) {
      if (this.selectedRow && row.projectId === this.selectedRow.projectId) {
        return 'selected-row'
      }
      return ''
    },
    handleRowClick(row) {
      this.selectedRow = row
    },
    openDetail(row) {
      // 获取项目详情并打开弹窗
      getProject(row.projectId).then(response => {
        const data = response.data || row
        this.detailForm = {
          ...this.detailForm,
          projectId: data.projectId,
          projectCode: data.projectCode || '',
          projectName: data.projectName || '',
          projectType: data.projectType || '定点开发',
          customer: data.customer || '',
          managerName: data.managerName || '',
          status: data.status || '评审中',
          priority: data.priority || '正常',
          description: data.description || '',
          startDate: data.startDate,
        }
        this.detailOpen = true
      })
    },
    handleFrontImageChange(file) {
      this.detailForm.frontImage = URL.createObjectURL(file.raw)
    },
    handleBackImageChange(file) {
      this.detailForm.backImage = URL.createObjectURL(file.raw)
    },
    saveDetail() {
      const formData = {
        projectId: this.detailForm.projectId,
        projectCode: this.detailForm.projectCode,
        projectName: this.detailForm.projectName,
        projectType: this.detailForm.projectType,
        customer: this.detailForm.customer,
        managerName: this.detailForm.managerName,
        status: this.detailForm.status,
        priority: this.detailForm.priority,
        description: this.detailForm.description,
      }
      if (formData.projectId) {
        updateProject(formData).then(() => {
          this.$modal.msgSuccess("保存成功")
          this.detailOpen = false
          this.getList()
        })
      } else {
        addProject(formData).then(() => {
          this.$modal.msgSuccess("新增成功")
          this.detailOpen = false
          this.getList()
        })
      }
    },
    handleDeleteDetail() {
      if (this.detailForm.projectId) {
        this.$modal.confirm('是否确认删除该项目？').then(() => {
          return delProject(this.detailForm.projectId)
        }).then(() => {
          this.detailOpen = false
          this.getList()
          this.$modal.msgSuccess("删除成功")
        }).catch(() => {})
      }
    },
    handleReview() {
      this.$modal.msgSuccess("项目评审功能开发中...")
    },
    handlePrint() {
      this.$modal.msgSuccess("打印预览功能开发中...")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增项目"
    },
    reset() {
      this.form = {
        projectId: null,
        projectCode: null,
        projectName: null,
        priority: '正常',
        managerName: null,
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addProject(this.form).then(() => {
            this.$modal.msgSuccess("新增成功")
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除该项目？').then(() => {
        return delProject(row.projectId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('tech/project/export', { ...this.queryParams }, `project_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped lang="scss">
.project-container {
  padding: 20px;
}

.top-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .search-input {
    width: 350px;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
  }
}

.project-code {
  font-weight: 500;
  color: #303133;
  
  &.clickable {
    color: #409EFF;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.process-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

::v-deep .el-table {
  .selected-row {
    background-color: #e8f4e8 !important;
  }
}

/* 详情弹窗样式 */
::v-deep .project-detail-dialog {
  .el-dialog__header {
    padding: 0;
    display: none;
  }
  .el-dialog__body {
    padding: 0;
  }
}

.detail-container {
  display: flex;
  min-height: 600px;
}

.left-panel {
  width: 220px;
  background: #f8f9fa;
  padding: 20px;
  border-right: 1px solid #eee;
  
  .image-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      font-weight: 500;
    }
  }
  
  .image-box {
    margin-bottom: 15px;
    
    .image-label {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .image-uploader {
      width: 100%;
      
      ::v-deep .el-upload {
        width: 100%;
        height: 120px;
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        
        &:hover {
          border-color: #409EFF;
        }
      }
      
      .product-image {
        width: 100%;
        height: 120px;
        object-fit: cover;
        border-radius: 6px;
      }
      
      .image-uploader-icon {
        font-size: 28px;
        color: #8c939d;
      }
    }
  }
  
  .timeline-section {
    margin-top: 20px;
    
    .timeline-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      .timeline-label {
        width: 65px;
        font-size: 12px;
        color: #606266;
      }
      
      ::v-deep .el-date-editor {
        width: 130px;
      }
    }
  }
}

.right-panel {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-height: 600px;
}

.info-section {
  margin-bottom: 25px;
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
    
    i {
      margin-right: 6px;
      color: #409EFF;
    }
  }
}

.form-item {
  margin-bottom: 16px;
  
  label {
    display: block;
    font-size: 13px;
    color: #606266;
    margin-bottom: 6px;
    
    .required {
      color: #f56c6c;
    }
  }
  
  .form-tip {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background: #f8f9fa;
  
  .right-buttons {
    display: flex;
    gap: 10px;
  }
}
</style>
