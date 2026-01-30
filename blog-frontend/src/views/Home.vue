<template>
  <div class="home">
    <Header />

    <main class="main">
      <div class="container">
        <div class="content-wrapper">
          <div class="article-list">
            <div class="list-header">
              <h2>最新文章</h2>
              <div class="filters">
                <el-select 
                  v-model="filters.categoryId" 
                  placeholder="选择分类" 
                  clearable
                  @change="handleFilterChange"
                  style="width: 150px; margin-right: 10px"
                >
                  <el-option 
                    v-for="category in categories" 
                    :key="category.id" 
                    :label="category.name" 
                    :value="category.id"
                  />
                </el-select>
                <el-select 
                  v-model="filters.tagId" 
                  placeholder="选择标签" 
                  clearable
                  @change="handleFilterChange"
                  style="width: 150px; margin-right: 10px"
                >
                  <el-option 
                    v-for="tag in tags" 
                    :key="tag.id" 
                    :label="tag.name" 
                    :value="tag.id"
                  />
                </el-select>
                <el-input 
                  v-model="filters.keyword" 
                  placeholder="搜索文章..." 
                  clearable
                  @keyup.enter="handleFilterChange"
                  style="width: 200px"
                >
                  <template #append>
                    <el-button icon="Search" @click="handleFilterChange" />
                  </template>
                </el-input>
              </div>
            </div>
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="articles.length === 0" class="empty">暂无文章</div>
            <div v-else>
              <div 
                v-for="(article, index) in articles" 
                :key="article.id" 
                class="article-card"
                :class="{ 'reverse': index % 2 === 1 }"
              >
                <img v-if="article.coverImage" :src="article.coverImage" class="cover" @click="goToDetail(article.id)" />
                <div class="article-info">
                  <h3 @click="goToDetail(article.id)">{{ article.title }}</h3>
                  <p class="summary">{{ article.summary || '暂无摘要' }}</p>
                  <div class="meta">
                    <span>👤 {{ article.authorName }}</span>
                    <span>📅 {{ formatDate(article.createTime) }}</span>
                    <span>👁 {{ article.viewCount }}</span>
                    <span>💬 {{ article.commentCount }}</span>
                  </div>
                  <div class="tags" v-if="article.tags && article.tags.length > 0">
                    <el-tag 
                      v-for="tag in article.tags" 
                      :key="tag.id" 
                      size="small"
                      type="primary"
                      @click.stop="filterByTag(tag.id)"
                      style="cursor: pointer"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <el-pagination
                v-if="total > pageSize"
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next"
                @current-change="handlePageChange"
                style="margin-top: 20px; text-align: center"
              />
            </div>
          </div>

          <aside class="sidebar">
            <div class="card">
              <h3>📢 公告</h3>
              <div v-if="announcements.length === 0" class="empty">暂无公告</div>
              <div v-else class="announcement-list">
                <div 
                  v-for="announcement in announcements" 
                  :key="announcement.id"
                  class="announcement-item"
                >
                  <h4>{{ announcement.title }}</h4>
                  <p>{{ announcement.content }}</p>
                  <span class="date">{{ formatDate(announcement.createTime) }}</span>
                </div>
              </div>
            </div>
            
            <div class="card">
              <h3>🔥 热门文章</h3>
              <div v-if="hotArticles.length === 0" class="empty">暂无数据</div>
              <ul v-else class="hot-list">
                <li 
                  v-for="(article, index) in hotArticles" 
                  :key="article.id" 
                  @click="goToDetail(article.id)"
                  class="hot-item"
                >
                  <span class="rank" :class="'rank-' + (article.rank || index + 1)">{{ article.rank || index + 1 }}</span>
                  <span class="title">{{ article.title }}</span>
                  <span class="views">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }}
                  </span>
                </li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArticleList, getHotArticles, getCategoryList, getTagList } from '@/api/article'
import { getLatestAnnouncements } from '@/api/announcement'
import { View } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import Header from '@/components/Header.vue'

const router = useRouter()
const userStore = useUserStore()

const articles = ref([])
const hotArticles = ref([])
const announcements = ref([])
const categories = ref([])
const tags = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = ref({
  categoryId: null,
  tagId: null,
  keyword: ''
})

const fetchArticles = async () => {
  loading.value = true
  try {
    // 从URL参数获取筛选条件
    const urlParams = new URLSearchParams(window.location.search)
    const categoryIdFromUrl = urlParams.get('categoryId')
    const tagIdFromUrl = urlParams.get('tagId')
    
    // 如果URL有参数，更新filters
    if (categoryIdFromUrl) {
      filters.value.categoryId = Number(categoryIdFromUrl)
      filters.value.tagId = null
    }
    if (tagIdFromUrl) {
      filters.value.tagId = Number(tagIdFromUrl)
      filters.value.categoryId = null
    }
    
    const res = await getArticleList({ 
      page: currentPage.value, 
      size: pageSize.value,
      categoryId: filters.value.categoryId,
      tagId: filters.value.tagId,
      keyword: filters.value.keyword,
      status: 'published' // 前台只显示公开的文章
    })
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchHotArticles = async () => {
  try {
    const res = await getHotArticles(10)
    hotArticles.value = res.data || []
  } catch (error) {
    console.error('获取热门文章失败:', error)
  }
}

const fetchAnnouncements = async () => {
  try {
    const res = await getLatestAnnouncements(5)
    announcements.value = res.data || []
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getTagList()
    tags.value = res.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchArticles()
}

const filterByCategory = (categoryId) => {
  filters.value.categoryId = categoryId
  filters.value.tagId = null
  handleFilterChange()
}

const filterByTag = (tagId) => {
  filters.value.tagId = tagId
  filters.value.categoryId = null
  handleFilterChange()
}

onMounted(() => {
  fetchArticles()
  fetchHotArticles()
  fetchAnnouncements()
  fetchCategories()
  fetchTags()
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  background: #e8ecf1;
}

.main {
  padding: 30px 0;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.article-list {
  min-width: 0;
  
  h2 {
    margin-bottom: 20px;
  }
  
  .list-header {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 15px 0;
      font-size: 24px;
      color: #333;
    }
    
    .filters {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }
  }
}

.article-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  transition: all 0.3s;
  display: flex;
  gap: 0;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }
  
  // 反向布局（图片在右）
  &.reverse {
    flex-direction: row-reverse;
  }
  
  .cover {
    width: 50%;
    height: 220px;
    object-fit: cover;
    flex-shrink: 0;
    cursor: pointer;
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  .article-info {
    flex: 1;
    padding: 25px;
    display: flex;
    flex-direction: column;
    min-width: 0;
    justify-content: space-between;
    height: 220px;
    overflow: hidden;
  }
  
  h3 {
    font-size: 22px;
    margin-bottom: 12px;
    color: #333;
    cursor: pointer;
    transition: color 0.3s;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    font-weight: 600;
    line-height: 1.3;
    
    &:hover {
      color: #409eff;
    }
  }
  
  .summary {
    color: #666;
    margin-bottom: 12px;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    font-size: 14px;
  }
  
  .meta {
    display: flex;
    gap: 15px;
    color: #999;
    font-size: 13px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }
  
  .tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

.sidebar {
  position: sticky;
  top: 90px;
  
  .card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    
    h3 {
      margin-bottom: 15px;
      font-size: 18px;
      color: #333;
      font-weight: 600;
      padding-bottom: 10px;
      border-bottom: 2px solid #409eff;
    }
  }
  
  .announcement-list {
    .announcement-item {
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      h4 {
        font-size: 15px;
        color: #333;
        margin-bottom: 8px;
        font-weight: 600;
      }
      
      p {
        font-size: 13px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 8px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .date {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .hot-list {
    list-style: none;
    
    .hot-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f5f7fa;
        padding-left: 8px;
        padding-right: 8px;
        border-radius: 6px;
        
        .title {
          color: #409eff;
        }
      }
      
      &:last-child {
        border-bottom: none;
      }
      
      .rank {
        flex-shrink: 0;
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 13px;
        font-weight: 600;
        border-radius: 4px;
        background: #f0f0f0;
        color: #999;
        
        &.rank-1 {
          background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
          color: #fff;
          box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
        }
        
        &.rank-2 {
          background: linear-gradient(135deg, #C0C0C0 0%, #A8A8A8 100%);
          color: #fff;
          box-shadow: 0 2px 8px rgba(192, 192, 192, 0.3);
        }
        
        &.rank-3 {
          background: linear-gradient(135deg, #CD7F32 0%, #B8860B 100%);
          color: #fff;
          box-shadow: 0 2px 8px rgba(205, 127, 50, 0.3);
        }
      }
      
      .title {
        flex: 1;
        color: #333;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        transition: color 0.3s;
      }
      
      .views {
        flex-shrink: 0;
        display: flex;
        align-items: center;
        gap: 4px;
        color: #999;
        font-size: 13px;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    position: static;
  }
  
  .article-card {
    flex-direction: column !important;
    
    .cover {
      width: 100%;
      height: 250px;
    }
    
    .article-info {
      padding: 25px;
      height: auto;
    }
  }
}

@media (max-width: 768px) {
  .main {
    padding: 15px 0;
  }
  
  .article-list {
    .list-header {
      padding: 15px;
      
      h2 {
        font-size: 20px;
      }
      
      .filters {
        width: 100%;
        
        .el-select, .el-input {
          width: 100% !important;
        }
      }
    }
  }
  
  .article-card {
    .cover {
      height: 200px;
    }
    
    .article-info {
      padding: 20px;
    }
    
    h3 {
      font-size: 18px;
    }
    
    .summary {
      font-size: 14px;
    }
    
    .meta {
      font-size: 12px;
      gap: 10px;
    }
  }
}
</style>
