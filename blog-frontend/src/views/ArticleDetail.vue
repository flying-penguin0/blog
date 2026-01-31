<template>
  <div class="article-detail-page">
    <Header />

    <main class="main">
      <div class="container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="article" class="content-wrapper">
          <div class="main-content">
            <article class="article-content">
              <h1 class="title">{{ article.title }}</h1>
              <div class="meta">
                <span>作者：{{ article.authorName }}</span>
                <span>发布时间：{{ formatDate(article.publishTime || article.createTime) }}</span>
                <span>👁 {{ article.viewCount }}</span>
                <span>💬 {{ article.commentCount }}</span>
                <el-button 
                  v-if="canEdit" 
                  type="primary" 
                  size="small" 
                  @click="editArticle"
                  style="margin-left: 20px"
                >
                  编辑文章
                </el-button>
                <el-button 
                  v-if="canEdit" 
                  type="danger" 
                  size="small" 
                  @click="deleteArticle"
                >
                  删除文章
                </el-button>
              </div>
              <div class="tags" v-if="article.tags && article.tags.length > 0">
                <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="primary">
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="summary" v-if="article.summary">
                <div class="summary-label">📝 文章摘要</div>
                <p class="summary-text">{{ article.summary }}</p>
              </div>
              <MdPreview 
                :model-value="article.content"
                :preview-theme="'github'"
                :code-theme="'github'"
              />
            </article>

            <!-- 评论区 -->
            <CommentSection :article-id="Number(route.params.id)" />
          </div>

          <aside class="sidebar">
            <div class="card author-card">
              <div class="author-info">
                <el-avatar :size="70" :src="article.authorAvatar || '/default-avatar.png'" />
                <h3 class="author-name">{{ article.authorName }}</h3>
                <p class="author-desc">{{ authorSignature }}</p>
              </div>
              
              <div class="author-stats">
                <div class="stat-item">
                  <div class="stat-number">{{ authorStats.articleCount }}</div>
                  <div class="stat-label">文章数</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-number">{{ authorStats.viewCount }}</div>
                  <div class="stat-label">总浏览量</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-number">{{ authorStats.commentCount }}</div>
                  <div class="stat-label">总评论数</div>
                </div>
              </div>
            </div>

            <!-- 文章目录 -->
            <div class="card toc-card">
              <div class="toc-header">
                <el-icon><List /></el-icon>
                <span>文章目录</span>
              </div>
              <div v-if="tocItems.length > 0" class="toc-content">
                <div
                  v-for="(item, index) in tocItems"
                  :key="index"
                  :class="['toc-item', `toc-level-${item.level}`, { active: activeHeading === item.id }]"
                  @click="scrollToHeading(item.id)"
                >
                  <span class="toc-text">{{ item.text }}</span>
                </div>
              </div>
              <div v-else class="toc-empty">
                <p>文章暂无目录</p>
              </div>
              <div class="reading-progress">
                <div class="progress-label">阅读进度</div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: readingProgress + '%' }"></div>
                </div>
                <div class="progress-text">{{ readingProgress }}%</div>
              </div>
            </div>

            <!-- AI 助手 -->
            <ArticleAIChat v-if="article.content" :article-content="article.content" />
          </aside>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, deleteArticle as deleteArticleApi } from '@/api/article'
import { getUserStats } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import CommentSection from '@/components/CommentSection.vue'
import Header from '@/components/Header.vue'
import ArticleAIChat from '@/components/ArticleAIChat.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const article = ref(null)
const loading = ref(false)

// 作者统计数据
const authorStats = ref({
  articleCount: 0,
  viewCount: 0,
  commentCount: 0
})

// 作者签名
const authorSignature = ref('生活总要活埋了我，不料我是一粒种子')

// 目录相关
const tocItems = ref([])
const activeHeading = ref('')
const readingProgress = ref(0)

// 判断是否可以编辑
const canEdit = computed(() => {
  if (!userStore.user || !article.value) return false
  return userStore.user.id === article.value.userId || userStore.user.role === 'admin'
})

// 提取文章标题生成目录
const generateTOC = () => {
  nextTick(() => {
    // 等待 DOM 完全渲染
    setTimeout(() => {
      const contentEl = document.querySelector('.article-content')
      if (!contentEl) {
        console.log('未找到文章内容容器')
        return
      }
      
      const headings = contentEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
      console.log('找到的标题数量:', headings.length)
      
      const items = []
      
      headings.forEach((heading, index) => {
        const level = parseInt(heading.tagName.substring(1))
        const text = heading.textContent.trim()
        const id = `heading-${index}`
        
        // 给标题添加 id
        heading.id = id
        
        // 跳过文章标题本身（第一个 h1）
        if (index === 0 && level === 1) {
          return
        }
        
        items.push({
          id,
          level,
          text
        })
      })
      
      console.log('生成的目录项:', items)
      tocItems.value = items
    }, 500)
  })
}

// 滚动到指定标题
const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    const offset = 80 // 顶部导航栏高度
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset
    
    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

// 监听滚动，更新当前激活的标题和阅读进度
const handleScroll = () => {
  // 更新阅读进度
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const progress = Math.min(100, Math.round((scrollTop / (documentHeight - windowHeight)) * 100))
  readingProgress.value = progress
  
  // 更新当前激活的标题
  const headings = document.querySelectorAll('.article-content h1, .article-content h2, .article-content h3, .article-content h4, .article-content h5, .article-content h6')
  let currentHeading = ''
  
  headings.forEach((heading) => {
    const rect = heading.getBoundingClientRect()
    if (rect.top <= 100) {
      currentHeading = heading.id
    }
  })
  
  if (currentHeading !== activeHeading.value) {
    activeHeading.value = currentHeading
    
    // 自动滚动目录到当前激活项
    nextTick(() => {
      const tocContent = document.querySelector('.toc-content')
      const activeItem = document.querySelector('.toc-item.active')
      
      if (tocContent && activeItem) {
        const tocRect = tocContent.getBoundingClientRect()
        const itemRect = activeItem.getBoundingClientRect()
        
        // 计算需要滚动的距离，让激活项居中显示
        const scrollOffset = activeItem.offsetTop - tocContent.offsetTop - (tocContent.clientHeight / 2) + (activeItem.clientHeight / 2)
        
        tocContent.scrollTo({
          top: scrollOffset,
          behavior: 'smooth'
        })
      }
    })
  }
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
    
    // 获取作者统计数据
    if (article.value.userId) {
      try {
        const statsRes = await getUserStats(article.value.userId)
        authorStats.value = statsRes.data
        // 设置作者签名，如果没有则使用默认值
        authorSignature.value = statsRes.data.signature || '生活总要活埋了我，不料我是一粒种子'
      } catch (error) {
        console.error('获取作者统计失败:', error)
        // 使用默认值
        authorStats.value = {
          articleCount: 0,
          viewCount: 0,
          commentCount: 0
        }
      }
    }
    
    // 生成目录
    generateTOC()
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 编辑文章
const editArticle = () => {
  router.push(`/admin/editor?id=${article.value.id}`)
}

// 删除文章
const deleteArticle = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteArticleApi(article.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
    }
  }
}

// 监听文章内容变化，重新生成目录
watch(() => article.value?.content, () => {
  generateTOC()
})

onMounted(() => {
  fetchArticle()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
.article-detail-page {
  min-height: 100vh;
  background: #e8ecf1;
}

.main {
  padding: 30px 0;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 30px;
  align-items: start;
}

.main-content {
  min-width: 0;
}

.article-content {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  margin-bottom: 30px;
  position: relative;
  z-index: 1;
  
  .title {
    font-size: 32px;
    margin-bottom: 20px;
    color: #333;
  }
  
  .meta {
    display: flex;
    gap: 20px;
    color: #999;
    font-size: 14px;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;
    flex-wrap: wrap;
    align-items: center;
  }
  
  .tags {
    margin-bottom: 30px;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .summary {
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
    border-left: 4px solid #409eff;
    border-radius: 8px;
    padding: 20px 24px;
    margin-bottom: 30px;
    
    .summary-label {
      font-size: 15px;
      font-weight: 600;
      color: #409eff;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    
    .summary-text {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      margin: 0;
      text-indent: 2em;
    }
  }
  
  // 修复代码块重影问题
  :deep(pre) {
    position: relative;
    z-index: 1;
    
    code {
      display: block;
      background: transparent !important;
    }
  }
  
  :deep(.md-editor-preview-wrapper) {
    position: relative;
    z-index: 1;
    
    pre {
      background: #f6f8fa;
      position: relative;
      z-index: 1;
      
      code {
        background: transparent !important;
      }
    }
  }
}

.sidebar {
  position: sticky;
  top: 70px;
  
  .card {
    background: #fff;
    border-radius: 8px;
    padding: 20px 16px;
    margin-bottom: 16px;
  }
  
  .author-card {
    text-align: center;
    padding: 16px 14px !important;
    
    .author-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding-bottom: 14px;
      border-bottom: 1px solid #f0f0f0;
      
      .author-name {
        font-size: 16px;
        font-weight: 600;
        margin: 6px 0 3px;
        color: #333;
      }
      
      .author-desc {
        font-size: 12px;
        color: #999;
        margin: 0;
        line-height: 1.4;
      }
    }
    
    .author-stats {
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding-top: 14px;
      
      .stat-item {
        flex: 1;
        text-align: center;
        
        .stat-number {
          font-size: 18px;
          font-weight: 600;
          color: #333;
          margin-bottom: 3px;
        }
        
        .stat-label {
          font-size: 11px;
          color: #999;
        }
      }
      
      .stat-divider {
        width: 1px;
        height: 28px;
        background: #e4e7ed;
      }
    }
  }
  
  .toc-card {
    padding: 16px 12px !important;
    
    .toc-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f0f0f0;
      
      .el-icon {
        font-size: 18px;
      }
    }
    
    .toc-content {
      max-height: 200px;
      overflow-y: auto;
      margin-bottom: 16px;
      
      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #dcdfe6;
        border-radius: 2px;
      }
      
      .toc-item {
        padding: 6px 10px;
        cursor: pointer;
        transition: all 0.3s;
        border-radius: 6px;
        margin-bottom: 3px;
        
        &:hover {
          background: #f5f7fa;
          
          .toc-text {
            color: #409eff;
          }
        }
        
        &.active {
          background: #e8f4ff;
          
          .toc-text {
            color: #409eff;
            font-weight: 500;
          }
        }
        
        .toc-text {
          font-size: 13px;
          color: #606266;
          line-height: 1.6;
          display: block;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        &.toc-level-1 {
          padding-left: 10px;
          font-weight: 500;
        }
        
        &.toc-level-2 {
          padding-left: 20px;
        }
        
        &.toc-level-3 {
          padding-left: 30px;
          font-size: 12px;
        }
        
        &.toc-level-4 {
          padding-left: 40px;
          font-size: 12px;
        }
        
        &.toc-level-5,
        &.toc-level-6 {
          padding-left: 50px;
          font-size: 11px;
        }
      }
    }
    
    .toc-empty {
      text-align: center;
      padding: 30px 16px;
      color: #999;
      font-size: 13px;
    }
    
    .reading-progress {
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      
      .progress-label {
        font-size: 13px;
        color: #606266;
        margin-bottom: 8px;
        font-weight: 500;
      }
      
      .progress-bar {
        height: 6px;
        background: #f0f2f5;
        border-radius: 3px;
        overflow: hidden;
        margin-bottom: 6px;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #42d392 0%, #647eff 100%);
          border-radius: 3px;
          transition: width 0.3s ease;
        }
      }
      
      .progress-text {
        text-align: right;
        font-size: 16px;
        font-weight: 600;
        color: #409eff;
      }
    }
  }
}

.loading {
  text-align: center;
  padding: 100px;
  color: #999;
}

@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .article-content {
    padding: 20px;
    
    .title {
      font-size: 24px;
    }
  }
}
</style>
