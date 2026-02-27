<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="轮型号" prop="wheelCode">
        <el-input v-model="queryParams.wheelCode" placeholder="请输入轮型号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户" prop="customer">
        <el-input v-model="queryParams.customer" placeholder="请输入客户" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品状态" prop="productStatus">
        <el-select v-model="queryParams.productStatus" placeholder="全部" clearable>
          <el-option label="量产" value="量产" />
          <el-option label="试制/小批量" value="试制/小批量" />
          <el-option label="退市/停产" value="退市/停产" />
          <el-option label="转移" value="转移" />
          <el-option label="还回品/报废" value="还回品/报废" />
        </el-select>
      </el-form-item>
      <el-form-item label="规格" prop="sizeSpec">
        <el-input v-model="queryParams.sizeSpec" placeholder="如1775" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="PCD" prop="pcd">
        <el-input v-model="queryParams.pcd" placeholder="如5-114.3" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['tech:product:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleBatchDelete" v-hasPermi="['tech:product:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['tech:product:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['tech:product:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-picture" size="mini" @click="batchImageOpen = true" v-hasPermi="['tech:product:edit']">批量上传图片</el-button>
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
      <span class="stats-item">共 <b>{{ total }}</b> 款产品</span>
      <el-divider direction="vertical"></el-divider>
      <span class="stats-item" v-if="queryParams.productStatus">筛选: {{ queryParams.productStatus }}</span>
      <span class="stats-item" v-if="queryParams.customer">客户: {{ queryParams.customer }}</span>
      <el-divider direction="vertical"></el-divider>
      <el-checkbox v-model="selectAll" style="margin-left:8px">全选所有 {{ total }} 条产品</el-checkbox>
      <el-button v-if="selectAll" type="danger" size="mini" icon="el-icon-delete" style="margin-left:12px" @click="handleCleanAll" v-hasPermi="['tech:product:remove']">删除全部</el-button>
    </div>

    <!-- ★ 卡片视图（默认） -->
    <div v-if="viewMode === 'card'" v-loading="loading" class="card-container">
      <div
        v-for="item in productList"
        :key="item.productId"
        class="product-card"
        @click="showDetail(item)"
      >
        <!-- 图片区域 -->
        <div class="card-image">
          <img v-if="item.frontImage" :src="baseUrl + item.frontImage" class="wheel-img" />
          <div v-else class="no-image">
            <i class="el-icon-picture-outline"></i>
            <span>暂无图片</span>
          </div>
          <!-- 状态角标 -->
          <span class="status-badge" :class="getStatusClass(item.productStatus)">{{ item.productStatus || '未知' }}</span>
        </div>
        <!-- 信息区域 -->
        <div class="card-body">
          <div class="wheel-code">{{ item.wheelCode }}</div>
          <div class="card-info-row">
            <span class="info-label">客户</span>
            <span class="info-value">{{ item.customer || '-' }}</span>
          </div>
          <div class="card-info-row">
            <span class="info-label">规格</span>
            <span class="info-value">{{ item.sizeSpec || '-' }}</span>
            <span class="info-label" style="margin-left:12px">ET</span>
            <span class="info-value">{{ item.offsetEt || '-' }}</span>
          </div>
          <div class="card-info-row">
            <span class="info-label">PCD</span>
            <span class="info-value">{{ item.pcd || '-' }}</span>
            <span class="info-label" style="margin-left:12px">孔径</span>
            <span class="info-value">{{ item.centerHole || '-' }}</span>
          </div>
          <div class="card-info-row" v-if="item.surfaceTreatment">
            <span class="info-label">表面</span>
            <span class="info-value">{{ item.surfaceTreatment }}</span>
          </div>
        </div>
      </div>
      <!-- 空状态 -->
      <div v-if="!loading && productList.length === 0" class="empty-card">
        <i class="el-icon-box" style="font-size:48px;color:#c0c4cc"></i>
        <p>暂无产品数据，请先导入</p>
      </div>
    </div>

    <!-- 表格视图 -->
    <el-table v-if="viewMode === 'table'" v-loading="loading" :data="productList" @selection-change="handleSelectionChange" border size="mini">
      <el-table-column type="selection" width="40" align="center" />
      <!-- 基础信息 -->
      <el-table-column label="序号" align="center" prop="serialNo" width="55" />
      <el-table-column label="轮型号" align="center" prop="wheelCode" width="100" fixed="left">
        <template slot-scope="scope">
          <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.wheelCode }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="客户" align="center" prop="customer" width="70" />
      <el-table-column label="发货地" align="center" prop="shipLocation" width="65" />
      <el-table-column label="产品类型" align="center" prop="productType" width="80" show-overflow-tooltip />
      <el-table-column label="产品来源" align="center" prop="productSource" width="80" show-overflow-tooltip />
      <el-table-column label="首模号" align="center" prop="firstMoldNo" width="90" />
      <el-table-column label="首模来源" align="center" prop="moldSource" width="80" show-overflow-tooltip />
      <el-table-column label="产品状态" align="center" prop="productStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.productStatus)" size="mini">{{ scope.row.productStatus || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态备注" align="center" prop="statusRemark" width="90" show-overflow-tooltip />
      <el-table-column label="相似轮型" align="center" prop="similarWheels" width="100" show-overflow-tooltip />
      <el-table-column label="差异点" align="center" prop="similarDiff" width="100" show-overflow-tooltip />
      <!-- 基础参数 -->
      <el-table-column label="规格" align="center" prop="sizeSpec" width="55" />
      <el-table-column label="ET" align="center" prop="offsetEt" width="55" />
      <el-table-column label="PCD" align="center" prop="pcd" width="70" />
      <el-table-column label="中心孔" align="center" prop="centerHole" width="60" />
      <el-table-column label="设计单重" align="center" prop="designWeight" width="75" />
      <el-table-column label="表面处理" align="center" prop="surfaceTreatment" width="80" />
      <el-table-column label="颜色" align="center" prop="color" width="70" />
      <el-table-column label="标签" align="center" prop="labelInfo" width="70" show-overflow-tooltip />
      <!-- 准备阶段里程碑 -->
      <el-table-column label="转移时间" align="center" prop="transferTime" width="90" />
      <el-table-column label="内评时间" align="center" prop="internalEvalTime" width="90" />
      <el-table-column label="工检清单" align="center" prop="qualityCheckTime" width="90" />
      <el-table-column label="开模时间" align="center" prop="moldOpenTime" width="90" />
      <el-table-column label="工检到厂" align="center" prop="qualityArrivalTime" width="90" />
      <el-table-column label="样轮到厂" align="center" prop="sampleWheelTime" width="90" />
      <el-table-column label="油漆到厂" align="center" prop="paintArrivalTime" width="90" />
      <el-table-column label="图纸签发" align="center" prop="drawingIssueTime" width="90" />
      <!-- 产品开发里程碑 -->
      <el-table-column label="FEA结果" align="center" prop="feaResult" width="100" show-overflow-tooltip />
      <el-table-column label="首模到厂" align="center" prop="firstMoldArrival" width="90" />
      <el-table-column label="首上机" align="center" prop="firstMachineTime" width="90" />
      <el-table-column label="送样时间" align="center" prop="sampleSubmitTime" width="90" />
      <el-table-column label="样件合格" align="center" prop="samplePassTime" width="90" />
      <el-table-column label="送样履历" align="center" prop="sampleHistory" width="100" show-overflow-tooltip />
      <el-table-column label="试制总结" align="center" prop="trialSummaryTime" width="90" />
      <!-- 量产阶段里程碑 -->
      <el-table-column label="试制情况" align="center" prop="trialSituation" width="100" show-overflow-tooltip />
      <el-table-column label="小批量总结" align="center" prop="batchSummaryTime" width="90" />
      <el-table-column label="小批量情况" align="center" prop="batchSituation" width="100" show-overflow-tooltip />
      <el-table-column label="影响交付" align="center" prop="impactDeliveryTime" width="90" />
      <el-table-column label="交付事项" align="center" prop="impactDeliveryItem" width="100" show-overflow-tooltip />
      <el-table-column label="量产时间" align="center" prop="massProdTime" width="90" />
      <!-- 持续改进里程碑 -->
      <el-table-column label="变更关闭" align="center" prop="latestChangeTime" width="90" />
      <el-table-column label="变更内容" align="center" prop="latestChangeContent" width="120" show-overflow-tooltip />
      <el-table-column label="控制要点" align="center" prop="controlPoints" width="120" show-overflow-tooltip />
      <!-- 操作 -->
      <el-table-column label="操作" align="center" width="140" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="showDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['tech:product:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['tech:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" :page-sizes="[10, 20, 24, 30, 50]" @pagination="getList" />

    <!-- ★ 产品详情抽屉（点击卡片/图片后弹出）★ -->
    <el-drawer :title="detailData.wheelCode || '产品详情'" :visible.sync="detailVisible" size="520px" direction="rtl">
      <div class="detail-drawer" v-if="detailData.productId">
        <!-- 大图 -->
        <div class="detail-image-wrap">
          <img v-if="detailData.frontImage" :src="baseUrl + detailData.frontImage" class="detail-image" />
          <div v-else class="detail-no-image">
            <i class="el-icon-picture-outline" style="font-size:60px"></i>
            <p>暂无正面图</p>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImageUpload"
              accept="image/*"
              v-hasPermi="['tech:product:edit']"
            >
              <el-button size="small" type="primary" plain>上传正面图</el-button>
            </el-upload>
          </div>
          <el-upload
            v-if="detailData.frontImage"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageUpload"
            accept="image/*"
            class="change-image-btn"
            v-hasPermi="['tech:product:edit']"
          >
            <el-button size="mini" type="primary" plain icon="el-icon-camera">更换图片</el-button>
          </el-upload>
        </div>

        <!-- 基础信息 -->
        <div class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="轮型号">{{ detailData.wheelCode }}</el-descriptions-item>
            <el-descriptions-item label="序号">{{ detailData.serialNo }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ detailData.customer || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发货地">{{ detailData.shipLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品类型">{{ detailData.productType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品来源">{{ detailData.productSource || '-' }}</el-descriptions-item>
            <el-descriptions-item label="首模号">{{ detailData.firstMoldNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="首模来源">{{ detailData.moldSource || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品状态">
              <el-tag :type="getStatusType(detailData.productStatus)" size="mini">{{ detailData.productStatus || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态备注">{{ detailData.statusRemark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="相似轮型" :span="2">{{ detailData.similarWheels || '-' }}</el-descriptions-item>
            <el-descriptions-item label="差异点" :span="2">{{ detailData.similarDiff || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 基础参数 -->
        <div class="detail-section">
          <div class="section-title">基础参数</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="规格">{{ detailData.sizeSpec || '-' }}</el-descriptions-item>
            <el-descriptions-item label="偏距ET">{{ detailData.offsetEt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="PCD">{{ detailData.pcd || '-' }}</el-descriptions-item>
            <el-descriptions-item label="中心孔">{{ detailData.centerHole || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设计单重">{{ detailData.designWeight ? detailData.designWeight + 'kg' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="表面处理">{{ detailData.surfaceTreatment || '-' }}</el-descriptions-item>
            <el-descriptions-item label="颜色">{{ detailData.color || '-' }}</el-descriptions-item>
            <el-descriptions-item label="标签">{{ detailData.labelInfo || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 准备阶段里程碑 -->
        <div class="detail-section">
          <div class="section-title">准备阶段里程碑</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="转移时间">{{ detailData.transferTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="内评时间">{{ detailData.internalEvalTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="工检清单">{{ detailData.qualityCheckTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开模时间">{{ detailData.moldOpenTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="工检到厂">{{ detailData.qualityArrivalTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="样轮到厂">{{ detailData.sampleWheelTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="油漆到厂">{{ detailData.paintArrivalTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="图纸签发">{{ detailData.drawingIssueTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 产品开发里程碑 -->
        <div class="detail-section">
          <div class="section-title">产品开发里程碑</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="FEA结果" :span="2">{{ detailData.feaResult || '-' }}</el-descriptions-item>
            <el-descriptions-item label="首模到厂">{{ detailData.firstMoldArrival || '-' }}</el-descriptions-item>
            <el-descriptions-item label="首上机">{{ detailData.firstMachineTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="送样时间">{{ detailData.sampleSubmitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="样件合格">{{ detailData.samplePassTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="送样履历" :span="2">{{ detailData.sampleHistory || '-' }}</el-descriptions-item>
            <el-descriptions-item label="试制总结">{{ detailData.trialSummaryTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 量产阶段里程碑 -->
        <div class="detail-section">
          <div class="section-title">量产阶段里程碑</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="试制情况" :span="2">{{ detailData.trialSituation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="小批量总结">{{ detailData.batchSummaryTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="小批量情况">{{ detailData.batchSituation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="影响交付">{{ detailData.impactDeliveryTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交付事项">{{ detailData.impactDeliveryItem || '-' }}</el-descriptions-item>
            <el-descriptions-item label="量产时间">{{ detailData.massProdTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 持续改进 -->
        <div class="detail-section">
          <div class="section-title">持续改进里程碑</div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="变更关闭时间">{{ detailData.latestChangeTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="变更内容">{{ detailData.latestChangeContent || '-' }}</el-descriptions-item>
            <el-descriptions-item label="控制要点">{{ detailData.controlPoints || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <el-button type="primary" size="small" icon="el-icon-edit" @click="handleUpdate(detailData)" v-hasPermi="['tech:product:edit']">编辑</el-button>
          <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(detailData)" v-hasPermi="['tech:product:remove']">删除</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body top="3vh">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="轮型号" prop="wheelCode"><el-input v-model="form.wheelCode" placeholder="如05115C09" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="序号" prop="serialNo"><el-input-number v-model="form.serialNo" :min="1" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="正面图">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleFormImageUpload"
            accept="image/*"
            list-type="picture"
          >
            <img v-if="form.frontImage" :src="baseUrl + form.frontImage" style="width:120px;height:120px;object-fit:cover;border-radius:8px" />
            <el-button v-else size="small" type="primary" plain icon="el-icon-upload">上传正面图</el-button>
          </el-upload>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customer" placeholder="如丰田" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货地"><el-input v-model="form.shipLocation" placeholder="如广州" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="产品类型"><el-input v-model="form.productType" placeholder="外部调入/常规转移等" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品来源"><el-input v-model="form.productSource" placeholder="散装调拨/集团转移等" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="首模号"><el-input v-model="form.firstMoldNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="首模来源"><el-input v-model="form.moldSource" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品状态">
              <el-select v-model="form.productStatus" style="width:100%">
                <el-option label="量产" value="量产" /><el-option label="试制/小批量" value="试制/小批量" />
                <el-option label="退市/停产" value="退市/停产" /><el-option label="转移" value="转移" />
                <el-option label="还回品/报废" value="还回品/报废" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="状态备注"><el-input v-model="form.statusRemark" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6"><el-form-item label="规格"><el-input v-model="form.sizeSpec" placeholder="1775" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="偏距ET"><el-input v-model="form.offsetEt" placeholder="ET45" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="PCD"><el-input v-model="form.pcd" placeholder="5-114.3" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="中心孔"><el-input v-model="form.centerHole" placeholder="O60" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">基础参数</el-divider>
        <el-row :gutter="20">
          <el-col :span="6"><el-form-item label="单重(kg)"><el-input v-model="form.designWeight" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="表面处理"><el-input v-model="form.surfaceTreatment" placeholder="精车/全涂装" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="颜色"><el-input v-model="form.color" placeholder="11BK19" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="标签"><el-input v-model="form.labelInfo" /></el-form-item></el-col>
        </el-row>

        <el-form-item label="相似轮型"><el-input v-model="form.similarWheels" placeholder="多个用逗号分隔" /></el-form-item>
        <el-form-item label="差异点"><el-input v-model="form.similarDiff" type="textarea" :rows="2" /></el-form-item>

        <el-divider content-position="left">里程碑（选填）</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="转移时间"><el-input v-model="form.transferTime" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="内评时间"><el-input v-model="form.internalEvalTime" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开模时间"><el-input v-model="form.moldOpenTime" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="首模到厂"><el-input v-model="form.firstMoldArrival" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="送样时间"><el-input v-model="form.sampleSubmitTime" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="量产时间"><el-input v-model="form.massProdTime" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="导入产品清单数据" :visible.sync="importOpen" width="520px" append-to-body>
      <!-- 导入方式选择 -->
      <el-radio-group v-model="importMode" style="margin-bottom:16px;width:100%">
        <el-radio-button label="raw">直接导入 DX-产品清单原表</el-radio-button>
        <el-radio-button label="template">使用标准模板导入</el-radio-button>
      </el-radio-group>

      <div v-if="importMode === 'raw'" style="margin-bottom:12px;padding:10px 14px;background:#e8f4fd;border-radius:4px;font-size:13px;color:#409EFF;">
        直接上传你的 <b>DX-产品清单.xlsm</b> 原文件即可，系统会自动跳过前面的统计行和表头行。
        <br/>如果列名匹配不上，可以调整下方的"跳过行数"。
      </div>
      <div v-else style="margin-bottom:12px;padding:10px 14px;background:#fdf6ec;border-radius:4px;font-size:13px;color:#E6A23C;">
        先<el-link type="primary" :underline="false" @click="handleImportTemplate">下载模板</el-link>，按模板格式填入数据后上传。
      </div>

      <el-upload ref="importUpload" :limit="1" accept=".xlsx,.xls,.xlsm" :headers="uploadHeaders" :action="computedImportUrl" :disabled="importUploading" :on-progress="handleImportProgress" :on-success="handleImportSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击选取</em></div>
      </el-upload>

      <div style="margin-top:12px;display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <el-checkbox v-model="importUpdateSupport">更新已有产品（按轮型号）</el-checkbox>
        <span style="font-size:12px;color:#909399">跳过前</span>
        <el-input-number v-model="importTitleNum" :min="0" :max="20" size="mini" style="width:100px" />
        <span style="font-size:12px;color:#909399">行（原表填8，模板填0）</span>
      </div>

      <div slot="footer" style="display:flex;justify-content:space-between;align-items:center">
        <el-link v-if="importMode === 'template'" type="primary" :underline="false" icon="el-icon-download" @click="handleImportTemplate">下载导入模板</el-link>
        <span v-else></span>
        <div>
          <el-button @click="importOpen = false">取消</el-button>
          <el-button type="primary" :loading="importUploading" @click="submitImport">开始导入</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 批量上传图片弹窗 -->
    <el-dialog title="批量上传产品正面图" :visible.sync="batchImageOpen" width="560px" append-to-body>
      <div style="margin-bottom:16px;padding:12px;background:#e8f4fd;border-radius:4px;font-size:13px;color:#409EFF;line-height:1.8">
        <b>操作说明：</b><br/>
        1. 将每张轮毂图片用<b>轮型号命名</b>（如 <code>00919F03.jpg</code>、<code>00923C02.png</code>）<br/>
        2. 选择全部图片，一次性上传<br/>
        3. 系统根据文件名<b>自动匹配</b>到产品清单中的对应产品<br/>
        4. 支持 jpg / png / jpeg / webp 格式
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
        <div slot="tip" class="el-upload__tip">图片文件名必须是轮型号，如 00919F03.jpg</div>
      </el-upload>
      <div slot="footer">
        <el-button @click="batchImageOpen = false">取消</el-button>
        <el-button type="primary" :loading="batchImageUploading" @click="submitBatchImages">开始匹配上传</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listProduct, getProduct, delProduct, addProduct, updateProduct, cleanAllProducts } from "@/api/tech/product"
import { getToken } from "@/utils/auth"

export default {
  name: "Product",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      productList: [],
      selectAll: false,
      title: "",
      open: false,
      viewMode: "card",
      // 详情抽屉
      detailVisible: false,
      detailData: {},
      // 导入
      importOpen: false,
      importUploading: false,
      importUpdateSupport: false,
      importMode: "raw",
      importTitleNum: 8,
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      batchImageUrl: process.env.VUE_APP_BASE_API + "/tech/product/batchUploadImages",
      batchImageOpen: false,
      batchImageUploading: false,
      batchImageFiles: [],
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      baseUrl: process.env.VUE_APP_BASE_API,
      queryParams: { pageNum: 1, pageSize: 24, wheelCode: null, customer: null, productStatus: null, sizeSpec: null, pcd: null },
      form: {},
      rules: { wheelCode: [{ required: true, message: "轮型号不能为空", trigger: "blur" }] }
    }
  },
  computed: {
    computedImportUrl() {
      return process.env.VUE_APP_BASE_API + "/tech/product/importData?titleNum=" + this.importTitleNum
    }
  },
  watch: {
    importMode(val) {
      this.importTitleNum = val === 'raw' ? 8 : 0
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listProduct(this.queryParams).then(res => {
        this.productList = res.rows; this.total = res.total; this.loading = false
      })
    },
    showDetail(row) {
      getProduct(row.productId).then(res => {
        this.detailData = res.data; this.detailVisible = true
      })
    },
    /** 图片上传成功（详情页） */
    handleImageUpload(res) {
      if (res.code === 200) {
        this.detailData.frontImage = res.fileName
        updateProduct({ productId: this.detailData.productId, frontImage: res.fileName }).then(() => {
          this.$modal.msgSuccess("图片上传成功")
          this.getList()
        })
      }
    },
    /** 图片上传成功（表单） */
    handleFormImageUpload(res) {
      if (res.code === 200) {
        this.$set(this.form, 'frontImage', res.fileName)
      }
    },
    getStatusType(status) {
      if (!status) return 'info'
      if (status === '量产') return 'success'
      if (status.includes('试制')) return 'warning'
      if (status.includes('退市')) return 'info'
      if (status.includes('报废')) return 'danger'
      return ''
    },
    getStatusClass(status) {
      if (!status) return 'badge-default'
      if (status === '量产') return 'badge-success'
      if (status.includes('试制')) return 'badge-warning'
      if (status.includes('退市')) return 'badge-info'
      if (status.includes('报废')) return 'badge-danger'
      return 'badge-default'
    },
    cancel() { this.open = false; this.reset() },
    reset() {
      this.form = {
        productId: null, serialNo: null, wheelCode: null, frontImage: null, customer: null,
        shipLocation: null, productType: null, productSource: null,
        firstMoldNo: null, moldSource: null, productStatus: '量产',
        statusRemark: null, similarWheels: null, similarDiff: null,
        sizeSpec: null, offsetEt: null, pcd: null, centerHole: null,
        designWeight: null, surfaceTreatment: null, color: null, labelInfo: null,
        transferTime: null, internalEvalTime: null, qualityCheckTime: null,
        moldOpenTime: null, qualityArrivalTime: null, sampleWheelTime: null,
        paintArrivalTime: null, drawingIssueTime: null,
        feaResult: null, firstMoldArrival: null, firstMachineTime: null,
        sampleSubmitTime: null, samplePassTime: null, sampleHistory: null, trialSummaryTime: null,
        trialSituation: null, batchSummaryTime: null, batchSituation: null,
        impactDeliveryTime: null, impactDeliveryItem: null, massProdTime: null,
        latestChangeTime: null, latestChangeContent: null, controlPoints: null, remark: null
      }
      this.resetForm("form")
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(sel) { this.ids = sel.map(i => i.productId); this.single = sel.length !== 1; this.multiple = !sel.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加产品" },
    handleUpdate(row) {
      this.reset()
      getProduct(row.productId).then(res => { this.form = res.data; this.open = true; this.title = "修改产品"; this.detailVisible = false })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.productId) {
            updateProduct(this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList() })
          } else {
            addProduct(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList() })
          }
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除轮型 "' + row.wheelCode + '" ？').then(() => delProduct(row.productId)).then(() => {
        this.getList(); this.detailVisible = false; this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$modal.confirm('确认删除选中的 ' + this.ids.length + ' 个产品？').then(() => delProduct(this.ids.join(','))).then(() => {
        this.getList(); this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleCleanAll() {
      const q = { ...this.queryParams }; delete q.pageNum; delete q.pageSize;
      this.$modal.confirm('确认删除全部 ' + this.total + ' 条产品？此操作不可恢复！').then(() => cleanAllProducts(q)).then(res => {
        this.selectAll = false; this.getList(); this.$modal.msgSuccess(res.msg || "删除成功")
      }).catch(() => {})
    },
    handleImport() { this.importOpen = true },
    handleImportTemplate() { this.download('tech/product/importTemplate', {}, '产品清单导入模板.xlsx') },
    submitImport() { this.$refs.importUpload.submit() },
    handleImportProgress() { this.importUploading = true },
    handleImportSuccess(res) {
      this.importUploading = false; this.$refs.importUpload.clearFiles()
      if (res.code === 200) { this.$modal.msgSuccess(res.msg || "导入成功"); this.importOpen = false; this.getList() }
      else { this.$modal.msgError(res.msg || "导入失败") }
    },
    handleExport() { this.download('tech/product/export', { ...this.queryParams }, '产品清单.xlsx') },
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
  &.badge-success { background: #f0f9eb; color: #67c23a; }
  &.badge-warning { background: #fdf6ec; color: #e6a23c; }
  &.badge-info { background: #f4f4f5; color: #909399; }
  &.badge-danger { background: #fef0f0; color: #f56c6c; }
  &.badge-default { background: #ecf5ff; color: #409eff; }
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

/* ===== 详情抽屉 ===== */
.detail-drawer {
  padding: 0 20px 20px;
}

.detail-image-wrap {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
}

.detail-image {
  width: 100%;
  max-height: 280px;
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
  bottom: 10px;
  right: 10px;
}

.detail-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  padding-left: 10px;
  border-left: 3px solid #409EFF;
}

.detail-actions {
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
}
</style>
