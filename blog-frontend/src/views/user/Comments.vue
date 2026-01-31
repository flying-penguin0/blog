<template>
  <div class="user-comments">
    <a-card :bordered="false" title="我的评论">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="内容">
          <a-input
            v-model:value="searchForm.content"
            placeholder="请输入评论内容"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="rejected">已拒绝</a-select-option>
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

      <!-- 评论列表 -->
      <a-table
        :columns="columns"
        :data-source="comments"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :table-layout="'fixed'"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.key === 'articleTitle'">
            <a @click="viewArticle(record.articleId)">{{ record.articleTitle }}</a>
          </template>
          <template v-else-if="column.key === 'parentContent'">
            <a-tooltip v-if="record.parentContent" :title="record.parentContent">
              <div class="content-cell">
                {{ record.parentContent }}
              </div>
            </a-tooltip>
            <span v-else>无</span>
          </template>
          <template v-else-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <div class="content-cell">{{ record.content }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 'approved'" color="green">已通过</a-tag>
            <a-tag v-else-if="record.status === 'pending'" color="orange">待审核</a-tag>
            <a-tag v-else-if="record.status === 'rejected'" color="red">已拒绝</a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除这条评论吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="deleteComment(record)"
            >
              <a-button type="link" danger size="small">
                删除
              </a-button>
            </a-popconfirm>
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
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getCommentList, deleteComment as deleteCommentAPI } from '@/api/comment'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const comments = ref([])
const searchForm = reactive({
  content: '',
  status: ''
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
    align: 'center',
    width: 80
  },
  {
    title: '文章标题',
    key: 'articleTitle',
    dataIndex: 'articleTitle',
    ellipsis: true,
    align: 'center',
    width: 150
  },
  {
    title: '父评论',
    key: 'parentContent',
    ellipsis: true,
    align: 'center',
    width: 200
  },
  {
    title: '当前评论',
    key: 'content',
    dataIndex: 'content',
    ellipsis: true,
    align: 'center',
    width: 200
  },
  {
    title: '评论状态',
    key: 'status',
    align: 'center',
    width: 100
  },
  {
    title: '评论时间',
    key: 'createTime',
    align: 'center',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 100
  }
]

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const deleteComment = async (comment) => {
  try {
    await deleteCommentAPI(comment.id)
    message.success('删除成功')
    loadComments()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadComments()
}

const loadComments = async () => {
  loading.value = true
  try {
    const res = await getCommentList({
      page: pagination.current,
      size: pagination.pageSize,
      userId: userStore.user.id,
      content: searchForm.content,
      status: searchForm.status
    })
    comments.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载评论列表失败:', error)
    message.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadComments()
}

const handleReset = () => {
  searchForm.content = ''
  searchForm.status = ''
  pagination.current = 1
  loadComments()
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped lang="scss">
.user-comments {
  .search-form {
    margin-bottom: 16px;
  }
  
  .content-cell {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
