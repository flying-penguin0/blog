<template>
  <div class="user-articles">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>我的文章</span>
          <a-button type="primary" @click="$router.push('/user/editor')">
            <template #icon>
              <PlusOutlined />
            </template>
            新建文章
          </a-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="标题">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入文章标题"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option value="published">公开</a-select-option>
            <a-select-option value="private">私密</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 文章列表 -->
      <a-table
        :columns="columns"
        :data-source="articles"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :table-layout="'fixed'"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.key === 'title'">
            <a-tooltip :title="record.title">
              <div class="title-cell">{{ record.title }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'published' ? 'success' : 'warning'">
              {{ record.status === 'published' ? '公开' : '私密' }}
            </a-tag>
            <a-switch
              v-model:checked="record.statusChecked"
              checked-children="公开"
              un-checked-children="私密"
              style="margin-top: 8px;"
              @change="toggleStatus(record)"
            />
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="4">
              <a-button type="link" size="small" @click="viewArticle(record.id)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="editArticle(record.id)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要永久删除这篇文章吗？删除后无法恢复！"
                ok-text="确定删除"
                cancel-text="取消"
                ok-type="danger"
                @confirm="deleteArticle(record)"
              >
                <a-button type="link" danger size="small">
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getArticleList, updateArticle, deleteArticle as deleteArticleApi } from '@/api/article'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const articles = ref([])
const searchForm = reactive({
  keyword: '',
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100']
})

const columns = [
  {
    title: '编号',
    key: 'index',
    align: 'center'
  },
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
    title: '浏览量',
    dataIndex: 'viewCount',
    key: 'viewCount',
    align: 'center'
  },
  {
    title: '评论数',
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

const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

const editArticle = (id) => {
  router.push(`/user/editor?id=${id}`)
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.statusChecked ? 'published' : 'private'
    await updateArticle(row.id, { ...row, status: newStatus })
    row.status = newStatus
    message.success(`已设为${newStatus === 'published' ? '公开' : '私密'}`)
  } catch (error) {
    console.error('操作失败:', error)
    row.statusChecked = !row.statusChecked
  }
}

const deleteArticle = async (row) => {
  try {
    await deleteArticleApi(row.id)
    message.success('删除成功')
    loadArticles()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadArticles()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadArticles()
}

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status,
      userId: userStore.user.id
    })
    articles.value = (res.data.records || []).map(item => ({
      ...item,
      statusChecked: item.status === 'published'
    }))
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped lang="scss">
.user-articles {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .title-cell {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
