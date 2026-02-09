<template>
  <div class="mobile-task">
    <!-- 顶部标题栏 -->
    <div class="mobile-header">
      <span class="header-badge">移动端菜单</span>
    </div>

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-avatar">
        <i class="el-icon-user-solid"></i>
      </div>
      <div class="user-info">
        <div class="user-name">{{ userName }}</div>
        <div class="user-roles">
          <i class="el-icon-lock"></i> {{ userRoles }}
        </div>
      </div>
      <div class="user-logout" @click="goBack">
        <i class="el-icon-switch-button"></i>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索任务、工序或日期..."
        prefix-icon="el-icon-search"
        clearable
        @input="filterTasks"
      />
    </div>

    <!-- 任务列表卡片 -->
    <div class="task-list-card">
      <div class="task-header">
        <h3>任务列表</h3>
        <div class="task-tabs">
          <span 
            class="tab-item" 
            :class="{ active: currentTab === 'doing' }" 
            @click="switchTab('doing')"
          >
            进行中 <em>{{ doingCount }}</em>
          </span>
          <span 
            class="tab-item" 
            :class="{ active: currentTab === 'done' }" 
            @click="switchTab('done')"
          >
            已完成 <em>{{ doneCount }}</em>
          </span>
        </div>
      </div>

      <div class="task-filter-info">
        <span class="filter-label">{{ currentTab === 'doing' ? '显示进行中任务' : '显示已完成任务' }}</span>
        <span class="task-count">{{ filteredTasks.length }} 个任务</span>
      </div>

      <!-- 任务条目 -->
      <div class="task-items" v-loading="loading">
        <div 
          v-for="item in filteredTasks" 
          :key="item.trialId" 
          class="task-item"
          @click="openDetail(item)"
        >
          <div class="task-icon">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="task-info">
            <div class="task-name">{{ item.trialCode }}-{{ item.craftProcess || '低压铸造' }}+{{ item.surfaceStatus || '全涂' }}</div>
            <div class="task-meta" v-if="item.trialStart">
              <i class="el-icon-time"></i> {{ formatDate(item.trialStart) }}
            </div>
          </div>
          <div class="task-priority" :class="getPriorityClass(item.urgency || item.priority)">
            {{ item.urgency || item.priority || '正常' }}
          </div>
        </div>

        <div v-if="filteredTasks.length === 0 && !loading" class="empty-state">
          <i class="el-icon-folder-opened"></i>
          <p>暂无{{ currentTab === 'doing' ? '进行中' : '已完成' }}的任务</p>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="footer-info">
      &copy; 2026 任务状态管理系统 | 版本 3.0
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="$router.push('/mobile/menu')">
        <i class="el-icon-s-home"></i>
        <span>首页</span>
      </div>
      <div class="nav-item active">
        <i class="el-icon-document"></i>
        <span>任务</span>
      </div>
      <div class="nav-item">
        <i class="el-icon-bell"></i>
        <span>通知</span>
      </div>
      <div class="nav-item">
        <i class="el-icon-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import { listTrialNotice } from "@/api/tech/trialNotice"
import { mapGetters } from 'vuex'

export default {
  name: "MobileTask",
  data() {
    return {
      loading: false,
      searchText: '',
      currentTab: 'doing',
      taskList: [],
      doingCount: 0,
      doneCount: 0
    }
  },
  computed: {
    ...mapGetters(['name']),
    userName() {
      return this.$store.state.user.name || '用户'
    },
    userRoles() {
      return '产品开发组-项目管理权限、产品开发组-试制管理权限'
    },
    filteredTasks() {
      let list = this.taskList.filter(item => {
        if (this.currentTab === 'doing') {
          return item.status !== '已完成'
        }
        return item.status === '已完成'
      })
      if (this.searchText) {
        const keyword = this.searchText.toLowerCase()
        list = list.filter(item => {
          return (item.wheelCode && item.wheelCode.toLowerCase().includes(keyword)) ||
                 (item.noticeCode && item.noticeCode.toLowerCase().includes(keyword)) ||
                 (item.craftProcess && item.craftProcess.toLowerCase().includes(keyword)) ||
                 (item.responsible && item.responsible.toLowerCase().includes(keyword))
        })
      }
      return list
    }
  },
  created() {
    this.loadTasks()
  },
  methods: {
    loadTasks() {
      this.loading = true
      listTrialNotice({ pageNum: 1, pageSize: 500 }).then(response => {
        this.taskList = response.rows || []
        this.doingCount = this.taskList.filter(i => i.status !== '已完成').length
        this.doneCount = this.taskList.filter(i => i.status === '已完成').length
        this.loading = false
      }).catch(() => {
        // 使用模拟数据
        this.taskList = [
          { noticeId: 1, wheelCode: '07125C67-M1', noticeCode: 'CD16-2025-0516', craftProcess: '低压铸造', surfaceStatus: '全涂', urgency: '正常', status: '进行中', trialStart: '2026-01-15', responsible: '王工', trialQuantity: 200, sampleQuantity: 0, sizeSpec: '1770', trialType: '新产品小批量', experimentItems: '13度冲击,90度冲击,CASS中性盐雾,化学成分,弯曲疲劳,径向疲劳,机械性能,金相' },
          { noticeId: 2, wheelCode: '05124C12-M1', noticeCode: 'CD16-2025-0412', craftProcess: '低压铸造', surfaceStatus: '全涂', urgency: '优先', status: '进行中', trialStart: '2026-01-20', responsible: '李工', trialQuantity: 150, sampleQuantity: 10, sizeSpec: '1880', trialType: '新产品小批量' },
          { noticeId: 3, wheelCode: '05525C35-M1', noticeCode: 'CD16-2025-0535', craftProcess: '低压铸造', surfaceStatus: '精车', urgency: '正常', status: '进行中', trialStart: '2026-02-01', responsible: '张工', trialQuantity: 100, sampleQuantity: 5, sizeSpec: '1670', trialType: '改进试制' },
        ]
        this.doingCount = 3
        this.doneCount = 216
        this.loading = false
      })
    },
    switchTab(tab) {
      this.currentTab = tab
    },
    filterTasks() {
      // 计算在computed中完成
    },
    formatDate(date) {
      if (!date) return ''
      return date.substring(0, 10)
    },
    getPriorityClass(priority) {
      const map = { '紧急': 'danger', '优先': 'warning', '正常': 'success' }
      return map[priority] || 'success'
    },
    openDetail(item) {
      this.$router.push({ path: '/mobile/detail', query: { id: item.noticeId } })
    },
    goBack() {
      this.$router.push('/mobile/menu')
    }
  }
}
</script>

<style scoped lang="scss">
.mobile-task {
  min-height: 100vh;
  background: linear-gradient(180deg, #4a3b8f 0%, #6c5bb3 35%, #f0f2f5 35%);
  padding-bottom: 80px;
}

.mobile-header {
  text-align: center;
  padding: 15px 0 10px;
  
  .header-badge {
    display: inline-block;
    background: rgba(255,255,255,0.2);
    color: #fff;
    padding: 6px 20px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255,255,255,0.3);
  }
}

.user-card {
  display: flex;
  align-items: center;
  margin: 10px 20px;
  padding: 18px 20px;
  background: rgba(255,255,255,0.15);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  
  .user-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: rgba(255,255,255,0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    
    i { font-size: 28px; color: #fff; }
  }
  
  .user-info {
    flex: 1;
    
    .user-name {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 4px;
    }
    
    .user-roles {
      font-size: 12px;
      color: rgba(255,255,255,0.75);
      line-height: 1.4;
      
      i { margin-right: 4px; }
    }
  }
  
  .user-logout {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255,255,255,0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    
    i { font-size: 18px; color: #fff; }
    &:active { background: rgba(255,255,255,0.3); }
  }
}

.search-bar {
  margin: 15px 20px;
  
  ::v-deep .el-input__inner {
    border-radius: 25px;
    border: none;
    padding-left: 40px;
    height: 44px;
    font-size: 14px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  }
  
  ::v-deep .el-input__prefix {
    left: 15px;
  }
}

.task-list-card {
  margin: 0 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  
  .task-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 15px;
    
    h3 {
      font-size: 18px;
      font-weight: 700;
      color: #303133;
      margin: 0;
    }
  }
  
  .task-tabs {
    display: flex;
    gap: 10px;
    
    .tab-item {
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
      cursor: pointer;
      background: #f0f2f5;
      color: #606266;
      transition: all 0.2s;
      
      em {
        font-style: normal;
        font-weight: 600;
      }
      
      &.active {
        background: #6c5bb3;
        color: #fff;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.task-filter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  
  .filter-label {
    font-size: 13px;
    color: #409EFF;
    cursor: pointer;
  }
  
  .task-count {
    font-size: 13px;
    color: #909399;
  }
}

.task-items {
  .task-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background 0.2s;
    
    &:active {
      background: #f8f9fd;
      border-radius: 10px;
      margin: 0 -8px;
      padding: 14px 8px;
    }
    
    &:last-child {
      border-bottom: none;
    }
    
    .task-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: #e8f0fe;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
      
      i {
        font-size: 20px;
        color: #6c5bb3;
      }
    }
    
    .task-info {
      flex: 1;
      min-width: 0;
      
      .task-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .task-meta {
        font-size: 12px;
        color: #909399;
        margin-top: 3px;
        
        i { margin-right: 3px; }
      }
    }
    
    .task-priority {
      padding: 3px 10px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;
      flex-shrink: 0;
      margin-left: 10px;
      
      &.success { color: #67c23a; background: #f0f9eb; }
      &.warning { color: #e6a23c; background: #fdf6ec; }
      &.danger { color: #f56c6c; background: #fef0f0; }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
  
  i { font-size: 40px; margin-bottom: 10px; }
  p { font-size: 14px; }
}

.footer-info {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 12px;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #fff;
  border-top: 1px solid #eee;
  padding: 8px 0;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  z-index: 100;
  
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    
    i { font-size: 22px; color: #909399; margin-bottom: 2px; }
    span { font-size: 11px; color: #909399; }
    
    &.active {
      i { color: #6c5bb3; }
      span { color: #6c5bb3; font-weight: 600; }
    }
    
    &:active {
      i, span { color: #6c5bb3; }
    }
  }
}
</style>
