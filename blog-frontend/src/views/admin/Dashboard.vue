<template>
  <div class="dashboard">
    <a-spin :spinning="loading" tip="加载中...">
      <!-- 统计卡片 -->
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card stat-card-blue">
            <a-statistic
              title="文章总数"
              :value="statistics.articleCount"
            >
              <template #prefix>
                <FileTextOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card stat-card-green">
            <a-statistic
              title="用户总数"
              :value="statistics.userCount"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card stat-card-orange">
            <a-statistic
              title="评论总数"
              :value="statistics.commentCount"
            >
              <template #prefix>
                <CommentOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="12" :lg="6">
          <a-card class="stat-card stat-card-red">
            <a-statistic
              title="总浏览量"
              :value="statistics.totalViews"
            >
              <template #prefix>
                <EyeOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <!-- 图表区域 -->
      <a-row :gutter="[16, 16]" style="margin-top: 16px; flex: 1;">
        <a-col :xs="24" :lg="12" style="height: 100%; display: flex;">
          <a-card title="文章分类统计" :bordered="false" class="chart-card" style="flex: 1;">
            <div ref="categoryChartRef" class="chart-container"></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12" style="height: 100%; display: flex;">
          <a-card title="标签文章统计" :bordered="false" class="chart-card" style="flex: 1;">
            <div ref="tagChartRef" class="chart-container"></div>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getStatistics } from '@/api/statistics'
import { getCategoryList, getTagList } from '@/api/article'
import {
  FileTextOutlined,
  UserOutlined,
  CommentOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'

const categoryChartRef = ref(null)
const tagChartRef = ref(null)

const loading = ref(false)

const statistics = ref({
  articleCount: 0,
  userCount: 0,
  commentCount: 0,
  totalViews: 0
})

const categories = ref([])
const tags = ref([])

// 初始化分类分布图表
const initCategoryChart = () => {
  if (!categories.value || categories.value.length === 0) {
    return
  }
  
  const chart = echarts.init(categoryChartRef.value)
  // 过滤掉文章数为 0 的分类
  const filteredCategories = categories.value.filter(cat => (cat.articleCount || 0) > 0)
  const chartData = filteredCategories.map(cat => ({
    value: cat.articleCount || 0,
    name: cat.name
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        color: '#666'
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData,
        color: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 初始化标签柱状图
const initTagChart = () => {
  if (!tags.value || tags.value.length === 0) {
    return
  }
  
  const chart = echarts.init(tagChartRef.value)
  // 过滤掉文章数为 0 的标签
  const filteredTags = tags.value.filter(tag => (tag.articleCount || 0) > 0)
  const tagNames = filteredTags.map(tag => tag.name)
  const tagCounts = filteredTags.map(tag => tag.articleCount || 0)
  
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
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: tagNames,
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666',
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666',
        formatter: '{value}'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        data: tagCounts,
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        },
        barWidth: '60%'
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
    initCategoryChart()
  } catch (error) {
    console.error('加载分类数据失败:', error)
  }
}

// 加载标签数据
const loadTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
    initTagChart()
  } catch (error) {
    console.error('加载标签数据失败:', error)
  }
}

onMounted(() => {
  loadStatistics()
  loadCategories()
  loadTags()
})
</script>

<style scoped lang="scss">
.dashboard {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.ant-spin-nested-loading),
  :deep(.ant-spin-container) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  .stat-card {
    border-radius: 8px;
    
    &.stat-card-blue {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      :deep(.ant-statistic) {
        .ant-statistic-title,
        .ant-statistic-content {
          color: #fff;
        }
      }
    }
    
    &.stat-card-green {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      
      :deep(.ant-statistic) {
        .ant-statistic-title,
        .ant-statistic-content {
          color: #fff;
        }
      }
    }
    
    &.stat-card-orange {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      
      :deep(.ant-statistic) {
        .ant-statistic-title,
        .ant-statistic-content {
          color: #fff;
        }
      }
    }
    
    &.stat-card-red {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      
      :deep(.ant-statistic) {
        .ant-statistic-title,
        .ant-statistic-content {
          color: #fff;
        }
      }
    }
  }
  
  .chart-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    :deep(.ant-card-head) {
      flex-shrink: 0;
    }
    
    :deep(.ant-card-body) {
      flex: 1;
      padding: 16px;
      overflow: hidden;
    }
  }
  
  .chart-container {
    width: 100%;
    height: 100%;
    min-height: 300px;
  }
}
</style>
