<template>
  <div class="scan-page">
    <!-- 顶部栏 -->
    <div class="scan-header">
      <i class="el-icon-arrow-left" @click="$router.back()"></i>
      <span>轮毂识别</span>
      <span style="width: 24px;"></span>
    </div>

    <!-- 预览区域：正方形 + 中间圆形，完整展示轮毂 -->
    <div class="scan-preview">
      <div class="preview-frame">
        <div class="preview-circle">
          <img v-if="previewUrl" :src="previewUrl" class="preview-image" />
          <div v-else class="preview-placeholder">
            <i class="el-icon-camera"></i>
            <p>将轮毂置于圆形区域内</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作区：拍照/上传 -->
    <div class="scan-actions">
      <el-upload
        class="scan-upload"
        :show-file-list="false"
        :auto-upload="false"
        accept="image/*"
        :on-change="handleFileChange"
        capture="camera"
      >
        <el-button type="primary" icon="el-icon-camera" :loading="uploading">
          {{ uploading ? '识别中...' : '拍照 / 选择图片' }}
        </el-button>
      </el-upload>
      <div class="tips">
        建议使用正面清晰图片，避免强烈反光和遮挡。
      </div>
    </div>

    <!-- 识别结果 -->
    <div class="result-section" v-if="results.length">
      <!-- 低置信度警告 -->
      <div class="confidence-warning" v-if="showLowConfidenceWarning">
        <i class="el-icon-warning"></i>
        <div class="warning-content">
          <div class="warning-title">识别置信度较低</div>
          <div class="warning-tips">建议: 1.调整为正面拍摄 2.确保光线充足 3.清洁轮毂表面 4.避免遮挡</div>
        </div>
      </div>

      <div class="result-title">
        识别结果
        <span class="sub">共 {{ results.length }} 条候选</span>
      </div>
      <div class="result-list">
        <div
          v-for="item in results"
          :key="item.productId"
          class="result-item"
          @click="openDetail(item)"
        >
          <div class="result-thumb">
            <img v-if="item.frontImage" :src="baseUrl + item.frontImage" />
            <div v-else class="thumb-placeholder">
              <i class="el-icon-picture-outline"></i>
            </div>
          </div>
          <div class="result-info">
            <div class="result-top">
              <span class="wheel-code">{{ item.wheelCode }}</span>
              <span 
                class="score" 
                :class="getScoreClass(item.similarityScore)"
                v-if="item.similarityScore != null"
              >
                相似度 {{ (item.similarityScore * 100).toFixed(1) }}%
              </span>
            </div>
            <div class="result-row">
              <span class="label">客户</span>
              <span class="value">{{ item.customer || '-' }}</span>
            </div>
            <div class="result-row">
              <span class="label">规格</span>
              <span class="value">{{ item.sizeSpec || '-' }}</span>
              <span class="label" style="margin-left: 8px;">PCD</span>
              <span class="value">{{ item.pcd || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-section" v-else>
      <p>拍照或选择一张轮毂图片开始识别。</p>
    </div>
  </div>
</template>

<script>
import { recognizeProduct } from '@/api/tech/product'

export default {
  name: 'MobileWheelScan',
  data() {
    return {
      previewUrl: '',
      uploading: false,
      results: [],
      baseUrl: process.env.VUE_APP_BASE_API
    }
  },
  computed: {
    showLowConfidenceWarning() {
      if (!this.results.length) return false
      const topScore = this.results[0].similarityScore
      return topScore != null && topScore < 0.40
    }
  },
  methods: {
    getScoreClass(score) {
      if (score == null) return ''
      if (score >= 0.60) return 'score-high'
      if (score >= 0.40) return 'score-medium'
      return 'score-low'
    },
    handleFileChange(file) {
      if (!file || !file.raw) return
      const raw = file.raw
      this.previewUrl = URL.createObjectURL(raw)
      this.uploading = true
      const formData = new FormData()
      formData.append('file', raw)
      recognizeProduct(formData)
        .then(res => {
          this.uploading = false
          if (res.code === 200) {
            this.results = res.data || []
            if (!this.results.length) {
              this.$toast && this.$toast('未识别到相似轮型')
              this.$message && this.$message.info('未识别到相似轮型')
            }
          } else {
            this.$message.error(res.msg || '识别失败')
          }
        })
        .catch(() => {
          this.uploading = false
          this.$message.error('识别失败，请稍后重试')
        })
    },
    openDetail(item) {
      // 与 PC 端保持一致，跳到产品清单详情页（如果后续有独立移动详情也可在此调整）
      this.$router.push({ path: '/tech/product', query: { wheelCode: item.wheelCode } })
    }
  }
}
</script>

<style scoped lang="scss">
.scan-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.scan-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: 600;
  color: #303133;

  i {
    font-size: 20px;
    cursor: pointer;
    color: #606266;
  }
}

.scan-preview {
  padding: 16px;
}

/* 正方形容器 */
.preview-frame {
  width: 100%;
  max-width: 340px;
  aspect-ratio: 1;
  margin: 0 auto;
  background: #f0f2f5;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 中间圆形区域，完整展示轮毂 */
.preview-circle {
  width: 88%;
  height: 88%;
  border-radius: 50%;
  overflow: hidden;
  border: 2px dashed rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-placeholder {
  text-align: center;
  color: #c0c4cc;

  i {
    font-size: 40px;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
  }
}

.scan-actions {
  padding: 0 16px 12px;
  text-align: center;
}

.scan-upload {
  .el-button {
    width: 100%;
    border-radius: 999px;
  }
}

.tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.result-section {
  margin: 0 16px;
}

.result-title {
  margin: 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;

  .sub {
    margin-left: 8px;
    font-size: 12px;
    color: #909399;
    font-weight: normal;
  }
}

.result-list {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.result-item {
  display: flex;
  padding: 10px 12px;
  border-bottom: 1px solid #f2f2f2;
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }
}

.result-thumb {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  background: #f5f7fa;
  overflow: hidden;
  margin-right: 10px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;

  i {
    font-size: 26px;
  }
}

.result-info {
  flex: 1;
  font-size: 12px;
}

.result-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.wheel-code {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.score {
  font-size: 12px;
  font-weight: 600;
}

.score-high {
  color: #67c23a;
}

.score-medium {
  color: #e6a23c;
}

.score-low {
  color: #f56c6c;
}

.confidence-warning {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 12px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;

  .el-icon-warning {
    font-size: 18px;
    color: #f56c6c;
    margin-right: 8px;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .warning-content {
    flex: 1;
  }

  .warning-title {
    font-size: 13px;
    font-weight: 600;
    color: #f56c6c;
    margin-bottom: 4px;
  }

  .warning-tips {
    font-size: 11px;
    color: #909399;
    line-height: 1.4;
  }
}

.result-row {
  margin-top: 2px;
  display: flex;
  align-items: center;
}

.label {
  color: #909399;
  margin-right: 4px;
}

.value {
  color: #606266;
}

.empty-section {
  padding: 40px 16px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}
</style>

