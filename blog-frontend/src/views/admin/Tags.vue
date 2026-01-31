<template>
  <div class="tags-management">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>标签管理</span>
          <a-button type="primary" @click="showAddDialog">
            <template #icon>
              <PlusOutlined />
            </template>
            新建标签
          </a-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="标签名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入标签名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="所属分类">
          <a-select
            v-model:value="searchForm.categoryId"
            placeholder="请选择分类"
            allow-clear
            style="width: 150px"
            :loading="categoriesLoading"
          >
            <a-select-option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </a-select-option>
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

      <!-- 标签列表 -->
      <a-table
        :columns="columns"
        :data-source="tags"
        :loading="loading"
        :pagination="pagination"
        :table-layout="'fixed'"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.key === 'name'">
            <a-tag color="blue">{{ record.name }}</a-tag>
          </template>
          <template v-else-if="column.key === 'categoryName'">
            <a-tag v-if="record.categoryName" color="green">{{ record.categoryName }}</a-tag>
            <span v-else style="color: #999;">未分类</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editTag(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个标签吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="deleteTag(record)"
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

    <!-- 添加/编辑标签对话框 -->
    <a-modal
      v-model:open="showDialog"
      :title="dialogTitle"
      @ok="saveTag"
      :confirmLoading="saving"
    >
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="标签名称" required>
          <a-input v-model:value="form.name" placeholder="请输入标签名称" />
        </a-form-item>
        <a-form-item label="所属分类">
          <a-select
            v-model:value="form.categoryId"
            placeholder="请选择分类（可选）"
            allow-clear
            :loading="categoriesLoading"
          >
            <a-select-option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getTagPage, createTag, updateTag, deleteTag as deleteTagApi, getCategoryList } from '@/api/article'
import dayjs from 'dayjs'

const loading = ref(false)
const saving = ref(false)
const categoriesLoading = ref(false)
const tags = ref([])
const categories = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  name: '',
  categoryId: null
})

const searchForm = reactive({
  name: '',
  categoryId: undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const dialogTitle = computed(() => isEdit.value ? '编辑标签' : '新建标签')

const columns = [
  {
    title: '编号',
    key: 'index',
    align: 'center',
    width: 80
  },
  {
    title: '标签名称',
    key: 'name',
    dataIndex: 'name',
    align: 'center'
  },
  {
    title: '所属分类',
    key: 'categoryName',
    dataIndex: 'categoryName',
    align: 'center'
  },
  {
    title: '文章数量',
    dataIndex: 'articleCount',
    key: 'articleCount',
    align: 'center',
    width: 100
  },
  {
    title: '创建时间',
    key: 'createTime',
    align: 'center',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 150
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
    categoryId: null
  }
  showDialog.value = true
  loadCategories()
}

const editTag = (record) => {
  isEdit.value = true
  form.value = {
    id: record.id,
    name: record.name,
    categoryId: record.categoryId
  }
  showDialog.value = true
  loadCategories()
}

const saveTag = async () => {
  if (!form.value.name) {
    message.warning('请输入标签名称')
    return
  }

  saving.value = true
  try {
    const data = {
      name: form.value.name,
      categoryId: form.value.categoryId
    }
    
    if (isEdit.value) {
      await updateTag(form.value.id, data)
      message.success('更新成功')
    } else {
      await createTag(data)
      message.success('创建成功')
    }
    showDialog.value = false
    loadTags()
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  } finally {
    saving.value = false
  }
}

const deleteTag = async (record) => {
  try {
    await deleteTagApi(record.id)
    message.success('删除成功')
    loadTags()
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTags()
}

const handleSearch = () => {
  pagination.current = 1
  loadTags()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = undefined
  handleSearch()
}

const loadTags = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }
    
    // 添加搜索条件
    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.categoryId) {
      params.categoryId = searchForm.categoryId
    }
    
    const res = await getTagPage(params)
    tags.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载标签列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  categoriesLoading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
  } finally {
    categoriesLoading.value = false
  }
}

onMounted(() => {
  loadTags()
  loadCategories()
})
</script>

<style scoped lang="scss">
.tags-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-form {
    margin-bottom: 16px;
  }
}
</style>
