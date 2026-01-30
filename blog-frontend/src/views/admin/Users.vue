<template>
  <div class="users-management">
    <a-card :bordered="false">
      <template #title>
        <span>用户管理</span>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.username"
            placeholder="请输入用户名"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="searchForm.role"
            placeholder="请选择角色"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
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

      <!-- 用户列表 -->
      <a-table
        :columns="columns"
        :data-source="users"
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
            <a-avatar :src="record.avatar">
              <template #icon><UserOutlined /></template>
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.role === 'admin' ? 'red' : 'blue'">
              {{ record.role === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editUser(record)">
                编辑
              </a-button>
              <a-popconfirm
                :title="`确定要${record.status === 1 ? '禁用' : '启用'}该用户吗？`"
                ok-text="确定"
                cancel-text="取消"
                @confirm="toggleStatus(record)"
              >
                <a-button type="link" size="small" :danger="record.status === 1">
                  {{ record.status === 1 ? '禁用' : '启用' }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑用户对话框 -->
    <a-modal
      v-model:open="showEditDialog"
      title="编辑用户"
      @ok="saveUser"
      :confirmLoading="saving"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="用户名">
          <a-input v-model:value="editForm.username" disabled />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="editForm.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="editForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="editForm.role">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { getUserList, updateUser } from '@/api/user'
import dayjs from 'dayjs'

const loading = ref(false)
const saving = ref(false)
const users = ref([])
const showEditDialog = ref(false)
const searchForm = reactive({
  username: '',
  role: undefined
})

const editForm = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  role: 'user'
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
    title: '头像',
    key: 'avatar',
    align: 'center'
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    align: 'center'
  },
  {
    title: '昵称',
    dataIndex: 'nickname',
    key: 'nickname',
    align: 'center'
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    ellipsis: true,
    align: 'center'
  },
  {
    title: '角色',
    key: 'role',
    align: 'center'
  },
  {
    title: '状态',
    key: 'status',
    align: 'center'
  },
  {
    title: '注册时间',
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

const editUser = (record) => {
  editForm.value = {
    id: record.id,
    username: record.username,
    nickname: record.nickname,
    email: record.email,
    role: record.role
  }
  showEditDialog.value = true
}

const saveUser = async () => {
  saving.value = true
  try {
    await updateUser(editForm.value.id, editForm.value)
    message.success('更新成功')
    showEditDialog.value = false
    loadUsers()
  } catch (error) {
    console.error('更新失败:', error)
    message.error('更新失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await updateUser(record.id, { ...record, status: newStatus })
    message.success(`${newStatus === 1 ? '启用' : '禁用'}成功`)
    loadUsers()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.role = undefined
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadUsers()
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.current,
      size: pagination.pageSize,
      username: searchForm.username,
      role: searchForm.role
    })
    users.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.users-management {
  .search-form {
    margin-bottom: 16px;
  }
}
</style>
