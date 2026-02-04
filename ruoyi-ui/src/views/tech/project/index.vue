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
      <div class="toolbar-center">
        <el-button type="info" plain icon="el-icon-folder" @click="handleFolder">文件夹</el-button>
        <el-badge :value="todoCount" :max="999" class="todo-badge">
          <el-button type="warning" icon="el-icon-bell" @click="openTodoList">待办任务</el-button>
        </el-badge>
      </div>
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
                  <el-select v-model="detailForm.managerName" placeholder="请选择" filterable style="width:100%">
                    <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
                  </el-select>
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
          <el-button icon="el-icon-close" @click="detailOpen = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 文件管理系统弹窗 -->
    <el-dialog 
      :visible.sync="fileManagerOpen" 
      width="1100px" 
      :close-on-click-modal="false"
      custom-class="file-manager-dialog"
      :show-close="true"
    >
      <div class="file-manager-container">
        <!-- 顶部标题区 -->
        <div class="fm-header">
          <div class="fm-title">
            <div class="fm-icon">FM</div>
            <div class="fm-title-text">
              <h2>文件管理系统</h2>
              <p>高效管理您的所有文件资源</p>
            </div>
          </div>
          <div class="fm-actions">
            <el-input 
              v-model="fileSearchKeyword" 
              placeholder="搜索文件、项目或流程..." 
              prefix-icon="el-icon-search"
              clearable
              style="width:250px"
            />
            <el-button-group style="margin-left:15px">
              <el-button :type="fileViewMode === 'list' ? 'primary' : 'default'" size="small" icon="el-icon-s-unfold" @click="fileViewMode = 'list'">列表</el-button>
              <el-button :type="fileViewMode === 'grid' ? 'primary' : 'default'" size="small" icon="el-icon-menu" @click="fileViewMode = 'grid'">网格</el-button>
            </el-button-group>
            <el-button type="danger" plain icon="el-icon-close" size="small" style="margin-left:15px" @click="fileManagerOpen = false">关闭</el-button>
          </div>
        </div>

        <!-- 面包屑和统计 -->
        <div class="fm-breadcrumb-bar">
          <div class="fm-breadcrumb">
            <span class="breadcrumb-item" @click="navigateToRoot">
              <i class="el-icon-folder"></i> {{ currentPath.length === 0 ? '全部文件' : '所有文件' }}
            </span>
            <template v-for="(item, index) in currentPath">
              <span :key="index" class="breadcrumb-separator">/</span>
              <span :key="'item'+index" class="breadcrumb-item" @click="navigateTo(index)">{{ item.name }}</span>
            </template>
          </div>
          <div class="fm-stats">
            <span class="stat-item"><i class="el-icon-folder" style="color:#409EFF"></i> 项目: {{ fileStats.projects }}</span>
            <span class="stat-item"><i class="el-icon-document" style="color:#E6A23C"></i> 文件: {{ fileStats.files }}</span>
            <span class="stat-item"><i class="el-icon-coin" style="color:#67C23A"></i> 存储: {{ fileStats.storage }}</span>
          </div>
        </div>

        <!-- 视图提示和操作 -->
        <div class="fm-view-info">
          <span><i class="el-icon-info"></i> 当前视图：{{ fileViewMode === 'list' ? '列表视图' : '网格视图' }}</span>
          <div class="fm-toolbar">
            <el-button type="primary" size="mini" icon="el-icon-plus" @click="openAddFolder">新建文件夹</el-button>
            <el-button type="success" size="mini" icon="el-icon-upload2">上传文件</el-button>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-if="fileViewMode === 'list'" class="fm-list-view">
          <div class="fm-list-header">
            <span class="col-name">名称</span>
            <span class="col-type">类型</span>
            <span class="col-size">大小/数量</span>
            <span class="col-date">修改日期</span>
            <span class="col-action">操作</span>
          </div>
          <div class="fm-list-body">
            <div 
              v-for="folder in filteredFolderList" 
              :key="folder.id" 
              class="fm-list-item"
              @dblclick="enterFolder(folder)"
            >
              <span class="col-name">
                <i class="el-icon-folder folder-icon"></i>
                <span class="folder-name" @click="enterFolder(folder)">{{ folder.name }}</span>
              </span>
              <span class="col-type">
                <el-tag size="mini" :type="getTypeTagType(folder.type)">{{ folder.type }}</el-tag>
              </span>
              <span class="col-size">{{ folder.count }} 项</span>
              <span class="col-date">{{ folder.date }}</span>
              <span class="col-action">
                <el-button type="text" size="mini" icon="el-icon-folder-add" style="color:#67c23a" @click.stop="addSubFolder(folder)">新增</el-button>
                <el-button type="text" size="mini" icon="el-icon-edit" @click.stop="editFolder(folder)">编辑</el-button>
                <el-button type="text" size="mini" icon="el-icon-delete" style="color:#f56c6c" @click.stop="deleteFolder(folder)">删除</el-button>
              </span>
            </div>
            <div v-if="filteredFolderList.length === 0" class="fm-empty">
              <i class="el-icon-folder-delete"></i>
              <p>暂无文件夹</p>
            </div>
          </div>
        </div>

        <!-- 网格视图 -->
        <div v-else class="fm-grid-view">
          <div 
            v-for="folder in filteredFolderList" 
            :key="folder.id" 
            class="fm-grid-item"
            @dblclick="enterFolder(folder)"
          >
            <div class="grid-folder-icon" @click="enterFolder(folder)">
              <i class="el-icon-folder"></i>
            </div>
            <div class="grid-folder-name">{{ folder.name }}</div>
            <div class="grid-folder-info">{{ folder.count }} 项</div>
            <div class="grid-folder-actions">
              <el-button type="text" size="mini" icon="el-icon-folder-add" style="color:#67c23a" @click.stop="addSubFolder(folder)"></el-button>
              <el-button type="text" size="mini" icon="el-icon-edit" @click.stop="editFolder(folder)"></el-button>
              <el-button type="text" size="mini" icon="el-icon-delete" style="color:#f56c6c" @click.stop="deleteFolder(folder)"></el-button>
            </div>
          </div>
          <div v-if="filteredFolderList.length === 0" class="fm-empty">
            <i class="el-icon-folder-delete"></i>
            <p>暂无文件夹</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 文件夹编辑弹窗 -->
    <el-dialog :title="folderDialogTitle" :visible.sync="folderDialogOpen" width="400px" append-to-body>
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="文件夹名">
          <el-input v-model="folderForm.name" placeholder="请输入文件夹名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="folderForm.type" placeholder="选择类型" style="width:100%">
            <el-option label="项目根目录" value="项目根目录" />
            <el-option label="项目" value="项目" />
            <el-option label="流程" value="流程" />
            <el-option label="文档" value="文档" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="folderDialogOpen = false">取消</el-button>
        <el-button type="primary" @click="saveFolder">确定</el-button>
      </div>
    </el-dialog>

    <!-- 待办任务弹窗 -->
    <el-dialog 
      title="过程/文件确认 - 待办任务"
      :visible.sync="todoOpen" 
      width="1200px" 
      :close-on-click-modal="false"
      custom-class="todo-dialog"
    >
      <!-- 顶部筛选和统计区域 -->
      <div class="todo-header">
        <div class="filter-section">
          <el-tag type="info" effect="plain" size="medium">
            已筛选: {{ todoQueryParams.status || '全部' }} + {{ todoQueryParams.responsible || '全部负责人' }}
          </el-tag>
          <el-select v-model="todoQueryParams.status" placeholder="任务状态" clearable size="small" style="width:120px;margin-left:10px">
            <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="todoQueryParams.responsible" placeholder="负责人" clearable size="small" style="width:120px;margin-left:10px" />
          <el-button type="primary" size="small" icon="el-icon-search" style="margin-left:10px" @click="filterTodo">筛选</el-button>
          <el-button size="small" icon="el-icon-refresh" @click="resetTodoFilter">重置</el-button>
        </div>
        
        <!-- 统计表格 -->
        <div class="stats-table-wrapper">
          <table class="stats-mini-table">
            <thead>
              <tr>
                <th></th>
                <th v-for="n in 12" :key="n">{{ n }}月</th>
                <th>合计</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="row-label">新</td>
                <td v-for="n in 12" :key="'new'+n">{{ Math.floor(Math.random() * 50) }}</td>
                <td class="total">{{ 34 }}</td>
              </tr>
              <tr>
                <td class="row-label">旧</td>
                <td v-for="n in 12" :key="'old'+n">{{ Math.floor(Math.random() * 30) }}</td>
                <td class="total">{{ 16 }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 任务列表表格 -->
      <el-table 
        :data="filteredTodoList" 
        v-loading="todoLoading"
        border
        size="small"
        max-height="400"
        highlight-current-row
        :row-class-name="todoRowClassName"
      >
        <el-table-column label="项目编号" prop="projectCode" width="120" fixed>
          <template slot-scope="scope">
            <span class="project-link" @click="goToProject(scope.row)">{{ scope.row.projectCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开发类型" prop="devType" width="90" />
        <el-table-column label="途程" prop="route" width="80" />
        <el-table-column label="工序" prop="process" width="120" />
        <el-table-column label="任务来源" prop="taskSource" width="110" />
        <el-table-column label="优先级" prop="priority" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.priority === '紧急' ? 'danger' : (scope.row.priority === '高' ? 'warning' : 'success')" size="mini">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" prop="taskStatus" width="100" align="center">
          <template slot-scope="scope">
            <el-select v-model="scope.row.taskStatus" size="mini" @change="handleTaskStatusChange(scope.row)">
              <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="负责人" prop="responsible" width="80" align="center" />
        <el-table-column label="开始时间" prop="startTime" width="100" align="center" />
        <el-table-column label="最迟时间" prop="deadline" width="100" align="center" />
        <el-table-column label="状态" prop="status" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '正常' ? 'success' : (scope.row.status === '延期' ? 'danger' : 'warning')" size="mini">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="序号" prop="seq" width="60" align="center" />
        <el-table-column label="上次负责人" prop="lastResponsible" width="90" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-view" @click="viewTaskDetail(scope.row)">查看</el-button>
            <el-button type="text" size="mini" icon="el-icon-check" style="color:#67c23a" @click="completeTask(scope.row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer" class="todo-footer">
        <span class="todo-summary">共 {{ filteredTodoList.length }} 条待办任务</span>
        <el-button @click="todoOpen = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 新品开发评审单弹窗 -->
    <el-dialog 
      :visible.sync="reviewOpen" 
      width="900px" 
      :close-on-click-modal="false"
      custom-class="review-dialog"
    >
      <div class="review-container">
        <!-- 标题区域 -->
        <div class="review-header">
          <h2>新品开发评审单</h2>
          <div class="review-meta">
            <div>编号：{{ reviewForm.reviewCode }}</div>
            <div>打印日期：{{ reviewForm.printDate }}</div>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-info"></i> 基本信息</div>
          <table class="info-table">
            <tr>
              <td class="label">戴卡部件号 *</td>
              <td>{{ reviewForm.projectCode }}</td>
              <td class="label">客户名称</td>
              <td>{{ reviewForm.customer }}</td>
              <td class="label">申请日期</td>
              <td>{{ reviewForm.applyDate || '-' }}</td>
            </tr>
            <tr>
              <td class="label">责任人</td>
              <td>{{ reviewForm.managerName }}</td>
              <td class="label">类型 *</td>
              <td>{{ reviewForm.projectType }}</td>
              <td class="label">优先级</td>
              <td>{{ reviewForm.priority }}</td>
            </tr>
          </table>
        </div>

        <!-- 项目时间线 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-date"></i> 项目时间线</div>
          <table class="info-table">
            <tr>
              <td class="label">立项时间</td>
              <td>{{ reviewForm.approvalDate || '-' }}</td>
              <td class="label">模具时间</td>
              <td>{{ reviewForm.moldDate || '-' }}</td>
              <td class="label">试制时间</td>
              <td>{{ reviewForm.trialDate || '-' }}</td>
            </tr>
            <tr>
              <td class="label">首评送样</td>
              <td>{{ reviewForm.sampleDate || '-' }}</td>
              <td class="label">自检实验</td>
              <td>{{ reviewForm.testDate || '-' }}</td>
              <td class="label">SOP时间</td>
              <td>{{ reviewForm.sopDate || '-' }}</td>
            </tr>
          </table>
        </div>

        <!-- 产品信息 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-goods"></i> 产品信息</div>
          <table class="info-table">
            <tr>
              <td class="label">平台/车型</td>
              <td>{{ reviewForm.platform || '-' }}</td>
              <td class="label">产品尺寸</td>
              <td>{{ reviewForm.productSize || '-' }}</td>
              <td class="label">ET值</td>
              <td>{{ reviewForm.etValue || '-' }}</td>
            </tr>
            <tr>
              <td class="label">PCD</td>
              <td>{{ reviewForm.pcd || '-' }}</td>
              <td class="label">成型工艺</td>
              <td>{{ reviewForm.moldingProcess || '-' }}</td>
              <td class="label">表面状态</td>
              <td>{{ reviewForm.surfaceState || '-' }}</td>
            </tr>
            <tr>
              <td class="label">重量 (kg)</td>
              <td>{{ reviewForm.weight || '-' }}</td>
              <td class="label">客户部件号</td>
              <td>{{ reviewForm.customerPartNo || '-' }}</td>
              <td class="label">合同年量</td>
              <td>{{ reviewForm.annualVolume || '-' }}</td>
            </tr>
          </table>
        </div>

        <!-- 其他信息 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-more"></i> 其他信息</div>
          <table class="info-table">
            <tr>
              <td class="label">上线地点</td>
              <td>{{ reviewForm.location || '-' }}</td>
              <td class="label">颜色代码</td>
              <td>{{ reviewForm.colorCode || '-' }}</td>
              <td class="label">配套件</td>
              <td>{{ reviewForm.accessories || '-' }}</td>
            </tr>
            <tr>
              <td class="label">装配方式</td>
              <td>{{ reviewForm.assemblyMethod || '-' }}</td>
              <td class="label">涂装工艺</td>
              <td colspan="3">{{ reviewForm.paintProcess || '-' }}</td>
            </tr>
            <tr>
              <td class="label">项目补充描述</td>
              <td colspan="5">{{ reviewForm.description || '-' }}</td>
            </tr>
          </table>
        </div>

        <!-- 产品图片 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-picture"></i> 产品图片</div>
          <table class="info-table">
            <tr>
              <td class="label" style="width:100px">正面图</td>
              <td style="width:45%">
                <img v-if="reviewForm.frontImage" :src="reviewForm.frontImage" class="review-image" />
                <span v-else class="no-image">暂无图片</span>
              </td>
              <td class="label" style="width:100px">反面图</td>
              <td>
                <img v-if="reviewForm.backImage" :src="reviewForm.backImage" class="review-image" />
                <span v-else class="no-image">暂无图片</span>
              </td>
            </tr>
          </table>
        </div>

        <!-- 评审意见 -->
        <div class="review-section">
          <div class="section-title"><i class="el-icon-s-check"></i> 评审意见</div>
          <table class="review-table">
            <thead>
              <tr>
                <th style="width:100px">科室</th>
                <th>问题记录</th>
                <th style="width:100px">预计合格率</th>
                <th style="width:80px">上传人</th>
                <th style="width:100px">更新日期</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in reviewForm.reviews" :key="index">
                <td class="dept-cell">{{ item.dept }}</td>
                <td>
                  <el-input 
                    v-model="item.issue" 
                    type="textarea" 
                    :rows="2" 
                    placeholder="请输入问题记录..."
                    size="small"
                  />
                </td>
                <td>
                  <el-input v-model="item.passRate" placeholder="如：95%" size="small" />
                </td>
                <td>
                  <el-input v-model="item.uploader" placeholder="姓名" size="small" />
                </td>
                <td>
                  <el-date-picker 
                    v-model="item.updateDate" 
                    type="date" 
                    value-format="yyyy-MM-dd"
                    placeholder="日期"
                    size="small"
                    style="width:100%"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="review-footer">
        <el-button @click="reviewOpen = false">关闭</el-button>
        <el-button type="warning" icon="el-icon-printer" @click="handlePrintReview">打印预览</el-button>
        <el-button type="primary" icon="el-icon-check" @click="saveReview">提交评审</el-button>
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
          <el-select v-model="form.managerName" placeholder="请选择负责人" filterable style="width:100%">
            <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.nickName" />
          </el-select>
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
import { getAllUsers } from "@/api/tech/common"

export default {
  name: "Project",
  data() {
    return {
      loading: true,
      userList: [],
      total: 0,
      projectList: [],
      title: "",
      open: false,
      detailOpen: false,
      reviewOpen: false,
      selectedRow: null,
      reviewForm: {
        reviewCode: '',
        printDate: '',
        reviews: []
      },
      // 待办任务相关
      todoOpen: false,
      todoCount: 135,
      todoLoading: false,
      todoQueryParams: {
        status: '',
        responsible: ''
      },
      todoList: [
        { id: 1, projectCode: '07325C06-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: 'CAE分析报告', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 2, projectCode: '07325C07-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: 'CAE分析报告', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 3, projectCode: '07325C06-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '二维码标准', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 4, projectCode: '07325C07-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '二维码标准', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 5, projectCode: '07325C06-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '包装工艺', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 6, projectCode: '07325C06-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '包装工艺', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 7, projectCode: '07325C06-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '发运单', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
        { id: 8, projectCode: '07325C07-M1', devType: '定点开发', route: '资料输入', process: '集团资料接收二', taskSource: '发运单', priority: '正常', taskStatus: '待处理', responsible: '李罗程', startTime: '2025/12/25', deadline: '2025/12/28', status: '正常', seq: -2, lastResponsible: '李罗程' },
      ],
      taskStatusOptions: [
        { label: '待处理', value: '待处理' },
        { label: '处理中', value: '处理中' },
        { label: '已完成', value: '已完成' },
        { label: '已驳回', value: '已驳回' }
      ],
      // 文件管理系统相关
      fileManagerOpen: false,
      fileViewMode: 'list', // list 或 grid
      fileSearchKeyword: '',
      currentPath: [], // 当前路径面包屑
      fileStats: { projects: 15, files: 2510, storage: '6059.5 MB' },
      folderList: [],
      folderDialogOpen: false,
      folderDialogTitle: '新建文件夹',
      folderForm: { id: null, name: '', type: '项目根目录', parentId: null },
      // 模拟的文件夹数据结构
      allFolders: {
        root: [
          { id: 1, name: '公用文件', type: '项目根目录', count: 25, date: '2025-12-25', children: [] },
          { id: 2, name: '产品照片', type: '项目根目录', count: 239, date: '2025-12-24', children: [] },
          { id: 3, name: '071', type: '项目根目录', count: 847, date: '2025-12-26', children: [
            { id: 31, name: '07125C23', type: '项目', count: 55, date: '2025-12-09', children: [
              { id: 311, name: '资料输入', type: '流程', count: 14, date: '2025-09-16', children: [] },
              { id: 312, name: '模具回厂试制', type: '流程', count: 4, date: '2025-07-01', children: [] },
              { id: 313, name: '试制准备', type: '流程', count: 6, date: '2025-08-15', children: [] },
              { id: 314, name: '开模准备', type: '流程', count: 7, date: '2025-09-12', children: [] },
              { id: 315, name: '首件审核', type: '流程', count: 14, date: '2025-09-03', children: [] },
              { id: 316, name: '内部通知', type: '流程', count: 5, date: '2025-11-25', children: [] },
              { id: 317, name: '内部工艺', type: '流程', count: 5, date: '2025-12-09', children: [] },
            ]},
            { id: 32, name: '07125F13', type: '项目', count: 48, date: '2025-09-17', children: [] },
            { id: 33, name: '07125C06', type: '项目', count: 52, date: '2025-09-10', children: [] },
            { id: 34, name: '07124F31', type: '项目', count: 49, date: '2025-09-17', children: [] },
            { id: 35, name: '07124F53', type: '项目', count: 43, date: '2025-09-17', children: [] },
            { id: 36, name: '07125F23', type: '项目', count: 48, date: '2025-09-22', children: [] },
            { id: 37, name: '07125C38', type: '项目', count: 45, date: '2025-12-25', children: [] },
          ]},
          { id: 4, name: '073', type: '项目根目录', count: 57, date: '2025-12-26', children: [] },
          { id: 5, name: '051', type: '项目根目录', count: 132, date: '2025-12-24', children: [] },
          { id: 6, name: '096', type: '项目根目录', count: 381, date: '2025-12-26', children: [] },
          { id: 7, name: '092', type: '项目根目录', count: 113, date: '2025-12-25', children: [] },
          { id: 8, name: '070', type: '项目根目录', count: 110, date: '2025-12-25', children: [] },
        ]
      },
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
  computed: {
    filteredTodoList() {
      let list = this.todoList
      if (this.todoQueryParams.status) {
        list = list.filter(item => item.taskStatus === this.todoQueryParams.status)
      }
      if (this.todoQueryParams.responsible) {
        list = list.filter(item => item.responsible.includes(this.todoQueryParams.responsible))
      }
      return list
    },
    filteredFolderList() {
      if (!this.fileSearchKeyword) {
        return this.folderList
      }
      return this.folderList.filter(f => 
        f.name.toLowerCase().includes(this.fileSearchKeyword.toLowerCase())
      )
    }
  },
  created() {
    this.getList()
    this.loadUserList()
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
    loadUserList() {
      getAllUsers().then(response => {
        this.userList = response.rows || []
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
      this.reviewForm = {
        ...this.detailForm,
        reviewCode: 'CD04-SQ' + new Date().toISOString().slice(2,10).replace(/-/g,'') + '-01',
        printDate: new Date().toLocaleDateString('zh-CN'),
        reviews: [
          { dept: '技术科', issue: '', passRate: '', uploader: '', updateDate: '' },
          { dept: '热工科', issue: '', passRate: '', uploader: '', updateDate: '' },
          { dept: '机加科', issue: '', passRate: '', uploader: '', updateDate: '' },
          { dept: '质量科', issue: '', passRate: '', uploader: '', updateDate: '' },
          { dept: '涂装科', issue: '', passRate: '', uploader: '', updateDate: '' },
          { dept: '生产计划科', issue: '', passRate: '', uploader: '', updateDate: '' },
        ]
      }
      this.reviewOpen = true
    },
    handlePrint() {
      this.$modal.msgSuccess("打印预览功能开发中...")
    },
    handlePrintReview() {
      this.$modal.msgSuccess("打印功能开发中...")
    },
    saveReview() {
      this.$modal.msgSuccess("评审信息已保存")
      this.reviewOpen = false
    },
    // 待办任务相关方法
    handleFolder() {
      this.fileManagerOpen = true
      this.currentPath = []
      this.loadFolderData()
    },
    openTodoList() {
      this.todoOpen = true
    },
    filterTodo() {
      this.$modal.msgSuccess("筛选已应用")
    },
    resetTodoFilter() {
      this.todoQueryParams = { status: '', responsible: '' }
    },
    handleTaskStatusChange(row) {
      this.$modal.msgSuccess(`任务 ${row.projectCode} 状态已更新为 ${row.taskStatus}`)
    },
    goToProject(row) {
      this.todoOpen = false
      // 查找并打开对应项目
      const project = this.projectList.find(p => p.projectCode === row.projectCode.split('-')[0])
      if (project) {
        this.openDetail(project)
      }
    },
    viewTaskDetail(row) {
      this.$modal.msgSuccess(`查看任务详情: ${row.projectCode} - ${row.taskSource}`)
    },
    completeTask(row) {
      this.$confirm(`确认完成任务 "${row.taskSource}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        row.taskStatus = '已完成'
        this.todoCount--
        this.$modal.msgSuccess('任务已完成')
      }).catch(() => {})
    },
    todoRowClassName({ row }) {
      if (row.status === '延期') return 'row-danger'
      if (row.taskStatus === '已完成') return 'row-success'
      return ''
    },
    // 文件管理系统相关方法
    loadFolderData() {
      if (this.currentPath.length === 0) {
        this.folderList = this.allFolders.root
        this.fileStats = { projects: 15, files: 2510, storage: '6059.5 MB' }
      } else {
        // 根据路径获取当前文件夹的子文件夹
        let current = this.allFolders.root
        for (const item of this.currentPath) {
          const found = current.find(f => f.id === item.id)
          if (found && found.children) {
            current = found.children
          }
        }
        this.folderList = current
        // 更新统计
        const totalCount = this.folderList.reduce((sum, f) => sum + f.count, 0)
        this.fileStats = { 
          projects: this.folderList.length, 
          files: totalCount, 
          storage: (totalCount * 1.5).toFixed(1) + ' MB' 
        }
      }
    },
    enterFolder(folder) {
      if (folder.children && folder.children.length > 0) {
        this.currentPath.push({ id: folder.id, name: folder.name })
        this.loadFolderData()
      } else {
        this.$modal.msgInfo(`文件夹 "${folder.name}" 为空或已是最底层`)
      }
    },
    navigateToRoot() {
      this.currentPath = []
      this.loadFolderData()
    },
    navigateTo(index) {
      this.currentPath = this.currentPath.slice(0, index + 1)
      this.loadFolderData()
    },
    getTypeTagType(type) {
      const map = {
        '项目根目录': 'info',
        '项目': 'primary',
        '流程': 'success',
        '文档': 'warning'
      }
      return map[type] || 'info'
    },
    openAddFolder() {
      this.folderDialogTitle = '新建文件夹'
      this.folderForm = { id: null, name: '', type: '项目根目录', parentId: null, parentFolder: null }
      this.folderDialogOpen = true
    },
    addSubFolder(parentFolder) {
      this.folderDialogTitle = `在 "${parentFolder.name}" 下新建文件夹`
      this.folderForm = { id: null, name: '', type: '项目', parentId: parentFolder.id, parentFolder: parentFolder }
      this.folderDialogOpen = true
    },
    editFolder(folder) {
      this.folderDialogTitle = '编辑文件夹'
      this.folderForm = { ...folder }
      this.folderDialogOpen = true
    },
    saveFolder() {
      if (!this.folderForm.name) {
        this.$modal.msgError('请输入文件夹名称')
        return
      }
      if (this.folderForm.id) {
        // 编辑
        const folder = this.folderList.find(f => f.id === this.folderForm.id)
        if (folder) {
          folder.name = this.folderForm.name
          folder.type = this.folderForm.type
        }
        this.$modal.msgSuccess('文件夹已更新')
      } else {
        // 新建
        const newFolder = {
          id: Date.now(),
          name: this.folderForm.name,
          type: this.folderForm.type,
          count: 0,
          date: new Date().toISOString().slice(0, 10),
          children: []
        }
        
        if (this.folderForm.parentFolder) {
          // 在指定父文件夹下新建
          if (!this.folderForm.parentFolder.children) {
            this.folderForm.parentFolder.children = []
          }
          this.folderForm.parentFolder.children.push(newFolder)
          this.folderForm.parentFolder.count++
          this.$modal.msgSuccess(`已在 "${this.folderForm.parentFolder.name}" 下创建文件夹`)
        } else {
          // 在当前目录下新建
          this.folderList.push(newFolder)
          this.$modal.msgSuccess('文件夹已创建')
        }
      }
      this.folderDialogOpen = false
    },
    deleteFolder(folder) {
      this.$confirm(`确认删除文件夹 "${folder.name}" 吗？此操作不可恢复。`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.folderList.findIndex(f => f.id === folder.id)
        if (index > -1) {
          this.folderList.splice(index, 1)
          this.$modal.msgSuccess('文件夹已删除')
        }
      }).catch(() => {})
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
  gap: 20px;
  
  .search-input {
    width: 280px;
    flex-shrink: 0;
  }
  
  .toolbar-center {
    display: flex;
    gap: 15px;
    align-items: center;
    flex: 1;
    justify-content: center;
  }
  
  .toolbar-buttons {
    display: flex;
    gap: 10px;
    flex-shrink: 0;
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

/* 评审单弹窗样式 */
::v-deep .review-dialog {
  .el-dialog__header {
    display: none;
  }
  .el-dialog__body {
    padding: 0;
    max-height: 80vh;
    overflow-y: auto;
  }
}

.review-container {
  padding: 25px;
}

.review-header {
  text-align: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409EFF;
  
  h2 {
    margin: 0 0 10px 0;
    font-size: 22px;
    color: #303133;
  }
  
  .review-meta {
    display: flex;
    justify-content: flex-end;
    gap: 30px;
    font-size: 13px;
    color: #606266;
  }
}

.review-section {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-left: 3px solid #409EFF;
    
    i {
      margin-right: 6px;
      color: #409EFF;
    }
  }
}

.info-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  
  td {
    border: 1px solid #ebeef5;
    padding: 10px 12px;
    
    &.label {
      background: #fafafa;
      color: #606266;
      font-weight: 500;
      width: 100px;
      white-space: nowrap;
    }
  }
}

.review-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  
  th, td {
    border: 1px solid #ebeef5;
    padding: 8px 10px;
  }
  
  th {
    background: #f5f7fa;
    color: #303133;
    font-weight: 600;
    text-align: center;
  }
  
  .dept-cell {
    background: #fafafa;
    font-weight: 500;
    text-align: center;
    color: #409EFF;
  }
}

.review-image {
  max-width: 150px;
  max-height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #c0c4cc;
  font-size: 12px;
}

.review-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 25px;
  border-top: 1px solid #eee;
  background: #f8f9fa;
}

/* 待办任务相关样式 */
.todo-badge {
  .el-badge__content {
    background: #f56c6c;
  }
}

::v-deep .todo-dialog {
  .el-dialog__body {
    padding: 15px 20px;
  }
}

.todo-header {
  margin-bottom: 15px;
  
  .filter-section {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    flex-wrap: wrap;
    gap: 5px;
  }
}

.stats-table-wrapper {
  overflow-x: auto;
  margin-bottom: 10px;
}

.stats-mini-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  
  th, td {
    border: 1px solid #ebeef5;
    padding: 6px 8px;
    text-align: center;
    min-width: 40px;
  }
  
  th {
    background: #f5f7fa;
    font-weight: 600;
    color: #606266;
  }
  
  .row-label {
    background: #f5f7fa;
    font-weight: 600;
    color: #409EFF;
  }
  
  .total {
    background: #e6f7ff;
    font-weight: 600;
    color: #1890ff;
  }
}

.project-link {
  color: #409EFF;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

.todo-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .todo-summary {
    color: #909399;
    font-size: 13px;
  }
}

::v-deep .row-danger {
  background: #fef0f0 !important;
}

::v-deep .row-success {
  background: #f0f9eb !important;
}

/* 文件管理系统样式 */
::v-deep .file-manager-dialog {
  .el-dialog__header {
    display: none;
  }
  .el-dialog__body {
    padding: 0;
  }
}

.file-manager-container {
  background: #f5f7fa;
  min-height: 500px;
}

.fm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px;
  background: #fff;
  border-bottom: 1px solid #eee;
  
  .fm-title {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .fm-icon {
    width: 50px;
    height: 50px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: bold;
    font-size: 18px;
  }
  
  .fm-title-text {
    h2 {
      margin: 0;
      font-size: 20px;
      color: #303133;
    }
    p {
      margin: 5px 0 0;
      font-size: 13px;
      color: #909399;
    }
  }
  
  .fm-actions {
    display: flex;
    align-items: center;
  }
}

.fm-breadcrumb-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.fm-breadcrumb {
  display: flex;
  align-items: center;
  font-size: 14px;
  
  .breadcrumb-item {
    color: #409EFF;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
    
    i {
      margin-right: 5px;
    }
  }
  
  .breadcrumb-separator {
    margin: 0 8px;
    color: #c0c4cc;
  }
}

.fm-stats {
  display: flex;
  gap: 20px;
  
  .stat-item {
    font-size: 13px;
    color: #606266;
    
    i {
      margin-right: 5px;
    }
  }
}

.fm-view-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: #ecf5ff;
  font-size: 13px;
  color: #409EFF;
  
  i {
    margin-right: 5px;
  }
  
  .fm-toolbar {
    display: flex;
    gap: 10px;
  }
}

.fm-list-view {
  background: #fff;
  margin: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.fm-list-header {
  display: flex;
  padding: 12px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.fm-list-body {
  max-height: 350px;
  overflow-y: auto;
}

.fm-list-item {
  display: flex;
  padding: 15px 20px;
  border-bottom: 1px solid #f5f5f5;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.col-name {
  flex: 1;
  display: flex;
  align-items: center;
  
  .folder-icon {
    font-size: 22px;
    color: #f7ba2a;
    margin-right: 12px;
  }
  
  .folder-name {
    color: #303133;
    font-weight: 500;
    
    &:hover {
      color: #409EFF;
    }
  }
}

.col-type {
  width: 100px;
  text-align: center;
}

.col-size {
  width: 100px;
  text-align: center;
  color: #409EFF;
}

.col-date {
  width: 120px;
  text-align: center;
  color: #909399;
}

.col-action {
  width: 180px;
  text-align: center;
}

.fm-grid-view {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 20px 30px;
  max-height: 400px;
  overflow-y: auto;
}

.fm-grid-item {
  width: 120px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transform: translateY(-2px);
  }
  
  .grid-folder-icon {
    font-size: 48px;
    color: #f7ba2a;
    margin-bottom: 10px;
  }
  
  .grid-folder-name {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .grid-folder-info {
    font-size: 12px;
    color: #909399;
  }
  
  .grid-folder-actions {
    margin-top: 8px;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  &:hover .grid-folder-actions {
    opacity: 1;
  }
}

.fm-empty {
  text-align: center;
  padding: 60px 20px;
  color: #c0c4cc;
  width: 100%;
  
  i {
    font-size: 48px;
    margin-bottom: 15px;
  }
  
  p {
    font-size: 14px;
  }
}
</style>
