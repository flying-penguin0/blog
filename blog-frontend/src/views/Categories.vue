<template>
  <div class="categories-page">
    <Header />

    <main class="main">
      <div class="container">
        <div class="page-header">
          <h1>文章分类</h1>
          <p class="subtitle">共 {{ categories.length }} 个分类</p>
        </div>

        <div class="categories-grid">
          <div 
            v-for="(category, index) in categories" 
            :key="category.id"
            class="category-card"
            @click="goToCategoryArticles(category.id)"
          >
            <div class="card-content">
              <div class="card-left">
                <h3 class="category-name">{{ category.name }}</h3>
                <p class="category-desc" v-if="category.description">{{ category.description }}</p>
                <p class="category-desc" v-else>{{ getDefaultDescription(category.name) }}</p>
                <div class="category-footer">
                  <span class="article-count">{{ category.articleCount }} 篇文章</span>
                  <span class="view-more">查看更多 →</span>
                </div>
              </div>
              <div class="card-right">
                <div class="category-icon" :style="{ background: getIconColor(index) }">
                  <el-icon><FolderOpened /></el-icon>
                </div>
              </div>
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
import { getCategoryList } from '@/api/article'
import Header from '@/components/Header.vue'

const router = useRouter()
const categories = ref([])

// 定义图标颜色数组 - 更暗的渐变色
const iconColors = [
  'linear-gradient(135deg, #5f4b8b 0%, #5a3d7a 100%)', // 深紫色
  'linear-gradient(135deg, #c44569 0%, #b33a5a 100%)', // 深粉红
  'linear-gradient(135deg, #2980b9 0%, #1f6fa8 100%)', // 深蓝色
  'linear-gradient(135deg, #27ae60 0%, #1e8449 100%)', // 深绿色
  'linear-gradient(135deg, #d68910 0%, #b87333 100%)', // 深橙色
  'linear-gradient(135deg, #117a65 0%, #0e6655 100%)', // 深青色
  'linear-gradient(135deg, #5dade2 0%, #3498db 100%)', // 中蓝色
  'linear-gradient(135deg, #8e44ad 0%, #7d3c98 100%)', // 深紫罗兰
  'linear-gradient(135deg, #c0392b 0%, #a93226 100%)', // 深红色
  'linear-gradient(135deg, #d35400 0%, #ba4a00 100%)', // 深橙红
  'linear-gradient(135deg, #16a085 0%, #138d75 100%)', // 深青绿
  'linear-gradient(135deg, #e74c3c 0%, #cb4335 100%)'  // 深朱红
]

const getIconColor = (index) => {
  return iconColors[index % iconColors.length]
}

// 默认描述
const getDefaultDescription = (name) => {
  const descriptions = {
    '算法': '分享提高工作学习效率的一些好物',
    '运维': '运维相关技术',
    '前端': '前端相关技术',
    '后端': '后端相关技术',
    '数据库': '数据库相关知识',
    '框架': '各类开发框架',
    '工具': '开发工具和效率工具'
  }
  return descriptions[name] || `分享关于${name}相关的内容`
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const goToCategoryArticles = (categoryId) => {
  router.push(`/?categoryId=${categoryId}`)
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped lang="scss">
.categories-page {
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

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  max-width: 1140px;
  margin: 0 auto;
}

.category-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.02);
  
  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    border-color: rgba(0, 0, 0, 0.05);
    
    .view-more {
      opacity: 1;
      transform: translateX(4px);
    }
    
    .category-icon {
      transform: scale(1.05);
    }
  }
  
  .card-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
  }
  
  .card-left {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .category-name {
    font-size: 22px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    line-height: 1.3;
  }
  
  .category-desc {
    font-size: 14px;
    color: #7f8c8d;
    margin: 0;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    min-height: 44px;
  }
  
  .category-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8px;
    padding-top: 12px;
    border-top: 1px solid #f0f2f5;
  }
  
  .article-count {
    font-size: 13px;
    color: #95a5a6;
    font-weight: 500;
  }
  
  .view-more {
    font-size: 13px;
    color: #409eff;
    font-weight: 500;
    opacity: 0.7;
    transition: all 0.3s ease;
    white-space: nowrap;
  }
  
  .card-right {
    flex-shrink: 0;
  }
  
  .category-icon {
    width: 68px;
    height: 68px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 34px;
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    
    .el-icon {
      font-size: 34px;
    }
  }
}

@media (max-width: 1024px) {
  .categories-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
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
  
  .categories-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .category-card {
    padding: 24px 20px;
    
    .category-name {
      font-size: 20px;
    }
    
    .category-desc {
      font-size: 13px;
      min-height: 40px;
    }
    
    .category-icon {
      width: 60px;
      height: 60px;
      
      .el-icon {
        font-size: 30px;
      }
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
  
  .category-card {
    padding: 20px 16px;
    
    .card-content {
      gap: 16px;
    }
    
    .category-icon {
      width: 56px;
      height: 56px;
      
      .el-icon {
        font-size: 28px;
      }
    }
  }
}
</style>
