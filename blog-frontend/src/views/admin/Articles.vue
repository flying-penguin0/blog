<template>
  <div class="articles-management">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>文章管理</span>
          <a-button type="primary" @click="createNewArticle">
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
          <template v-else-if="column.key === 'coverImage'">
            <a-image
              v-if="record.coverImage"
              :src="record.coverImage"
              :width="100"
              :height="60"
              style="border-radius: 4px; object-fit: cover;"
            />
            <div v-else class="no-cover">无封面</div>
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
              <a-popconfirm
                title="确定要永久删除这篇文章吗？删除后无法恢复！"
                ok-text="确定删除"
                cancel-text="取消"
                ok-type="danger"
                @confirm="deleteArticle(record)"
              >
                <a-button type="link" danger size="small" translate="no">
                  删除
                </a-button>
              </a-popconfirm>
              <a-button type="link" size="small" @click="editArticle(record.id)" translate="no">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="viewArticle(record.id)" translate="no">
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
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getArticleList, updateArticle, deleteArticle as deleteArticleApi } from '@/api/article'
import dayjs from 'dayjs'

const router = useRouter()

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
    title: '封面',
    key: 'coverImage',
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
    title: '作者',
    dataIndex: 'authorName',
    key: 'authorName',
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

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 查看文章
const viewArticle = (id) => {
  router.push(`/article/${id}`)
}

// 新建文章
const createNewArticle = () => {
  router.push('/admin/editor')
}

// 编辑文章
const editArticle = (id) => {
  console.log('编辑文章 ID:', id)
  if (!id) {
    message.error('文章ID不存在')
    return
  }
  router.push(`/admin/editor?id=${id}`)
}

// 切换文章状态
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

// 删除文章
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadArticles()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  handleSearch()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadArticles()
}

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
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
.articles-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 16px;
  }
  
  .no-cover {
    width: 100px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
    border-radius: 4px;
    color: #999;
    font-size: 12px;
  }
  
  .title-cell {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
