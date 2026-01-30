<template>
  <div class="tags-page">
    <Header />

    <main class="main">
      <div class="container">
        <div class="page-header">
          <h1>文章标签</h1>
          <p class="subtitle">通过标签快速找到相关文章</p>
        </div>

        <!-- 标签云 -->
        <div class="tags-cloud-section">
          <div class="tags-cloud">
            <span
              v-for="(tag, index) in tags"
              :key="tag.id"
              class="tag-item"
              :style="getTagStyle(index)"
              @click="goToTagArticles(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>
        </div>

        <!-- 所有标签列表 -->
        <div class="tags-list-section">
          <h2 class="section-title">所有标签</h2>
          <div class="tags-list">
            <div
              v-for="(tag, index) in tags"
              :key="tag.id"
              class="tag-card"
              @click="goToTagArticles(tag.id)"
            >
              <div class="tag-info">
                <span class="tag-dot" :style="{ background: getTagColor(index) }"></span>
                <span class="tag-name">{{ tag.name }}</span>
              </div>
              <span class="tag-date">{{ formatDate(tag.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTagList } from '@/api/article'
import Header from '@/components/Header.vue'
import dayjs from 'dayjs'

const router = useRouter()
const tags = ref([])

// 定义标签颜色数组 - 更柔和暗淡的颜色
const tagColors = [
  '#e74c3c', '#16a085', '#2980b9', '#27ae60', '#f39c12',
  '#95a5a6', '#3498db', '#9b59b6', '#e91e63', '#ff9800',
  '#d35400', '#1abc9c', '#34495e', '#2c3e50', '#8e44ad',
  '#7f8c8d', '#c0392b', '#d35400', '#16a085', '#2980b9',
  '#8e44ad', '#2c3e50', '#c0392b', '#d68910', '#117a65'
]

// 定义标签背景颜色（浅色）
const tagBgColors = [
  'rgba(231, 76, 60, 0.08)', 'rgba(22, 160, 133, 0.08)', 'rgba(41, 128, 185, 0.08)',
  'rgba(39, 174, 96, 0.08)', 'rgba(243, 156, 18, 0.08)', 'rgba(149, 165, 166, 0.08)',
  'rgba(52, 152, 219, 0.08)', 'rgba(155, 89, 182, 0.08)', 'rgba(233, 30, 99, 0.08)',
  'rgba(255, 152, 0, 0.08)', 'rgba(211, 84, 0, 0.08)', 'rgba(26, 188, 156, 0.08)',
  'rgba(52, 73, 94, 0.08)', 'rgba(44, 62, 80, 0.08)', 'rgba(142, 68, 173, 0.08)',
  'rgba(127, 140, 141, 0.08)', 'rgba(192, 57, 43, 0.08)', 'rgba(211, 84, 0, 0.08)',
  'rgba(22, 160, 133, 0.08)', 'rgba(41, 128, 185, 0.08)', 'rgba(142, 68, 173, 0.08)',
  'rgba(44, 62, 80, 0.08)', 'rgba(192, 57, 43, 0.08)', 'rgba(214, 137, 16, 0.08)',
  'rgba(17, 122, 101, 0.08)'
]

const getTagColor = (index) => {
  return tagColors[index % tagColors.length]
}

const getTagStyle = (index) => {
  const color = tagColors[index % tagColors.length]
  const bgColor = tagBgColors[index % tagBgColors.length]
  return {
    color: color,
    background: bgColor,
    borderColor: color
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY年MM月DD日')
}

const fetchTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

const goToTagArticles = (tagId) => {
  router.push(`/?tagId=${tagId}`)
}

onMounted(() => {
  fetchTags()
})
</script>

<style scoped lang="scss">
.tags-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f0f2f5 0%, #e8ecf1 100%);
}

.main {
  padding: 60px 0 80px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 50px;
  
  h1 {
    font-size: 38px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 12px;
    letter-spacing: 1px;
  }
  
  .subtitle {
    color: #95a5a6;
    font-size: 15px;
    font-weight: 400;
  }
}

// 标签云区域
.tags-cloud-section {
  margin-bottom: 50px;
}

.tags-cloud {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.02);
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1.5px solid;
  white-space: nowrap;
  
  &:hover {
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

// 标签列表区域
.tags-list-section {
  .section-title {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 24px;
    padding-left: 4px;
  }
}

.tags-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.tag-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.02);
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    border-color: rgba(0, 0, 0, 0.05);
  }
  
  .tag-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
  }
  
  .tag-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
  }
  
  .tag-name {
    font-size: 16px;
    font-weight: 500;
    color: #2c3e50;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .tag-date {
    font-size: 13px;
    color: #95a5a6;
    white-space: nowrap;
    margin-left: 12px;
  }
}

@media (max-width: 1024px) {
  .tags-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .main {
    padding: 40px 0 60px;
  }
  
  .page-header {
    margin-bottom: 30px;
    
    h1 {
      font-size: 30px;
    }
    
    .subtitle {
      font-size: 14px;
    }
  }
  
  .tags-cloud {
    padding: 24px 20px;
    gap: 10px;
  }
  
  .tag-item {
    padding: 6px 16px;
    font-size: 13px;
  }
  
  .tags-list-section {
    .section-title {
      font-size: 20px;
      margin-bottom: 20px;
    }
  }
  
  .tags-list {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .tag-card {
    padding: 16px 20px;
    
    .tag-name {
      font-size: 15px;
    }
    
    .tag-date {
      font-size: 12px;
    }
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 16px;
  }
  
  .page-header h1 {
    font-size: 26px;
  }
  
  .tags-cloud {
    padding: 20px 16px;
  }
  
  .tag-card {
    padding: 14px 16px;
    
    .tag-info {
      gap: 10px;
    }
    
    .tag-dot {
      width: 8px;
      height: 8px;
    }
  }
}
</style>
