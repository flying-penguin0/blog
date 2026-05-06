<template>
  <div class="sensitive-words-management">
    <a-card :bordered="false">
      <template #title>
        <div class="card-header">
          <span>敏感词管理</span>
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <PlusOutlined />
            </template>
            添加敏感词
          </a-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="敏感词">
          <a-input
            v-model:value="searchForm.word"
            placeholder="请输入敏感词"
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
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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

      <!-- 敏感词列表 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
        :table-layout="'fixed'"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
          </template>
          <template v-else-if="column.key === 'word'">
            <a-tag color="red">{{ record.word }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
            <a-switch
              v-model:checked="record.statusChecked"
              checked-children="启用"
              un-checked-children="禁用"
              style="margin-top: 8px;"
              @change="toggleStatus(record)"
            />
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个敏感词吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
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

    <!-- 添加/编辑对话框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="敏感词" name="word">
          <a-input v-model:value="formData.word" placeholder="请输入敏感词" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  PlusOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import {
  getSensitiveWordPage,
  createSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord
} from '@/api/sensitiveWord'
import dayjs from 'dayjs'

// 搜索表单
const searchForm = reactive({
  word: '',
  status: undefined
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100']
})

// 表格列
const columns = [
  {
    title: '编号',
    key: 'index',
    align: 'center',
    width: 80
  },
  {
    title: '敏感词',
    key: 'word',
    dataIndex: 'word',
    align: 'center'
  },
  {
    title: '状态',
    key: 'status',
    align: 'center',
    width: 100
  },
  {
    title: '创建时间',
    key: 'createTime',
    align: 'center',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
    width: 200
  }
]

// 对话框
const modalVisible = ref(false)
const modalTitle = ref('添加敏感词')
const formRef = ref()
const formData = reactive({
  id: null,
  word: '',
  status: 1
})

// 表单验证规则
const rules = {
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 格式化日期
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      word: searchForm.word || undefined,
      status: searchForm.status
    }
    const res = await getSensitiveWordPage(params)
    tableData.value = (res.data.records || []).map(item => ({
      ...item,
      statusChecked: item.status === 1
    }))
    pagination.total = res.data.total
  } catch (error) {
    // 显示后端返回的错误信息
    const errorMsg = error.response?.data?.message || error.message || '加载数据失败'
    message.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.word = ''
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 添加
const handleAdd = () => {
  modalTitle.value = '添加敏感词'
  formData.id = null
  formData.word = ''
  formData.status = 1
  modalVisible.value = true
}

// 编辑
const handleEdit = (record) => {
  modalTitle.value = '编辑敏感词'
  formData.id = record.id
  formData.word = record.word
  formData.status = record.status
  modalVisible.value = true
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const data = {
      word: formData.word,
      status: formData.status
    }
    
    if (formData.id) {
      await updateSensitiveWord(formData.id, data)
      message.success('更新成功')
    } else {
      await createSensitiveWord(data)
      message.success('添加成功')
    }
    
    modalVisible.value = false
    loadData()
  } catch (error) {
    if (error.errorFields) {
      return
    }
    // 只显示后端返回的错误信息
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    message.error(errorMsg)
  }
}

// 取消
const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

// 切换状态
const toggleStatus = async (record) => {
  try {
    const newStatus = record.statusChecked ? 1 : 0
    await updateSensitiveWord(record.id, { status: newStatus })
    record.status = newStatus
    message.success(`已设为${newStatus === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('操作失败:', error)
    record.statusChecked = !record.statusChecked
    // 显示后端返回的错误信息
    const errorMsg = error.response?.data?.message || error.message || '操作失败'
    message.error(errorMsg)
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await deleteSensitiveWord(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    // 显示后端返回的错误信息
    const errorMsg = error.response?.data?.message || error.message || '删除失败'
    message.error(errorMsg)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.sensitive-words-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .search-form {
    margin-bottom: 16px;
  }
}
</style>
