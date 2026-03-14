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

    <!-- 客户分布 + 产品状态 - 并排显示 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
        <div class="chart-card chart-card-medium">
          <div class="card-header">
            <span class="card-title">客户分布统计</span>
            <span class="card-total">客户种类总数: {{ productStats.customerTotal || 0 }}</span>
          </div>
          <div ref="customerChart" class="chart-content-medium"></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
        <div class="chart-card chart-card-medium">
          <div class="card-header">
            <span class="card-title">产品状态分布</span>
            <span class="card-total">总数: {{ productStats.total || 0 }}</span>
          </div>
          <div ref="productStatusChart" class="chart-content-medium"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 规格分布 + 数量为1的规格 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <div class="chart-card chart-card-large">
          <div class="card-header">
            <span class="card-title">轮毂规格数量统计</span>
            <span class="card-total">规格种类总数: {{ productStats.sizeSpecTotal || 0 }} (不含数量为1的规格)</span>
          </div>
          <div ref="sizeSpecChart" class="chart-content-large"></div>
          <div v-if="singleCountSpecs.length > 0" class="single-specs-section">
            <div class="single-specs-header">
              数量为1的规格列表 (共 {{ singleCountSpecs.length }} 种)
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
      singleCountSpecs: []
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
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
        
        if (this.productStats.sizeSpecStats) {
          this.singleCountSpecs = this.productStats.sizeSpecStats.filter(item => item.value === 1)
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
      
      // 规格分布 - 堆叠柱状图(按轮毂半径和轮辋宽度)
      this.initStackedBarChart('sizeSpecChart', this.productStats.sizeSpecStats || [])
    },

    /**
     * 初始化扇形图
     */
    initPieChart(refName, data, color, isLarge = false) {
      const chartDom = this.$refs[refName]
      if (!chartDom) return
      
      const chart = echarts.init(chartDom)
      this.charts.push(chart)
      
      if (isLarge) {
        // 客户分布统计 - 标签融入饼图，小占比项悬浮显示
        const total = data.reduce((sum, item) => sum + item.value, 0)
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
            show: false
          },
          series: [
            {
              type: 'pie',
              radius: ['25%', '55%'],
              center: ['50%', '50%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 14,
                borderColor: '#fff',
                borderWidth: 3
              },
              label: {
                show: true,
                position: 'outside',
                fontSize: 13,
                fontWeight: '500',
                formatter: (params) => {
                  if (params.percent > 3) {
                    return `${params.name}: ${params.value}\n(${params.percent}%)`
                  }
                  return ''
                },
                overflow: 'none',
                lineHeight: 18
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 18,
                  fontWeight: 'bold',
                  formatter: '{b}: {c}\n({d}%)'
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
                length2: 20,
                smooth: true,
                lineStyle: {
                  width: 2
                }
              },
              data: data.map(item => ({
                name: item.name,
                value: item.value,
                label: {
                  show: (item.value / total * 100) > 3
                }
              }))
            }
          ],
          color: this.generateColors(color, data.length)
        }
        chart.setOption(option)
      } else {
        // 小图表 - 数据标签直接显示在饼图上
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)',
            confine: true
          },
          legend: {
            show: false
          },
          series: [
            {
              type: 'pie',
              radius: ['28%', '55%'],
              center: ['50%', '50%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 8,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: true,
                position: 'outside',
                fontSize: 12,
                fontWeight: '500',
                formatter: '{b}: {c}\n({d}%)',
                overflow: 'none',
                lineHeight: 16
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 14,
                  fontWeight: 'bold',
                  formatter: '{b}: {c}\n({d}%)'
                },
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              },
              labelLine: {
                show: true,
                length: 20,
                length2: 0,
                smooth: false,
                lineStyle: {
                  width: 1.5
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
              rotate: 0,
              fontSize: 12,
              color: '#606266',
              formatter: (value) => {
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
     * 初始化堆叠柱状图(轮毂规格按半径和轮辋宽度分组)
     */
    initStackedBarChart(refName, data) {
      const chartDom = this.$refs[refName]
      if (!chartDom) return

      const chart = echarts.init(chartDom)
      this.charts.push(chart)

      const parsed = []
      data.forEach(item => {
        const name = String(item.name).trim()
        if (name.length >= 4) {
          parsed.push({
            radius: name.substring(0, 2),
            width: name.substring(2),
            count: item.value
          })
        }
      })

      const radiusMap = {}
      parsed.forEach(item => {
        if (!radiusMap[item.radius]) {
          radiusMap[item.radius] = {}
        }
        radiusMap[item.radius][item.width] =
          (radiusMap[item.radius][item.width] || 0) + item.count
      })

      const radii = Object.keys(radiusMap).sort()
      const allWidths = [...new Set(parsed.map(p => p.width))].sort()

      const radiusTotals = {}
      radii.forEach(r => {
        radiusTotals[r] = Object.values(radiusMap[r]).reduce((a, b) => a + b, 0)
      })

      const colorPalette = [
        '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
        '#3ba272', '#fc8452', '#9a60b4', '#4992ff', '#7cffb2',
        '#fddd60', '#ff6e76', '#58d9f9', '#05c091', '#ff8a45',
        '#8d48e3', '#ea7ccc', '#48b8d0', '#c4b5fd', '#f97316'
      ]

      const series = allWidths.map((w, idx) => ({
        name: w,
        type: 'bar',
        stack: 'total',
        barMaxWidth: radii.length > 15 ? 40 : 60,
        itemStyle: {
          color: colorPalette[idx % colorPalette.length]
        },
        label: {
          show: true,
          position: 'inside',
          fontSize: 11,
          color: '#303133',
          fontWeight: 'bold',
          formatter: (params) => {
            const count = params.value
            if (count === 0) return ''
            const r = radii[params.dataIndex]
            const total = radiusTotals[r]
            const percent = ((count / total) * 100).toFixed(1)
            if (count / total < 0.08) return ''
            return `${w}: ${percent}%, ${count}`
          }
        },
        emphasis: {
          label: {
            fontSize: 13,
            fontWeight: 'bold',
            formatter: (params) => {
              const count = params.value
              if (count === 0) return ''
              const r = radii[params.dataIndex]
              const total = radiusTotals[r]
              const percent = ((count / total) * 100).toFixed(1)
              return `${w}: ${percent}%, ${count}`
            }
          },
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        data: radii.map(r => radiusMap[r][w] || 0)
      }))

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          textStyle: { fontSize: 14 },
          formatter: (params) => {
            const r = params[0].name
            let html = `<strong>轮毂半径: ${r}</strong><br/>总数: ${radiusTotals[r]}<br/><hr style="margin:4px 0;border-color:#eee"/>`
            params.filter(p => p.value > 0).forEach(p => {
              const percent = ((p.value / radiusTotals[r]) * 100).toFixed(1)
              html += `${p.marker} 轮辋宽度 ${p.seriesName}: ${p.value} (${percent}%)<br/>`
            })
            return html
          }
        },
        legend: {
          type: 'scroll',
          bottom: '0%',
          textStyle: { fontSize: 12 },
          itemWidth: 16,
          itemHeight: 12,
          formatter: (name) => `宽度 ${name}`
        },
        grid: {
          left: '3%',
          right: '3%',
          bottom: '12%',
          top: '8%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: radii,
          name: '轮毂半径',
          nameLocation: 'center',
          nameGap: 35,
          nameTextStyle: {
            fontSize: 14,
            fontWeight: 'bold',
            color: '#303133'
          },
          axisLabel: {
            fontSize: 13,
            color: '#606266',
            fontWeight: 'bold'
          },
          axisLine: {
            lineStyle: { color: '#dcdfe6' }
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
        series: series
      }

      chart.setOption(option)
    },

    /**
     * 生成渐变色系
     */
    generateColors(baseColor, count) {
      const palette = [
        '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
        '#3ba272', '#fc8452', '#9a60b4', '#4992ff', '#7cffb2',
        '#fddd60', '#ff6e76', '#58d9f9', '#05c091', '#ff8a45',
        '#8d48e3', '#ea7ccc', '#48b8d0', '#c4b5fd', '#f97316',
        '#0ea5e9', '#22c55e', '#f43f5e', '#a855f7', '#14b8a6',
        '#e11d48', '#6366f1', '#84cc16', '#f59e0b', '#06b6d4',
        '#d946ef', '#10b981', '#ef4444', '#8b5cf6', '#f472b6'
      ]
      return palette
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
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
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

.chart-content-medium {
  height: 400px;
  width: 100%;
}

.chart-card-large {
  min-height: 520px;
}

.chart-card-xlarge {
  min-height: 820px;
}

.chart-card-medium {
  min-height: 470px;
}

.single-specs-section {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  margin-top: 8px;
}

.single-specs-header {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
  padding-left: 4px;
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

  .chart-content-medium {
    height: 300px;
  }
  
  .chart-card-large {
    min-height: 420px;
  }

  .chart-card-xlarge {
    min-height: 620px;
  }

  .chart-card-medium {
    min-height: 370px;
  }

  .specs-list {
    max-height: 200px;
  }
}
</style>
