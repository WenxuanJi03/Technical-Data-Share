<template>
  <view class="page">
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">{{ currentStep.name || '' }} · 试制信息记录</text>
      <view class="nav-add"></view>
    </view>

    <scroll-view scroll-y class="fullpage-body">
      <!-- 完成日期 + 责任人 -->
      <view class="record-meta-section">
        <view class="record-meta-row">
          <view class="record-meta-item">
            <text class="record-meta-label">完成日期</text>
            <picker
              mode="date"
              :value="recordCompletionDate || currentDate"
              @change="onRecordCompletionDateChange"
              :disabled="!canEditPhase(currentStepIndex)"
            >
              <view class="record-meta-input">
                <text :style="{ color: recordCompletionDate ? '#303133' : '#999' }">{{ recordCompletionDate || '选择完成日期' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
          </view>
          <view class="record-meta-item">
            <text class="record-meta-label">责任人</text>
            <picker
              v-if="userList.length > 0"
              mode="selector"
              :range="userNameList"
              :value="selectedResponsibleIndex"
              @change="onResponsibleChange"
              :disabled="!canEditPhase(currentStepIndex)"
            >
              <view class="record-meta-input">
                <text :style="{ color: stepResponsible ? '#303133' : '#999' }">{{ stepResponsible || '选择责任人' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
            <input v-else class="record-meta-input-text" v-model="stepResponsible" placeholder="请输入责任人" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </view>
      </view>

      <!-- 提示 -->
      <view class="oe-tip">
        <text class="oe-tip-icon">ℹ</text>
        <text class="oe-tip-text">以下数据与 OE试制跟踪 联动，保存后可在跟踪卡片中查看</text>
      </view>
      <view v-if="!canEditPhase(currentStepIndex)" class="oe-tip oe-tip-warn">
        <text class="oe-tip-icon">⚠</text>
        <text class="oe-tip-text">您没有此节点的编辑权限，仅可查看</text>
      </view>

      <!-- OE数据录入表单 -->
      <view class="oe-form-section">
        <text class="section-label">OE跟踪数据</text>

        <!-- 基础信息 -->
        <template v-if="currentPhase === 'base'">
          <view class="oe-form-item">
            <text class="oe-label">产品规格</text>
            <input class="oe-input" v-model="oeForm.productSpec" placeholder="请输入产品规格" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">上机次数</text>
            <input class="oe-input" v-model="oeForm.machineCount" type="number" placeholder="请输入上机次数" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">模具类型</text>
            <input class="oe-input" v-model="oeForm.moldType" placeholder="首模/改模等" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">表面状态</text>
            <input class="oe-input" v-model="oeForm.surfaceStatus" placeholder="精车/全涂等" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">上机类型</text>
            <input class="oe-input" v-model="oeForm.machineType" placeholder="小批量/量产等" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">预计上机时间</text>
            <picker mode="date" :value="formatPickerDate(oeForm.planMachineTime)" @change="onOEDateChange('planMachineTime', $event)" :disabled="!canEditPhase(currentStepIndex)">
              <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                <text :style="{ color: oeForm.planMachineTime ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.planMachineTime || 'YYYY-MM-DD' }}</text>
              </view>
            </picker>
          </view>
        </template>

        <!-- 热工阶段 -->
        <template v-else-if="currentPhase === 'hot'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">压铸上机日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.hotMachineDate)" @change="onOEDateChange('hotMachineDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.hotMachineDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.hotMachineDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">机台</text>
              <input class="oe-input" v-model="oeForm.hotMachineStation" placeholder="机台编号" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">保压时间</text>
              <input class="oe-input" v-model="oeForm.roundKeepTime" placeholder="保压时间" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">负责人</text>
              <input class="oe-input" v-model="oeForm.hotImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">测量数据</text>
            <input class="oe-input" v-model="oeForm.hotCheckMeasureData" placeholder="请输入测量数据" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">生产情况</text>
            <textarea class="oe-textarea" v-model="oeForm.hotProduction" placeholder="请输入生产情况" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">改善记录</text>
            <textarea class="oe-textarea" v-model="oeForm.improveRecord" placeholder="请输入改善记录" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>

        <!-- 旋压阶段 -->
        <template v-else-if="currentPhase === 'spin'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">旋压上机日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.spinMachineDate)" @change="onOEDateChange('spinMachineDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.spinMachineDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.spinMachineDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">旋压机台</text>
              <input class="oe-input" v-model="oeForm.spinMachineStation" placeholder="机台编号" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">负责人</text>
            <input class="oe-input" v-model="oeForm.spinImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">生产情况</text>
            <textarea class="oe-textarea" v-model="oeForm.spinProduction" placeholder="请输入生产情况" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">改模记录（原OE字段）</text>
            <textarea class="oe-textarea" v-model="oeForm.moldModifyRecord" placeholder="请输入改模记录" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>

        <!-- 热处理阶段 -->
        <template v-else-if="currentPhase === 'heat'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">下转时间</text>
              <picker mode="date" :value="formatPickerDate(oeForm.heatTransferTime)" @change="onOEDateChange('heatTransferTime', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.heatTransferTime ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.heatTransferTime || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">接收数量</text>
              <input class="oe-input" type="number" v-model="oeForm.heatReceiveCount" placeholder="数量" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">下转数量</text>
              <input class="oe-input" type="number" v-model="oeForm.heatTransferCount" placeholder="数量" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">负责人</text>
              <input class="oe-input" v-model="oeForm.heatImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
        </template>

        <!-- 粗车阶段 -->
        <template v-else-if="currentPhase === 'rough'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">粗车上机日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.roughMachineDate)" @change="onOEDateChange('roughMachineDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.roughMachineDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.roughMachineDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">负责人</text>
              <input class="oe-input" v-model="oeForm.roughImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">生产情况</text>
            <textarea class="oe-textarea" v-model="oeForm.roughProduction" placeholder="请输入生产情况" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">改善方案（原OE字段）</text>
            <textarea class="oe-textarea" v-model="oeForm.improvePlan" placeholder="请输入改善方案" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>

        <!-- 精车阶段 -->
        <template v-else-if="currentPhase === 'fine'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">精车上机日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.fineMachineDate)" @change="onOEDateChange('fineMachineDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.fineMachineDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.fineMachineDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">精车负责人</text>
              <input class="oe-input" v-model="oeForm.fineImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">精车生产情况</text>
            <textarea class="oe-textarea" v-model="oeForm.fineProduction" placeholder="请输入精车生产情况" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>

        <!-- 涂装阶段 -->
        <template v-else-if="currentPhase === 'paint'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">涂装上机日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.paintMachineDate)" @change="onOEDateChange('paintMachineDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.paintMachineDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.paintMachineDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">涂装负责人</text>
              <input class="oe-input" v-model="oeForm.paintImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">涂装生产情况</text>
            <textarea class="oe-textarea" v-model="oeForm.paintProduction" placeholder="请输入涂装生产情况" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>

        <!-- 实验/总结 -->
        <template v-else-if="currentPhase === 'test'">
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">冲击试验日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.impactTestDate)" @change="onOEDateChange('impactTestDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.impactTestDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.impactTestDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">冲击试验结果</text>
              <input class="oe-input" v-model="oeForm.impactTestResult" placeholder="试验结果" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </view>
          <view class="oe-form-row">
            <view class="oe-form-item half">
              <text class="oe-label">生产完成日期</text>
              <picker mode="date" :value="formatPickerDate(oeForm.completeDate)" @change="onOEDateChange('completeDate', $event)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: oeForm.completeDate ? '#303133' : '#999', fontSize: '26rpx' }">{{ oeForm.completeDate || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
            <view class="oe-form-item half">
              <text class="oe-label">全序是否完成</text>
              <view class="radio-group">
                <view class="radio-item" @tap="canEditPhase(currentStepIndex) && (oeForm.allProcessDone = '是')">
                  <view class="radio-circle" :class="{ 'radio-checked': oeForm.allProcessDone === '是' }"></view>
                  <text class="radio-label">是</text>
                </view>
                <view class="radio-item" @tap="canEditPhase(currentStepIndex) && (oeForm.allProcessDone = '否')">
                  <view class="radio-circle" :class="{ 'radio-checked': oeForm.allProcessDone === '否' }"></view>
                  <text class="radio-label">否</text>
                </view>
              </view>
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">实验说明</text>
            <textarea class="oe-textarea" v-model="oeForm.testDescription" placeholder="请输入实验说明" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-row">
            <view class="oe-form-item" style="flex:1">
              <text class="oe-label">实验关闭情况</text>
            </view>
            <view class="oe-form-item" style="flex:0 0 auto">
              <view class="radio-group">
                <view class="radio-item" @tap="canEditPhase(currentStepIndex) && (oeForm.testCloseStatus = '是')">
                  <view class="radio-circle" :class="{ 'radio-checked': oeForm.testCloseStatus === '是' }"></view>
                  <text class="radio-label">是</text>
                </view>
                <view class="radio-item" @tap="canEditPhase(currentStepIndex) && (oeForm.testCloseStatus = '否')">
                  <view class="radio-circle" :class="{ 'radio-checked': oeForm.testCloseStatus === '否' }"></view>
                  <text class="radio-label">否</text>
                </view>
              </view>
            </view>
          </view>
          <view class="oe-form-item">
            <text class="oe-label">实验失效分析</text>
            <textarea class="oe-textarea" v-model="oeForm.failAnalysis" placeholder="请输入实验失效分析" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">本次生产总结</text>
            <textarea class="oe-textarea" v-model="oeForm.productionSummary" placeholder="请输入生产总结" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
          <view class="oe-form-item">
            <text class="oe-label">改善措施简述（原OE字段）</text>
            <textarea class="oe-textarea" v-model="oeForm.improveMeasures" placeholder="请输入改善措施" :disabled="!canEditPhase(currentStepIndex)" />
          </view>
        </template>
      </view>

      <!-- 下一步需求 -->
      <view class="next-demand-section" v-if="currentStepIndex > 0">
        <text class="next-demand-label">下一步需求</text>
        <view class="next-demand-options">
          <view
            class="demand-option"
            :class="{ 'demand-selected': recordDemand === 'normal' }"
            @tap="setRecordDemand('normal')"
          >
            <text class="demand-option-text">✓ 无异常</text>
          </view>
          <view
            class="demand-option demand-warn"
            :class="{ 'demand-selected demand-warn-selected': recordDemand === 'minor' }"
            @tap="setRecordDemand('minor')"
          >
            <text class="demand-option-text">⚠ 小异常，待下次关注</text>
          </view>
          <view
            class="demand-option demand-danger"
            :class="{ 'demand-selected demand-danger-selected': recordDemand === 'major' }"
            @tap="setRecordDemand('major')"
          >
            <text class="demand-option-text">✗ 大异常，待改善</text>
          </view>
        </view>
        <view v-if="recordDemand === 'minor' || recordDemand === 'major'" class="improve-record-input-area">
          <textarea
            class="improve-textarea"
            v-model="recordImproveText"
            placeholder="请输入改善措施..."
            :disabled="!canEditPhase(currentStepIndex)"
          />
        </view>
      </view>

      <!-- 附件上传区 -->
      <view class="upload-section">
        <view class="section-divider">
          <text class="section-label">📎 {{ currentStep.name || '' }}流转单照片上传</text>
          <view v-if="canEditPhase(currentStepIndex)" class="add-file-btn" @tap="chooseAndUpload">
            <text class="add-file-btn-text">{{ uploadingFile ? '上传中...' : '+ 添加图片/文件' }}</text>
          </view>
        </view>
        <text class="upload-tip-text">支持图片（JPG、PNG、GIF等）及 PDF 文件</text>
        
        <view class="file-grid" v-if="historyFiles.length > 0">
          <view v-for="(file, fIdx) in historyFiles" :key="fIdx" class="file-thumb-item">
            <view class="file-thumb" @tap="previewFile(file, fIdx)" @longpress="showFileInfo(file)">
              <image v-if="isImageFile(file.name) && !getImgError(fIdx)" :src="getImageSrc(file, fIdx)" :data-idx="fIdx" class="file-thumb-img" mode="aspectFill" lazy-load :show-menu-by-longpress="true" @error="onImageError" @load="onImageLoad"/>
              <view v-if="isImageFile(file.name) && getImgError(fIdx)" class="file-thumb-error">
                <text class="file-error-icon">⚠️</text>
                <text class="file-error-text">加载失败</text>
              </view>
              <view v-if="isImageFile(file.name) && !getImgLoaded(fIdx) && !getImgError(fIdx)" class="file-loading-placeholder">
                <text class="loading-text">🖼️</text>
              </view>
              <view v-if="!isImageFile(file.name)" class="file-thumb-doc">
                <text class="file-doc-icon">📄</text>
              </view>
            </view>
            <text class="file-thumb-name">{{ shortenName(file.name) }}</text>
            <view v-if="canEditPhase(currentStepIndex)" class="file-delete-btn" @tap.stop="deleteFile(fIdx)">
              <text class="file-delete-icon">✕</text>
            </view>
          </view>
        </view>
        <view class="file-empty" v-else>
          <text class="file-empty-text">暂无附件</text>
        </view>
      </view>

      <view style="height:200rpx"></view>
    </scroll-view>
    
    <!-- 底部栏 -->
    <view class="bottom-action-fixed">
      <view v-if="canEditPhase(currentStepIndex) && currentStep.status !== 'done'" class="record-done-btn-top" @tap="markStepDoneFromRecord">
        <text class="record-done-icon">✅</text>
        <text class="record-done-text">标记该阶段已完成</text>
      </view>
      <view v-if="canEditPhase(currentStepIndex)" class="btn-save-bottom" @tap="submitOE">
        <text class="btn-save-text">{{ oeLoading ? '保存中...' : '保存' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { updateTrialProcess } from '@/api/trialProcess'
import { listTrialTrack, addTrialTrack, updateTrialTrack } from '@/api/trialTrack'
import { uploadFile, getProductBaseUrl } from '@/api/product'
import { getAllUsers } from '@/api/common'

export default {
  data() {
    return {
      statusBarHeight: 44,
      capsuleRightPadding: 104,
      currentProcess: null,
      currentStep: {},
      currentStepIndex: 0,
      currentPhase: 'base',
      
      recordCompletionDate: '',
      recordDemand: '',
      recordImproveText: '',
      stepResponsible: '',
      selectedResponsibleIndex: 0,
      userList: [],
      
      oeForm: {
        planMachineTime: '', hotMachineDate: '', spinMachineDate: '', heatTransferTime: '',
        roughMachineDate: '', fineMachineDate: '', paintMachineDate: '', impactTestDate: '', completeDate: '',
        allProcessDone: '否'
      },
      oeLoading: false,
      
      historyFiles: [],
      uploadingFile: false,
      baseUrl: '',
      imageLocalPaths: {},
      imageLoaded: {},
      imageLoadErrors: {}
    }
  },
  computed: {
    userNameList() {
      return this.userList.map(u => u.nickName)
    },
    currentDate() {
      const d = new Date()
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    navBarStyle() {
      return {
        paddingTop: this.statusBarHeight + 'px',
        paddingRight: this.capsuleRightPadding + 'px',
        paddingBottom: '16px',
        paddingLeft: '16px'
      }
    }
  },
  onLoad(options) {
    try {
      const sysInfo = uni.getSystemInfoSync()
      this.statusBarHeight = sysInfo.statusBarHeight || 44
      // #ifdef MP-WEIXIN
      try {
        const capsule = wx.getMenuButtonBoundingClientRect()
        this.capsuleRightPadding = sysInfo.windowWidth - capsule.left + 8
      } catch (e) {
        this.capsuleRightPadding = 104
      }
      // #endif
    } catch (e) {}

    this.baseUrl = getProductBaseUrl()
    this.loadUserList()

    const eventChannel = this.getOpenerEventChannel()
    if (eventChannel && eventChannel.on) {
      eventChannel.on('acceptDataFromOpenerPage', (data) => {
        this.initData(data.data, parseInt(options.stepIndex))
      })
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    loadUserList() {
      getAllUsers().then(res => {
        this.userList = res.rows || []
      }).catch(() => {})
    },
    getPhaseKey(index) {
      const map = ['base', 'hot', 'spin', 'heat', 'rough', 'fine', 'paint', 'test']
      return map[index] || 'base'
    },
    canEditPhase(index) {
      const perms = this.$store.state.permissions || []
      if (perms.includes('*:*:*') || perms.includes('tech:trialTrack:edit') || perms.includes('tech:process:edit')) return true
      const phasePerms = ['tech:trial:phase:base:edit', 'tech:trial:phase:hot:edit', 'tech:trial:phase:spin:edit', 'tech:trial:phase:heat:edit', 'tech:trial:phase:rough:edit', 'tech:trial:phase:fine:edit', 'tech:trial:phase:paint:edit', 'tech:trial:phase:test:edit']
      return index >= 0 && index < 8 && perms.includes(phasePerms[index])
    },
    initData(process, index) {
      this.currentProcess = process
      this.currentStepIndex = index
      // Steps logic matching original trialProcess/index.vue
      const steps = [
        { name: '基础信息', status: process.step1Status, deadline: process.step1Deadline, responsible: process.step1Responsible },
        { name: '压铸阶段', status: process.step2Status, deadline: process.step2Deadline, responsible: process.step2Responsible },
        { name: '旋压阶段', status: process.step3Status, deadline: process.step3Deadline, responsible: process.step3Responsible },
        { name: '热处理阶段', status: process.step4Status, deadline: process.step4Deadline, responsible: process.step4Responsible },
        { name: '粗车阶段', status: process.step5Status, deadline: process.step5Deadline, responsible: process.step5Responsible },
        { name: '精车阶段', status: process.step6Status, deadline: process.step6Deadline, responsible: process.step6Responsible },
        { name: '涂装阶段', status: process.step7Status, deadline: process.step7Deadline, responsible: process.step7Responsible },
        { name: '实验/总结', status: process.step8Status, deadline: process.step8Deadline, responsible: process.step8Responsible }
      ]
      this.currentStep = steps[index]
      this.currentPhase = this.getPhaseKey(index)

      const filesKey = `step${index + 1}Files`
      try {
        this.historyFiles = process[filesKey] ? JSON.parse(process[filesKey]) : []
      } catch (e) {
        this.historyFiles = []
      }
      this.imageLocalPaths = {}
      this.imageLoaded = {}
      this.imageLoadErrors = {}
      this.prefetchImagesForDisplay()

      this.recordCompletionDate = this.currentStep.deadline || ''
      const demandKey = `step${index + 1}Demand`
      const recordKey = `step${index + 1}ImproveRecord`
      this.recordDemand = process[demandKey] || ''
      this.recordImproveText = process[recordKey] || ''
      
      const currentName = this.$store.state.name || ''
      this.stepResponsible = this.currentStep.responsible || currentName
      const respIdx = this.userList.findIndex(u => u.nickName === this.stepResponsible)
      this.selectedResponsibleIndex = respIdx >= 0 ? respIdx : 0

      // load OE Form
      const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
        'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
      const baseOeForm = {
        moldCode: process.moldCode,
        allProcessDone: '否',
        planMachineTime: '', hotMachineDate: '', spinMachineDate: '', heatTransferTime: '',
        roughMachineDate: '', fineMachineDate: '', paintMachineDate: '', impactTestDate: '', completeDate: '',
        hotImprovePerson: currentName, spinImprovePerson: currentName, heatImprovePerson: currentName,
        roughImprovePerson: currentName, fineImprovePerson: currentName, paintImprovePerson: currentName
      }
      
      if (process.moldCode) {
        uni.showLoading({ title: '加载中...' })
        listTrialTrack({ pageNum: 1, pageSize: 1, moldCode: process.moldCode }).then(res => {
          const rows = res.rows || []
          if (rows.length > 0) {
            const loaded = { ...baseOeForm, ...rows[0] }
            dateFields.forEach(f => { if (loaded[f]) { loaded[f] = this.formatPickerDate(loaded[f]) } })
            this.oeForm = loaded
          } else {
            this.oeForm = { ...baseOeForm }
          }
        }).catch(() => {
          this.oeForm = { ...baseOeForm }
        }).finally(() => {
          uni.hideLoading()
        })
      } else {
        this.oeForm = { ...baseOeForm }
      }
    },
    formatPickerDate(val) {
      if (!val) return ''
      if (typeof val === 'number') {
        try { return new Date(val).toISOString().substring(0, 10) } catch(e) { return '' }
      }
      if (typeof val === 'string') return val.substring(0, 10)
      if (val instanceof Date) {
        const y = val.getFullYear()
        const m = String(val.getMonth() + 1).padStart(2, '0')
        const d = String(val.getDate()).padStart(2, '0')
        return `${y}-${m}-${d}`
      }
      if (typeof val === 'object') {
        try { return new Date(val).toISOString().substring(0, 10) } catch(e) { return '' }
      }
      return ''
    },
    onRecordCompletionDateChange(e) {
      this.recordCompletionDate = (e && e.detail && e.detail.value) ? e.detail.value.substring(0, 10) : ''
    },
    onResponsibleChange(e) {
      const idx = e.detail.value
      this.selectedResponsibleIndex = idx
      this.stepResponsible = this.userList[idx].nickName
    },
    onOEDateChange(field, e) {
      const val = (e && e.detail && e.detail.value) ? e.detail.value.substring(0, 10) : ''
      this.$set(this.oeForm, field, val)
    },
    setRecordDemand(demand) {
      if (!this.canEditPhase(this.currentStepIndex)) {
        uni.showToast({ title: '无编辑权限', icon: 'none' })
        return
      }
      this.recordDemand = demand
      if (demand === 'normal') {
        this.recordImproveText = ''
      }
    },
    markStepDoneFromRecord() {
      const item = this.currentProcess
      const index = this.currentStepIndex
      if (!item) return
      uni.showModal({
        title: '确认完成',
        content: `确认将"${this.currentStep.name}"标记为已完成？`,
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            const stepNum = index + 1
            const updateData = { processId: item.processId }
            updateData[`step${stepNum}Status`] = 'done'
            if (stepNum < 8) {
              updateData[`step${stepNum + 1}Status`] = 'active'
            }
            if (this.recordCompletionDate) updateData[`step${stepNum}Deadline`] = this.recordCompletionDate
            if (this.stepResponsible) updateData[`step${stepNum}Responsible`] = this.stepResponsible
            if (index > 0) {
              updateData[`step${stepNum}Demand`] = this.recordDemand || ''
              updateData[`step${stepNum}ImproveRecord`] = (this.recordDemand === 'minor' || this.recordDemand === 'major') ? (this.recordImproveText || '') : ''
            }
            
            uni.showLoading({ title: '保存中...' })
            this.submitOESilent().then(() => {
              updateTrialProcess(updateData).then(() => {
                uni.hideLoading()
                uni.showToast({ title: '已标记为完成', icon: 'success' })
                uni.$emit('refreshProcessList')
                setTimeout(() => uni.navigateBack(), 800)
              })
            }).catch(() => {
              updateTrialProcess(updateData).then(() => {
                uni.hideLoading()
                uni.showToast({ title: '已标记为完成', icon: 'success' })
                uni.$emit('refreshProcessList')
                setTimeout(() => uni.navigateBack(), 800)
              })
            })
          }
        }
      })
    },
    submitOESilent() {
      return new Promise((resolve, reject) => {
        const imageUrls = this.historyFiles.filter(f => this.isImageFile(f.name)).map(f => f.url).join(',')
        if (imageUrls) {
          if (this.currentPhase === 'base') this.$set(this.oeForm, 'baseImage', imageUrls)
          else if (this.currentPhase === 'hot') this.$set(this.oeForm, 'hotCheckMeasureImage', imageUrls)
          else if (this.currentPhase === 'spin') this.$set(this.oeForm, 'spinFrontDistanceImage', imageUrls)
          else if (this.currentPhase === 'heat') this.$set(this.oeForm, 'heatFlowSheetImage', imageUrls)
          else if (this.currentPhase === 'rough') this.$set(this.oeForm, 'roughImage', imageUrls)
          else if (this.currentPhase === 'fine') this.$set(this.oeForm, 'fineImage', imageUrls)
          else if (this.currentPhase === 'paint') this.$set(this.oeForm, 'paintFlowSheetImage', imageUrls)
          else if (this.currentPhase === 'test') this.$set(this.oeForm, 'testImage', imageUrls)
        }
        const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
          'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
        const payload = { ...this.oeForm }
        dateFields.forEach(f => {
          if (!payload[f]) return
          if (typeof payload[f] === 'string') { payload[f] = payload[f].substring(0, 10); return }
          try { payload[f] = new Date(payload[f]).toISOString().substring(0, 10) } catch(e) { payload[f] = null }
        })
        const save = payload.trackId ? updateTrialTrack : addTrialTrack
        save(payload).then(resolve).catch(reject)
      })
    },
    submitOE() {
      if (this.oeLoading) return
      this.oeLoading = true
      
      const imageUrls = this.historyFiles.filter(f => this.isImageFile(f.name)).map(f => f.url).join(',')
      if (imageUrls) {
        if (this.currentPhase === 'base') this.$set(this.oeForm, 'baseImage', imageUrls)
        else if (this.currentPhase === 'hot') this.$set(this.oeForm, 'hotCheckMeasureImage', imageUrls)
        else if (this.currentPhase === 'spin') this.$set(this.oeForm, 'spinFrontDistanceImage', imageUrls)
        else if (this.currentPhase === 'heat') this.$set(this.oeForm, 'heatFlowSheetImage', imageUrls)
        else if (this.currentPhase === 'rough') this.$set(this.oeForm, 'roughImage', imageUrls)
        else if (this.currentPhase === 'fine') this.$set(this.oeForm, 'fineImage', imageUrls)
        else if (this.currentPhase === 'paint') this.$set(this.oeForm, 'paintFlowSheetImage', imageUrls)
        else if (this.currentPhase === 'test') this.$set(this.oeForm, 'testImage', imageUrls)
      }

      const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
        'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
      const payload = { ...this.oeForm }
      dateFields.forEach(f => {
        if (!payload[f]) return
        if (typeof payload[f] === 'string') { payload[f] = payload[f].substring(0, 10); return }
        try { payload[f] = new Date(payload[f]).toISOString().substring(0, 10) } catch(e) { payload[f] = null }
      })
      const save = payload.trackId ? updateTrialTrack : addTrialTrack
      save(payload).then(() => {
        const stepNum = this.currentStepIndex + 1
        const stepUpdate = { processId: this.currentProcess.processId }
        if (this.recordCompletionDate) stepUpdate[`step${stepNum}Deadline`] = this.recordCompletionDate
        if (this.stepResponsible) stepUpdate[`step${stepNum}Responsible`] = this.stepResponsible
        if (this.currentStepIndex > 0) {
          stepUpdate[`step${stepNum}Demand`] = this.recordDemand || ''
          stepUpdate[`step${stepNum}ImproveRecord`] = (this.recordDemand === 'minor' || this.recordDemand === 'major') ? (this.recordImproveText || '') : ''
        }
        if (Object.keys(stepUpdate).length > 1) {
          updateTrialProcess(stepUpdate).then(() => {
            uni.$emit('refreshProcessList')
          }).catch(() => { uni.$emit('refreshProcessList') })
        } else {
          uni.$emit('refreshProcessList')
        }
        uni.showToast({ title: 'OE数据已保存', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      }).catch(() => {
        uni.showToast({ title: '保存失败', icon: 'none' })
      }).finally(() => {
        this.oeLoading = false
      })
    },
    
    // File Upload handling
    chooseAndUpload() {
      if (this.uploadingFile) return
      uni.showActionSheet({
        itemList: ['从相册选择图片', '拍照', '选择PDF文件'],
        success: (action) => {
          if (action.tapIndex === 0 || action.tapIndex === 1) {
            uni.chooseImage({
              count: 9, sizeType: ['original', 'compressed'], sourceType: [action.tapIndex === 0 ? 'album' : 'camera'],
              success: (res) => {
                const files = res.tempFilePaths.map(p => ({ path: p, name: '' }))
                this.doUpload(files)
              }
            })
          } else {
            // #ifdef MP-WEIXIN
            wx.chooseMessageFile({
              count: 5, type: 'file', extension: ['pdf'],
              success: (res) => {
                const files = res.tempFiles.map(f => ({ path: f.path, name: f.name }))
                this.doUpload(files)
              },
              fail: () => { uni.showToast({ title: '请先将PDF发送到微信，再从聊天文件中选择', icon: 'none', duration: 3000 }) }
            })
            // #endif
            // #ifndef MP-WEIXIN
            uni.chooseFile({
              count: 5, extension: ['.pdf'],
              success: (res) => {
                const files = res.tempFilePaths.map(p => ({ path: p, name: p.split('/').pop() || 'file.pdf' }))
                this.doUpload(files)
              }
            })
            // #endif
          }
        }
      })
    },
    doUpload(files) {
      this.uploadingFile = true
      const uploadNext = (i) => {
        if (i >= files.length) {
          this.uploadingFile = false
          uni.showToast({ title: '上传完成', icon: 'success' })
          return
        }
        const { path: filePath, name: originalName } = files[i]
        const extMatch = filePath.match(/\.([a-zA-Z0-9]+)(\?|$)/)
        const ext = extMatch ? extMatch[1].toLowerCase() : 'jpg'
        const fileName = originalName || ('file_' + Date.now() + '_' + i + '.' + ext)
        uploadFile(filePath).then(data => {
          let fileUrl = data.fileName || data.url || filePath
          if (fileUrl && !fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
            fileUrl = this.baseUrl + fileUrl
          }
          const newFile = { name: fileName, url: fileUrl, time: new Date().toISOString() }
          this.historyFiles.push(newFile)
          this.prefetchImagesForDisplay()
          const filesKey = `step${this.currentStepIndex + 1}Files`
          const updatedJson = JSON.stringify(this.historyFiles)
          const updateData = { processId: this.currentProcess.processId, [filesKey]: updatedJson }
          updateTrialProcess(updateData).then(() => {
            uploadNext(i + 1)
          })
        }).catch(() => { uploadNext(i + 1) })
      }
      uploadNext(0)
    },
    deleteFile(fIdx) {
      uni.showModal({
        title: '确认删除',
        content: '确认删除此文件？',
        success: (res) => {
          if (res.confirm) {
            this.historyFiles.splice(fIdx, 1)
            const filesKey = `step${this.currentStepIndex + 1}Files`
            const updatedJson = JSON.stringify(this.historyFiles)
            const updateData = { processId: this.currentProcess.processId, [filesKey]: updatedJson }
            updateTrialProcess(updateData).then(() => {
              uni.showToast({ title: '已删除', icon: 'success' })
            })
          }
        }
      })
    },
    isImageFile(name) {
      if (!name) return false
      const ext = name.toLowerCase().split('.').pop()
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'heic', 'heif'].includes(ext)
    },
    getFileUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      return this.baseUrl + url
    },
    getImageSrc(file, fIdx) {
      const fullUrl = this.getFileUrl(file.url)
      if (!fullUrl) return ''
      // #ifdef MP-WEIXIN
      const key = 'i' + fIdx
      const localPath = this.imageLocalPaths[key]
      if (localPath) return localPath
      return fullUrl
      // #endif
      // #ifndef MP-WEIXIN
      return fullUrl
      // #endif
    },
    prefetchImagesForDisplay() {
      // #ifdef MP-WEIXIN
      this.historyFiles.forEach((file, fIdx) => {
        if (!this.isImageFile(file.name)) return
        const fullUrl = this.getFileUrl(file.url)
        if (!fullUrl) return
        const key = 'i' + fIdx
        if (this.imageLocalPaths[key] || this.imageLoadErrors[key]) return
        uni.downloadFile({
          url: fullUrl, timeout: 30000,
          success: (res) => {
            if (res.statusCode !== 200 || !res.tempFilePath) {
              this.$set(this.imageLoadErrors, key, true)
              return
            }
            this.$nextTick(() => {
              if (fIdx >= this.historyFiles.length) return
              try { this.$set(this.imageLocalPaths, key, res.tempFilePath) } catch (e) {}
            })
          },
          fail: () => { this.$set(this.imageLoadErrors, key, true) }
        })
      })
      // #endif
    },
    previewFile(file, fIdx) {
      if (this.isImageFile(file.name)) {
        // #ifdef MP-WEIXIN
        const key = 'i' + fIdx
        if (this.imageLocalPaths[key]) {
          this.doPreviewImages(fIdx)
        } else {
          const fullUrl = this.getFileUrl(file.url)
          if (!fullUrl) { uni.showToast({ title: '图片地址无效', icon: 'none' }); return }
          uni.showLoading({ title: '加载图片...' })
          uni.downloadFile({
            url: fullUrl, timeout: 30000,
            success: (res) => {
              uni.hideLoading()
              if (res.statusCode === 200 && res.tempFilePath) {
                this.$set(this.imageLocalPaths, key, res.tempFilePath)
                this.doPreviewImages(fIdx)
              } else { uni.showToast({ title: '加载失败', icon: 'none' }) }
            },
            fail: () => { uni.hideLoading(); this.doPreviewImages(fIdx) }
          })
        }
        // #endif
        // #ifndef MP-WEIXIN
        this.doPreviewImages(fIdx)
        // #endif
      } else {
        const fileUrl = this.getFileUrl(file.url)
        if (!fileUrl) { uni.showToast({ title: '文件地址无效', icon: 'none' }); return }
        uni.showLoading({ title: '加载中...' })
        // #ifdef MP-WEIXIN
        wx.downloadFile({
          url: fileUrl,
          success: (res) => {
            uni.hideLoading()
            if (res.statusCode === 200) {
              wx.openDocument({ filePath: res.tempFilePath, showMenu: true, fail: (err) => { uni.showToast({ title: '无法预览', icon: 'none' }) } })
            } else { uni.showToast({ title: '下载失败', icon: 'none' }) }
          },
          fail: (err) => { uni.hideLoading(); uni.showToast({ title: '加载失败', icon: 'none' }) }
        })
        // #endif
        // #ifndef MP-WEIXIN
        uni.hideLoading()
        uni.showToast({ title: '请在微信中打开', icon: 'none' })
        // #endif
      }
    },
    doPreviewImages(fIdx) {
      const imageUrls = this.historyFiles.map((f, i) => this.isImageFile(f.name) ? this.getImageSrc(f, i) : null).filter(Boolean)
      const targetFile = this.historyFiles[fIdx]
      if (!targetFile) return
      const currentUrl = this.getImageSrc(targetFile, fIdx)
      if (!currentUrl) { uni.showToast({ title: '图片无效', icon: 'none' }); return }
      uni.previewImage({ urls: imageUrls, current: currentUrl, fail: () => { uni.showToast({ title: '预览失败', icon: 'none' }) } })
    },
    shortenName(name) {
      if (!name) return '文件'
      return name.length > 8 ? name.substring(0, 8) + '...' : name
    },
    showFileInfo(file) {
      const fullUrl = this.getFileUrl(file.url)
      uni.showModal({
        title: '文件信息', content: `文件名: ${file.name}\n\n完整URL: ${fullUrl}`, showCancel: true, cancelText: '关闭', confirmText: '复制URL',
        success: (res) => {
          if (res.confirm) uni.setClipboardData({ data: fullUrl, success: () => uni.showToast({ title: '已复制', icon: 'success' }) })
        }
      })
    },
    onImageError(e) {
      // #ifdef MP-WEIXIN
      const fIdx = e && e.currentTarget && e.currentTarget.dataset ? parseInt(e.currentTarget.dataset.idx) : -1
      if (fIdx < 0) return
      const key = 'i' + fIdx
      if (this.imageLocalPaths[key]) return
      const file = this.historyFiles[fIdx]
      if (!file) return
      const fullUrl = this.getFileUrl(file.url)
      if (!fullUrl) { this.$set(this.imageLoadErrors, key, true); return }
      uni.downloadFile({
        url: fullUrl, timeout: 30000,
        success: (res) => {
          if (res.statusCode === 200 && res.tempFilePath) {
            this.$set(this.imageLocalPaths, key, res.tempFilePath)
            this.$set(this.imageLoadErrors, key, false)
          } else { this.$set(this.imageLoadErrors, key, true) }
        },
        fail: () => { this.$set(this.imageLoadErrors, key, true) }
      })
      // #endif
    },
    onImageLoad(e) {
      // #ifdef MP-WEIXIN
      const fIdx = e && e.currentTarget && e.currentTarget.dataset ? parseInt(e.currentTarget.dataset.idx) : -1
      if (fIdx < 0) return
      const key = 'i' + fIdx
      this.$set(this.imageLoaded, key, true)
      this.$set(this.imageLoadErrors, key, false)
      // #endif
    },
    getImgError(fIdx) { return !!this.imageLoadErrors['i' + fIdx] },
    getImgLoaded(fIdx) { return !!this.imageLoaded['i' + fIdx] }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 88rpx 208rpx 16rpx 30rpx;
  flex-shrink: 0;
}
.nav-back {
  width: 60rpx; height: 60rpx;
  display: flex; align-items: center; justify-content: center;
}
.nav-back-icon { font-size: 52rpx; color: #fff; font-weight: 300; line-height: 1; }
.nav-title { font-size: 34rpx; font-weight: 600; color: #fff; }
.nav-add { width: 60rpx; height: 60rpx; }

.fullpage-body { flex: 1; overflow-y: auto; }

/* 试制信息记录弹窗样式提取 */
.record-meta-section {
  margin: 20rpx 24rpx; background: #fff; border-radius: 20rpx; padding: 24rpx;
}
.record-meta-row { display: flex; gap: 20rpx; }
.record-meta-item { flex: 1; }
.record-meta-label {
  display: block; font-size: 24rpx; color: #606266; margin-bottom: 8rpx; font-weight: 600;
}
.record-meta-input {
  display: flex; align-items: center; justify-content: space-between;
  border: 2rpx solid #dcdfe6; border-radius: 10rpx; padding: 14rpx 16rpx;
  background: #f8f9fc; font-size: 26rpx; color: #303133;
}
.record-meta-input-text {
  border: 2rpx solid #dcdfe6; border-radius: 10rpx; padding: 14rpx 16rpx;
  background: #f8f9fc; font-size: 26rpx; color: #303133; width: 100%; box-sizing: border-box;
}
.picker-arrow { font-size: 22rpx; color: #909399; }

/* OE数据录入 */
.oe-tip {
  margin: 20rpx 24rpx; background: #ecf5ff; border-radius: 12rpx; padding: 16rpx 20rpx;
  display: flex; gap: 10rpx; align-items: flex-start;
}
.oe-tip-icon { font-size: 28rpx; flex-shrink: 0; }
.oe-tip-text { font-size: 22rpx; color: #409eff; line-height: 1.6; }
.oe-tip-warn { background: #fdf6ec; }
.oe-tip-warn .oe-tip-text { color: #e6a23c; }

.oe-form-section {
  margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 24rpx;
}
.section-label { font-size: 28rpx; font-weight: 600; color: #303133; margin-bottom: 20rpx; display: block; }
.oe-form-item { margin-bottom: 20rpx; &.half { flex: 1; } }
.oe-form-row { display: flex; gap: 16rpx; margin-bottom: 0; }
.oe-label { display: block; font-size: 24rpx; color: #606266; margin-bottom: 8rpx; }
.oe-input {
  background: #f5f7fa; border-radius: 10rpx; padding: 24rpx 20rpx;
  font-size: 28rpx; line-height: 40rpx; min-height: 90rpx; color: #303133;
  width: 100%; box-sizing: border-box;
}
.oe-textarea {
  background: #f5f7fa; border-radius: 10rpx; padding: 24rpx 20rpx;
  font-size: 28rpx; line-height: 40rpx; color: #303133; width: 100%; min-height: 160rpx; box-sizing: border-box;
}
.radio-group { display: flex; gap: 30rpx; padding: 10rpx 0; }
.radio-item { display: flex; align-items: center; gap: 8rpx; }
.radio-circle {
  width: 36rpx; height: 36rpx; border-radius: 50%; border: 3rpx solid #dcdfe6; background: #fff;
}
.radio-checked { border-color: #6c5bb3; background: #6c5bb3; box-shadow: inset 0 0 0 6rpx #fff; }
.radio-label { font-size: 26rpx; color: #303133; }

/* 下一步需求 */
.next-demand-section {
  margin: 12rpx 24rpx; background: #fff; border-radius: 12rpx; padding: 24rpx;
}
.next-demand-label { font-size: 28rpx; color: #303133; font-weight: 600; margin-bottom: 16rpx; display: block; }
.next-demand-options { display: flex; gap: 12rpx; margin-bottom: 12rpx; }
.demand-option {
  flex: 1; padding: 16rpx 0; border-radius: 10rpx; border: 2rpx solid #dcdfe6;
  display: flex; align-items: center; justify-content: center; background: #fff;
}
.demand-selected { border-color: #67c23a; background: #f0f9eb; }
.demand-warn { border-color: #e6a23c; }
.demand-warn-selected { background: #fdf6ec; border-color: #e6a23c; }
.demand-danger { border-color: #f56c6c; }
.demand-danger-selected { background: #fef0f0; border-color: #f56c6c; }
.demand-option-text { font-size: 22rpx; color: #606266; }
.demand-selected .demand-option-text { color: #67c23a; font-weight: 600; }
.demand-warn-selected .demand-option-text { color: #e6a23c; font-weight: 600; }
.demand-danger-selected .demand-option-text { color: #f56c6c; font-weight: 600; }
.improve-record-input-area { margin-top: 16rpx; }
.improve-textarea {
  width: 100%; border: 2rpx solid #ebeef5; border-radius: 10rpx; padding: 16rpx;
  font-size: 26rpx; color: #303133; background: #f8f9fc; box-sizing: border-box; min-height: 120rpx;
}

/* 附件上传区 */
.upload-section { margin: 0 24rpx 20rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; }
.section-divider { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.add-file-btn { background: #f0f4ff; border-radius: 20rpx; padding: 12rpx 24rpx; }
.add-file-btn-text { font-size: 24rpx; color: #6c5bb3; font-weight: 600; }
.upload-tip-text { font-size: 22rpx; color: #c0c4cc; margin-bottom: 10rpx; display: block; }
.file-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; margin-top: 16rpx; }
.file-thumb-item { position: relative; display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.file-thumb {
  width: 100%; height: 200rpx; border-radius: 12rpx; overflow: hidden;
  background: #f5f7fa; border: 1rpx solid #ebeef5; position: relative;
}
.file-thumb-img { width: 100%; height: 100%; display: block; object-fit: cover; position: relative; z-index: 2; }
.file-loading-placeholder {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f5f7fa; z-index: 1;
}
.loading-text { font-size: 48rpx; opacity: 0.3; }
.file-thumb-doc { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.file-doc-icon { font-size: 50rpx; }
.file-thumb-name {
  font-size: 20rpx; color: #909399; text-align: center; width: 100%;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.file-delete-btn {
  position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx;
  background: rgba(0,0,0,0.6); border-radius: 50%; display: flex; align-items: center; justify-content: center; z-index: 5;
}
.file-delete-icon { font-size: 20rpx; color: #fff; }
.file-empty { padding: 40rpx 0; text-align: center; }
.file-empty-text { font-size: 28rpx; color: #c0c4cc; }
.file-thumb-error {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center; background: #fef0f0; z-index: 3;
}
.file-error-icon { font-size: 36rpx; margin-bottom: 6rpx; }
.file-error-text { font-size: 18rpx; color: #f56c6c; }

/* 底部操作按钮 */
.bottom-action-fixed {
  position: fixed; bottom: 0; left: 0; right: 0; padding: 20rpx 40rpx;
  background: #fff; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05); z-index: 100;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
.record-done-btn-top {
  width: 100%; height: 80rpx; background: #fff; border: 2rpx solid #07c160; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center; margin-bottom: 20rpx;
}
.record-done-icon { font-size: 32rpx; margin-right: 8rpx; }
.record-done-text { font-size: 28rpx; font-weight: 600; color: #07c160; }
.btn-save-bottom {
  width: 100%; height: 88rpx; background: #07c160; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center;
}
.btn-save-text { font-size: 32rpx; font-weight: 600; color: #ffffff; }
</style>
