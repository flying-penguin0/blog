<template>
  <div class="user-messages">
    <a-card :bordered="false" title="我的留言">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="内容">
          <a-input
            v-model:value="searchForm.content"
            placeholder="请输入留言内容"
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

      <a-table
        :columns="columns"
        :data-source="messages"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>

          <template v-else-if="column.key === 'content'">
            <div class="content-cell">{{ record.content }}</div>
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
              title="确定要删除这条留言吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getMyMessages, deleteMessage } from '@/api/message'
import dayjs from 'dayjs'

const loading = ref(false)
const messages = ref([])
const searchForm = reactive({
  content: '',
  status: undefined
})

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  {
    title: '编号',
    key: 'index',
    width: 80,
    align: 'center'
  },
  {
    title: '留言内容',
    key: 'content',
    width: 400
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 100,
    align: 'center'
  }
]

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMyMessages({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      content: searchForm.content,
      status: searchForm.status
    })
    messages.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    console.error('加载留言失败:', error)
    message.error('加载留言失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  loadMessages()
}

const handleReset = () => {
  searchForm.content = ''
  searchForm.status = undefined
  pagination.value.current = 1
  loadMessages()
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadMessages()
}

const handleDelete = async (id) => {
  try {
    await deleteMessage(id)
    message.success('删除成功')
    loadMessages()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="scss">
.user-messages {
  .search-form {
    margin-bottom: 16px;
  }
  
  .content-cell {
    max-width: 400px;
    word-break: break-word;
    white-space: pre-wrap;
  }
}
</style>
