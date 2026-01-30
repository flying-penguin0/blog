<template>
  <div class="comments-management">
    <a-card :bordered="false">
      <template #title>
        <span>评论管理</span>
      </template>

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
            style="width: 120px"
          >
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
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
          <template v-else-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <div class="content-cell">{{ record.content }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'avatar'">
            <a-avatar :size="40" :src="record.avatar || '/default-avatar.png'" />
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 'pending'" color="orange">待审核</a-tag>
            <a-tag v-else-if="record.status === 'approved'" color="green">已通过</a-tag>
            <a-tag v-else-if="record.status === 'rejected'" color="red">已拒绝</a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status === 'pending'"
                type="link"
                size="small"
                @click="auditComment(record.id, 'approved')"
              >
                通过
              </a-button>
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
              <a-button
                v-if="record.status === 'pending' || record.status === 'approved'"
                type="link"
                danger
                size="small"
                @click="auditComment(record.id, 'rejected')"
              >
                拒绝
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
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { getCommentList, deleteComment as deleteCommentApi, auditComment as auditCommentApi } from '@/api/comment'
import dayjs from 'dayjs'

const loading = ref(false)
const comments = ref([])
const searchForm = reactive({
  content: '',
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
    align: 'center',
    width: 80
  },
  {
    title: '评论内容',
    key: 'content',
    dataIndex: 'content',
    ellipsis: true,
    align: 'center'
  },
  {
    title: '头像',
    key: 'avatar',
    align: 'center',
    width: 80
  },
  {
    title: '评论人',
    dataIndex: 'username',
    key: 'username',
    align: 'center',
    width: 120
  },
  {
    title: '文章标题',
    dataIndex: 'articleTitle',
    key: 'articleTitle',
    ellipsis: true,
    align: 'center',
    width: 200
  },
  {
    title: '状态',
    key: 'status',
    align: 'center',
    width: 100
  },
  {
    title: '评论时间',
    key: 'createTime',
    align: 'center',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 180
  }
]

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const deleteComment = async (record) => {
  try {
    await deleteCommentApi(record.id)
    message.success('删除成功')
    loadComments()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const auditComment = async (id, status) => {
  try {
    await auditCommentApi(id, status)
    message.success(status === 'approved' ? '审核通过' : '已拒绝')
    loadComments()
  } catch (error) {
    console.error('审核失败:', error)
    message.error('审核失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadComments()
}

const handleReset = () => {
  searchForm.content = ''
  searchForm.status = undefined
  handleSearch()
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
      keyword: searchForm.content,
      status: searchForm.status
    })
    comments.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped lang="scss">
.comments-management {
  .search-form {
    margin-bottom: 16px;
  }
  
  .content-cell {
    max-width: 400px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
