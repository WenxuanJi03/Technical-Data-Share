<template>
  <div class="app-container trial-process-container">
    <!-- 顶部操作栏 -->
    <div class="top-toolbar">
      <el-input
        v-model="queryParams.moldCode"
        placeholder="输入轮形-模号..."
        prefix-icon="el-icon-search"
        class="search-input"
        clearable
        @keyup.enter.native="handleQuery"
      />
      <el-button type="primary" icon="el-icon-search" @click="handleQuery">查找</el-button>
      <div class="toolbar-buttons">
        <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">发起试制</el-button>
      </div>
      <el-button type="danger" plain icon="el-icon-close" style="margin-left:auto" @click="$router.go(-1)">关闭</el-button>
    </div>

    <!-- 流程列表 -->
    <div class="process-list" v-loading="loading">
      <div v-for="item in processList" :key="item.processId" class="process-card">
        <div class="process-header">
          <span class="process-title">{{ item.moldCode }}</span>
          <span class="process-initiator">发起人：{{ item.initiator }}</span>
          <span class="process-date">{{ formatDate(item.createTime) }}</span>
          <span class="process-desc" v-if="item.description">{{ item.description }}</span>
        </div>
        
        <!-- 流程节点 -->
        <div class="process-steps">
          <div 
            v-for="(step, index) in getSteps(item)" 
            :key="index" 
            class="step-item"
            :class="getStepClass(step)"
          >
            <div class="step-icon" :class="getStepIconClass(step)">
              <i v-if="step.status === 'done'" class="el-icon-check"></i>
              <i v-else :class="getStepIcon(step.name)"></i>
            </div>
            <div class="step-name">{{ step.name }}</div>
            <div class="step-responsible" v-if="step.responsible">
              <i class="el-icon-user"></i> {{ step.responsible }}
            </div>
            <div class="step-deadline" v-if="step.deadline">
              <i class="el-icon-time"></i> {{ step.deadline }}
            </div>
            <div class="step-status-tag" v-if="step.status === 'done'">
              <el-tag type="info" size="mini" effect="plain">已完成</el-tag>
            </div>
            <div class="step-actions">
              <el-button size="mini" type="text" icon="el-icon-upload2" @click="openUpload(item, step, index)">录入/上传</el-button>
              <el-button v-if="canEditPhase(index)" size="mini" type="text" icon="el-icon-edit" @click="openComment(item, step, index)">意见</el-button>
              <template v-if="canEditPhase(index) && step.status !== 'done'">
                <el-button size="mini" type="text" icon="el-icon-date" @click="setDeadline(item, step, index)">截止</el-button>
                <el-button size="mini" type="text" icon="el-icon-circle-check" style="color:#67c23a" @click="markStepDone(item, index)">完成</el-button>
              </template>
              <template v-else-if="canEditPhase(index) && step.status === 'done'">
                <el-button size="mini" type="text" icon="el-icon-refresh-left" style="color:#e6a23c" @click="markStepUndo(item, index)">撤回</el-button>
              </template>
            </div>
            <div class="step-line" v-if="index < 7" :class="{ 'line-done': step.status === 'done' }"></div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="process-footer">
          <el-button size="small" @click="handleEdit(item)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(item)">删除</el-button>
        </div>
      </div>

      <div v-if="processList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-folder-opened"></i>
        <p>暂无试制流程</p>
      </div>
    </div>

    <!-- 发起试制对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="轮形-模号" prop="moldCode">
          <el-input v-model="form.moldCode" placeholder="请输入轮形-模号，如 07122C26-M3" />
        </el-form-item>
        <el-form-item label="发起人" prop="initiator">
          <el-select v-model="form.initiator" placeholder="请选择发起人" filterable style="width:100%">
            <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
          </el-select>
        </el-form-item>
        <el-form-item label="试制说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入试制说明" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </div>
    </el-dialog>

    <!-- 阶段数据录入 + 文件上传合并对话框 -->
    <el-dialog :title="currentStep.name + ' — 数据录入与文件上传'" :visible.sync="uploadVisible" width="760px" append-to-body>
      <div class="upload-section">
        <!-- OE试制跟踪数据录入区 -->
        <el-alert
          title="以下数据与 OE试制跟踪 卡片联动，保存后可在跟踪卡片中直接查看"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom:16px"
        />
        <el-form :model="oeForm" label-width="120px" size="small">
          <el-alert v-if="!canEditPhase(currentStepIndex)" title="您没有此节点的编辑权限，仅可查看" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
          <!-- 基础信息 -->
          <template v-if="currentPhase === 'base'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="产品规格"><el-input v-model="oeForm.productSpec" placeholder="请输入产品规格" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="上机次数"><el-input-number v-model="oeForm.machineCount" :min="0" style="width:100%" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="模具类型"><el-input v-model="oeForm.moldType" placeholder="首模/改模等" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="表面状态"><el-input v-model="oeForm.surfaceStatus" placeholder="精车/全涂等" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="上机类型"><el-input v-model="oeForm.machineType" placeholder="小批量/量产等" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="预计上机"><el-date-picker v-model="oeForm.planMachineTime" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
            </el-row>
          </template>
          <!-- 压铸阶段 -->
          <template v-else-if="currentPhase === 'hot'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="压铸上机日期"><el-date-picker v-model="oeForm.hotMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="机台"><el-input v-model="oeForm.hotMachineStation" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="保压时间"><el-input v-model="oeForm.roundKeepTime" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="负责人"><el-input v-model="oeForm.hotImprovePerson" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="测量数据"><el-input v-model="oeForm.hotCheckMeasureData" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="生产情况"><el-input v-model="oeForm.hotProduction" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="改善记录"><el-input v-model="oeForm.improveRecord" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
          <!-- 旋压阶段 -->
          <template v-else-if="currentPhase === 'spin'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="旋压上机日期"><el-date-picker v-model="oeForm.spinMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="旋压机台"><el-input v-model="oeForm.spinMachineStation" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="负责人"><el-input v-model="oeForm.spinImprovePerson" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="生产情况"><el-input v-model="oeForm.spinProduction" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="改模记录"><el-input v-model="oeForm.moldModifyRecord" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
          <!-- 热处理阶段 -->
          <template v-else-if="currentPhase === 'heat'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="下转时间"><el-date-picker v-model="oeForm.heatTransferTime" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="接收数量"><el-input v-model="oeForm.heatReceiveCount" type="number" oninput="if(value.length>10)value=value.slice(0,10)" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="转下数量"><el-input v-model="oeForm.heatTransferCount" type="number" oninput="if(value.length>10)value=value.slice(0,10)" style="width:50%" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-alert title="请在下方附件区上传流转单照片" type="info" :closable="false" style="margin-bottom:15px"/>
          </template>
          <!-- 粗车阶段 -->
          <template v-else-if="currentPhase === 'rough'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="粗车上机日期"><el-date-picker v-model="oeForm.roughMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="负责人"><el-input v-model="oeForm.roughImprovePerson" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="生产情况"><el-input v-model="oeForm.roughProduction" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="改善方案"><el-input v-model="oeForm.improvePlan" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
          <!-- 精车阶段 -->
          <template v-else-if="currentPhase === 'fine'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="精车上机日期"><el-date-picker v-model="oeForm.fineMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="精车负责人"><el-input v-model="oeForm.fineImprovePerson" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="精车生产情况"><el-input v-model="oeForm.fineProduction" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
          <!-- 涂装阶段 -->
          <template v-else-if="currentPhase === 'paint'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="涂装上机日期"><el-date-picker v-model="oeForm.paintMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="涂装负责人"><el-input v-model="oeForm.paintImprovePerson" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="涂装生产情况"><el-input v-model="oeForm.paintProduction" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
          <!-- 实验/总结 -->
          <template v-else-if="currentPhase === 'test'">
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="冲击试验日期"><el-date-picker v-model="oeForm.impactTestDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="冲击试验结果"><el-input v-model="oeForm.impactTestResult" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12"><el-form-item label="生产完成日期"><el-date-picker v-model="oeForm.completeDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" :disabled="!canEditPhase(currentStepIndex)" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12">
                <el-form-item label="全序是否完成">
                  <el-select v-model="oeForm.allProcessDone" style="width:100%" :disabled="!canEditPhase(currentStepIndex)">
                    <el-option label="是" value="是" /><el-option label="否" value="否" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="实验说明"><el-input v-model="oeForm.testDescription" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="实验关闭情况"><el-input v-model="oeForm.testCloseStatus" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="失效产品清场"><el-input v-model="oeForm.failProductTrace" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="实验失效分析"><el-input v-model="oeForm.failAnalysis" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="本次生产总结"><el-input v-model="oeForm.productionSummary" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="改善措施简述"><el-input v-model="oeForm.improveMeasures" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
            <el-form-item label="经验教训总结"><el-input v-model="oeForm.lessonsLearned" type="textarea" :rows="2" :disabled="!canEditPhase(currentStepIndex)" /></el-form-item>
          </template>
        </el-form>

        <!-- 附件上传区 -->
        <el-divider content-position="left"><i class="el-icon-paperclip"></i> 附件上传</el-divider>
        <p class="upload-tip">支持上传 PDF、JPG、PNG、GIF、BMP 等格式文件</p>
        <el-upload
          v-show="canEditPhase(currentStepIndex)"
          class="upload-area"
          drag
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :file-list="fileList"
          multiple
          accept=".pdf,.jpg,.jpeg,.png,.gif,.bmp"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
        <!-- 历史上传记录 -->
        <div class="history-section" v-if="historyFiles.length > 0">
          <div class="history-title"><i class="el-icon-folder-opened"></i> 已上传文件</div>
          <div class="image-gallery">
            <div v-for="(file, idx) in historyFiles" :key="idx" class="gallery-item">
              <template v-if="isImageFile(file.name)">
                <el-image :src="file.url" :preview-src-list="getPreviewList()" fit="cover" class="thumbnail">
                  <div slot="error" class="image-error"><i class="el-icon-picture-outline"></i></div>
                </el-image>
              </template>
              <template v-else>
                <div class="file-icon-box"><i class="el-icon-document"></i></div>
              </template>
              <div class="file-info">
                <span class="file-name" :title="file.name">{{ file.name }}</span>
                <span class="file-time">{{ formatFileTime(file.time) }}</span>
              </div>
              <el-button v-if="canEditPhase(currentStepIndex)" type="text" icon="el-icon-delete" class="delete-btn" @click="deleteHistoryFile(idx)"></el-button>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="uploadVisible = false">关闭</el-button>
        <el-button v-if="canEditPhase(currentStepIndex)" type="primary" icon="el-icon-check" @click="submitUploadAndOE" :loading="submitLoading">保存OE跟踪数据</el-button>
      </div>
    </el-dialog>

    <!-- 发表意见对话框 -->
    <el-dialog title="发表意见" :visible.sync="commentVisible" width="600px" append-to-body>
      <div class="comment-section">
        <p class="comment-info">流程节点：<strong>{{ currentStep.name }}</strong></p>
        <el-alert v-if="!canEditPhase(currentStepIndex)" title="您没有此节点的编辑权限，仅可查看" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="3"
          placeholder="请输入您的意见..."
          :disabled="!canEditPhase(currentStepIndex)"
        />
        <!-- 历史意见记录 -->
        <div class="history-section" v-if="historyComments.length > 0">
          <div class="history-title"><i class="el-icon-chat-line-square"></i> 历史意见记录</div>
          <div class="comment-history-list">
            <div v-for="(c, index) in historyComments" :key="index" class="comment-history-item">
              <div class="comment-header">
                <span class="comment-user"><i class="el-icon-user"></i> {{ c.user }}</span>
                <span class="comment-time">{{ c.time }}</span>
                <div v-if="canEditPhase(currentStepIndex)" class="comment-actions">
                  <el-button type="text" size="mini" icon="el-icon-edit" @click="editComment(index)">编辑</el-button>
                  <el-button type="text" size="mini" icon="el-icon-delete" style="color:#f56c6c" @click="deleteComment(index)">删除</el-button>
                </div>
              </div>
              <div class="comment-content" v-if="editingCommentIndex !== index">{{ c.content }}</div>
              <div class="comment-edit" v-else>
                <el-input v-model="editingCommentText" type="textarea" :rows="2" />
                <div class="edit-actions">
                  <el-button size="mini" @click="cancelEditComment">取消</el-button>
                  <el-button size="mini" type="primary" @click="saveEditComment(index)">保存</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button v-if="canEditPhase(currentStepIndex)" type="primary" @click="submitComment">提交意见</el-button>
      </div>
    </el-dialog>

    <!-- 设置截止日期对话框 -->
    <el-dialog title="设置截止日期和负责人" :visible.sync="deadlineVisible" width="450px" append-to-body>
      <div class="deadline-section">
        <p>流程节点：<strong>{{ currentStep.name }}</strong></p>
        <el-form label-width="80px" style="margin-top:15px">
          <el-form-item label="截止日期">
            <el-date-picker
              v-model="deadlineDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="选择截止日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="负责人">
            <el-select v-model="stepResponsible" placeholder="选择负责人" filterable clearable style="width:100%">
              <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer">
        <el-button @click="deadlineVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDeadline">确定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listTrialProcess, getTrialProcess, addTrialProcess, updateTrialProcess, delTrialProcess } from "@/api/tech/trialProcess"
import { getAllUsers } from "@/api/tech/common"
import { getToken } from "@/utils/auth"
import { listTrialTrack, addTrialTrack, updateTrialTrack } from "@/api/tech/trialTrack"

export default {
  name: "TrialProcess",
  data() {
    return {
      loading: false,
      submitLoading: false,
      queryParams: { 
        moldCode: '',
        pageNum: 1,
        pageSize: 100
      },
      processList: [],
      dialogVisible: false,
      dialogTitle: '发起试制',
      form: {},
      rules: {
        moldCode: [{ required: true, message: '请输入轮形-模号', trigger: 'blur' }],
        initiator: [{ required: true, message: '请选择发起人', trigger: 'blur' }]
      },
      uploadVisible: false,
      fileList: [],
      historyFiles: [],
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      currentProcess: null,
      currentStep: {},
      currentStepIndex: 0,
      commentVisible: false,
      commentText: '',
      historyComments: [],
      editingCommentIndex: -1,
      editingCommentText: '',
      deadlineVisible: false,
      deadlineDate: null,
      stepResponsible: '',
      stepNames: ['基础信息', '压铸阶段', '旋压阶段', '热处理阶段', '粗车阶段', '精车阶段', '涂装阶段', '实验/总结'],
      userList: [],
      // OE 试制跟踪阶段数据（与上传对话框合并）
      oeForm: {},
      currentPhase: ''
    }
  },
  created() {
    this.getList()
    this.loadUserList()
  },
  methods: {
    getList() {
      this.loading = true
      listTrialProcess(this.queryParams).then(response => {
        this.processList = response.rows || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    loadUserList() {
      getAllUsers().then(response => {
        this.userList = response.rows || []
      })
    },
    formatDate(date) {
      if (!date) return ''
      return date.substring(0, 10)
    },
    getSteps(item) {
      return [
        { name: '基础信息',    status: item.step1Status, deadline: this.formatDate(item.step1Deadline), responsible: item.step1Responsible },
        { name: '压铸阶段',    status: item.step2Status, deadline: this.formatDate(item.step2Deadline), responsible: item.step2Responsible },
        { name: '旋压阶段',    status: item.step3Status, deadline: this.formatDate(item.step3Deadline), responsible: item.step3Responsible },
        { name: '热处理阶段',  status: item.step4Status, deadline: this.formatDate(item.step4Deadline), responsible: item.step4Responsible },
        { name: '粗车阶段',    status: item.step5Status, deadline: this.formatDate(item.step5Deadline), responsible: item.step5Responsible },
        { name: '精车阶段',    status: item.step6Status, deadline: this.formatDate(item.step6Deadline), responsible: item.step6Responsible },
        { name: '涂装阶段',    status: item.step7Status, deadline: this.formatDate(item.step7Deadline), responsible: item.step7Responsible },
        { name: '实验/总结',   status: item.step8Status, deadline: this.formatDate(item.step8Deadline), responsible: item.step8Responsible }
      ]
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 判断步骤是否临近截止或已过期（截止日期当天或前一天） */
    isExpired(step) {
      if (!step.deadline || step.status === 'done') return false
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const deadlineDate = new Date(step.deadline + 'T00:00:00')
      const diffDays = Math.floor((deadlineDate - today) / (1000 * 60 * 60 * 24))
      return diffDays <= 1 // 截止日期当天(0)或前一天(1)都标红
    },
    /** 获取步骤整体CSS类 */
    getStepClass(step) {
      if (step.status === 'done') return 'is-done'
      if (this.isExpired(step)) return 'is-expired'
      // 有截止日期或负责人但未到期的也算进行中，显示蓝色
      if (step.status === 'active' || step.deadline || step.responsible) return 'is-active'
      return ''
    },
    /** 获取步骤图标CSS类 */
    getStepIconClass(step) {
      if (step.status === 'done') return 'icon-done'
      if (this.isExpired(step)) return 'icon-expired'
      if (step.status === 'active' || step.deadline || step.responsible) return 'icon-active'
      return ''
    },
    /** 标记步骤为已完成 */
    markStepDone(item, index) {
      const stepNum = index + 1
      this.$confirm(`确认将"${this.stepNames[index]}"标记为已完成？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
      }).then(() => {
        const updateData = { processId: item.processId }
        updateData[`step${stepNum}Status`] = 'done'
        // 自动激活下一个步骤
        if (stepNum < 8) {
          const nextKey = `step${stepNum + 1}Status`
          if (!item[nextKey] || item[nextKey] === 'pending') {
            updateData[nextKey] = 'active'
          }
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('已标记为完成')
          this.getList()
        })
      }).catch(() => {})
    },
    /** 撤回步骤完成状态 */
    markStepUndo(item, index) {
      const stepNum = index + 1
      this.$confirm(`确认撤回"${this.stepNames[index]}"的完成状态？`, '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        const updateData = { processId: item.processId }
        updateData[`step${stepNum}Status`] = 'active'
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('已撤回')
          this.getList()
        })
      }).catch(() => {})
    },
    getStepIcon(name) {
      const icons = {
        '基础信息': 'el-icon-document',
        '压铸阶段': 'el-icon-s-opportunity',
        '旋压阶段': 'el-icon-s-operation',
        '热处理阶段': 'el-icon-sunny',
        '粗车阶段': 'el-icon-truck',
        '精车阶段': 'el-icon-setting',
        '涂装阶段': 'el-icon-brush',
        '实验/总结': 'el-icon-notebook-2'
      }
      return icons[name] || 'el-icon-more'
    },
    getFileIcon(filename) {
      if (filename.endsWith('.pdf')) return 'el-icon-document'
      return 'el-icon-picture'
    },
    handleAdd() {
      this.dialogTitle = '发起试制'
      this.form = {
        moldCode: '',
        initiator: '',
        description: '',
        status: 'active',
        step1Status: 'active',
        step2Status: 'pending',
        step3Status: 'pending',
        step4Status: 'pending',
        step5Status: 'pending',
        step6Status: 'pending',
        step7Status: 'pending',
        step8Status: 'pending'
      }
      this.dialogVisible = true
    },
    handleEdit(item) {
      this.dialogTitle = '编辑试制'
      this.form = { ...item }
      this.dialogVisible = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          if (this.form.processId) {
            updateTrialProcess(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.dialogVisible = false
              this.getList()
            }).finally(() => {
              this.submitLoading = false
            })
          } else {
            addTrialProcess(this.form).then(() => {
              this.$modal.msgSuccess('发起成功')
              this.dialogVisible = false
              this.getList()
            }).finally(() => {
              this.submitLoading = false
            })
          }
        }
      })
    },
    handleDelete(item) {
      this.$modal.confirm('确认删除该试制流程？').then(() => {
        return delTrialProcess(item.processId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    openUpload(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.currentPhase = this.getPhaseKeyByIndex(index)
      this.fileList = []
      const stepKey = `step${index + 1}Files`
      try {
        this.historyFiles = process[stepKey] ? JSON.parse(process[stepKey]) : []
      } catch (e) {
        this.historyFiles = []
      }
      // 加载对应的OE试制跟踪数据
      if (process.moldCode) {
        listTrialTrack({ pageNum: 1, pageSize: 1, moldCode: process.moldCode }).then(res => {
          const rows = res.rows || []
          if (rows.length > 0) {
            const loaded = { ...rows[0] }
            const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
              'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
            dateFields.forEach(f => { if (loaded[f] && typeof loaded[f] !== 'string') { loaded[f] = new Date(loaded[f]).toISOString().substring(0, 10) } })
            this.oeForm = loaded
          } else {
            this.oeForm = { moldCode: process.moldCode, allProcessDone: '否' }
          }
          this.uploadVisible = true
        }).catch(() => {
          this.oeForm = { moldCode: process.moldCode, allProcessDone: '否' }
          this.uploadVisible = true
        })
      } else {
        this.oeForm = { allProcessDone: '否' }
        this.uploadVisible = true
      }
    },
    submitUploadAndOE() {
      // 自动同步附件照片到OE相应的图片字段
      const imageUrls = this.historyFiles.filter(f => this.isImageFile(f.name)).map(f => f.url).join(',');
      if (imageUrls) {
        if (this.currentPhase === 'hot') this.oeForm.hotCheckMeasureImage = imageUrls;
        else if (this.currentPhase === 'spin') this.oeForm.spinFrontDistanceImage = imageUrls;
        else if (this.currentPhase === 'heat') this.oeForm.heatFlowSheetImage = imageUrls;
        else if (this.currentPhase === 'paint') this.oeForm.paintFlowSheetImage = imageUrls;
      }

      // 提交前把所有日期字段转成字符串，避免 Jackson 反序列化失败
      const payload = this.normalizeOeFormDates({ ...this.oeForm })
      const save = payload.trackId ? updateTrialTrack : addTrialTrack
      save(payload).then(() => {
        this.$modal.msgSuccess('OE试制跟踪数据已保存')
        this.uploadVisible = false
      }).catch(() => {
        this.$modal.msgError('保存失败，请重试')
      })
    },
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        // 上传成功，保存文件信息到数据库
        const stepKey = `step${this.currentStepIndex + 1}Files`
        const existingFiles = this.currentProcess[stepKey] ? JSON.parse(this.currentProcess[stepKey]) : []
        const newFile = {
          name: file.name,
          url: response.url || response.fileName,
          time: new Date().toISOString()
        }
        const updateData = {
          processId: this.currentProcess.processId,
          [stepKey]: JSON.stringify([...existingFiles, newFile])
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess(`${file.name} 上传成功`)
          this.getList()
          // 刷新历史文件列表
          this.historyFiles = [...existingFiles, newFile]
        })
      } else {
        this.$modal.msgError(response.msg || '上传失败')
      }
    },
    handleUploadError(err, file) {
      this.$modal.msgError(`${file.name} 上传失败`)
    },
    isImageFile(filename) {
      if (!filename) return false
      const ext = filename.toLowerCase().split('.').pop()
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
    },
    // 提交前将所有日期字段转为 YYYY-MM-DD 字符串，避免 Jackson 报错
    normalizeOeFormDates(form) {
      const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
        'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
      dateFields.forEach(f => {
        if (form[f] === null || form[f] === undefined || form[f] === '') return
        if (typeof form[f] === 'string') { form[f] = form[f].substring(0, 10); return }
        try { form[f] = new Date(form[f]).toISOString().substring(0, 10) } catch(e) { form[f] = null }
      })
      return form
    },
    getPreviewList() {
      return this.historyFiles
        .filter(f => this.isImageFile(f.name))
        .map(f => f.url)
    },
    formatFileTime(time) {
      if (!time) return ''
      try {
        const date = new Date(time)
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5)
      } catch (e) {
        return time
      }
    },
    deleteHistoryFile(fileIndex) {
      this.$modal.confirm('确认删除此文件？').then(() => {
        const stepKey = `step${this.currentStepIndex + 1}Files`
        this.historyFiles.splice(fileIndex, 1)
        const updateData = {
          processId: this.currentProcess.processId,
          [stepKey]: JSON.stringify(this.historyFiles)
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('删除成功')
          this.getList()
        })
      }).catch(() => {})
    },
    openComment(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.commentText = ''
      this.editingCommentIndex = -1
      this.editingCommentText = ''
      // 加载历史意见
      const stepKey = `step${index + 1}Comments`
      try {
        this.historyComments = process[stepKey] ? JSON.parse(process[stepKey]) : []
      } catch (e) {
        this.historyComments = []
      }
      this.commentVisible = true
    },
    editComment(commentIndex) {
      this.editingCommentIndex = commentIndex
      this.editingCommentText = this.historyComments[commentIndex].content
    },
    cancelEditComment() {
      this.editingCommentIndex = -1
      this.editingCommentText = ''
    },
    saveEditComment(commentIndex) {
      if (!this.editingCommentText.trim()) {
        this.$modal.msgWarning('意见内容不能为空')
        return
      }
      const stepKey = `step${this.currentStepIndex + 1}Comments`
      this.historyComments[commentIndex].content = this.editingCommentText
      this.historyComments[commentIndex].time = new Date().toLocaleString() + ' (已编辑)'
      const updateData = {
        processId: this.currentProcess.processId,
        [stepKey]: JSON.stringify(this.historyComments)
      }
      updateTrialProcess(updateData).then(() => {
        this.$modal.msgSuccess('修改成功')
        this.editingCommentIndex = -1
        this.editingCommentText = ''
        this.getList()
      })
    },
    deleteComment(commentIndex) {
      this.$modal.confirm('确认删除此意见？').then(() => {
        const stepKey = `step${this.currentStepIndex + 1}Comments`
        this.historyComments.splice(commentIndex, 1)
        const updateData = {
          processId: this.currentProcess.processId,
          [stepKey]: JSON.stringify(this.historyComments)
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('删除成功')
          this.getList()
        })
      }).catch(() => {})
    },
    submitComment() {
      if (this.commentText.trim()) {
        const stepKey = `step${this.currentStepIndex + 1}Comments`
        const existingComments = this.currentProcess[stepKey] ? JSON.parse(this.currentProcess[stepKey]) : []
        const newComment = {
          user: '当前用户',
          time: new Date().toLocaleString(),
          content: this.commentText
        }
        const updateData = {
          processId: this.currentProcess.processId,
          [stepKey]: JSON.stringify([...existingComments, newComment])
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('意见已提交')
          this.commentVisible = false
          this.getList()
        })
      } else {
        this.$modal.msgWarning('请输入意见内容')
      }
    },
    setDeadline(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.deadlineDate = step.deadline
      this.stepResponsible = step.responsible || ''
      this.deadlineVisible = true
    },
    confirmDeadline() {
      if (this.deadlineDate || this.stepResponsible) {
        const stepNum = this.currentStepIndex + 1
        const updateData = {
          processId: this.currentProcess.processId
        }
        if (this.deadlineDate) {
          updateData[`step${stepNum}Deadline`] = this.deadlineDate
        }
        if (this.stepResponsible) {
          updateData[`step${stepNum}Responsible`] = this.stepResponsible
        }
        updateTrialProcess(updateData).then(() => {
          this.$modal.msgSuccess('设置成功')
          this.deadlineVisible = false
          this.getList()
        })
      } else {
        this.$modal.msgWarning('请设置截止日期或负责人')
      }
    },
    getPhaseKeyByIndex(index) {
      const map = ['base', 'hot', 'spin', 'heat', 'rough', 'fine', 'paint', 'test']
      return map[index] || 'base'
    },
    /** 判断当前用户是否有某节点的编辑权限 */
    canEditPhase(index) {
      const perms = this.$store.getters.permissions || []
      if (perms.includes('*:*:*') || perms.includes('tech:trialTrack:edit') || perms.includes('tech:process:edit')) return true
      const phasePerms = ['tech:trial:phase:base:edit', 'tech:trial:phase:hot:edit', 'tech:trial:phase:spin:edit', 'tech:trial:phase:heat:edit', 'tech:trial:phase:rough:edit', 'tech:trial:phase:fine:edit', 'tech:trial:phase:paint:edit', 'tech:trial:phase:test:edit']
      return index >= 0 && index < 8 && perms.includes(phasePerms[index])
    }
  }
}
</script>

<style scoped lang="scss">
.trial-process-container {
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
  
  .search-input {
    width: 300px;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
    margin-left: 15px;
  }
}

.process-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.process-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.process-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  flex-wrap: wrap;
  
  .process-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
  
  .process-initiator, .process-date {
    font-size: 13px;
    color: #909399;
  }

  .process-desc {
    font-size: 13px;
    color: #606266;
    background: #f0f2f5;
    border-radius: 4px;
    padding: 2px 10px;
    max-width: 400px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.process-steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px 0;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  
  .step-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #909399;
    margin-bottom: 10px;
    transition: all 0.3s;
    
    /* 蓝色 - 进行中、未过期 */
    &.icon-active {
      background: #409EFF;
      color: #fff;
    }
    
    /* 红色 - 截止日期当天或前一天 */
    &.icon-expired {
      background: #f56c6c;
      color: #fff;
      animation: pulse 1.5s infinite;
    }
    
    /* 灰色 - 已完成 */
    &.icon-done {
      background: #c0c4cc;
      color: #fff;
    }
  }
  
  .step-name {
    font-size: 13px;
    color: #303133;
    text-align: center;
    margin-bottom: 5px;
  }
  
  &.is-done .step-name {
    color: #c0c4cc;
    text-decoration: line-through;
  }
  
  &.is-expired .step-name {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .step-responsible {
    font-size: 11px;
    color: #409EFF;
    margin-bottom: 3px;
    
    i {
      margin-right: 3px;
    }
  }
  
  &.is-expired .step-responsible {
    color: #f56c6c;
  }
  
  .step-deadline {
    font-size: 11px;
    color: #909399;
    margin-bottom: 4px;
    
    i {
      margin-right: 3px;
    }
  }
  
  &.is-expired .step-deadline {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .step-status-tag {
    margin-bottom: 4px;
  }
  
  .step-actions {
    display: flex;
    gap: 3px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .step-line {
    position: absolute;
    top: 25px;
    left: calc(50% + 30px);
    width: calc(100% - 60px);
    height: 2px;
    background: #e0e0e0;
    
    &.line-done {
      background: #c0c4cc;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }
}

.process-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 8px;
  color: #c0c4cc;
  
  i {
    font-size: 48px;
    margin-bottom: 15px;
  }
}

.upload-section {
  .upload-tip {
    margin-bottom: 15px;
    color: #909399;
    font-size: 13px;
  }
  
  .upload-area {
    width: 100%;
  }
  
  .file-list {
    margin-top: 15px;
    
    .file-item {
      display: flex;
      align-items: center;
      padding: 8px 10px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 8px;
      
      i {
        margin-right: 8px;
        font-size: 18px;
        color: #409EFF;
      }
      
      span {
        flex: 1;
      }
    }
  }
}

.comment-section {
  .comment-info {
    margin-bottom: 15px;
    color: #606266;
  }
}

.deadline-section {
  p {
    margin-bottom: 15px;
    color: #606266;
  }
}

// 历史记录样式
.history-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  
  .history-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    font-size: 14px;
    
    i {
      margin-right: 6px;
      color: #409EFF;
    }
  }
  
  // 图片画廊样式
  .image-gallery {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .gallery-item {
      width: 120px;
      position: relative;
      
      .thumbnail {
        width: 120px;
        height: 120px;
        border-radius: 6px;
        cursor: pointer;
        border: 1px solid #eee;
        transition: all 0.3s;
        
        &:hover {
          border-color: #409EFF;
          box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
        }
      }
      
      .image-error {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        color: #909399;
        font-size: 30px;
      }
      
      .file-icon-box {
        width: 120px;
        height: 120px;
        border-radius: 6px;
        background: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #eee;
        
        i {
          font-size: 40px;
          color: #409EFF;
        }
      }
      
      .file-info {
        margin-top: 6px;
        text-align: center;
        
        .file-name {
          display: block;
          font-size: 12px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .file-time {
          display: block;
          font-size: 11px;
          color: #909399;
          margin-top: 2px;
        }
      }
      
      .delete-btn {
        position: absolute;
        top: 2px;
        right: 2px;
        background: rgba(255,255,255,0.9);
        border-radius: 50%;
        padding: 4px;
        color: #f56c6c;
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      &:hover .delete-btn {
        opacity: 1;
      }
    }
  }
  
  .history-list {
    max-height: 200px;
    overflow-y: auto;
    
    .history-item {
      display: flex;
      align-items: center;
      padding: 10px 12px;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 8px;
      
      i {
        margin-right: 10px;
        font-size: 18px;
        color: #409EFF;
      }
      
      .file-name {
        flex: 1;
        color: #303133;
      }
      
      .file-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .comment-history-list {
    max-height: 300px;
    overflow-y: auto;
    
    .comment-history-item {
      background: #f5f7fa;
      border-radius: 6px;
      padding: 12px;
      margin-bottom: 10px;
      
      .comment-header {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        
        .comment-user {
          font-weight: 500;
          color: #409EFF;
          font-size: 13px;
          
          i {
            margin-right: 4px;
          }
        }
        
        .comment-time {
          font-size: 12px;
          color: #909399;
          margin-left: 10px;
        }
        
        .comment-actions {
          margin-left: auto;
        }
      }
      
      .comment-content {
        color: #303133;
        font-size: 13px;
        line-height: 1.5;
      }
      
      .comment-edit {
        .edit-actions {
          display: flex;
          justify-content: flex-end;
          gap: 8px;
          margin-top: 8px;
        }
      }
    }
  }
}
</style>
