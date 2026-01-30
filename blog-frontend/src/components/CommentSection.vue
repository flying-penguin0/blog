<template>
  <div class="comment-section">
    <h3 class="title">评论 ({{ totalCommentCount }})</h3>
    
    <!-- 评论输入框 -->
    <div class="comment-input">
      <el-avatar :size="40" :src="userStore.user?.avatar || '/default-avatar.png'" />
      <div class="input-wrapper">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          :placeholder="userStore.token ? '写下你的评论...' : '请先登录后再发表评论'"
          maxlength="500"
          :show-word-limit="userStore.token"
          :disabled="!userStore.token"
          @focus="handleCommentFocus"
        />
        <div class="actions">
          <el-button 
            type="primary" 
            @click="submitComment" 
            :loading="submitting"
            :disabled="!userStore.token"
          >
            发表评论
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 评论列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="comments.length === 0" class="empty">暂无评论，快来抢沙发吧~</div>
    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- 一级评论 -->
        <div class="comment-main">
          <el-avatar :size="40" :src="comment.avatar || '/default-avatar.png'" />
          <div class="comment-content">
            <div class="user-info">
              <span class="username">{{ comment.nickname }}</span>
              <span class="time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <p class="content">{{ comment.content }}</p>
            <div class="actions">
              <span 
                class="action-btn like-btn" 
                :class="{ liked: comment.isLiked }"
                @click="handleLike(comment)"
              >
                <svg v-if="comment.isLiked" class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311h-.3v428h472.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM112 528v364c0 17.7 14.3 32 32 32h65V496h-65c-17.7 0-32 14.3-32 32z" fill="currentColor"/>
                </svg>
                <svg v-else class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4A106.62 106.62 0 0 0 471 99.9c-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353.4c-3.2 4.2-4.4 9.6-3.2 14.7l6.3 27.8c1.5 6.6 2.2 13.4 2.2 20.3 0 44.6-25.5 85.2-66.3 105.5-6.2 3.1-12.6 4.6-19 4.6H244V585.5l89.7-323.7c5.6-20.1 23.8-34.4 44.6-34.4 8.7 0 16.9 2.6 23.8 7.5 11.9 8.4 18.5 21.8 17.9 36.4l-10.8 219.2h462.4c7.4 0 14.6 2.2 20.5 6.2 24.1 16.2 37.4 43.8 37.4 77.7 0 22.1-7.6 43.7-21.1 60.8z" fill="currentColor"/>
                </svg>
                <span v-if="comment.likeCount > 0">{{ comment.likeCount }}</span>
                点赞
              </span>
              <span class="action-btn" @click="handleReply(comment)">
                <el-icon><ChatDotRound /></el-icon>
                回复
              </span>
              <span 
                v-if="canDelete(comment)" 
                class="action-btn delete" 
                @click="handleDelete(comment)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </span>
            </div>
            
            <!-- 回复输入框 -->
            <div v-if="replyingTo === comment.id" class="reply-input">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="`回复 @${comment.nickname}...`"
                maxlength="500"
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button size="small" type="primary" @click="submitReply(comment)" :loading="submitting">
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 二级评论 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <el-avatar :size="32" :src="reply.avatar || '/default-avatar.png'" />
            <div class="reply-content">
              <div class="user-info">
                <span class="username">{{ reply.nickname }}</span>
                <span v-if="reply.parentUsername" class="reply-to">
                  回复 @{{ reply.parentUsername }}
                </span>
                <span class="time">{{ formatTime(reply.createTime) }}</span>
              </div>
              <p class="content">{{ reply.content }}</p>
              <div class="actions">
                <span 
                  class="action-btn like-btn" 
                  :class="{ liked: reply.isLiked }"
                  @click="handleLike(reply)"
                >
                  <svg v-if="reply.isLiked" class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311h-.3v428h472.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM112 528v364c0 17.7 14.3 32 32 32h65V496h-65c-17.7 0-32 14.3-32 32z" fill="currentColor"/>
                  </svg>
                  <svg v-else class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4A106.62 106.62 0 0 0 471 99.9c-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353.4c-3.2 4.2-4.4 9.6-3.2 14.7l6.3 27.8c1.5 6.6 2.2 13.4 2.2 20.3 0 44.6-25.5 85.2-66.3 105.5-6.2 3.1-12.6 4.6-19 4.6H244V585.5l89.7-323.7c5.6-20.1 23.8-34.4 44.6-34.4 8.7 0 16.9 2.6 23.8 7.5 11.9 8.4 18.5 21.8 17.9 36.4l-10.8 219.2h462.4c7.4 0 14.6 2.2 20.5 6.2 24.1 16.2 37.4 43.8 37.4 77.7 0 22.1-7.6 43.7-21.1 60.8z" fill="currentColor"/>
                  </svg>
                  <span v-if="reply.likeCount > 0">{{ reply.likeCount }}</span>
                  点赞
                </span>
                <span 
                  v-if="canDelete(reply)" 
                  class="action-btn delete" 
                  @click="handleDelete(reply)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArticleComments, createComment, deleteComment, likeComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()
const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref(null)

// 计算总评论数（包括回复）
const totalCommentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(comment => {
    if (comment.replies && comment.replies.length > 0) {
      count += comment.replies.length
    }
  })
  return count
})

// 判断是否可以删除（前台不允许删除评论）
const canDelete = (comment) => {
  return false
}

// 处理评论框聚焦
const handleCommentFocus = () => {
  if (!userStore.token) {
    ElMessageBox.confirm(
      '需要登录后才能发表评论',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login')
    }).catch(() => {})
  }
}

// 加载评论
const loadComments = async () => {
  loading.value = true
  try {
    const res = await getArticleComments(props.articleId)
    comments.value = res.data || []
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
  }
}

// 发表评论
const submitComment = async () => {
  if (!userStore.token) {
    ElMessageBox.confirm(
      '需要登录后才能发表评论',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    const res = await createComment({
      articleId: props.articleId,
      content: commentContent.value
    })
    // 使用后端返回的消息
    ElMessage.success(res.message || '评论成功')
    commentContent.value = ''
    loadComments()
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    submitting.value = false
  }
}

// 回复评论
const handleReply = (comment) => {
  if (!userStore.token) {
    ElMessageBox.confirm(
      '需要登录后才能回复评论',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }
  replyingTo.value = comment.id
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (comment) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  submitting.value = true
  try {
    const res = await createComment({
      articleId: props.articleId,
      parentId: comment.id,
      content: replyContent.value
    })
    // 使用后端返回的消息
    ElMessage.success(res.message || '回复成功')
    cancelReply()
    loadComments()
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    submitting.value = false
  }
}

// 点赞
const handleLike = async (comment) => {
  if (!userStore.token) {
    ElMessageBox.confirm(
      '需要登录后才能点赞',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }
  
  // 保存原始状态，用于失败时回滚
  const originalLiked = comment.isLiked
  const originalCount = comment.likeCount
  
  // 乐观更新UI
  if (comment.isLiked) {
    comment.likeCount--
    comment.isLiked = false
  } else {
    comment.likeCount++
    comment.isLiked = true
  }
  
  try {
    await likeComment(comment.id)
    // 不显示提示消息，保持简洁
  } catch (error) {
    // 失败时回滚UI状态
    comment.isLiked = originalLiked
    comment.likeCount = originalCount
    console.error('点赞失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 删除评论
const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped lang="scss">
.comment-section {
  margin-top: 40px;
  
  .title {
    font-size: 20px;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #e4e7ed;
  }
  
  .comment-input {
    display: flex;
    gap: 15px;
    margin-bottom: 30px;
    
    .input-wrapper {
      flex: 1;
      
      :deep(.el-textarea__inner) {
        &:disabled {
          background-color: #f5f7fa;
          cursor: not-allowed;
        }
      }
      
      .actions {
        margin-top: 10px;
        text-align: right;
      }
    }
  }
  
  .loading, .empty {
    text-align: center;
    padding: 40px;
    color: #999;
  }
  
  .comment-list {
    .comment-item {
      margin-bottom: 30px;
      
      .comment-main {
        display: flex;
        gap: 15px;
        
        .comment-content {
          flex: 1;
          
          .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
            
            .username {
              font-weight: bold;
              color: #333;
            }
            
            .time {
              font-size: 12px;
              color: #999;
            }
          }
          
          .content {
            color: #666;
            line-height: 1.6;
            margin-bottom: 10px;
          }
          
          .actions {
            display: flex;
            gap: 20px;
            
            .action-btn {
              display: inline-flex;
              align-items: center;
              gap: 5px;
              font-size: 14px;
              color: #999;
              cursor: pointer;
              transition: all 0.3s;
              user-select: none;
              
              .icon-svg {
                width: 16px;
                height: 16px;
                transition: all 0.3s;
              }
              
              &:hover {
                color: #409eff;
              }
              
              &.like-btn {
                &.liked {
                  color: #409eff;
                  
                  .icon-svg {
                    animation: likeAnimation 0.3s ease;
                  }
                }
                
                &:hover {
                  color: #409eff;
                }
              }
              
              &.delete:hover {
                color: #f56c6c;
              }
            }
          }
          
          @keyframes likeAnimation {
            0% {
              transform: scale(1);
            }
            50% {
              transform: scale(1.3);
            }
            100% {
              transform: scale(1);
            }
          }
          
          .reply-input {
            margin-top: 15px;
            padding: 15px;
            background: #f5f7fa;
            border-radius: 4px;
            
            .reply-actions {
              margin-top: 10px;
              text-align: right;
            }
          }
        }
      }
      
      .reply-list {
        margin-top: 15px;
        margin-left: 55px;
        padding-left: 20px;
        border-left: 2px solid #e4e7ed;
        
        .reply-item {
          display: flex;
          gap: 10px;
          margin-bottom: 15px;
          
          .reply-content {
            flex: 1;
            
            .user-info {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 5px;
              
              .username {
                font-weight: bold;
                color: #333;
                font-size: 14px;
              }
              
              .reply-to {
                color: #409eff;
                font-size: 14px;
              }
              
              .time {
                font-size: 12px;
                color: #999;
              }
            }
            
            .content {
              color: #666;
              line-height: 1.6;
              margin-bottom: 8px;
              font-size: 14px;
            }
            
            .actions {
              display: flex;
              gap: 15px;
              
              .action-btn {
                display: inline-flex;
                align-items: center;
                gap: 5px;
                font-size: 13px;
                color: #999;
                cursor: pointer;
                transition: all 0.3s;
                user-select: none;
                
                .icon-svg {
                  width: 14px;
                  height: 14px;
                  transition: all 0.3s;
                }
                
                &:hover {
                  color: #409eff;
                }
                
                &.like-btn {
                  &.liked {
                    color: #409eff;
                    
                    .icon-svg {
                      animation: likeAnimation 0.3s ease;
                    }
                  }
                  
                  &:hover {
                    color: #409eff;
                  }
                }
                
                &.delete:hover {
                  color: #f56c6c;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
