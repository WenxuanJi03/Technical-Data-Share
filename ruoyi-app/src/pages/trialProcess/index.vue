<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="navBarStyle">
      <view class="nav-back" @tap="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <text class="nav-title">试制流程</text>
      <view class="nav-add" @tap="showAddDialog">
        <text class="nav-add-icon">＋</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索轮形-模号 / 发起人..."
        @input="onSearch"
        confirm-type="search"
      />
    </view>

    <!-- 流程列表 -->
    <scroll-view scroll-y class="list" @scrolltolower="loadMore">
      <view v-if="filteredList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无试制流程</text>
      </view>

      <view v-for="item in filteredList" :key="item.processId" class="process-card">
        <!-- 卡片头部 -->
        <view class="card-header">
          <view class="header-left">
            <text class="mold-code">{{ item.moldCode }}</text>
            <view
              class="status-badge"
              :class="
                item.step1Status === 'done' &&
                item.step2Status === 'done' &&
                item.step3Status === 'done' &&
                item.step4Status === 'done' &&
                item.step5Status === 'done' &&
                item.step6Status === 'done'
                  ? 'badge-done'
                  : (
                    item.step1Status !== 'done' &&
                    item.step2Status !== 'done' &&
                    item.step3Status !== 'done' &&
                    item.step4Status !== 'done' &&
                    item.step5Status !== 'done' &&
                    item.step6Status !== 'done'
                      ? 'badge-pending'
                      : 'badge-active'
                  )
              "
            >
              <text class="status-text">{{ getCardStatus(item) }}</text>
            </view>
          </view>
          <view class="header-right">
            <text class="initiator">{{ item.initiator }}</text>
            <text class="create-time">{{ formatDate(item.createTime) }}</text>
          </view>
        </view>

        <!-- 试制说明 -->
        <view class="desc-row" v-if="item.description">
          <text class="desc-label">说明：</text>
          <text class="desc-text">{{ item.description }}</text>
        </view>

        <!-- 水平进度指示器 -->
        <view class="steps-progress">
          <view
            v-for="(step, idx) in getSteps(item)"
            :key="idx"
            class="progress-dot-wrap"
          >
            <view
              class="progress-dot"
              :class="step.status === 'done' ? 'dot-done' : (step.status === 'active' ? 'dot-active' : 'dot-pending')"
            >
              <text class="dot-icon">{{ step.status === 'done' ? '✓' : (step.status === 'active' ? '●' : '○') }}</text>
            </view>
            <view class="progress-line" v-if="idx < 5" :class="{ 'line-done': step.status === 'done' }"></view>
          </view>
        </view>

        <!-- 步骤标签 -->
        <view class="progress-labels">
          <view v-for="(step, idx) in getSteps(item)" :key="idx" class="label-item">
            <text
              class="label-text"
              :class="step.status === 'done' ? 'l-done' : (step.status === 'active' ? 'l-active' : '')"
            >{{ step.shortName }}</text>
          </view>
        </view>

        <!-- 纵向步骤列表（含操作按钮）-->
        <view class="step-list">
          <view
            v-for="(step, idx) in getSteps(item)"
            :key="idx"
            class="step-item"
            :class="step.status === 'done'
              ? 'step-done'
              : ((step.deadline && step.status !== 'done')
                  ? 'step-expired'
                  : (step.status === 'active' ? 'step-active' : '')
                )"
          >
            <!-- 步骤标题行 -->
            <view class="step-head">
              <view
                class="step-dot-sm"
                :class="step.status === 'done'
                  ? 'sm-done'
                  : ((step.deadline && step.status !== 'done')
                      ? 'sm-expired'
                      : (step.status === 'active' ? 'sm-active' : 'sm-pending')
                    )"
              >
                <text class="step-dot-sm-icon">{{ step.status === 'done' ? '✓' : (step.status === 'active' ? '●' : '○') }}</text>
              </view>
              <text class="step-name">{{ step.name }}</text>
              <view class="step-status-tag" v-if="step.status === 'done'">
                <text class="tag-done">已完成</text>
              </view>
              <view class="step-status-tag" v-else-if="isExpired(step)">
                <text class="tag-expired">临近截止</text>
              </view>
              <view class="step-status-tag" v-else-if="step.status === 'active'">
                <text class="tag-active">进行中</text>
              </view>
            </view>

            <!-- 负责人 + 截止日期 -->
            <view class="step-meta" v-if="step.responsible || step.deadline">
              <text class="meta-item" v-if="step.responsible">👤 {{ step.responsible }}</text>
              <text
                class="meta-item"
                v-if="step.deadline"
                :class="step.status !== 'done' && step.deadline ? 'meta-expired' : ''"
              >
                ⏰ {{ step.deadline }}
              </text>
            </view>

            <!-- 操作按钮（2列网格）-->
            <view class="step-actions">
              <view class="action-btn btn-upload" @tap="openUpload(item, step, idx)">
                <text class="action-btn-icon">📁</text>
                <text class="action-btn-text">录入/上传</text>
              </view>
              <view v-if="canEditPhase(idx)" class="action-btn btn-comment" @tap="openComment(item, step, idx)">
                <text class="action-btn-icon">💬</text>
                <text class="action-btn-text">
                  意见
                  <text v-if="getCommentCount(item, idx) > 0" class="comment-badge">{{ getCommentCount(item, idx) }}</text>
                </text>
              </view>
              <view
                v-if="canEditPhase(idx) && step.status !== 'done'"
                class="action-btn btn-deadline"
                @tap="openDeadline(item, step, idx)"
              >
                <text class="action-btn-icon">📅</text>
                <text class="action-btn-text">截止</text>
              </view>
              <view
                v-if="canEditPhase(idx) && step.status !== 'done'"
                class="action-btn btn-done"
                @tap="markStepDone(item, idx)"
              >
                <text class="action-btn-icon">✅</text>
                <text class="action-btn-text">完成</text>
              </view>
              <view
                v-if="canEditPhase(idx) && step.status === 'done'"
                class="action-btn btn-undo"
                @tap="markStepUndo(item, idx)"
              >
                <text class="action-btn-icon">↩</text>
                <text class="action-btn-text">撤回</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 卡片底部 -->
        <view class="card-footer">
          <view class="footer-btn" @tap="handleEdit(item)">
            <text class="footer-btn-text">编辑</text>
          </view>
          <view class="footer-btn danger" @tap="handleDelete(item)">
            <text class="footer-btn-text">删除</text>
          </view>
        </view>
      </view>

      <view class="list-bottom" v-if="filteredList.length > 0">
        <text class="list-bottom-text">共 {{ filteredList.length }} 条记录</text>
      </view>
    </scroll-view>

    <!-- ===== 弹窗：发起/编辑试制 ===== -->
    <view class="modal-mask" v-if="dialogVisible" @tap.self="dialogVisible = false">
      <view class="modal-box">
        <view class="modal-title">{{ dialogTitle }}</view>
        <view class="modal-form">
          <view class="form-item">
            <text class="form-label">轮形-模号 *</text>
            <input class="form-input" v-model="form.moldCode" placeholder="如 07122C26-M3" />
          </view>
          <view class="form-item">
            <text class="form-label">发起人 *</text>
            <picker
              v-if="userList.length > 0"
              mode="selector"
              :range="userNameList"
              :value="selectedUserIndex"
              @change="onUserChange"
              class="form-picker"
            >
              <view class="picker-wrap">
                <text class="picker-value">{{ form.initiator || '请选择发起人' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
            <input v-else class="form-input" v-model="form.initiator" placeholder="请输入发起人姓名" />
          </view>
          <view class="form-item">
            <text class="form-label">试制说明</text>
            <textarea class="form-textarea" v-model="form.description" placeholder="请输入试制说明（选填）" />
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn cancel" @tap="dialogVisible = false">
            <text>取消</text>
          </view>
          <view class="modal-btn confirm" @tap="submitForm">
            <text>{{ submitLoading ? '保存中...' : '确定' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ===== 全屏弹窗：录入/上传 ===== -->
    <view class="fullpage-modal" v-if="uploadVisible">
      <view class="fullpage-nav" :style="navBarStyle">
        <view class="fullpage-back" @tap="uploadVisible = false">
          <text class="fullpage-back-icon">✕</text>
        </view>
        <text class="fullpage-title">{{ currentStep.name }} · 录入/上传</text>
        <view v-if="canEditPhase(currentStepIndex)" class="fullpage-save" @tap="submitOE">
          <text class="fullpage-save-text">{{ oeLoading ? '保存中' : '保存' }}</text>
        </view>
      </view>

      <scroll-view scroll-y class="fullpage-body">
        <!-- 提示 -->
        <view class="oe-tip">
          <text class="oe-tip-icon">ℹ</text>
          <text class="oe-tip-text">以下数据与 OE试制跟踪 联动，保存后可在跟踪卡片中查看</text>
        </view>
        <view v-if="!canEditPhase(currentStepIndex)" class="oe-tip oe-tip-warn">
          <text class="oe-tip-icon">⚠</text>
          <text class="oe-tip-text">您没有此节点的编辑权限，仅可查看</text>
        </view>

        <!-- OE数据录入表单（按阶段显示不同字段）-->
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
              <picker mode="date" :value="formatPickerDate(oeForm.planMachineTime)" @change="$set(oeForm, 'planMachineTime', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                  <text :style="{ color: formatPickerDate(oeForm.planMachineTime) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.planMachineTime) || 'YYYY-MM-DD' }}</text>
                </view>
              </picker>
            </view>
          </template>

          <!-- 热工阶段 -->
          <template v-else-if="currentPhase === 'hot'">
            <view class="oe-form-row">
              <view class="oe-form-item half">
                <text class="oe-label">压铸上机日期</text>
                <picker mode="date" :value="formatPickerDate(oeForm.hotMachineDate)" @change="$set(oeForm, 'hotMachineDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.hotMachineDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.hotMachineDate) || 'YYYY-MM-DD' }}</text>
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
                <picker mode="date" :value="formatPickerDate(oeForm.spinMachineDate)" @change="$set(oeForm, 'spinMachineDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.spinMachineDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.spinMachineDate) || 'YYYY-MM-DD' }}</text>
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
              <text class="oe-label">改模记录</text>
              <textarea class="oe-textarea" v-model="oeForm.moldModifyRecord" placeholder="请输入改模记录" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </template>

          <!-- 粗车阶段 -->
          <template v-else-if="currentPhase === 'rough'">
            <view class="oe-form-row">
              <view class="oe-form-item half">
                <text class="oe-label">粗车上机日期</text>
                <picker mode="date" :value="formatPickerDate(oeForm.roughMachineDate)" @change="$set(oeForm, 'roughMachineDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.roughMachineDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.roughMachineDate) || 'YYYY-MM-DD' }}</text>
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
              <text class="oe-label">改善方案</text>
              <textarea class="oe-textarea" v-model="oeForm.improvePlan" placeholder="请输入改善方案" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </template>

          <!-- 精车/涂装 -->
          <template v-else-if="currentPhase === 'finePaint'">
            <view class="oe-form-row">
              <view class="oe-form-item half">
                <text class="oe-label">精车上机日期</text>
                <picker mode="date" :value="formatPickerDate(oeForm.fineMachineDate)" @change="$set(oeForm, 'fineMachineDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.fineMachineDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.fineMachineDate) || 'YYYY-MM-DD' }}</text>
                  </view>
                </picker>
              </view>
              <view class="oe-form-item half">
                <text class="oe-label">涂装上机日期</text>
                <picker mode="date" :value="formatPickerDate(oeForm.paintMachineDate)" @change="$set(oeForm, 'paintMachineDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.paintMachineDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.paintMachineDate) || 'YYYY-MM-DD' }}</text>
                  </view>
                </picker>
              </view>
            </view>
            <view class="oe-form-item">
              <text class="oe-label">精车生产情况</text>
              <textarea class="oe-textarea" v-model="oeForm.fineProduction" placeholder="请输入精车生产情况" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item">
              <text class="oe-label">涂装生产情况</text>
              <textarea class="oe-textarea" v-model="oeForm.paintProduction" placeholder="请输入涂装生产情况" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item">
              <text class="oe-label">涂装负责人</text>
              <input class="oe-input" v-model="oeForm.paintImprovePerson" placeholder="负责人姓名" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </template>

          <!-- 实验/总结 -->
          <template v-else-if="currentPhase === 'test'">
            <view class="oe-form-row">
              <view class="oe-form-item half">
                <text class="oe-label">冲击试验日期</text>
                <picker mode="date" :value="formatPickerDate(oeForm.impactTestDate)" @change="$set(oeForm, 'impactTestDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.impactTestDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.impactTestDate) || 'YYYY-MM-DD' }}</text>
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
                <picker mode="date" :value="formatPickerDate(oeForm.completeDate)" @change="$set(oeForm, 'completeDate', $event.detail.value)" :disabled="!canEditPhase(currentStepIndex)">
                  <view class="oe-input" style="display:flex;align-items:center;justify-content:space-between">
                    <text :style="{ color: formatPickerDate(oeForm.completeDate) ? '#303133' : '#999', fontSize: '26rpx' }">{{ formatPickerDate(oeForm.completeDate) || 'YYYY-MM-DD' }}</text>
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
            <view class="oe-form-item">
              <text class="oe-label">实验关闭情况</text>
              <input class="oe-input" v-model="oeForm.testCloseStatus" placeholder="请输入实验关闭情况" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item">
              <text class="oe-label">失效产品清场</text>
              <textarea class="oe-textarea" v-model="oeForm.failProductTrace" placeholder="请输入失效产品清场情况" :disabled="!canEditPhase(currentStepIndex)" />
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
              <text class="oe-label">改善措施简述</text>
              <textarea class="oe-textarea" v-model="oeForm.improveMeasures" placeholder="请输入改善措施" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
            <view class="oe-form-item">
              <text class="oe-label">经验教训总结</text>
              <textarea class="oe-textarea" v-model="oeForm.lessonsLearned" placeholder="请输入经验教训" :disabled="!canEditPhase(currentStepIndex)" />
            </view>
          </template>
        </view>

        <!-- 附件上传区 -->
        <view class="upload-section">
          <view class="section-divider">
            <text class="section-label">📎 附件上传</text>
            <view v-if="canEditPhase(currentStepIndex)" class="add-file-btn" @tap="chooseAndUpload">
              <text class="add-file-btn-text">{{ uploadingFile ? '上传中...' : '+ 添加图片/文件' }}</text>
            </view>
          </view>
          <text class="upload-tip-text">支持图片（JPG、JPEG、PNG、GIF、BMP、WEBP）及 PDF 文件</text>

          <!-- 已上传文件列表 -->
          <view class="file-grid" v-if="historyFiles.length > 0">
            <view
              v-for="(file, fIdx) in historyFiles"
              :key="fIdx"
              class="file-thumb-item"
            >
              <view class="file-thumb" @tap="previewFile(file, fIdx)" @longpress="showFileInfo(file)">
                <image
                  v-if="isImageFile(file.name)"
                  :src="getImageSrc(file, fIdx)"
                  class="file-thumb-img"
                  mode="aspectFill"
                  lazy-load
                  :show-menu-by-longpress="true"
                  @error="onImageError"
                  @load="onImageLoad"
                />
                <view v-else class="file-thumb-doc">
                  <text class="file-doc-icon">📄</text>
                </view>
                <view v-if="isImageFile(file.name)" class="file-loading-placeholder">
                  <text class="loading-text">🖼️</text>
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

        <view style="height:60rpx"></view>
      </scroll-view>
    </view>

    <!-- ===== 全屏弹窗：意见 ===== -->
    <view class="fullpage-modal" v-if="commentVisible">
      <view class="fullpage-nav" :style="navBarStyle">
        <view class="fullpage-back" @tap="commentVisible = false">
          <text class="fullpage-back-icon">✕</text>
        </view>
        <text class="fullpage-title">{{ currentStep.name }} · 意见</text>
        <view style="width:100rpx"></view>
      </view>

      <scroll-view scroll-y class="fullpage-body">
        <!-- 发表新意见 -->
        <view class="comment-input-section">
          <view v-if="!canEditPhase(currentStepIndex)" class="oe-tip oe-tip-warn" style="margin-bottom:12rpx">
            <text class="oe-tip-icon">⚠</text>
            <text class="oe-tip-text">您没有此节点的编辑权限，仅可查看</text>
          </view>
          <text class="section-label">发表意见</text>
          <textarea
            class="comment-textarea"
            v-model="commentText"
            placeholder="请输入您的意见..."
            maxlength="500"
            :disabled="!canEditPhase(currentStepIndex)"
          />
          <view class="comment-submit-row">
            <text class="comment-char-count">{{ commentText.length }}/500</text>
            <view v-if="canEditPhase(currentStepIndex)" class="comment-submit-btn" @tap="submitComment">
              <text class="comment-submit-text">提交意见</text>
            </view>
          </view>
        </view>

        <!-- 历史意见列表 -->
        <view class="comment-history-section" v-if="historyComments.length > 0">
          <text class="section-label">历史意见（{{ historyComments.length }}）</text>
          <view
            v-for="(c, cIdx) in historyComments"
            :key="cIdx"
            class="comment-card"
          >
            <view class="comment-card-header">
              <text class="comment-user">👤 {{ c.user }}</text>
              <text class="comment-time">{{ formatChineseDateTime(c.time) }}{{ c.edited ? ' (已编辑)' : '' }}</text>
            </view>
            <view v-if="editCommentIdx === cIdx" class="comment-edit-area">
              <textarea class="comment-textarea" v-model="editCommentText" />
              <view class="comment-edit-actions">
                <view class="edit-cancel-btn" @tap="cancelEditComment">
                  <text>取消</text>
                </view>
                <view class="edit-save-btn" @tap="saveEditComment(cIdx)">
                  <text>保存</text>
                </view>
              </view>
            </view>
            <text v-else class="comment-content">{{ c.content }}</text>
            <view v-if="canEditPhase(currentStepIndex)" class="comment-card-actions">
              <view class="comment-action-btn" @tap="editComment(cIdx)">
                <text class="comment-action-text">✏ 编辑</text>
              </view>
              <view class="comment-action-btn danger" @tap="deleteComment(cIdx)">
                <text class="comment-action-text danger">🗑 删除</text>
              </view>
            </view>
          </view>
        </view>
        <view class="comment-history-empty" v-else>
          <text class="history-empty-text">暂无历史意见</text>
        </view>

        <view style="height:60rpx"></view>
      </scroll-view>
    </view>

    <!-- ===== 底部弹窗：截止日期 ===== -->
    <view class="modal-mask" v-if="deadlineVisible" @tap.self="deadlineVisible = false">
      <view class="modal-box">
        <view class="modal-title">{{ currentStep.name }} · 截止日期</view>
        <view class="modal-form">
          <view class="form-item">
            <text class="form-label">截止日期</text>
            <picker
              mode="date"
              :value="deadlineDate || currentDate"
              @change="onDeadlineChange"
            >
              <view class="picker-wrap">
                <text class="picker-value">{{ deadlineDate || '点击选择截止日期' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">负责人</text>
            <picker
              v-if="userList.length > 0"
              mode="selector"
              :range="userNameList"
              :value="selectedResponsibleIndex"
              @change="onResponsibleChange"
            >
              <view class="picker-wrap">
                <text class="picker-value">{{ stepResponsible || '请选择负责人' }}</text>
                <text class="picker-arrow">▾</text>
              </view>
            </picker>
            <input v-else class="form-input" v-model="stepResponsible" placeholder="请输入负责人姓名" />
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn cancel" @tap="deadlineVisible = false">
            <text>取消</text>
          </view>
          <view class="modal-btn confirm" @tap="submitDeadline">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { listTrialProcess, addTrialProcess, updateTrialProcess, delTrialProcess } from '@/api/trialProcess'
import { listTrialTrack, addTrialTrack, updateTrialTrack } from '@/api/trialTrack'
import { uploadFile, BASE_URL } from '@/api/product'
import { getAllUsers } from '@/api/common'

export default {
  data() {
    return {
      loading: false,
      list: [],
      searchKeyword: '',
      // 发起/编辑弹窗
      dialogVisible: false,
      dialogTitle: '发起试制',
      form: {},
      submitLoading: false,
      // 当前操作的流程和步骤
      currentProcess: null,
      currentStep: {},
      currentStepIndex: 0,
      // 录入/上传弹窗
      uploadVisible: false,
      oeLoading: false,
      oeForm: {},
      currentPhase: 'base',
      historyFiles: [],
      uploadingFile: false,
      baseUrl: BASE_URL,
      // 微信小程序 HTTP 图片无法直接显示，需先 downloadFile 转本地路径
      imageLocalPaths: {},
      // 意见弹窗
      commentVisible: false,
      commentText: '',
      historyComments: [],
      editCommentIdx: -1,
      editCommentText: '',
      // 截止弹窗
      deadlineVisible: false,
      deadlineDate: '',
      stepResponsible: '',
      selectedResponsibleIndex: 0,
      // 用户列表（发起人 + 负责人选择）
      userList: [],
      showUserPicker: false,
      selectedUserIndex: 0,
      // 导航栏适配
      statusBarHeight: 44,
      capsuleRightPadding: 104
    }
  },
  onLoad() {
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
  },
  computed: {
    filteredList() {
      if (!this.searchKeyword) return this.list
      const kw = this.searchKeyword.toLowerCase()
      return this.list.filter(i =>
        (i.moldCode && i.moldCode.toLowerCase().includes(kw)) ||
        (i.initiator && i.initiator.toLowerCase().includes(kw))
      )
    },
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
  onShow() {
    this.loadList()
    this.loadUserList()
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    loadList() {
      this.loading = true
      listTrialProcess({ pageNum: 1, pageSize: 100 }).then(res => {
        this.list = res.rows || []
      }).finally(() => {
        this.loading = false
      })
    },
    loadUserList() {
      getAllUsers().then(res => {
        this.userList = res.rows || []
      }).catch(() => {})
    },
    onSearch() {},
    loadMore() {},

    // =================== 步骤数据处理 ===================
    getSteps(item) {
      return [
        { name: '基础信息', shortName: '基础', status: item.step1Status, deadline: item.step1Deadline, responsible: item.step1Responsible },
        { name: '热工阶段', shortName: '热工', status: item.step2Status, deadline: item.step2Deadline, responsible: item.step2Responsible },
        { name: '旋压阶段', shortName: '旋压', status: item.step3Status, deadline: item.step3Deadline, responsible: item.step3Responsible },
        { name: '粗车阶段', shortName: '粗车', status: item.step4Status, deadline: item.step4Deadline, responsible: item.step4Responsible },
        { name: '精车/涂装', shortName: '精车', status: item.step5Status, deadline: item.step5Deadline, responsible: item.step5Responsible },
        { name: '实验/总结', shortName: '实验', status: item.step6Status, deadline: item.step6Deadline, responsible: item.step6Responsible }
      ]
    },
    getCardStatus(item) {
      const steps = this.getSteps(item)
      const doneCount = steps.filter(s => s.status === 'done').length
      if (doneCount === 6) return '已完成'
      if (doneCount === 0) return '未开始'
      return `${doneCount}/6`
    },
    getCardStatusClass(item) {
      const steps = this.getSteps(item)
      const doneCount = steps.filter(s => s.status === 'done').length
      if (doneCount === 6) return 'badge-done'
      if (doneCount === 0) return 'badge-pending'
      return 'badge-active'
    },
    getStepIcon(step) {
      if (step.status === 'done') return '✓'
      if (step.status === 'active') return '●'
      return '○'
    },
    isExpired(step) {
      if (!step.deadline || step.status === 'done') return false
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const deadlineDate = new Date(step.deadline + 'T00:00:00')
      const diffDays = Math.floor((deadlineDate - today) / (1000 * 60 * 60 * 24))
      return diffDays <= 1
    },
    getCommentCount(item, idx) {
      const key = `step${idx + 1}Comments`
      try {
        const arr = item[key] ? JSON.parse(item[key]) : []
        return arr.length
      } catch (e) {
        return 0
      }
    },
    formatDate(date) {
      if (!date) return ''
      return date.substring(0, 10)
    },
    // \u65e5\u671f\u5b57\u6bb5\u8f6c\u6362\u4e3a YYYY-MM-DD \u5b57\u7b26\u4e32\uff08\u9632\u6b62 [object Object]\uff09
    formatPickerDate(val) {
      if (!val) return ''
      if (typeof val === 'string') return val.substring(0, 10)
      if (val instanceof Date) {
        const y = val.getFullYear()
        const m = String(val.getMonth() + 1).padStart(2, '0')
        const d = String(val.getDate()).padStart(2, '0')
        return `${y}-${m}-${d}`
      }
      // \u5bf9\u8c61 (\u5982 {year, month, day}\uff09
      if (typeof val === 'object') {
        const s = String(val)
        if (s.length >= 10) return s.substring(0, 10)
        return ''
      }
      return String(val).substring(0, 10)
    },
    formatChineseDateTime(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = d.getMonth() + 1
      const day = d.getDate()
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekDay = weekDays[d.getDay()]
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year} 年 ${month} 月 ${day} 日，${weekDay}，${hours}:${minutes}:${seconds} 中国标准时间`
    },
    getPhaseKey(index) {
      const map = ['base', 'hot', 'spin', 'rough', 'finePaint', 'test']
      return map[index] || 'base'
    },

    // =================== 标记完成/撤回 ===================
    markStepDone(item, index) {
      const stepName = this.getSteps(item)[index].name
      uni.showModal({
        title: '确认完成',
        content: `确认将"${stepName}"标记为已完成？`,
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            const stepNum = index + 1
            const updateData = { processId: item.processId }
            updateData[`step${stepNum}Status`] = 'done'
            if (stepNum < 6) {
              updateData[`step${stepNum + 1}Status`] = 'active'
            }
            updateTrialProcess(updateData).then(() => {
              uni.showToast({ title: '已标记为完成', icon: 'success' })
              this.loadList()
            })
          }
        }
      })
    },
    markStepUndo(item, index) {
      const stepName = this.getSteps(item)[index].name
      uni.showModal({
        title: '确认撤回',
        content: `确认撤回"${stepName}"的完成状态？`,
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            const stepNum = index + 1
            const updateData = { processId: item.processId }
            updateData[`step${stepNum}Status`] = 'active'
            updateTrialProcess(updateData).then(() => {
              uni.showToast({ title: '已撤回', icon: 'success' })
              this.loadList()
            })
          }
        }
      })
    },

    // =================== 发起/编辑试制 ===================
    showAddDialog() {
      this.dialogTitle = '发起试制'
      const currentName = this.$store.state.name || ''
      const idx = this.userList.findIndex(u => u.nickName === currentName)
      this.selectedUserIndex = idx >= 0 ? idx : 0
      const defaultInitiator = idx >= 0 ? currentName : (this.userList.length > 0 ? this.userList[0].nickName : currentName)
      this.form = {
        moldCode: '',
        initiator: defaultInitiator,
        description: '',
        status: 'active',
        step1Status: 'active',
        step2Status: 'pending',
        step3Status: 'pending',
        step4Status: 'pending',
        step5Status: 'pending',
        step6Status: 'pending'
      }
      this.dialogVisible = true
    },
    handleEdit(item) {
      this.dialogTitle = '编辑试制'
      this.form = { ...item }
      this.dialogVisible = true
    },
    handleDelete(item) {
      uni.showModal({
        title: '确认删除',
        content: `确认删除 ${item.moldCode} 的试制流程？`,
        confirmColor: '#f56c6c',
        success: (res) => {
          if (res.confirm) {
            delTrialProcess(item.processId).then(() => {
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadList()
            })
          }
        }
      })
    },
    submitForm() {
      if (!this.form.moldCode || !this.form.moldCode.trim()) {
        uni.showToast({ title: '请输入轮形-模号', icon: 'none' })
        return
      }
      if (!this.form.initiator || !this.form.initiator.trim()) {
        uni.showToast({ title: '请输入发起人', icon: 'none' })
        return
      }
      this.submitLoading = true
      const save = this.form.processId ? updateTrialProcess : addTrialProcess
      save(this.form).then(() => {
        uni.showToast({ title: this.form.processId ? '修改成功' : '发起成功', icon: 'success' })
        this.dialogVisible = false
        this.loadList()
      }).finally(() => {
        this.submitLoading = false
      })
    },
    onUserChange(e) {
      const idx = e.detail.value
      this.selectedUserIndex = idx
      this.form.initiator = this.userList[idx].nickName
      this.showUserPicker = false
    },

    // =================== 录入/上传 ===================
    openUpload(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.currentPhase = this.getPhaseKey(index)
      const filesKey = `step${index + 1}Files`
      try {
        this.historyFiles = process[filesKey] ? JSON.parse(process[filesKey]) : []
      } catch (e) {
        this.historyFiles = []
      }
      // 微信小程序：HTTP 图片无法直接显示，需先 downloadFile 转本地路径
      this.imageLocalPaths = {}
      this.prefetchImagesForDisplay()
      // 加载OE跟踪数据
      this.oeForm = { moldCode: process.moldCode, allProcessDone: '否' }
      if (process.moldCode) {
        listTrialTrack({ pageNum: 1, pageSize: 1, moldCode: process.moldCode }).then(res => {
          const rows = res.rows || []
          if (rows.length > 0) {
            const loaded = { ...rows[0] }
            // 格式化日期字段，避免 对象显示 [object Object]
            const dateFields = ['planMachineTime','hotMachineDate','spinMachineDate','heatTransferTime',
              'roughMachineDate','fineMachineDate','paintMachineDate','impactTestDate','completeDate']
            dateFields.forEach(f => { if (loaded[f]) { loaded[f] = this.formatPickerDate(loaded[f]) } })
            this.oeForm = loaded
          }
        }).catch(() => {})
      }
      this.uploadVisible = true
    },
    submitOE() {
      if (this.oeLoading) return
      this.oeLoading = true

      // 自定同步图片到OE轨道字段
      const imageUrls = this.historyFiles.filter(f => this.isImageFile(f.name)).map(f => f.url).join(',');
      if (imageUrls) {
        if (this.currentPhase === 'hot') this.oeForm.hotCheckMeasureImage = imageUrls;
        else if (this.currentPhase === 'spin') this.oeForm.spinFrontDistanceImage = imageUrls;
        else if (this.currentPhase === 'heat') this.oeForm.heatFlowSheetImage = imageUrls;
        else if (this.currentPhase === 'paint') this.oeForm.paintFlowSheetImage = imageUrls;
      }

      const save = this.oeForm.trackId ? updateTrialTrack : addTrialTrack
      save(this.oeForm).then(() => {
        uni.showToast({ title: 'OE数据已保存', icon: 'success' })
        this.uploadVisible = false
        this.loadList()
      }).catch(() => {
        uni.showToast({ title: '保存失败', icon: 'none' })
      }).finally(() => {
        this.oeLoading = false
      })
    },
    chooseAndUpload() {
      if (this.uploadingFile) return
      uni.showActionSheet({
        itemList: ['从相册选择图片', '拍照', '选择PDF文件'],
        success: (action) => {
          if (action.tapIndex === 0 || action.tapIndex === 1) {
            uni.chooseImage({
              count: 9,
              sizeType: ['original', 'compressed'],
              sourceType: [action.tapIndex === 0 ? 'album' : 'camera'],
              success: (res) => {
                const files = res.tempFilePaths.map(p => ({ path: p, name: '' }))
                this.doUpload(files)
              }
            })
          } else {
            // #ifdef MP-WEIXIN
            wx.chooseMessageFile({
              count: 5,
              type: 'file',
              extension: ['pdf'],
              success: (res) => {
                const files = res.tempFiles.map(f => ({ path: f.path, name: f.name }))
                this.doUpload(files)
              },
              fail: () => {
                uni.showToast({ title: '请先将PDF发送到微信，再从聊天文件中选择', icon: 'none', duration: 3000 })
              }
            })
            // #endif
            // #ifndef MP-WEIXIN
            uni.chooseFile({
              count: 5,
              extension: ['.pdf'],
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
          // 优先使用 fileName（相对路径），由前端统一拼接完整URL
          // 这样可以避免后端返回的 url 地址不正确的问题
          let fileUrl = data.fileName || data.url || filePath
          
          // 如果是相对路径，拼接 baseUrl
          if (fileUrl && !fileUrl.startsWith('http://') && !fileUrl.startsWith('https://')) {
            fileUrl = this.baseUrl + fileUrl
          }
          
          const newFile = {
            name: fileName,
            url: fileUrl,
            time: new Date().toISOString()
          }
          this.historyFiles.push(newFile)
          this.prefetchImagesForDisplay()
          const filesKey = `step${this.currentStepIndex + 1}Files`
          const updateData = {
            processId: this.currentProcess.processId,
            [filesKey]: JSON.stringify(this.historyFiles)
          }
          updateTrialProcess(updateData).then(() => {
            uploadNext(i + 1)
          })
        }).catch(() => {
          uploadNext(i + 1)
        })
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
            const updateData = {
              processId: this.currentProcess.processId,
              [filesKey]: JSON.stringify(this.historyFiles)
            }
            updateTrialProcess(updateData).then(() => {
              uni.showToast({ title: '已删除', icon: 'success' })
              this.loadList()
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
    showFileInfo(file) {
      const fullUrl = this.getFileUrl(file.url)
      uni.showModal({
        title: '文件信息',
        content: `文件名: ${file.name}\n\n原始URL: ${file.url}\n\n完整URL: ${fullUrl}`,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '复制URL',
        success: (res) => {
          if (res.confirm) {
            uni.setClipboardData({
              data: fullUrl,
              success: () => {
                uni.showToast({ title: '已复制到剪贴板', icon: 'success' })
              }
            })
          }
        }
      })
    },
    getFileUrl(url) {
      if (!url) return ''
      // 如果已经是完整URL，直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      
      // 如果是相对路径，拼接baseUrl
      const fullUrl = this.baseUrl + url
      return fullUrl
    },
    /**
     * 获取图片显示用的 src。
     * 微信小程序对 HTTP 图片限制严格，需先用 downloadFile 下载到本地再显示。
     * @param fIdx 文件在 historyFiles 中的索引，用于索引 key 避免长 URL 导致 setData 异常
     */
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
    /**
     * 预下载图片到本地（仅微信小程序，解决 HTTP 图片无法直接显示）
     * 使用索引作为 key，避免长 URL 导致微信 setData 序列化异常
     */
    prefetchImagesForDisplay() {
      // #ifdef MP-WEIXIN
      this.historyFiles.forEach((file, fIdx) => {
        if (!this.isImageFile(file.name)) return
        const fullUrl = this.getFileUrl(file.url)
        if (!fullUrl || fullUrl.startsWith('https://')) return
        const key = 'i' + fIdx
        if (this.imageLocalPaths[key]) return
        uni.downloadFile({
          url: fullUrl,
          success: (res) => {
            if (res.statusCode !== 200 || !res.tempFilePath) return
            this.$nextTick(() => {
              if (!this.uploadVisible || fIdx >= this.historyFiles.length) return
              try {
                this.$set(this.imageLocalPaths, key, res.tempFilePath)
              } catch (e) {}
            })
          },
          fail: () => {}
        })
      })
      // #endif
    },
    previewFile(file, fIdx) {
      if (this.isImageFile(file.name)) {
        const imageUrls = this.historyFiles
          .map((f, i) => this.isImageFile(f.name) ? this.getImageSrc(f, i) : null)
          .filter(Boolean)
        const currentUrl = this.getImageSrc(file, fIdx)
        
        // 检查URL是否有效
        if (!currentUrl) {
          uni.showToast({ title: '图片地址无效', icon: 'none' })
          return
        }
        
        uni.previewImage({
          urls: imageUrls,
          current: currentUrl,
          fail: (err) => {
            console.error('预览失败:', err)
            uni.showToast({ title: '图片预览失败', icon: 'none' })
          }
        })
      } else {
        const fileUrl = this.getFileUrl(file.url)
        if (!fileUrl) {
          uni.showToast({ title: '文件地址无效', icon: 'none' })
          return
        }
        
        uni.showLoading({ title: '加载中...' })
        // #ifdef MP-WEIXIN
        wx.downloadFile({
          url: fileUrl,
          success: (res) => {
            uni.hideLoading()
            if (res.statusCode === 200) {
              wx.openDocument({
                filePath: res.tempFilePath,
                showMenu: true,
                fail: (err) => {
                  console.error('文档打开失败:', err)
                  uni.showToast({ title: '无法预览此文件', icon: 'none' })
                }
              })
            } else {
              uni.showToast({ title: '文件下载失败', icon: 'none' })
            }
          },
          fail: (err) => {
            uni.hideLoading()
            console.error('下载失败:', err)
            uni.showToast({ title: '文件加载失败，请检查网络', icon: 'none' })
          }
        })
        // #endif
        // #ifndef MP-WEIXIN
        uni.hideLoading()
        uni.showToast({ title: '请在微信小程序中打开文件', icon: 'none' })
        // #endif
      }
    },
    shortenName(name) {
      if (!name) return '文件'
      return name.length > 8 ? name.substring(0, 8) + '...' : name
    },
    onImageError() {
      // 静默处理：微信小程序 HTTP 图片会先失败，等 downloadFile 完成后会重新渲染
      // 避免频繁 toast 和打印大对象导致卡顿
    },
    onImageLoad() {},

    // =================== 意见 ===================
    openComment(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.commentText = ''
      this.editCommentIdx = -1
      this.editCommentText = ''
      const key = `step${index + 1}Comments`
      try {
        this.historyComments = process[key] ? JSON.parse(process[key]) : []
      } catch (e) {
        this.historyComments = []
      }
      this.commentVisible = true
    },
    submitComment() {
      if (!this.commentText.trim()) {
        uni.showToast({ title: '请输入意见内容', icon: 'none' })
        return
      }
      const key = `step${this.currentStepIndex + 1}Comments`
      const newComment = {
        user: this.$store.state.name || '用户',
        time: new Date().toISOString(),
        content: this.commentText
      }
      const comments = [...this.historyComments, newComment]
      const updateData = {
        processId: this.currentProcess.processId,
        [key]: JSON.stringify(comments)
      }
      updateTrialProcess(updateData).then(() => {
        uni.showToast({ title: '意见已提交', icon: 'success' })
        this.historyComments = comments
        this.commentText = ''
        this.loadList()
      })
    },
    editComment(cIdx) {
      this.editCommentIdx = cIdx
      this.editCommentText = this.historyComments[cIdx].content
    },
    cancelEditComment() {
      this.editCommentIdx = -1
      this.editCommentText = ''
    },
    saveEditComment(cIdx) {
      if (!this.editCommentText.trim()) {
        uni.showToast({ title: '内容不能为空', icon: 'none' })
        return
      }
      const key = `step${this.currentStepIndex + 1}Comments`
      this.historyComments[cIdx].content = this.editCommentText
      this.historyComments[cIdx].time = new Date().toISOString()
      this.historyComments[cIdx].edited = true
      const updateData = {
        processId: this.currentProcess.processId,
        [key]: JSON.stringify(this.historyComments)
      }
      updateTrialProcess(updateData).then(() => {
        uni.showToast({ title: '修改成功', icon: 'success' })
        this.editCommentIdx = -1
        this.editCommentText = ''
        this.loadList()
      })
    },
    deleteComment(cIdx) {
      uni.showModal({
        title: '确认删除',
        content: '确认删除此条意见？',
        success: (res) => {
          if (res.confirm) {
            const key = `step${this.currentStepIndex + 1}Comments`
            this.historyComments.splice(cIdx, 1)
            const updateData = {
              processId: this.currentProcess.processId,
              [key]: JSON.stringify(this.historyComments)
            }
            updateTrialProcess(updateData).then(() => {
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadList()
            })
          }
        }
      })
    },

    // =================== 截止日期 ===================
    openDeadline(process, step, index) {
      this.currentProcess = process
      this.currentStep = step
      this.currentStepIndex = index
      this.deadlineDate = step.deadline || ''
      this.stepResponsible = step.responsible || ''
      this.deadlineVisible = true
    },
    onDeadlineChange(e) {
      this.deadlineDate = e.detail.value
    },
    onResponsibleChange(e) {
      const idx = e.detail.value
      this.selectedResponsibleIndex = idx
      this.stepResponsible = this.userList[idx].nickName
    },
    submitDeadline() {
      if (!this.deadlineDate && !this.stepResponsible) {
        uni.showToast({ title: '请设置截止日期或负责人', icon: 'none' })
        return
      }
      const stepNum = this.currentStepIndex + 1
      const updateData = { processId: this.currentProcess.processId }
      if (this.deadlineDate) {
        updateData[`step${stepNum}Deadline`] = this.deadlineDate
      }
      if (this.stepResponsible) {
        updateData[`step${stepNum}Responsible`] = this.stepResponsible
      }
      updateTrialProcess(updateData).then(() => {
        uni.showToast({ title: '设置成功', icon: 'success' })
        this.deadlineVisible = false
        this.loadList()
      })
    },

    /** 判断当前用户是否有某节点的编辑权限（与PC试制流程、OE试制跟踪联动） */
    canEditPhase(index) {
      const perms = this.$store.state.permissions || []
      if (perms.includes('*:*:*') || perms.includes('tech:trialTrack:edit') || perms.includes('tech:process:edit')) return true
      const phasePerms = ['tech:trial:phase:base:edit', 'tech:trial:phase:hot:edit', 'tech:trial:phase:spin:edit', 'tech:trial:phase:rough:edit', 'tech:trial:phase:finePaint:edit', 'tech:trial:phase:test:edit']
      return index >= 0 && index < 6 && perms.includes(phasePerms[index])
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f0f2f5;
  padding-bottom: 40rpx;
}

/* ===== 导航栏 ===== */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f 0%, #6c5bb3 100%);
  padding: 88rpx 208rpx 16rpx 30rpx;
}
.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-back-icon {
  font-size: 52rpx;
  color: #fff;
  font-weight: 300;
  line-height: 1;
}
.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}
.nav-add {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
}
.nav-add-icon {
  font-size: 40rpx;
  color: #fff;
  line-height: 1;
}

/* ===== 搜索栏 ===== */
.search-bar {
  margin: 20rpx 24rpx;
}
.search-input {
  background: #fff;
  border-radius: 40rpx;
  padding: 16rpx 30rpx;
  font-size: 26rpx;
  color: #303133;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

/* ===== 列表 ===== */
.list {
  padding: 0 24rpx;
  height: calc(100vh - 300rpx);
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

/* ===== 流程卡片 ===== */
.process-card {
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-wrap: wrap;
}
.mold-code {
  font-size: 32rpx;
  font-weight: 700;
  color: #303133;
}
.status-badge {
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
}
.badge-done { background: #f0f9eb; }
.badge-done .status-text { color: #67c23a; }
.badge-active { background: #ecf5ff; }
.badge-active .status-text { color: #409eff; }
.badge-pending { background: #f5f7fa; }
.badge-pending .status-text { color: #909399; }
.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}
.initiator { font-size: 22rpx; color: #606266; }
.create-time { font-size: 20rpx; color: #c0c4cc; }

/* 说明行 */
.desc-row {
  display: flex;
  margin-bottom: 20rpx;
  background: #f8f9fc;
  border-radius: 10rpx;
  padding: 12rpx 16rpx;
}
.desc-label { font-size: 22rpx; color: #909399; flex-shrink: 0; }
.desc-text { font-size: 22rpx; color: #606266; flex: 1; }

/* 水平进度条 */
.steps-progress {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}
.progress-dot-wrap {
  display: flex;
  align-items: center;
  flex: 1;
}
.progress-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dot-done { background: #c0c4cc; }
.dot-done .dot-icon { color: #fff; font-size: 20rpx; }
.dot-active { background: #6c5bb3; }
.dot-active .dot-icon { color: #fff; font-size: 18rpx; }
.dot-pending { background: #e0e0e0; }
.dot-pending .dot-icon { color: #c0c4cc; font-size: 18rpx; }
.progress-line {
  flex: 1;
  height: 3rpx;
  background: #e0e0e0;
}
.progress-line.line-done { background: #c0c4cc; }

/* 进度标签 */
.progress-labels {
  display: flex;
  margin-bottom: 24rpx;
}
.label-item { flex: 1; text-align: center; }
.label-text { font-size: 18rpx; color: #909399; }
.l-done { color: #c0c4cc; }
.l-active { color: #6c5bb3; font-weight: 600; }

/* ===== 纵向步骤列表 ===== */
.step-list {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 20rpx;
}
.step-item {
  padding: 20rpx 0;
  border-bottom: 1rpx dashed #f0f0f0;
  &:last-child { border-bottom: none; }
}
.step-done .step-name { color: #c0c4cc; text-decoration: line-through; }
.step-expired .step-name { color: #f56c6c; }
.step-active .step-name { color: #303133; font-weight: 600; }

/* 步骤头部 */
.step-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 10rpx;
}
.step-dot-sm {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sm-done { background: #c0c4cc; }
.sm-expired { background: #f56c6c; }
.sm-active { background: #6c5bb3; }
.sm-pending { background: #e0e0e0; }
.step-dot-sm-icon { font-size: 18rpx; color: #fff; }
.step-name { font-size: 28rpx; color: #303133; flex: 1; }
.step-status-tag { flex-shrink: 0; }
.tag-done {
  font-size: 20rpx;
  color: #67c23a;
  background: #f0f9eb;
  padding: 3rpx 12rpx;
  border-radius: 16rpx;
}
.tag-expired {
  font-size: 20rpx;
  color: #f56c6c;
  background: #fef0f0;
  padding: 3rpx 12rpx;
  border-radius: 16rpx;
}
.tag-active {
  font-size: 20rpx;
  color: #6c5bb3;
  background: #f0f0ff;
  padding: 3rpx 12rpx;
  border-radius: 16rpx;
}

/* 步骤元信息 */
.step-meta {
  display: flex;
  gap: 20rpx;
  margin-bottom: 14rpx;
  padding-left: 48rpx;
}
.meta-item {
  font-size: 22rpx;
  color: #409eff;
}
.meta-expired { color: #f56c6c; font-weight: 600; }

/* 步骤操作按钮（2列网格）*/
.step-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
  padding-left: 48rpx;
}
.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 14rpx 0;
  border-radius: 12rpx;
  font-size: 22rpx;
  &:active { opacity: 0.7; }
}
.btn-upload { background: #f0f4ff; }
.btn-upload .action-btn-text { color: #6c5bb3; }
.btn-comment { background: #ecf5ff; }
.btn-comment .action-btn-text { color: #409eff; }
.btn-deadline { background: #fdf6ec; }
.btn-deadline .action-btn-text { color: #e6a23c; }
.btn-done { background: #f0f9eb; }
.btn-done .action-btn-text { color: #67c23a; }
.btn-undo { background: #fdf6ec; }
.btn-undo .action-btn-text { color: #e6a23c; }
.action-btn-icon { font-size: 26rpx; }
.comment-badge {
  display: inline-block;
  background: #409eff;
  color: #fff;
  font-size: 16rpx;
  border-radius: 20rpx;
  padding: 1rpx 8rpx;
  margin-left: 4rpx;
  vertical-align: middle;
}

/* ===== 卡片底部 ===== */
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 20rpx;
  margin-top: 20rpx;
}
.footer-btn {
  padding: 10rpx 30rpx;
  border-radius: 30rpx;
  background: #f5f7fa;
  border: 1rpx solid #dcdfe6;
  &:active { opacity: 0.7; }
}
.footer-btn.danger { background: #fef0f0; border-color: #fbc4c4; }
.footer-btn-text { font-size: 24rpx; color: #606266; }
.footer-btn.danger .footer-btn-text { color: #f56c6c; }

/* 列表底部 */
.list-bottom { text-align: center; padding: 30rpx 0; }
.list-bottom-text { font-size: 22rpx; color: #c0c4cc; }

/* ===== 通用底部弹窗 ===== */
.modal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: flex-end;
  z-index: 100;
}
.modal-box {
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
  max-height: 80vh;
  overflow-y: auto;
}
.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 30rpx;
  text-align: center;
}
.modal-form { margin-bottom: 20rpx; width: 100%; }
.form-item { margin-bottom: 24rpx; display: block; width: 100%; }
.form-label {
  display: block;
  font-size: 26rpx;
  color: #606266;
  margin-bottom: 10rpx;
}
.form-input {
  width: 100%;
  min-height: 96rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  line-height: 1.5;
  color: #303133;
  box-sizing: border-box;
  background: #f8f9fc;
}
.form-picker {
  display: block;
  width: 100%;
}
.form-textarea {
  width: 100%;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  color: #303133;
  box-sizing: border-box;
  background: #f8f9fc;
  min-height: 140rpx;
}
.picker-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 2rpx solid #dcdfe6;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  background: #f8f9fc;
  width: 100%;
  box-sizing: border-box;
}
.picker-value { font-size: 28rpx; color: #303133; }
.picker-arrow { font-size: 22rpx; color: #909399; }
.modal-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 10rpx;
}
.modal-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 50rpx;
  text-align: center;
  font-size: 28rpx;
  &:active { opacity: 0.8; }
}
.modal-btn.cancel { background: #f5f7fa; color: #606266; }
.modal-btn.confirm { background: linear-gradient(135deg, #4a3b8f, #6c5bb3); color: #fff; font-weight: 600; }

/* ===== 全屏弹窗（录入/上传、意见）===== */
.fullpage-modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: #f0f2f5;
  z-index: 200;
  display: flex;
  flex-direction: column;
}
.fullpage-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  padding: 88rpx 208rpx 16rpx 30rpx;
  flex-shrink: 0;
}
.fullpage-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fullpage-back-icon {
  font-size: 32rpx;
  color: rgba(255,255,255,0.9);
}
.fullpage-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  flex: 1;
  text-align: center;
  margin: 0 20rpx;
}
.fullpage-save {
  background: rgba(255,255,255,0.2);
  border-radius: 30rpx;
  padding: 10rpx 24rpx;
}
.fullpage-save-text { font-size: 26rpx; color: #fff; }
.fullpage-body { flex: 1; overflow-y: auto; }

/* OE数据录入 */
.oe-tip {
  margin: 20rpx 24rpx;
  background: #ecf5ff;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  display: flex;
  gap: 10rpx;
  align-items: flex-start;
}
.oe-tip-icon { font-size: 28rpx; flex-shrink: 0; }
.oe-tip-text { font-size: 22rpx; color: #409eff; line-height: 1.6; }
.oe-tip-warn { background: #fdf6ec; }
.oe-tip-warn .oe-tip-text { color: #e6a23c; }

.oe-form-section {
  margin: 0 24rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}
.section-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20rpx;
  display: block;
}
.oe-form-item {
  margin-bottom: 20rpx;
  &.half { flex: 1; }
}
.oe-form-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 0;
}
.oe-label {
  display: block;
  font-size: 24rpx;
  color: #606266;
  margin-bottom: 8rpx;
}
.oe-input {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border: 2rpx solid #ebeef5;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #303133;
  background: #f8f9fc;
  box-sizing: border-box;
}
.oe-textarea {
  width: 100%;
  border: 2rpx solid #ebeef5;
  border-radius: 10rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #303133;
  background: #f8f9fc;
  box-sizing: border-box;
  min-height: 120rpx;
}
.radio-group {
  display: flex;
  gap: 30rpx;
  padding: 10rpx 0;
}
.radio-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.radio-circle {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 3rpx solid #dcdfe6;
  background: #fff;
}
.radio-checked {
  border-color: #6c5bb3;
  background: #6c5bb3;
  box-shadow: inset 0 0 0 6rpx #fff;
}
.radio-label { font-size: 26rpx; color: #303133; }

/* 附件上传区 */
.upload-section {
  margin: 0 24rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}
.section-divider {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.add-file-btn {
  background: #f0f4ff;
  border-radius: 20rpx;
  padding: 8rpx 20rpx;
  &:active { opacity: 0.7; }
}
.add-file-btn-text { font-size: 24rpx; color: #6c5bb3; }
.upload-tip-text { font-size: 22rpx; color: #c0c4cc; margin-bottom: 20rpx; display: block; }
.file-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  margin-top: 16rpx;
}
.file-thumb-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.file-thumb {
  width: 100%;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: #f5f7fa;
  border: 1rpx solid #ebeef5;
  position: relative;
}
.file-thumb-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  position: relative;
  z-index: 2;
}
.file-loading-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  z-index: 1;
}
.loading-text {
  font-size: 48rpx;
  opacity: 0.3;
}
.file-thumb-doc {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.file-doc-icon { font-size: 50rpx; }
.file-thumb-name {
  font-size: 20rpx;
  color: #909399;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-delete-btn {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 36rpx;
  height: 36rpx;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.file-delete-icon { font-size: 20rpx; color: #fff; }
.file-empty { padding: 30rpx 0; text-align: center; }
.file-empty-text { font-size: 26rpx; color: #c0c4cc; }

/* ===== 意见弹窗样式 ===== */
.comment-input-section {
  margin: 20rpx 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}
.comment-textarea {
  width: 100%;
  border: 2rpx solid #ebeef5;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #303133;
  background: #f8f9fc;
  box-sizing: border-box;
  min-height: 160rpx;
  margin-bottom: 16rpx;
}
.comment-submit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.comment-char-count { font-size: 22rpx; color: #c0c4cc; }
.comment-submit-btn {
  background: linear-gradient(135deg, #4a3b8f, #6c5bb3);
  border-radius: 30rpx;
  padding: 12rpx 30rpx;
  &:active { opacity: 0.8; }
}
.comment-submit-text { font-size: 26rpx; color: #fff; font-weight: 600; }

.comment-history-section {
  margin: 0 24rpx 20rpx;
}
.comment-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.comment-card-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.comment-user { font-size: 24rpx; color: #409eff; font-weight: 600; flex: 1; }
.comment-time { font-size: 20rpx; color: #c0c4cc; }
.comment-content {
  font-size: 26rpx;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12rpx;
}
.comment-edit-area { margin-bottom: 12rpx; }
.comment-edit-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 12rpx;
  justify-content: flex-end;
}
.edit-cancel-btn {
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  background: #f5f7fa;
  font-size: 24rpx;
  color: #606266;
}
.edit-save-btn {
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  background: #6c5bb3;
  font-size: 24rpx;
  color: #fff;
}
.comment-card-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
  border-top: 1rpx solid #f5f5f5;
  padding-top: 12rpx;
}
.comment-action-btn { &:active { opacity: 0.7; } }
.comment-action-text { font-size: 22rpx; color: #909399; }
.comment-action-text.danger { color: #f56c6c; }
.comment-history-empty { padding: 40rpx 24rpx; text-align: center; }
.history-empty-text { font-size: 26rpx; color: #c0c4cc; }
</style>
