<template>
  <div class="messages-management">
    <a-card title="留言管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-select
            v-model:value="searchStatus"
            placeholder="留言状态"
            style="width: 120px"
            @change="handleSearch"
            allowClear
          >
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="rejected">已拒绝</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="messages"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>

          <template v-else-if="column.key === 'user'">
            <a-space>
              <a-avatar :src="record.avatar">
                {{ record.username?.charAt(0).toUpperCase() }}
              </a-avatar>
              <span>{{ record.username }}</span>
            </a-space>
          </template>

          <template v-else-if="column.key === 'content'">
            <div class="content-cell">{{ record.content }}</div>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record.status === 'approved'" color="success">已通过</a-tag>
            <a-tag v-else-if="record.status === 'pending'" color="warning">待审核</a-tag>
            <a-tag v-else-if="record.status === 'rejected'" color="error">已拒绝</a-tag>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.status !== 'approved'"
                type="link"
                size="small"
                @click="handleAudit(record.id, 'approved')"
              >
                通过
              </a-button>
              <a-popconfirm
                title="确定要删除这条留言吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
              <a-button
                v-if="record.status !== 'rejected'"
                type="link"
                danger
                size="small"
                @click="handleAudit(record.id, 'rejected')"
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
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getMessageList, deleteMessage, auditMessage } from '@/api/message'
import dayjs from 'dayjs'

const loading = ref(false)
const messages = ref([])
const searchStatus = ref(undefined)

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
    width: 80
  },
  {
    title: '用户',
    key: 'user',
    width: 150
  },
  {
    title: '留言内容',
    key: 'content',
    width: 400
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 200
  }
]

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMessageList({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      status: searchStatus.value
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

const handleAudit = async (id, status) => {
  try {
    await auditMessage(id, status)
    message.success(status === 'approved' ? '已通过' : '已拒绝')
    loadMessages()
  } catch (error) {
    console.error('审核失败:', error)
    message.error('审核失败')
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="scss">
.messages-management {
  .content-cell {
    max-width: 400px;
    word-break: break-word;
    white-space: pre-wrap;
  }
}
</style>
