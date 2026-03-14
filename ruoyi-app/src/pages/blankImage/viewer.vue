<template>
  <view class="vpage">
    <!-- 顶部栏 -->
    <view class="vtop" :style="vtopStyle">
      <view class="vback" @tap="goBack">
        <text class="vback-text">✕</text>
      </view>
      <text class="vtitle">{{ pageTitle }}</text>
      <view style="width:64rpx"></view>
    </view>

    <!-- 图片缩放区域，尺寸在 data() 中已同步计算好 -->
    <movable-area :style="{ width: areaW + 'px', height: areaH + 'px', flexShrink: 0 }">
      <movable-view
        direction="all"
        :scale="true"
        :scale-min="1"
        :scale-max="5"
        :style="{ width: areaW + 'px', height: areaH + 'px' }"
      >
        <image
          :src="imageUrl"
          mode="aspectFit"
          :style="vimgStyle"
        />
      </movable-view>
    </movable-area>

    <!-- 旋转按钮工具栏 -->
    <view class="vtoolbar" :style="vtoolbarStyle">
      <view class="vbtn" @tap="rotateImage">
        <text class="vbtn-icon">↻</text>
        <text class="vbtn-text">旋转</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    // 必须在 data() 内同步计算，保证 movable-area 首次渲染尺寸正确
    var sys = uni.getSystemInfoSync()
    var statusH = sys.statusBarHeight || 20
    var safeB = (sys.safeAreaInsets && sys.safeAreaInsets.bottom > 0) ? sys.safeAreaInsets.bottom : 0
    var topH = statusH + 44       // 状态栏 + 导航栏内容区
    var bottomH = 68 + safeB      // 旋转按钮区高度
    var areaW = sys.windowWidth
    var areaH = sys.windowHeight - topH - bottomH
    return {
      statusH: statusH,
      safeB: safeB,
      topH: topH,
      bottomH: bottomH,
      areaW: areaW,
      areaH: areaH,
      imageUrl: '',
      pageTitle: '',
      rotation: 0
    }
  },
  computed: {
    vtopStyle() {
      return 'height:' + this.topH + 'px; padding-top:' + this.statusH + 'px'
    },
    vtoolbarStyle() {
      return 'height:' + this.bottomH + 'px; padding-bottom:' + this.safeB + 'px'
    },
    vimgStyle() {
      return (
        'width:' + this.areaW + 'px;' +
        'height:' + this.areaH + 'px;' +
        'transform:rotate(' + this.rotation + 'deg);' +
        'transform-origin:center center;' +
        'transition:transform 0.3s ease;'
      )
    }
  },
  onLoad(options) {
    this.pageTitle = options.title ? decodeURIComponent(options.title) : ''
    this.imageUrl = uni.getStorageSync('_blankViewerUrl') || ''
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    rotateImage() {
      this.rotation = (this.rotation + 90) % 360
    }
  }
}
</script>

<style lang="scss" scoped>
.vpage {
  height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.vtop {
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-left: 24rpx;
  padding-right: 24rpx;
  padding-bottom: 10px;
  box-sizing: border-box;
  background: rgba(0, 0, 0, 0.85);
  position: relative;
  z-index: 2;
}

.vback {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { background: rgba(255, 255, 255, 0.28); }
}

.vback-text {
  font-size: 28rpx;
  color: #fff;
  line-height: 1;
}

.vtitle {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  max-width: 380rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vtoolbar {
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 12px;
  box-sizing: border-box;
  position: relative;
  z-index: 2;
}

.vbtn {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 18rpx 80rpx;
  border-radius: 44rpx;
  background: rgba(255, 255, 255, 0.18);
  &:active { background: rgba(255, 255, 255, 0.32); }
}

.vbtn-icon {
  font-size: 38rpx;
  color: #fff;
  line-height: 1;
}

.vbtn-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #fff;
}
</style>
