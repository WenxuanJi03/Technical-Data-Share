<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-header">
        <span class="welcome-icon">🔧</span>
        <h1>欢迎使用技术科管理系统</h1>
      </div>
      <p class="welcome-desc">
        本系统提供全面的项目管理、试制管理、实验室数据管理和权限控制功能。通过左侧菜单访问各项功能，系统将根据您的权限动态显示可访问的菜单项。
      </p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon blue">
          <i class="el-icon-folder-opened"></i>
        </div>
        <div class="stat-number">{{ projectCount }}</div>
        <div class="stat-label">进行中项目</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon green">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-number">{{ docCount }}</div>
        <div class="stat-label">技术文档</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orange">
          <i class="el-icon-refresh"></i>
        </div>
        <div class="stat-number">{{ changeCount }}</div>
        <div class="stat-label">本月变更</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      projectCount: 42,
      docCount: 128,
      changeCount: 24
    }
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  min-height: calc(100vh - 84px);
  position: relative;
  background: transparent;

  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('~@/assets/images/home-background.jpg') center / cover no-repeat;
    opacity: 0.4;
    z-index: -1;
    pointer-events: none;
  }
}

.welcome-section {
  background: linear-gradient(135deg, #e8f4fc 0%, #f5fafd 100%);
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 30px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.welcome-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.welcome-icon {
  font-size: 32px;
  margin-right: 12px;
}

.welcome-header h1 {
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.welcome-desc {
  color: #666;
  font-size: 14px;
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.8;
}

.stats-section {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.stat-card {
  background: linear-gradient(135deg, #e8f4fc 0%, #f5fafd 100%);
  border-radius: 12px;
  padding: 30px 50px;
  text-align: center;
  min-width: 200px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}
</style>
