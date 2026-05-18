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
              
              <div class="tags" v-if="article.tags && article.tags.length > 0">
                <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="primary">
                  {{ tag.name }}
                </el-tag>
              </div>
              
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
            <div class="card author-card" :style="{ '--author-card-bg': `url(${authorCardBackground})` }">
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
const authorBackgrounds = [
  new URL('../assets/images/bg1.png', import.meta.url).href,
  new URL('../assets/images/bg2.png', import.meta.url).href,
  new URL('../assets/images/bg3.png', import.meta.url).href,
  new URL('../assets/images/bg4.png', import.meta.url).href
]
const authorCardBackground = ref(authorBackgrounds[Math.floor(Math.random() * authorBackgrounds.length)])

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
    font-weight: 700;
    margin-bottom: 20px;
    color: #333;
  }
  
  .tags {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
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
  
  .summary {
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
    border-left: 4px solid #409eff;
    border-radius: 8px;
    padding: 18px 22px;
    margin-bottom: 16px;
    
    .summary-label {
      font-size: 15px;
      font-weight: 600;
      color: #409eff;
      margin-bottom: 10px;
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
    padding: 0 !important;
    background: transparent;
  }

  :deep(.md-editor-preview) {
    padding: 0 !important;
    background: transparent;

    > *:first-child {
      margin-top: 4px !important;
    }

    h1:first-child,
    h2:first-child,
    h3:first-child,
    h4:first-child,
    h5:first-child,
    h6:first-child {
      margin-top: 4px !important;
    }
  }

  :deep(.md-editor-preview-wrapper > *:first-child) {
    margin-top: 0 !important;
  }

  :deep(.md-editor-preview-wrapper h1:first-child),
  :deep(.md-editor-preview-wrapper h2:first-child),
  :deep(.md-editor-preview-wrapper h3:first-child),
  :deep(.md-editor-preview-wrapper h4:first-child),
  :deep(.md-editor-preview-wrapper h5:first-child),
  :deep(.md-editor-preview-wrapper h6:first-child) {
    margin-top: 4px !important;
  }

  :deep(.md-editor-preview-wrapper p:first-child) {
    margin-top: 0 !important;
  }

  :deep(.md-editor-preview h1),
  :deep(.md-editor-preview h2),
  :deep(.md-editor-preview h3),
  :deep(.md-editor-preview h4),
  :deep(.md-editor-preview h5),
  :deep(.md-editor-preview h6) {
    scroll-margin-top: 96px;
  }

  :deep(.md-editor-preview-wrapper pre) {
    background: #f6f8fa;
    position: relative;
    z-index: 1;
    
    code {
      background: transparent !important;
    }
  }
}

.sidebar {
  position: sticky;
  top: 70px;
  
  .card {
    background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
    border: 1px solid #e7edf5;
    border-radius: 12px;
    padding: 18px 16px;
    margin-bottom: 16px;
    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  }
  
  .author-card {
    position: relative;
    overflow: hidden;
    text-align: center;
    padding: 14px 14px 16px !important;

    &::before {
      content: '';
      position: absolute;
      inset: 0 0 96px 0;
      background:
        linear-gradient(180deg, rgba(255, 255, 255, 0.02) 0%, rgba(255, 255, 255, 0.06) 34%, rgba(255, 255, 255, 0.18) 58%, rgba(255, 255, 255, 0.52) 82%, rgba(255, 255, 255, 0.9) 100%),
        var(--author-card-bg) center/cover no-repeat;
      opacity: 1;
    }

    .author-info {
      position: relative;
      z-index: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 56px 6px 18px;

      :deep(.el-avatar) {
        border: 3px solid rgba(255, 255, 255, 0.92);
        box-shadow: 0 14px 26px rgba(37, 99, 235, 0.18);
      }
      
      .author-name {
        font-size: 16px;
        font-weight: 700;
        margin: 8px 0 2px;
        color: #1f2a37;
      }
      
      .author-desc {
        font-size: 12px;
        color: #66758a;
        margin: 0;
        line-height: 1.5;
      }
    }
    
    .author-stats {
      position: relative;
      z-index: 1;
      display: flex;
      justify-content: space-around;
      align-items: center;
      margin-top: 2px;
      padding-top: 14px;
      background: rgba(247, 250, 253, 0.88);
      border: 1px solid #edf2f7;
      border-radius: 10px;
      padding: 14px 8px 12px;
      
      .stat-item {
        flex: 1;
        text-align: center;
        
        .stat-number {
          font-size: 20px;
          font-weight: 700;
          color: #1f2a37;
          margin-bottom: 4px;
          line-height: 1;
        }
        
        .stat-label {
          font-size: 11px;
          color: #7b8794;
          line-height: 1.35;
        }
      }
      
      .stat-divider {
        width: 1px;
        height: 32px;
        background: linear-gradient(180deg, transparent 0%, #e2e8f0 20%, #e2e8f0 80%, transparent 100%);
      }
    }
  }
  
  .toc-card {
    padding: 14px 12px 12px !important;
    
    .toc-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 17px;
      font-weight: 700;
      color: #1f2a37;
      margin-bottom: 14px;
      padding: 0 4px 12px;
      border-bottom: 1px solid #edf2f7;
      
      .el-icon {
        font-size: 18px;
        color: #4b5563;
        width: 28px;
        height: 28px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        background: linear-gradient(135deg, #f5f9ff 0%, #e8f1ff 100%);
        border: 1px solid #e0ebf8;
        flex-shrink: 0;
      }
    }
    
    .toc-content {
      max-height: 246px;
      overflow-y: auto;
      margin-bottom: 14px;
      padding-right: 2px;
      counter-reset: toc-counter;
      
      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #d9e3ef;
        border-radius: 2px;
      }
      
      .toc-item {
        position: relative;
        display: flex;
        align-items: center;
        gap: 10px;
        min-height: 40px;
        padding: 8px 12px 8px 14px;
        cursor: pointer;
        transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
        border-radius: 10px;
        margin-bottom: 4px;
        counter-increment: toc-counter;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 8px;
          bottom: 8px;
          width: 3px;
          border-radius: 999px;
          background: transparent;
          transition: background 0.2s ease;
        }
        
        &::after {
          content: counter(toc-counter, decimal-leading-zero);
          flex-shrink: 0;
          width: 24px;
          font-size: 11px;
          font-weight: 700;
          color: #9aa6b2;
          line-height: 1;
        }
        
        &:hover {
          background: #f5f9ff;
          transform: translateX(2px);
          
          .toc-text {
            color: #409eff;
          }

          &::after {
            color: #5b8def;
          }
        }
        
        &.active {
          background: linear-gradient(90deg, #eaf4ff 0%, #f4f9ff 100%);
          
          .toc-text {
            color: #2f7fe8;
            font-weight: 600;
          }

          &::before {
            background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
          }

          &::after {
            color: #2f7fe8;
          }
        }
        
        .toc-text {
          font-size: 13px;
          color: #4b5563;
          line-height: 1.5;
          display: block;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        &.toc-level-1 {
          padding-left: 14px;
        }
        
        &.toc-level-2 {
          padding-left: 26px;
        }
        
        &.toc-level-3 {
          padding-left: 38px;
        }
        
        &.toc-level-4 {
          padding-left: 50px;
        }
        
        &.toc-level-5,
        &.toc-level-6 {
          padding-left: 62px;
        }
      }
    }
    
    .toc-empty {
      text-align: center;
      padding: 28px 16px;
      color: #94a3b8;
      font-size: 13px;
    }
    
    .reading-progress {
      padding: 14px 4px 0;
      border-top: 1px solid #edf2f7;
      
      .progress-label {
        font-size: 13px;
        color: #4b5563;
        margin-bottom: 10px;
        font-weight: 600;
      }
      
      .progress-bar {
        height: 8px;
        background: #eef2f7;
        border-radius: 999px;
        overflow: hidden;
        margin-bottom: 10px;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #42d392 0%, #647eff 100%);
          border-radius: 999px;
          transition: width 0.3s ease;
        }
      }
      
      .progress-text {
        text-align: right;
        font-size: 14px;
        font-weight: 600;
        color: #2f7fe8;
        letter-spacing: 0.01em;
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
