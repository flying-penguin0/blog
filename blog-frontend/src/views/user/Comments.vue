<template>
  <div class="user-comments">
    <a-card :bordered="false">
      <template #title>
        <span>我的评论</span>
      </template>

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
              <a-button type="link" size="small" @click="replyComment(record)">
                回复
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
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 回复对话框 -->
    <a-modal
      v-model:open="showReplyDialog"
      title="回复评论"
      @ok="submitReply"
      :confirmLoading="replying"
    >
      <div class="reply-info">
        <p><strong>评论者：</strong>{{ currentComment?.nickname }}</p>
        <p><strong>评论内容：</strong>{{ currentComment?.content }}</p>
      </div>
      <a-textarea
        v-model:value="replyContent"
        :rows="4"
        placeholder="请输入回复内容..."
        :maxlength="500"
        show-count
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import { getCommentList, createComment, deleteComment as deleteCommentAPI } from '@/api/comment'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const replying = ref(false)
const comments = ref([])
const showReplyDialog = ref(false)
const currentComment = ref(null)
const replyContent = ref('')

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
    title: '文章标题',
    key: 'articleTitle',
    dataIndex: 'articleTitle',
    ellipsis: true,
    align: 'center'
  },
  {
    title: '评论者',
    dataIndex: 'nickname',
    key: 'nickname',
    align: 'center'
  },
  {
    title: '评论内容',
    key: 'content',
    dataIndex: 'content',
    ellipsis: true,
    align: 'center'
  },
  {
    title: '评论时间',
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

const viewArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const replyComment = (comment) => {
  currentComment.value = comment
  replyContent.value = ''
  showReplyDialog.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    message.warning('请输入回复内容')
    return
  }

  replying.value = true
  try {
    await createComment({
      articleId: currentComment.value.articleId,
      content: replyContent.value,
      parentId: currentComment.value.id
    })
    message.success('回复成功')
    showReplyDialog.value = false
    loadComments()
  } catch (error) {
    console.error('回复失败:', error)
    message.error('回复失败，请重试')
  } finally {
    replying.value = false
  }
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
      userId: userStore.user.id
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

onMounted(() => {
  loadComments()
})
</script>

<style scoped lang="scss">
.user-comments {
  .content-cell {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .reply-info {
    margin-bottom: 15px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    
    p {
      margin: 5px 0;
      color: #606266;
    }
  }
}
</style>
