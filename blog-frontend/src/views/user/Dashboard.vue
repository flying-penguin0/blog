<template>
  <div class="user-dashboard">
    <a-row :gutter="[16, 16]">
      <!-- 统计卡片 -->
      <a-col :xs="24" :sm="8">
        <a-card class="stat-card stat-card-blue">
          <a-statistic
            title="我的文章"
            :value="stats.articleCount"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="stat-card stat-card-green">
          <a-statistic
            title="总浏览量"
            :value="stats.viewCount"
          >
            <template #prefix>
              <EyeOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="stat-card stat-card-orange">
          <a-statistic
            title="我的评论"
            :value="stats.myCommentCount"
          >
            <template #prefix>
              <CommentOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 最近文章 -->
    <a-card title="最近文章" style="margin-top: 16px;" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="$router.push('/user/editor')">
          <template #icon>
            <PlusOutlined />
          </template>
          写文章
        </a-button>
      </template>
      <a-table
        :columns="columns"
        :data-source="recentArticles"
        :pagination="false"
        :loading="loading"
        :table-layout="'fixed'"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a @click="viewArticle(record.id)">{{ record.title }}</a>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'published' ? 'success' : 'default'">
              {{ record.status === 'published' ? '已发布' : '草稿' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editArticle(record.id)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="viewArticle(record.id)">
                查看
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArticleList } from '@/api/article'
import { getUserStats } from '@/api/user'
import {
  FileTextOutlined,
  EyeOutlined,
  CommentOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const stats = ref({
  articleCount: 0,
  viewCount: 0,
  myCommentCount: 0
})
const recentArticles = ref([])

const columns = [
  {
    title: '标题',
    key: 'title',
    dataIndex: 'title',
    ellipsis: true,
    align: 'center'
  },
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'categoryName',
    align: 'center'
  },
  {
    title: '浏览',
    dataIndex: 'viewCount',
    key: 'viewCount',
    align: 'center'
  },
  {
    title: '评论',
    dataIndex: 'commentCount',
    key: 'commentCount',
    align: 'center'
  },
  {
    title: '状态',
    key: 'status',
    align: 'center'
  },
  {
    title: '创建时间',
    key: 'createTime',
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    align: 'center'
  }
]

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const editArticle = (id) => {
  router.push(`/user/editor?id=${id}`)
}

const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

const loadStats = async () => {
  try {
    const res = await getUserStats(userStore.user.id)
    stats.value = res.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadRecentArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      page: 1,
      size: 5,
      userId: userStore.user.id
    })
    recentArticles.value = res.data.records || []
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadRecentArticles()
})
</script>

<style scoped lang="scss">
.user-dashboard {
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
  }
}
</style>
