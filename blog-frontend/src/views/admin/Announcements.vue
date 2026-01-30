<template>
  <div class="announcements-management">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>公告管理</span>
          <a-button type="primary" @click="showAddDialog">
            <template #icon>
              <PlusOutlined />
            </template>
            新建公告
          </a-button>
        </div>
      </template>

      <!-- 公告列表 -->
      <a-table
        :columns="columns"
        :data-source="announcements"
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
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editAnnouncement(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这条公告吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="deleteAnnouncement(record)"
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

    <!-- 添加/编辑公告对话框 -->
    <a-modal
      v-model:open="showDialog"
      :title="dialogTitle"
      @ok="saveAnnouncement"
      :confirmLoading="saving"
      width="600px"
    >
      <a-form :model="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea
            v-model:value="form.content"
            placeholder="请输入公告内容"
            :rows="6"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import {
  getAnnouncementList,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement as deleteAnnouncementApi
} from '@/api/announcement'
import dayjs from 'dayjs'

const loading = ref(false)
const saving = ref(false)
const announcements = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  title: '',
  content: ''
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

const dialogTitle = computed(() => isEdit.value ? '编辑公告' : '新建公告')

const columns = [
  {
    title: '编号',
    key: 'index',
    align: 'center'
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    align: 'center'
  },
  {
    title: '内容',
    key: 'content',
    dataIndex: 'content',
    ellipsis: true,
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

const showAddDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    content: ''
  }
  showDialog.value = true
}

const editAnnouncement = (record) => {
  isEdit.value = true
  form.value = {
    id: record.id,
    title: record.title,
    content: record.content
  }
  showDialog.value = true
}

const saveAnnouncement = async () => {
  if (!form.value.title || !form.value.content) {
    message.warning('请填写完整信息')
    return
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement(form.value.id, form.value)
      message.success('更新成功')
    } else {
      await createAnnouncement(form.value)
      message.success('创建成功')
    }
    showDialog.value = false
    loadAnnouncements()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteAnnouncement = async (record) => {
  try {
    await deleteAnnouncementApi(record.id)
    message.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadAnnouncements()
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({
      page: pagination.current,
      size: pagination.pageSize
    })
    // 后端返回的是列表，不是分页对象
    if (Array.isArray(res.data)) {
      announcements.value = res.data
      pagination.total = res.data.length
    } else {
      announcements.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped lang="scss">
.announcements-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .content-cell {
    max-width: 400px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
