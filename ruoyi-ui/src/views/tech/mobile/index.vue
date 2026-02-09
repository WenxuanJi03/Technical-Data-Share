<template>
  <div class="mobile-home">
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
        <div class="user-dept">{{ userDept }}</div>
      </div>
      <div class="user-logout" @click="handleLogout">
        <i class="el-icon-switch-button"></i>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-title">
      <i class="el-icon-folder-opened"></i>
      技术科管理平台
    </div>

    <!-- 菜单卡片 -->
    <div class="menu-section">
      <div class="section-title"><i class="el-icon-setting"></i> 菜单</div>
      <div class="menu-grid">
        <div class="menu-card" @click="goTo('task')">
          <div class="card-icon purple">
            <i class="el-icon-s-cooperation"></i>
          </div>
          <div class="card-title">试制任务</div>
          <div class="card-desc">试制任务处理列表</div>
        </div>
        <div class="menu-card" @click="goTo('review')">
          <div class="card-icon green">
            <i class="el-icon-s-check"></i>
          </div>
          <div class="card-title">新产品评审</div>
          <div class="card-desc">新产品评审任务列表</div>
        </div>
        <div class="menu-card" @click="goTo('files')">
          <div class="card-icon orange">
            <i class="el-icon-folder"></i>
          </div>
          <div class="card-title">文件夹</div>
          <div class="card-desc">项目文件、产品标签</div>
        </div>
        <div class="menu-card" @click="goTo('workorder')">
          <div class="card-icon blue">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="card-title">工单管理</div>
          <div class="card-desc">创建处理工单任务</div>
        </div>
      </div>
    </div>

    <!-- 底部快捷操作 -->
    <div class="quick-actions">
      <div class="action-item" @click="goTo('task')">
        <i class="el-icon-s-home"></i>
        <span>首页</span>
      </div>
      <div class="action-item" @click="goTo('task')">
        <i class="el-icon-document"></i>
        <span>任务</span>
      </div>
      <div class="action-item" @click="$router.push('/trial/notice')">
        <i class="el-icon-bell"></i>
        <span>通知</span>
      </div>
      <div class="action-item">
        <i class="el-icon-user"></i>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: "MobileHome",
  computed: {
    ...mapGetters(['name', 'avatar']),
    userName() {
      return this.$store.state.user.name || '用户'
    },
    userDept() {
      return '产品开发组-项目管理权限、产品开发组-试制管理权限'
    }
  },
  methods: {
    goTo(page) {
      const routes = {
        task: '/mobile/task',
        review: '/mobile/task',
        files: '/mobile/task',
        workorder: '/mobile/task'
      }
      this.$router.push(routes[page] || '/mobile/task')
    },
    handleLogout() {
      this.$confirm('确定退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/index'
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped lang="scss">
.mobile-home {
  min-height: 100vh;
  background: linear-gradient(180deg, #4a3b8f 0%, #6c5bb3 30%, #f0f2f5 30%);
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
    
    i {
      font-size: 28px;
      color: #fff;
    }
  }
  
  .user-info {
    flex: 1;
    
    .user-name {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 4px;
    }
    
    .user-dept {
      font-size: 12px;
      color: rgba(255,255,255,0.75);
      line-height: 1.4;
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
    
    i {
      font-size: 18px;
      color: #fff;
    }
    
    &:active {
      background: rgba(255,255,255,0.3);
    }
  }
}

.page-title {
  padding: 20px 20px 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  
  i {
    margin-right: 8px;
    color: #6c5bb3;
  }
}

.menu-section {
  margin: 0 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  
  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 18px;
    
    i {
      margin-right: 6px;
      color: #909399;
    }
  }
}

.menu-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.menu-card {
  background: #f8f9fd;
  border-radius: 14px;
  padding: 24px 18px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f5;
  
  &:active {
    transform: scale(0.96);
    background: #f0f1f8;
  }
  
  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    
    i {
      font-size: 28px;
      color: #fff;
    }
    
    &.purple { background: linear-gradient(135deg, #7c5cbf, #9b7fd4); }
    &.green { background: linear-gradient(135deg, #52c41a, #73d13d); }
    &.orange { background: linear-gradient(135deg, #fa8c16, #ffa940); }
    &.blue { background: linear-gradient(135deg, #1890ff, #40a9ff); }
  }
  
  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .card-desc {
    font-size: 12px;
    color: #909399;
  }
}

.quick-actions {
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
  
  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    
    i {
      font-size: 22px;
      color: #909399;
      margin-bottom: 2px;
    }
    
    span {
      font-size: 11px;
      color: #909399;
    }
    
    &:active {
      i, span { color: #6c5bb3; }
    }
  }
}
</style>
