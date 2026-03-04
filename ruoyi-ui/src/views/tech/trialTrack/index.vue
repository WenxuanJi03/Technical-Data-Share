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
      <el-form-item label="状态" prop="allProcessDone">
        <el-select v-model="queryParams.allProcessDone" placeholder="全部" clearable>
          <el-option label="进行中" value="doing" /><el-option label="已完成" value="done" />
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
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleBatchDelete" v-hasPermi="['tech:trialTrack:remove']">删除</el-button></el-col>
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
      <el-divider direction="vertical"></el-divider>
      <el-checkbox v-model="selectAll" style="margin-left:8px">全选所有 {{ total }} 条记录</el-checkbox>
      <el-button v-if="selectAll" type="danger" size="mini" icon="el-icon-delete" style="margin-left:12px" @click="handleCleanAll" v-hasPermi="['tech:trialTrack:remove']">删除全部</el-button>
    </div>

    <!-- 卡片视图 -->
    <div v-if="viewMode==='card'" v-loading="loading" class="card-grid">
      <div v-for="item in trackList" :key="item.trackId" class="track-card" @click="showDetail(item)">
        <div class="card-top">
          <span class="mold-code">{{ item.moldCode }}</span>
          <span
            class="done-badge"
            :class="[item.allProcessDone==='是'?'done-yes':'done-no', { 'done-badge-readonly': !canEditPhase(4) }]"
            :style="canEditPhase(4) ? 'cursor:pointer' : 'cursor:default'"
            :title="canEditPhase(4) ? (item.allProcessDone==='是'?'点击标记为进行中':'点击标记为已完成') : '无编辑权限'"
            @click.stop="canEditPhase(4) && toggleDoneStatus(item)"
          >
            {{ item.allProcessDone === '是' ? '✓ 已完成' : '● 进行中' }}
          </span>
        </div>
        <div class="card-tags">
          <el-tag v-if="item.moldType" size="mini" effect="plain" type="warning">{{ item.moldType }}</el-tag>
          <el-tag v-if="item.surfaceStatus" size="mini" effect="plain" type="success">{{ item.surfaceStatus }}</el-tag>
          <el-tag v-if="item.machineType" size="mini" effect="plain">{{ item.machineType }}</el-tag>
        </div>
        <div class="card-meta">
          <div class="meta-row" v-if="item.impactTestResult"><span class="meta-l">实验结果</span><span class="meta-v meta-wrap">{{ item.impactTestResult }}</span></div>
          <div class="meta-row" v-if="item.hotMachineDate || item.hotMachineStation || item.roundKeepTime"><span class="meta-l">压铸信息</span><span class="meta-v meta-wrap">{{ item.hotMachineDate || '-' }} / {{ item.hotMachineStation || '-' }} / {{ item.roundKeepTime || '-' }}</span></div>
          <div class="meta-row" v-if="item.hotProduction"><span class="meta-l">压铸情况</span><span class="meta-v meta-wrap">{{ item.hotProduction }}</span></div>
          <div class="meta-row" v-if="item.heatReceiveCount || item.heatTransferCount"><span class="meta-l">热处理</span><span class="meta-v meta-wrap">接{{ item.heatReceiveCount || '-' }} / 转{{ item.heatTransferCount || '-' }}</span></div>
          <div class="meta-row" v-if="item.spinMachineStation || item.spinProduction"><span class="meta-l">旋压信息</span><span class="meta-v meta-wrap">{{ item.spinMachineStation || '-' }} / {{ item.spinProduction || '-' }}</span></div>
        </div>
      </div>
      <div v-if="!loading && trackList.length===0" class="empty-card">
        <i class="el-icon-box" style="font-size:48px;color:#c0c4cc"></i>
        <p>暂无数据，请先导入WPS导出的Excel</p>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-if="viewMode==='table'" v-loading="loading" :data="trackList" @selection-change="handleSelectionChange" border size="mini">
      <el-table-column type="selection" width="40" align="center" />
      <!-- 基础信息 -->
      <el-table-column label="模号" prop="moldCode" width="120" fixed="left">
        <template slot-scope="s"><el-link type="primary" @click="showDetail(s.row)">{{ s.row.moldCode }}</el-link></template>
      </el-table-column>
      <el-table-column label="轮型" prop="productSpec" width="90" align="center" />
      <el-table-column label="模具类型" prop="moldType" width="80" align="center" />
      <el-table-column label="表面状态" prop="surfaceStatus" width="80" align="center" />
      <el-table-column label="上机类型" prop="machineType" width="80" align="center" />
      <el-table-column label="上机次数" prop="machineCount" width="70" align="center" />
      <el-table-column label="预上机时间" prop="planMachineTime" width="100" align="center" />
      <!-- 压铸阶段 -->
      <el-table-column label="压铸上机" prop="hotMachineDate" width="100" align="center" />
      <el-table-column label="机台" prop="hotMachineStation" width="60" align="center" />
      <el-table-column label="保压" prop="roundKeepTime" width="60" align="center" />
      <el-table-column label="压铸生产情况" prop="hotProduction" width="160" show-overflow-tooltip />
      <el-table-column label="改善记录" prop="improveRecord" width="140" show-overflow-tooltip />
      <el-table-column label="压铸负责人" prop="hotImprovePerson" width="90" align="center" />
      <!-- 旋压阶段 -->
      <el-table-column label="旋压上机" prop="spinMachineDate" width="100" align="center" />
      <el-table-column label="旋压机台" prop="spinMachineStation" width="80" align="center" />
      <el-table-column label="旋压生产情况" prop="spinProduction" width="140" show-overflow-tooltip />
      <el-table-column label="改模记录" prop="moldModifyRecord" width="120" show-overflow-tooltip />
      <el-table-column label="旋压负责人" prop="spinImprovePerson" width="90" align="center" />
      <el-table-column label="改善情况" prop="spinImproveStatus" width="120" show-overflow-tooltip />
      <!-- 热处理阶段 -->
      <el-table-column label="热处理接收" prop="heatReceiveCount" width="80" align="center" />
      <el-table-column label="热处理转下" prop="heatTransferCount" width="80" align="center" />
      <el-table-column label="下转时间" prop="heatTransferTime" width="100" align="center" />
      <!-- 粗车阶段 -->
      <el-table-column label="粗车上机" prop="roughMachineDate" width="100" align="center" />
      <el-table-column label="粗车生产情况" prop="roughProduction" width="140" show-overflow-tooltip />
      <el-table-column label="粗车负责人" prop="roughImprovePerson" width="100" align="center" />
      <el-table-column label="改善方案" prop="improvePlan" width="140" show-overflow-tooltip />
      <!-- 精车阶段 -->
      <el-table-column label="精车上机" prop="fineMachineDate" width="100" align="center" />
      <el-table-column label="精车情况" prop="fineProduction" width="120" show-overflow-tooltip />
      <el-table-column label="精车负责人" prop="fineImprovePerson" width="90" align="center" />
      <!-- 涂装阶段 -->
      <el-table-column label="涂装上机" prop="paintMachineDate" width="100" align="center" />
      <el-table-column label="涂装情况" prop="paintProduction" width="120" show-overflow-tooltip />
      <el-table-column label="涂装负责人" prop="paintImprovePerson" width="90" align="center" />
      <!-- 实验/总结 -->
      <el-table-column label="冲击试验日期" prop="impactTestDate" width="100" align="center" />
      <el-table-column label="冲击结果" prop="impactTestResult" width="120" show-overflow-tooltip />
      <el-table-column label="实验说明" prop="testDescription" width="120" show-overflow-tooltip />
      <el-table-column label="实验关闭" prop="testCloseStatus" width="100" show-overflow-tooltip />
      <el-table-column label="完成日期" prop="completeDate" width="100" align="center" />
      <el-table-column label="失效清场" prop="failProductTrace" width="120" show-overflow-tooltip />
      <el-table-column label="失效分析" prop="failAnalysis" width="120" show-overflow-tooltip />
      <el-table-column label="生产总结" prop="productionSummary" width="140" show-overflow-tooltip />
      <el-table-column label="改善措施" prop="improveMeasures" width="140" show-overflow-tooltip />
      <el-table-column label="经验教训" prop="lessonsLearned" width="140" show-overflow-tooltip />
      <el-table-column label="全序完成" prop="allProcessDone" width="80" align="center">
        <template slot-scope="s">
          <el-tag
              :type="s.row.allProcessDone==='是'?'success':'info'"
              size="mini"
              :style="canEditPhase(4) ? 'cursor:pointer' : 'cursor:default'"
              :title="canEditPhase(4) ? (s.row.allProcessDone==='是'?'点击标记为进行中':'点击标记为已完成') : '无编辑权限'"
              @click.stop="canEditPhase(4) && toggleDoneStatus(s.row)"
            >{{ s.row.allProcessDone === '是' ? '已完成' : '进行中' }}</el-tag>
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
    <el-drawer title="开发科的OE试制跟踪卡片详情" :visible.sync="detailVisible" size="560px" direction="rtl">
      <div class="detail-drawer" v-if="detail.trackId">
        <!-- 顶部概览 -->
        <div class="detail-header">
          <div class="dh-code">{{ detail.moldCode }}</div>
          <div class="dh-tags">
            <el-tag v-if="detail.moldType" effect="dark" size="small" type="warning">{{ detail.moldType }}</el-tag>
            <el-tag v-if="detail.surfaceStatus" effect="dark" size="small" type="success">{{ detail.surfaceStatus }}</el-tag>
            <el-tag v-if="detail.machineType" effect="dark" size="small">{{ detail.machineType }}</el-tag>
            <el-tag
              effect="dark"
              size="small"
              :type="detail.allProcessDone==='是'?'success':'info'"
              :style="canEditPhase(4) ? 'cursor:pointer' : 'cursor:default'"
              :title="canEditPhase(4) ? (detail.allProcessDone==='是'?'点击标记为进行中':'点击标记为已完成') : '无编辑权限'"
              @click="canEditPhase(4) && toggleDoneStatus(detail)"
            >{{ detail.allProcessDone === '是' ? '✓ 全序完成' : '● 进行中' }}</el-tag>
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

        <!-- 压铸阶段 -->
        <div class="detail-section">
          <div class="section-title">压铸阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="上机日期">{{ detail.hotMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="机台">{{ detail.hotMachineStation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保压时间">{{ detail.roundKeepTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="检查站测量数据">
              <el-link v-if="detail.hotCheckMeasureData" type="primary" :underline="false" @click="openLightbox(detail.hotCheckMeasureImage)">{{ detail.hotCheckMeasureData }}</el-link>
              <span v-else>-</span>
            </el-descriptions-item>
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
            <el-descriptions-item label="前距图片">
              <el-link v-if="detail.spinFrontDistanceImage" type="primary" :underline="false" @click="openLightbox(detail.spinFrontDistanceImage)">查看图片</el-link>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="生产情况" :span="2">{{ detail.spinProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="改模记录" :span="2">{{ detail.moldModifyRecord || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ detail.spinImprovePerson || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 热处理阶段 -->
        <div class="detail-section">
          <div class="section-title">热处理阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="下转时间">{{ detail.heatTransferTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="接收数量">{{ detail.heatReceiveCount != null ? detail.heatReceiveCount : '-' }}</el-descriptions-item>
            <el-descriptions-item label="转下数量">{{ detail.heatTransferCount != null ? detail.heatTransferCount : '-' }}</el-descriptions-item>
            <el-descriptions-item label="流转单照片">
              <el-link v-if="detail.heatFlowSheetImage" type="primary" :underline="false" @click="openLightbox(detail.heatFlowSheetImage)">查看照片</el-link>
              <span v-else>-</span>
            </el-descriptions-item>
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

        <!-- 精车阶段 -->
        <div class="detail-section">
          <div class="section-title">精车阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="精车上机">{{ detail.fineMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="精车负责人">{{ detail.fineImprovePerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="精车情况" :span="2">{{ detail.fineProduction || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 涂装阶段 -->
        <div class="detail-section">
          <div class="section-title">涂装阶段</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="涂装上机">{{ detail.paintMachineDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="涂装情况">{{ detail.paintProduction || '-' }}</el-descriptions-item>
            <el-descriptions-item label="涂装负责人">{{ detail.paintImprovePerson || '-' }}</el-descriptions-item>
            <el-descriptions-item label="流转单照片">
              <el-link v-if="detail.paintFlowSheetImage" type="primary" :underline="false" @click="openLightbox(detail.paintFlowSheetImage)">查看照片</el-link>
              <span v-else>-</span>
            </el-descriptions-item>
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
            <el-descriptions-item label="失效分析" :span="2">{{ detail.failAnalysis || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经验教训" :span="2">{{ detail.lessonsLearned || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-actions">
          <el-button v-if="canEditAnyPhase" type="primary" size="small" icon="el-icon-edit" @click="handleUpdate(detail)">编辑</el-button>
          <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(detail)" v-hasPermi="['tech:trialTrack:remove']">删除</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- Lightbox 图片预览 -->
    <el-dialog :visible.sync="lightboxVisible" width="66vw" append-to-body :show-close="true" custom-class="lightbox-dialog" @close="lightboxVisible=false">
      <img v-if="lightboxImageUrl" :src="baseUrl + lightboxImageUrl" style="width:100%;display:block;" />
      <div v-else style="text-align:center;padding:40px;color:#909399">暂无图片</div>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="formTitle" :visible.sync="formOpen" width="780px" append-to-body top="3vh">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-alert v-if="form.trackId && !canEditPhase(0) && !canEditPhase(1) && !canEditPhase(2) && !canEditPhase(3) && !canEditPhase(4) && !canEditPhase(5)" title="您没有任一阶段编辑权限，仅可查看" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="模号" prop="moldCode"><el-input v-model="form.moldCode" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="产品规格"><el-input v-model="form.productSpec" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="上机次数"><el-input-number v-model="form.machineCount" :min="0" style="width:100%" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="模具类型"><el-input v-model="form.moldType" placeholder="首模/改模" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="表面状态"><el-input v-model="form.surfaceStatus" placeholder="精车/全涂" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="上机类型"><el-input v-model="form.machineType" placeholder="小批量/量产" :disabled="form.trackId && !canEditPhase(0)" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">压铸阶段</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="压铸上机"><el-date-picker v-model="form.hotMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="机台"><el-input v-model="form.hotMachineStation" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="保压时间"><el-input v-model="form.roundKeepTime" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="负责人"><el-input v-model="form.hotImprovePerson" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item></el-col>
          <el-col :span="16"><el-form-item label="测量数据"><el-input v-model="form.hotCheckMeasureData" placeholder="热工检查站首件测量数据" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="测量数据图片"><image-upload v-model="form.hotCheckMeasureImage" :limit="3" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item>
        <el-form-item label="生产情况"><el-input v-model="form.hotProduction" type="textarea" :rows="2" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item>
        <el-form-item label="改善记录"><el-input v-model="form.improveRecord" type="textarea" :rows="2" :disabled="form.trackId && !canEditPhase(1)" /></el-form-item>

        <el-divider content-position="left">热处理阶段</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="下转时间"><el-date-picker v-model="form.heatTransferTime" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(3)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="接收数量"><el-input v-model="form.heatReceiveCount" type="number" oninput="if(value.length>10)value=value.slice(0,10)" :disabled="form.trackId && !canEditPhase(3)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="转下数量"><el-input v-model="form.heatTransferCount" type="number" oninput="if(value.length>10)value=value.slice(0,10)" :disabled="form.trackId && !canEditPhase(3)" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="流转单照片"><image-upload v-model="form.heatFlowSheetImage" :limit="3" :disabled="form.trackId && !canEditPhase(3)" /></el-form-item>

        <el-divider content-position="left">旋压 / 粗车 / 精车 / 涂装</el-divider>
        <el-form-item label="旋压前距图片"><image-upload v-model="form.spinFrontDistanceImage" :limit="3" :disabled="form.trackId && !canEditPhase(2)" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="旋压上机"><el-date-picker v-model="form.spinMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(2)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="粗车上机"><el-date-picker v-model="form.roughMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(4)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="精车上机"><el-date-picker v-model="form.fineMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(5)" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="精车负责人"><el-input v-model="form.fineImprovePerson" :disabled="form.trackId && !canEditPhase(5)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="涂装上机"><el-date-picker v-model="form.paintMachineDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(6)" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="涂装负责人"><el-input v-model="form.paintImprovePerson" :disabled="form.trackId && !canEditPhase(6)" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="流转单照片"><image-upload v-model="form.paintFlowSheetImage" :limit="3" :disabled="form.trackId && !canEditPhase(4)" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="完成日期"><el-date-picker v-model="form.completeDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width:100%" :disabled="form.trackId && !canEditPhase(7)" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="全序完成">
              <el-select v-model="form.allProcessDone" style="width:100%" :disabled="form.trackId && !canEditPhase(4)">
                <el-option label="是" value="是" /><el-option label="否" value="否" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" :disabled="form.trackId && !canEditPhase(5)" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm" :disabled="form.trackId && !canEditAnyPhase">确 定</el-button>
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
import { listTrialTrack, getTrialTrack, addTrialTrack, updateTrialTrack, delTrialTrack, cleanAllTrialTracks } from "@/api/tech/trialTrack"
import { getToken } from "@/utils/auth"
import ImageUpload from "@/components/ImageUpload"

export default {
  name: "TrialTrack",
  components: { ImageUpload },
  data() {
    return {
      loading: true, trackList: [], total: 0, viewMode: "card", showSearch: true, ids: [], multiple: true, selectAll: false,
      queryParams: { pageNum: 1, pageSize: 20, moldCode: null, moldType: null, surfaceStatus: null, machineType: null, allProcessDone: null },
      // 详情
      detailVisible: false, detail: {},
      // 表单
      formOpen: false, formTitle: "", form: {},
      rules: { moldCode: [{ required: true, message: "模号不能为空", trigger: "blur" }] },
      // 导入
      importOpen: false, uploading: false, updateSupport: false, importMode: "raw", titleNum: 0,
      headers: { Authorization: "Bearer " + getToken() },
      // Lightbox 图片预览
      baseUrl: process.env.VUE_APP_BASE_API,
      lightboxVisible: false,
      lightboxImageUrl: '',
    }
  },
  computed: {
    importUrl() { return process.env.VUE_APP_BASE_API + "/tech/trialTrack/importData?titleNum=" + this.titleNum },
    canEditAnyPhase() {
      const perms = this.$store.getters.permissions || []
      if (perms.includes('*:*:*') || perms.includes('tech:trialTrack:edit')) return true
      const phasePerms = ['tech:trial:phase:base:edit', 'tech:trial:phase:hot:edit', 'tech:trial:phase:spin:edit', 'tech:trial:phase:heat:edit', 'tech:trial:phase:rough:edit', 'tech:trial:phase:fine:edit', 'tech:trial:phase:paint:edit', 'tech:trial:phase:test:edit']
      return phasePerms.some(p => perms.includes(p))
    }
  },
  watch: {
    importMode(v) { this.titleNum = v === 'raw' ? 0 : 0 }
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
    handleSelectionChange(sel) {
      this.ids = sel.map(i => i.trackId)
      this.multiple = !sel.length
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除模号 "' + row.moldCode + '" ？').then(() => delTrialTrack(row.trackId)).then(() => {
        this.getList(); this.detailVisible = false; this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$modal.confirm('确认删除选中的 ' + this.ids.length + ' 条记录？').then(() => delTrialTrack(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleCleanAll() {
      const q = { ...this.queryParams }; delete q.pageNum; delete q.pageSize;
      this.$modal.confirm('确认删除全部 ' + this.total + ' 条试制记录？此操作不可恢复！').then(() => cleanAllTrialTracks(q)).then(res => {
        this.selectAll = false; this.getList(); this.$modal.msgSuccess(res.msg || "删除成功")
      }).catch(() => {})
    },
    downloadTemplate() { this.download('tech/trialTrack/importTemplate', {}, 'OE试制跟踪导入模板.xlsx') },
    importSuccess(res) {
      this.uploading = false; this.$refs.importUpload.clearFiles()
      if (res.code === 200) { this.$modal.msgSuccess(res.msg); this.importOpen = false; this.getList() }
      else { this.$modal.msgError(res.msg || "导入失败") }
    },
    toggleDoneStatus(row) {
      const newVal = row.allProcessDone === '是' ? '否' : '是'
      const label = newVal === '是' ? '已完成' : '进行中'
      this.$modal.confirm(`确认将 "${row.moldCode}" 标记为【${label}】？`).then(() => {
        return updateTrialTrack({ trackId: row.trackId, allProcessDone: newVal })
      }).then(() => {
        this.$modal.msgSuccess(`已更新为【${label}】`)
        this.getList()
        if (this.detailVisible && this.detail.trackId === row.trackId) {
          this.detail.allProcessDone = newVal
        }
      }).catch(() => {})
    },
    handleExport() { this.download('tech/trialTrack/export', { ...this.queryParams }, 'OE试制跟踪.xlsx') },
    openLightbox(imageUrl) {
      if (imageUrl) {
        this.lightboxImageUrl = imageUrl.split(',')[0]
        this.lightboxVisible = true
      } else {
        this.$modal.msgWarning('暂无图片')
      }
    },
    canEditPhase(index) {
      const perms = this.$store.getters.permissions || []
      if (perms.includes('*:*:*') || perms.includes('tech:trialTrack:edit')) return true
      const phasePerms = ['tech:trial:phase:base:edit', 'tech:trial:phase:hot:edit', 'tech:trial:phase:spin:edit', 'tech:trial:phase:heat:edit', 'tech:trial:phase:rough:edit', 'tech:trial:phase:fine:edit', 'tech:trial:phase:paint:edit', 'tech:trial:phase:test:edit']
      return index >= 0 && index < 8 && perms.includes(phasePerms[index])
    }
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
.meta-wrap { word-break: break-all; white-space: normal; }
::v-deep .lightbox-dialog .el-dialog__body { padding: 0; }
::v-deep .lightbox-dialog .el-dialog__header { position: absolute; right: 0; z-index: 1; }
</style>
