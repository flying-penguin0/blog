<template>
  <div class="comment-section">
    <div class="section-header">
      <div>
        <h3 class="title">评论 ({{ totalCommentCount }})</h3>
        <p class="subtitle">留下你的看法，也看看大家的观点。</p>
      </div>
    </div>

    <div class="comment-editor">
      <div class="editor-avatar">
        <el-avatar :size="44" :src="userStore.user?.avatar || '/default-avatar.png'" />
      </div>
      <div class="editor-panel">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="4"
          :placeholder="userStore.token ? '写下你的评论...' : '请先登录后再发表评论'"
          maxlength="500"
          :show-word-limit="userStore.token"
          :disabled="!userStore.token"
          resize="none"
          @focus="handleCommentFocus"
        />
        <div class="editor-toolbar">
          <div class="editor-tip">
            <span class="tip-dot"></span>
            评论将显示省份、浏览器和系统信息
          </div>
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

    <div v-if="loading" class="feedback-state">加载中...</div>
    <div v-else-if="comments.length === 0" class="feedback-state empty-state">
      还没有评论，来坐沙发。
    </div>
    <div v-else class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-card">
        <div class="comment-main">
          <el-avatar :size="46" :src="comment.avatar || '/default-avatar.png'" />

          <div class="comment-body">
            <div class="comment-head">
              <div class="identity-row">
                <span class="username">{{ comment.nickname }}</span>
                <span class="time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-chip region-chip">
                  <span class="meta-icon">地区</span>
                  <span>{{ comment.province || '未知地区' }}</span>
                </span>
                <span class="meta-chip">
                  <span class="meta-icon" :class="browserClass(comment.browser)">
                    {{ browserShortName(comment.browser) }}
                  </span>
                  <span>{{ formatClientLabel(comment.browser, comment.browserVersion, 'Unknown Browser') }}</span>
                </span>
                <span class="meta-chip">
                  <span class="meta-icon" :class="osClass(comment.operatingSystem)">
                    {{ osShortName(comment.operatingSystem) }}
                  </span>
                  <span>{{ formatClientLabel(comment.operatingSystem, comment.operatingSystemVersion, 'Unknown OS') }}</span>
                </span>
              </div>
            </div>

            <p class="content">{{ comment.content }}</p>

            <div class="actions">
              <button
                type="button"
                class="action-btn like-btn"
                :class="{ liked: comment.isLiked }"
                @click="handleLike(comment)"
              >
                <svg v-if="comment.isLiked" class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311h-.3v428h472.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM112 528v364c0 17.7 14.3 32 32 32h65V496h-65c-17.7 0-32 14.3-32 32z" fill="currentColor" />
                </svg>
                <svg v-else class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                  <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4A106.62 106.62 0 0 0 471 99.9c-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353.4c-3.2 4.2-4.4 9.6-3.2 14.7l6.3 27.8c1.5 6.6 2.2 13.4 2.2 20.3 0 44.6-25.5 85.2-66.3 105.5-6.2 3.1-12.6 4.6-19 4.6H244V585.5l89.7-323.7c5.6-20.1 23.8-34.4 44.6-34.4 8.7 0 16.9 2.6 23.8 7.5 11.9 8.4 18.5 21.8 17.9 36.4l-10.8 219.2h462.4c7.4 0 14.6 2.2 20.5 6.2 24.1 16.2 37.4 43.8 37.4 77.7 0 22.1-7.6 43.7-21.1 60.8z" fill="currentColor" />
                </svg>
                <span>{{ comment.likeCount > 0 ? comment.likeCount : '点赞' }}</span>
              </button>

              <button type="button" class="action-btn" @click="handleReply(comment)">
                <el-icon><ChatDotRound /></el-icon>
                <span>回复</span>
              </button>

              <button
                v-if="canDelete(comment)"
                type="button"
                class="action-btn delete-btn"
                @click="handleDelete(comment)"
              >
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </button>
            </div>

            <div v-if="replyingTo === comment.id" class="reply-editor">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="3"
                :placeholder="`回复 @${comment.nickname}...`"
                maxlength="500"
                resize="none"
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

        <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-card">
            <el-avatar :size="36" :src="reply.avatar || '/default-avatar.png'" />

            <div class="reply-body">
              <div class="comment-head">
                <div class="identity-row">
                  <span class="username">{{ reply.nickname }}</span>
                  <span v-if="reply.parentUsername" class="reply-target">回复 @{{ reply.parentUsername }}</span>
                  <span class="time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <div class="meta-row compact">
                  <span class="meta-chip region-chip">
                    <span class="meta-icon">地区</span>
                    <span>{{ reply.province || '未知地区' }}</span>
                  </span>
                  <span class="meta-chip">
                    <span class="meta-icon" :class="browserClass(reply.browser)">
                      {{ browserShortName(reply.browser) }}
                    </span>
                    <span>{{ formatClientLabel(reply.browser, reply.browserVersion, 'Unknown Browser') }}</span>
                  </span>
                  <span class="meta-chip">
                    <span class="meta-icon" :class="osClass(reply.operatingSystem)">
                      {{ osShortName(reply.operatingSystem) }}
                    </span>
                    <span>{{ formatClientLabel(reply.operatingSystem, reply.operatingSystemVersion, 'Unknown OS') }}</span>
                  </span>
                </div>
              </div>

              <p class="content">{{ reply.content }}</p>

              <div class="actions compact">
                <button
                  type="button"
                  class="action-btn like-btn"
                  :class="{ liked: reply.isLiked }"
                  @click="handleLike(reply)"
                >
                  <svg v-if="reply.isLiked" class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311h-.3v428h472.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM112 528v364c0 17.7 14.3 32 32 32h65V496h-65c-17.7 0-32 14.3-32 32z" fill="currentColor" />
                  </svg>
                  <svg v-else class="icon-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                    <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4A106.62 106.62 0 0 0 471 99.9c-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353.4c-3.2 4.2-4.4 9.6-3.2 14.7l6.3 27.8c1.5 6.6 2.2 13.4 2.2 20.3 0 44.6-25.5 85.2-66.3 105.5-6.2 3.1-12.6 4.6-19 4.6H244V585.5l89.7-323.7c5.6-20.1 23.8-34.4 44.6-34.4 8.7 0 16.9 2.6 23.8 7.5 11.9 8.4 18.5 21.8 17.9 36.4l-10.8 219.2h462.4c7.4 0 14.6 2.2 20.5 6.2 24.1 16.2 37.4 43.8 37.4 77.7 0 22.1-7.6 43.7-21.1 60.8z" fill="currentColor" />
                  </svg>
                  <span>{{ reply.likeCount > 0 ? reply.likeCount : '点赞' }}</span>
                </button>

                <button
                  v-if="canDelete(reply)"
                  type="button"
                  class="action-btn delete-btn"
                  @click="handleDelete(reply)"
                >
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
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

const totalCommentCount = computed(() => {
  let count = comments.value.length
  comments.value.forEach(comment => {
    if (comment.replies && comment.replies.length > 0) {
      count += comment.replies.length
    }
  })
  return count
})

const canDelete = () => {
  return false
}

const browserShortName = (browser) => {
  const value = browser || ''
  if (value.includes('Safari')) return 'S'
  if (value.includes('Chrome')) return 'C'
  if (value.includes('Firefox')) return 'F'
  if (value.includes('Edge')) return 'E'
  if (value.includes('Opera')) return 'O'
  if (value.includes('Explorer')) return 'IE'
  return '?'
}

const osShortName = (operatingSystem) => {
  const value = operatingSystem || ''
  if (value.includes('macOS')) return 'AP'
  if (value.includes('Windows')) return 'W'
  if (value.includes('iOS')) return 'i'
  if (value.includes('Android')) return 'A'
  if (value.includes('Linux')) return 'L'
  return '?'
}

const browserClass = (browser) => {
  const value = browser || ''
  if (value.includes('Safari')) return 'safari'
  if (value.includes('Chrome')) return 'chrome'
  if (value.includes('Firefox')) return 'firefox'
  if (value.includes('Edge')) return 'edge'
  if (value.includes('Opera')) return 'opera'
  return 'neutral'
}

const osClass = (operatingSystem) => {
  const value = operatingSystem || ''
  if (value.includes('macOS') || value.includes('iOS')) return 'apple'
  if (value.includes('Windows')) return 'windows'
  if (value.includes('Android')) return 'android'
  if (value.includes('Linux')) return 'linux'
  return 'neutral'
}

const shortBrowserName = (browser) => {
  const value = browser || ''
  if (value.includes('Google Chrome')) return 'Chrome'
  if (value.includes('Microsoft Edge')) return 'Edge'
  if (value.includes('Mozilla Firefox')) return 'Firefox'
  if (value.includes('Internet Explorer')) return 'IE'
  return value || 'Unknown Browser'
}

const shortOsName = (operatingSystem) => {
  const value = operatingSystem || ''
  if (value.includes('Windows')) return 'Windows'
  if (value.includes('macOS')) return 'macOS'
  if (value.includes('iOS')) return 'iOS'
  if (value.includes('Android')) return 'Android'
  if (value.includes('Linux')) return 'Linux'
  return value || 'Unknown OS'
}

const compactVersion = (version) => {
  if (!version) return ''
  if (version.includes('/')) return version
  return version.split('.')[0]
}

const formatClientLabel = (name, version, fallback) => {
  const label = fallback.includes('Browser')
    ? shortBrowserName(name)
    : shortOsName(name)
  const shortVersion = compactVersion(version)
  return shortVersion ? `${label} ${shortVersion}` : label
}

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

const submitComment = async () => {
  if (!userStore.token) {
    handleCommentFocus()
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

    if (res.data === 'pending') {
      ElMessage.warning('评论已提交，包含敏感词需审核后显示')
    } else {
      ElMessage.success(res.message || '评论成功')
    }

    commentContent.value = ''
    loadComments()
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    submitting.value = false
  }
}

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

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

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

    if (res.data === 'pending') {
      ElMessage.warning('回复已提交，包含敏感词需审核后显示')
    } else {
      ElMessage.success(res.message || '回复成功')
    }

    cancelReply()
    loadComments()
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    submitting.value = false
  }
}

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

  const originalLiked = comment.isLiked
  const originalCount = comment.likeCount

  if (comment.isLiked) {
    comment.likeCount--
    comment.isLiked = false
  } else {
    comment.likeCount++
    comment.isLiked = true
  }

  try {
    await likeComment(comment.id)
  } catch (error) {
    comment.isLiked = originalLiked
    comment.likeCount = originalCount
    console.error('点赞失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

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
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;

  .title {
    margin: 0;
    font-size: 28px;
    font-weight: 700;
    color: #1f2a37;
  }

  .subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    color: #7b8794;
  }
}

.comment-editor {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 28px;
  padding: 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #f3f7fb 100%);
  border: 1px solid #e4ebf3;
  border-radius: 8px;
}

.editor-panel {
  min-width: 0;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.editor-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #7b8794;
}

.tip-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #5b8def;
  flex-shrink: 0;
}

.feedback-state {
  padding: 48px 20px;
  text-align: center;
  color: #7b8794;
  background: #fff;
  border: 1px dashed #d8e2ee;
  border-radius: 8px;
}

.empty-state {
  background: #f9fbfd;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-card {
  padding: 18px 18px 16px;
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.comment-main,
.reply-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
}

.comment-body,
.reply-body {
  min-width: 0;
}

.comment-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.identity-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 15px;
  font-weight: 700;
  color: #1f2a37;
}

.reply-target {
  color: #4f7cff;
  font-size: 13px;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: #8a94a6;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  &.compact {
    gap: 4px;
  }
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 24px;
  padding: 0 8px 0 5px;
  border-radius: 999px;
  background: #f5f8fc;
  color: #4f5d75;
  font-size: 11px;
  line-height: 1;
  white-space: nowrap;
  font-weight: 500;
}

.meta-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 999px;
  background: #d7e3f4;
  color: #3a4d68;
  font-size: 9px;
  font-weight: 700;
  flex-shrink: 0;

  &.chrome {
    background: #fff0d8;
    color: #bb6b00;
  }

  &.safari {
    background: #dceeff;
    color: #2479d9;
  }

  &.firefox {
    background: #ffe2d8;
    color: #d05b1e;
  }

  &.edge {
    background: #daf7f2;
    color: #0a8f7b;
  }

  &.opera {
    background: #ffe0e5;
    color: #cc3054;
  }

  &.apple {
    background: #eceff3;
    color: #111827;
  }

  &.windows {
    background: #ddeafe;
    color: #2563eb;
  }

  &.android {
    background: #e1f7dd;
    color: #2f8f2f;
  }

  &.linux {
    background: #f0f1f4;
    color: #4b5563;
  }
}

.region-chip .meta-icon {
  width: 16px;
  font-size: 8px;
}

.content {
  margin: 10px 0 0;
  color: #364152;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;

  &.compact {
    margin-top: 12px;
  }
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 0 10px;
  border: 0;
  border-radius: 999px;
  background: #f5f8fc;
  color: #5b6b80;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
  font-size: 12px;

  &:hover {
    background: #e8f1ff;
    color: #2d5bdb;
    transform: translateY(-1px);
  }

  &.liked {
    background: #e8f1ff;
    color: #2d5bdb;
  }

  &.delete-btn:hover {
    background: #ffe8e8;
    color: #d14343;
  }
}

.icon-svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.reply-editor {
  margin-top: 14px;
  padding: 14px;
  background: #f6f9fc;
  border: 1px solid #e1e8f0;
  border-radius: 8px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.reply-list {
  margin-top: 14px;
  margin-left: 44px;
  padding: 14px;
  background: #f8fbfe;
  border: 1px solid #e7eef6;
  border-radius: 8px;
}

.reply-card + .reply-card {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e7eef6;
}

:deep(.el-textarea__inner) {
  min-height: 96px !important;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.7;
  border-radius: 8px;
  box-shadow: none;
}

.reply-editor :deep(.el-textarea__inner) {
  min-height: 88px !important;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(91, 141, 239, 0.12);
}

@media (max-width: 768px) {
  .section-header .title {
    font-size: 24px;
  }

  .comment-editor,
  .comment-main,
  .reply-card {
    grid-template-columns: 1fr;
  }

  .editor-avatar {
    display: none;
  }

  .editor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .reply-list {
    margin-left: 0;
    padding: 14px;
  }

  .comment-card {
    padding: 18px;
  }
}
</style>
