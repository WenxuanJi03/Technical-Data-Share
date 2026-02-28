<template>
  <div class="dashboard-container">
    <!-- 顶部标题 -->
    <div class="dashboard-header">
      <h1>技术部数据分析看板</h1>
      <p>基于OE试制跟踪表和产品清单的数据可视化展示</p>
    </div>

    <!-- OE试制跟踪表统计 -->
    <div class="section-title">
      <i class="el-icon-data-analysis"></i>
      OE试制跟踪表统计
    </div>
    
    <el-row :gutter="20" style="margin-bottom: 30px;">
      <!-- 模具类型 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">模具类型分布</span>
            <span class="card-total">总数: {{ trialTrackStats.moldTypeTotal || 0 }}</span>
          </div>
          <div ref="moldTypeChart" class="chart-content"></div>
        </div>
      </el-col>

      <!-- 表面状态 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">表面状态分布</span>
            <span class="card-total">总数: {{ trialTrackStats.surfaceStatusTotal || 0 }}</span>
          </div>
          <div ref="surfaceStatusChart" class="chart-content"></div>
        </div>
      </el-col>

      <!-- 上机类型 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">上机类型数量</span>
            <span class="card-total">总数: {{ trialTrackStats.machineTypeTotal || 0 }}</span>
          </div>
          <div ref="machineTypeChart" class="chart-content"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 产品清单统计 -->
    <div class="section-title">
      <i class="el-icon-s-data"></i>
      产品清单统计
    </div>

    <!-- 产品状态 - 单独一行 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">产品状态分布</span>
            <span class="card-total">总数: {{ productStats.total || 0 }}</span>
          </div>
          <div ref="productStatusChart" class="chart-content"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 客户分布 - 占整行,更大的空间 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <div class="chart-card chart-card-xlarge">
          <div class="card-header">
            <span class="card-title">客户分布统计</span>
            <span class="card-total">客户种类总数: {{ productStats.customerTotal || 0 }}</span>
          </div>
          <div ref="customerChart" class="chart-content-xlarge"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 规格分布 - 占整行 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <div class="chart-card chart-card-large">
          <div class="card-header">
            <span class="card-title">轮毂规格数量统计</span>
            <span class="card-total">规格种类总数: {{ productStats.sizeSpecTotal || 0 }} (不含数量为1的规格)</span>
          </div>
          <div ref="sizeSpecChart" class="chart-content-large"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 数量为1的规格列表 -->
    <el-row :gutter="20" v-if="singleCountSpecs.length > 0">
      <el-col :span="24">
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">数量为1的规格列表</span>
            <span class="card-total">共 {{ singleCountSpecs.length }} 种规格</span>
          </div>
          <div class="specs-list">
            <el-tag 
              v-for="spec in singleCountSpecs" 
              :key="spec.name"
              size="medium"
              effect="plain"
              style="margin: 4px 8px;"
            >
              {{ spec.name }}
            </el-tag>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getAllStats } from '@/api/tech/dashboard'

export default {
  name: "Index",
  data() {
    return {
      trialTrackStats: {},
      productStats: {},
      charts: [],
      singleCountSpecs: [] // 数量为1的规格列表
    }
  },
  mounted() {
    this.loadData()
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 销毁所有图表
    this.charts.forEach(chart => {
      if (chart) {
        chart.dispose()
      }
    })
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    loadData() {
      getAllStats().then(response => {
        this.trialTrackStats = response.data.trialTrack || {}
        this.productStats = response.data.product || {}
        
        // 分离数量为1的规格
        if (this.productStats.sizeSpecStats) {
          this.singleCountSpecs = this.productStats.sizeSpecStats.filter(item => item.value === 1)
          // 过滤掉数量为1的,只保留数量大于1的用于图表展示
          this.productStats.sizeSpecStats = this.productStats.sizeSpecStats.filter(item => item.value > 1)
        }
        
        this.$nextTick(() => {
          this.initCharts()
        })
      }).catch(err => {
        console.error('加载数据失败:', err)
        this.$message.error('加载数据失败')
      })
    },

    initCharts() {
      // 模具类型 - 扇形图
      this.initPieChart('moldTypeChart', this.trialTrackStats.moldTypeStats || [], '#5470c6', false)
      
      // 表面状态 - 扇形图
      this.initPieChart('surfaceStatusChart', this.trialTrackStats.surfaceStatusStats || [], '#91cc75', false)
      
      // 上机类型 - 柱状图
      this.initBarChart('machineTypeChart', this.trialTrackStats.machineTypeStats || [], '#fac858', false)
      
      // 产品状态 - 扇形图
      this.initPieChart('productStatusChart', this.productStats.productStatusStats || [], '#ee6666', false)
      
      // 客户分布 - 扇形图(大图)
      this.initPieChart('customerChart', this.productStats.customerStats || [], '#73c0de', true)
      
      // 规格分布 - 柱状图(大图)
      this.initBarChart('sizeSpecChart', this.productStats.sizeSpecStats || [], '#3ba272', true)
    },

    /**
     * 初始化扇形图
     */
    initPieChart(refName, data, color, isLarge = false) {
      const chartDom = this.$refs[refName]
      if (!chartDom) return
      
      const chart = echarts.init(chartDom)
      this.charts.push(chart)
      
      // 大图表配置(客户分布等)
      if (isLarge) {
        // 计算图例列数 - 更大的图例区域
        const legendColumns = data.length > 30 ? 3 : (data.length > 15 ? 2 : 1)
        const legendWidth = legendColumns === 3 ? '35%' : (legendColumns === 2 ? '30%' : '25%')
        const pieCenter = legendColumns === 3 ? '30%' : (legendColumns === 2 ? '33%' : '35%')
        
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)',
            textStyle: {
              fontSize: 16
            },
            confine: true
          },
          legend: {
            type: 'plain',
            orient: 'vertical',
            right: '1%',
            top: '5%',
            bottom: '5%',
            width: legendWidth,
            itemGap: 14,
            itemWidth: 22,
            itemHeight: 16,
            textStyle: {
              fontSize: 15,
              lineHeight: 22,
              rich: {
                name: {
                  fontSize: 15,
                  width: legendColumns === 3 ? 150 : (legendColumns === 2 ? 170 : 210)
                }
              }
            },
            formatter: (name) => {
              const item = data.find(d => d.name === name)
              if (!item) return name
              // 不截断名称,完整显示
              return `{name|${name}: ${item.value}}`
            },
            tooltip: {
              show: true,
              textStyle: {
                fontSize: 14
              }
            }
          },
          grid: {
            left: '3%',
            right: legendWidth,
            containLabel: true
          },
          series: [
            {
              type: 'pie',
              radius: ['38%', '70%'],
              center: [pieCenter, '50%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 14,
                borderColor: '#fff',
                borderWidth: 3
              },
              label: {
                show: true,
                position: 'outside',
                fontSize: 14,
                fontWeight: '600',
                rotate: 0,
                align: 'center',
                formatter: (params) => {
                  // 只显示百分比大于2.5%的标签
                  if (params.percent > 2.5) {
                    return `${params.name} ${params.percent}%`
                  }
                  return ''
                },
                overflow: 'none',
                lineHeight: 18
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 20,
                  fontWeight: 'bold',
                  rotate: 0,
                  formatter: '{b}\n{c} ({d}%)'
                },
                itemStyle: {
                  shadowBlur: 18,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                },
                scaleSize: 12
              },
              labelLine: {
                show: true,
                length: 25,
                length2: 18,
                smooth: true,
                lineStyle: {
                  width: 2
                }
              },
              data: data.map(item => ({
                name: item.name,
                value: item.value
              }))
            }
          ],
          color: this.generateColors(color, data.length)
        }
        chart.setOption(option)
      } else {
        // 小图表配置(原有的)
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)',
            confine: true
          },
          legend: {
            orient: 'vertical',
            left: 'right',
            top: 'center',
            textStyle: {
              fontSize: 12
            },
            formatter: (name) => {
              const item = data.find(d => d.name === name)
              return item ? `${name}: ${item.value}` : name
            },
            tooltip: {
              show: true
            }
          },
          series: [
            {
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['40%', '50%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 8,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 14,
                  fontWeight: 'bold',
                  formatter: '{b}\n{c} ({d}%)'
                },
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              },
              labelLine: {
                show: false
              },
              data: data.map(item => ({
                name: item.name,
                value: item.value
              }))
            }
          ],
          color: this.generateColors(color, data.length)
        }
        chart.setOption(option)
      }
    },

    /**
     * 初始化柱状图
     */
    initBarChart(refName, data, color, isLarge = false) {
      const chartDom = this.$refs[refName]
      if (!chartDom) return
      
      const chart = echarts.init(chartDom)
      this.charts.push(chart)
      
      // 大图表配置(规格分布等)
      if (isLarge) {
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            textStyle: {
              fontSize: 14
            },
            formatter: (params) => {
              const param = params[0]
              return `${param.name}<br/>数量: ${param.value}`
            }
          },
          grid: {
            left: '3%',
            right: '3%',
            bottom: '10%',
            top: '8%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.map(item => item.name),
            axisLabel: {
              interval: 0,
              rotate: 0, // 不旋转,横向显示
              fontSize: 12,
              color: '#606266',
              formatter: (value) => {
                // 如果标签太长,进行截断
                return value.length > 8 ? value.substring(0, 8) + '...' : value
              }
            },
            axisLine: {
              lineStyle: {
                color: '#dcdfe6'
              }
            },
            axisTick: {
              alignWithLabel: true
            }
          },
          yAxis: {
            type: 'value',
            minInterval: 1,
            name: '数量',
            nameTextStyle: {
              fontSize: 13,
              color: '#606266',
              padding: [0, 0, 0, 10]
            },
            axisLabel: {
              fontSize: 12,
              color: '#606266'
            },
            splitLine: {
              lineStyle: {
                type: 'dashed',
                color: '#e4e7ed'
              }
            }
          },
          series: [
            {
              type: 'bar',
              data: data.map(item => item.value),
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: color },
                  { offset: 1, color: color + '80' }
                ]),
                borderRadius: [8, 8, 0, 0]
              },
              label: {
                show: true,
                position: 'top',
                fontSize: 13,
                fontWeight: 'bold',
                color: '#303133'
              },
              barMaxWidth: data.length > 20 ? 35 : 50,
              emphasis: {
                itemStyle: {
                  color: color,
                  shadowBlur: 10,
                  shadowColor: 'rgba(0, 0, 0, 0.3)'
                }
              }
            }
          ]
        }
        chart.setOption(option)
      } else {
        // 小图表配置(原有的)
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.map(item => item.name),
            axisLabel: {
              interval: 0,
              rotate: data.length > 6 ? 45 : 0,
              fontSize: 11
            }
          },
          yAxis: {
            type: 'value',
            minInterval: 1
          },
          series: [
            {
              type: 'bar',
              data: data.map(item => item.value),
              itemStyle: {
                color: color,
                borderRadius: [6, 6, 0, 0]
              },
              label: {
                show: true,
                position: 'top',
                fontSize: 12,
                fontWeight: 'bold'
              },
              barMaxWidth: 50
            }
          ]
        }
        chart.setOption(option)
      }
    },

    /**
     * 生成渐变色系
     */
    generateColors(baseColor, count) {
      const colors = [
        ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'],
        ['#4992ff', '#7cffb2', '#fddd60', '#ff6e76', '#58d9f9', '#05c091', '#ff8a45', '#8d48e3']
      ]
      
      // 根据baseColor选择色系
      if (baseColor === '#5470c6') return colors[0]
      if (baseColor === '#91cc75') return colors[1]
      if (baseColor === '#fac858') return colors[0].slice(2)
      if (baseColor === '#ee6666') return colors[1].slice(3)
      if (baseColor === '#73c0de') return colors[0].slice(4)
      if (baseColor === '#3ba272') return colors[1].slice(5)
      
      return colors[0]
    },

    /**
     * 窗口大小变化时重绘图表
     */
    handleResize() {
      this.charts.forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 25px 30px;
  margin-bottom: 25px;
  text-align: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  h1 {
    margin: 0 0 8px 0;
    font-size: 28px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;

  i {
    margin-right: 8px;
    color: #409eff;
  }
}

.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 18px 20px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .card-total {
    font-size: 13px;
    color: #909399;
    background: #f4f4f5;
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.chart-content {
  height: 300px;
  width: 100%;
}

.chart-content-large {
  height: 450px;
  width: 100%;
}

.chart-content-xlarge {
  height: 750px;
  width: 100%;
}

.chart-card-large {
  min-height: 520px;
}

.chart-card-xlarge {
  min-height: 820px;
}

.specs-list {
  padding: 16px;
  line-height: 2;
  max-height: 300px;
  overflow-y: auto;
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard-header {
    padding: 20px;

    h1 {
      font-size: 22px;
    }

    p {
      font-size: 13px;
    }
  }

  .section-title {
    font-size: 16px;
  }

  .chart-content {
    height: 250px;
  }

  .chart-content-large {
    height: 350px;
  }

  .chart-content-xlarge {
    height: 550px;
  }
  
  .chart-card-large {
    min-height: 420px;
  }

  .chart-card-xlarge {
    min-height: 620px;
  }

  .specs-list {
    max-height: 200px;
  }
}
</style>
