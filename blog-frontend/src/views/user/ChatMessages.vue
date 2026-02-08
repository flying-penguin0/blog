<template>
  <div class="user-chat-messages">
    <a-card :bordered="false" title="我的聊天">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="内容">
          <a-input
            v-model:value="searchForm.content"
            placeholder="请输入消息内容"
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

      <!-- 消息列表 -->
      <a-table
        :columns="columns"
        :data-source="messages"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :table-layout="'fixed'"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>

          <template v-else-if="column.key === 'avatar'">
            <a-avatar :size="40" :src="record.avatar || '/default-avatar.png'" />
          </template>

          <template v-else-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <div class="content-cell">{{ record.content }}</div>
            </a-tooltip>
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
            <a-popconfirm
              title="确定要删除这条消息吗？"
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
import { getMyChatMessages, deleteMessage } from '@/api/chatroom'
import dayjs from 'dayjs'

const loading = ref(false)
const messages = ref([])
const searchForm = reactive({
  content: '',
  status: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
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
    title: '消息内容',
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
    title: '状态',
    key: 'status',
    align: 'center',
    width: 100
  },
  {
    title: '发送时间',
    key: 'createTime',
    align: 'center',
    width: 160
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

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMyChatMessages({
      page: pagination.current,
      size: pagination.pageSize,
      content: searchForm.content,
      status: searchForm.status
    })
    messages.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    message.error('加载聊天记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadMessages()
}

const handleReset = () => {
  searchForm.content = ''
  searchForm.status = undefined
  pagination.current = 1
  loadMessages()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
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
.user-chat-messages {
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
