<template>
  <div class="categories-management">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>分类管理</span>
          <a-button type="primary" @click="showAddDialog">
            <template #icon>
              <PlusOutlined />
            </template>
            新建分类
          </a-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="分类名">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入分类名"
            allow-clear
            style="width: 200px"
          />
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

      <!-- 分类列表 -->
      <a-table
        :columns="columns"
        :data-source="categories"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`
        }"
        :table-layout="'fixed'"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ index + 1 }}
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editCategory(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个分类吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="deleteCategory(record)"
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

    <!-- 添加/编辑分类对话框 -->
    <a-modal
      v-model:open="showDialog"
      :title="dialogTitle"
      okText="确认"
      cancelText="取消"
      @ok="saveCategory"
      :confirmLoading="saving"
    >
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="分类名称" required>
          <a-input v-model:value="form.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="分类描述">
          <a-textarea v-model:value="form.description" placeholder="请输入分类描述（可选）" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getCategoryPage, createCategory, updateCategory, deleteCategory as deleteCategoryApi } from '@/api/article'
import dayjs from 'dayjs'

const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const searchForm = ref({
  name: ''
})
const form = ref({
  id: null,
  name: '',
  description: ''
})
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑分类' : '新建分类')

const columns = [
  {
    title: '编号',
    key: 'index',
    align: 'center'
  },
  {
    title: '分类名称',
    dataIndex: 'name',
    key: 'name',
    align: 'center'
  },
  {
    title: '文章数量',
    dataIndex: 'articleCount',
    key: 'articleCount',
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
    name: '',
    description: ''
  }
  showDialog.value = true
}

const editCategory = (record) => {
  isEdit.value = true
  form.value = {
    id: record.id,
    name: record.name,
    description: record.description || ''
  }
  showDialog.value = true
}

const saveCategory = async () => {
  if (!form.value.name) {
    message.warning('请输入分类名称')
    return
  }

  saving.value = true
  try {
    const data = {
      name: form.value.name,
      description: form.value.description
    }
    if (isEdit.value) {
      await updateCategory(form.value.id, data)
      message.success('更新成功')
    } else {
      await createCategory(data)
      message.success('创建成功')
    }
    showDialog.value = false
    loadCategories()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteCategory = async (record) => {
  try {
    await deleteCategoryApi(record.id)
    message.success('删除成功')
    loadCategories()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadCategories()
}

const handleSearch = () => {
  pagination.value.current = 1
  loadCategories()
}

const handleReset = () => {
  searchForm.value.name = ''
  pagination.value.current = 1
  loadCategories()
}

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryPage({
      page: pagination.value.current,
      size: pagination.value.pageSize,
      name: searchForm.value.name || undefined
    })
    categories.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    console.error('加载分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.categories-management {
  .search-form {
    margin-bottom: 16px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
