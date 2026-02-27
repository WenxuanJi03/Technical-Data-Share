<template>
  <view class="login-page">
    <!-- 顶部装饰 -->
    <view class="header-bg">
      <view class="header-content">
        <text class="app-title">技术管理系统</text>
        <text class="app-subtitle">技术科管理平台</text>
      </view>
    </view>

    <!-- 登录表单 -->
    <view class="login-card">
      <view class="card-title">账号登录</view>

      <!-- 服务器地址快速切换 -->
      <view class="server-switch" v-if="showServerSwitch">
        <view class="server-label">🌐 服务器:</view>
        <picker @change="onServerChange" :value="serverIndex" :range="serverList" range-key="name">
          <view class="server-picker">
            {{ currentServer.name }}
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>
      <view class="server-tip" @tap="toggleServerSwitch">
        <text>{{ showServerSwitch ? '隐藏' : '显示' }}服务器设置</text>
      </view>

      <view class="form-item">
        <view class="input-icon">👤</view>
        <input
          v-model="loginForm.username"
          placeholder="请输入账号"
          class="form-input"
        />
      </view>

      <view class="form-item">
        <view class="input-icon">🔒</view>
        <input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          class="form-input"
        />
      </view>

      <view class="form-item captcha-row">
        <view class="input-icon">🔢</view>
        <input
          v-model="loginForm.code"
          placeholder="验证码"
          class="form-input captcha-input"
          @confirm="handleLogin"
        />
        <image
          :src="captchaUrl"
          class="captcha-image"
          @tap="getCaptcha"
          mode="aspectFit"
        />
      </view>

      <button class="login-btn" :loading="loading" @tap="handleLogin">
        登 录
      </button>

      <view class="login-tip">
        <text>提示：使用Web端账号密码登录</text>
      </view>
    </view>

    <!-- 底部信息 -->
    <view class="footer">
      <text>© 2026 技术管理系统 v1.0</text>
    </view>
  </view>
</template>

<script>
import { captchaImage } from '@/api/login'
import config from '@/config/index'

export default {
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'admin123',
        code: '',
        uuid: ''
      },
      captchaUrl: '',
      loading: false,
      showServerSwitch: false,
      serverList: config.serverList || [],
      serverIndex: 0,
      currentServer: { name: '默认', url: config.baseUrl }
    }
  },
  onLoad() {
    // 初始化服务器列表
    if (this.serverList.length > 0) {
      this.currentServer = this.serverList[0]
      // 从缓存读取上次选择的服务器
      const savedIndex = uni.getStorageSync('serverIndex')
      if (savedIndex !== null && savedIndex < this.serverList.length) {
        this.serverIndex = savedIndex
        this.currentServer = this.serverList[savedIndex]
      }
    }
    this.getCaptcha()
  },
  methods: {
    // 切换服务器选择显示
    toggleServerSwitch() {
      this.showServerSwitch = !this.showServerSwitch
    },
    // 切换服务器
    onServerChange(e) {
      const index = e.detail.value
      this.serverIndex = index
      this.currentServer = this.serverList[index]
      // 保存选择
      uni.setStorageSync('serverIndex', index)
      uni.setStorageSync('customBaseUrl', this.currentServer.url)
      uni.showToast({ 
        title: '服务器已切换至: ' + this.currentServer.name, 
        icon: 'none',
        duration: 2000
      })
      // 重新获取验证码测试连接
      this.getCaptcha()
    },
    // 获取验证码
    getCaptcha() {
      captchaImage().then(res => {
        this.loginForm.uuid = res.uuid
        this.captchaUrl = 'data:image/gif;base64,' + res.img
      }).catch(() => {
        // 后端未连接时使用占位
        this.captchaUrl = ''
      })
    },
    // 登录
    handleLogin() {
      if (!this.loginForm.username) {
        uni.showToast({ title: '请输入账号', icon: 'none' })
        return
      }
      if (!this.loginForm.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }
      this.loading = true
      this.$store.dispatch('Login', this.loginForm).then(() => {
        return this.$store.dispatch('GetInfo')
      }).then(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }).catch(err => {
        this.getCaptcha()
        this.loginForm.code = ''
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f0f2f5;
}

.header-bg {
  height: 480rpx;
  background: linear-gradient(135deg, #4a3b8f 0%, #6c5bb3 50%, #9b7fd4 100%);
  border-radius: 0 0 60rpx 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  .header-content {
    text-align: center;

    .app-title {
      display: block;
      font-size: 48rpx;
      font-weight: 700;
      color: #fff;
      margin-bottom: 16rpx;
    }

    .app-subtitle {
      display: block;
      font-size: 28rpx;
      color: rgba(255,255,255,0.75);
    }
  }
}

.login-card {
  margin: -120rpx 40rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.08);

  .card-title {
    font-size: 36rpx;
    font-weight: 700;
    color: #303133;
    text-align: center;
    margin-bottom: 50rpx;
  }
}

.form-item {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 96rpx;
  margin-bottom: 28rpx;

  .input-icon {
    font-size: 36rpx;
    margin-right: 16rpx;
    flex-shrink: 0;
  }

  .form-input {
    flex: 1;
    height: 96rpx;
    font-size: 30rpx;
    color: #303133;
  }
}

.captcha-row {
  .captcha-input {
    flex: 1;
  }

  .captcha-image {
    width: 220rpx;
    height: 70rpx;
    margin-left: 16rpx;
    border-radius: 8rpx;
    background: #eee;
    flex-shrink: 0;
  }
}

.login-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #6c5bb3, #9b7fd4);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 16rpx;
  border: none;
  margin-top: 20rpx;

  &::after {
    border: none;
  }
}

.login-tip {
  text-align: center;
  margin-top: 30rpx;
  font-size: 24rpx;
  color: #909399;
}

// 服务器切换
.server-switch {
  display: flex;
  align-items: center;
  background: #e8f4fd;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 20rpx;
  
  .server-label {
    font-size: 28rpx;
    color: #409eff;
    margin-right: 16rpx;
    flex-shrink: 0;
  }
  
  .server-picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 26rpx;
    color: #303133;
    font-weight: 500;
    
    .picker-arrow {
      color: #909399;
      font-size: 20rpx;
    }
  }
}

.server-tip {
  text-align: right;
  margin-bottom: 20rpx;
  font-size: 24rpx;
  color: #409eff;
  text-decoration: underline;
  padding: 8rpx 0;
}

.footer {
  text-align: center;
  margin-top: 80rpx;
  font-size: 24rpx;
  color: #c0c4cc;
}
</style>
