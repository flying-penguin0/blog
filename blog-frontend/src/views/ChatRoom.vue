<template>
  <div>
    <!-- 导航栏 -->
    <Header />
    
    <div class="chatroom-page" :style="{ backgroundImage: `url(${randomBackground})` }">
      <div class="chatroom-container">
        <div class="chatroom-header">
          <h2>💬 博客聊天室</h2>
          <span class="online-count">在线: {{ onlineUsers.length }}</span>
        </div>

        <div class="chatroom-body">
          <!-- 消息区域 -->
          <div class="message-area" ref="messageArea">
            <div v-if="messages.length === 0" class="empty-message">
              暂无消息，快来聊天吧~
            </div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              :class="['message-item', msg.userId === currentUserId ? 'own' : 'other']"
            >
              <div class="user-message">
                <img :src="msg.avatar || '/default-avatar.png'" class="avatar" />
                <div class="message-content">
                  <div class="message-header">
                    <span class="nickname">{{ msg.nickname || msg.username }}</span>
                    <span class="time">{{ formatTime(msg.createTime) }}</span>
                  </div>
                  <div class="message-text">{{ msg.content }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 在线用户 -->
          <div class="online-users">
            <h3>在线用户 ({{ onlineUsers.length }})</h3>
            <div class="user-list">
              <div v-for="user in onlineUsers" :key="user.id" class="user-item">
                <img :src="user.avatar || '/default-avatar.png'" class="user-avatar" />
                <span class="user-nickname">{{ user.nickname || user.username }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <input
            v-model="inputMessage"
            @keyup.enter="sendMessage"
            placeholder="输入消息... (按Enter发送)"
            maxlength="500"
          />
          <button @click="sendMessage" :disabled="!inputMessage.trim()">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Modal } from 'ant-design-vue'
import { getHistory, getOnlineUsers, userOnline, userOffline } from '@/api/chatroom'
import ChatWebSocket from '@/utils/chatWebSocket'
import Header from '@/components/Header.vue'

// 导入背景图片
import bg1 from '@/assets/images/bg1.png'
import bg2 from '@/assets/images/bg2.png'
import bg3 from '@/assets/images/bg3.png'
import bg4 from '@/assets/images/bg4.png'

const router = useRouter()
const userStore = useUserStore()
const currentUserId = userStore.user?.id

const messages = ref([])
const onlineUsers = ref([])
const inputMessage = ref('')
const messageArea = ref(null)

// 背景图片数组
const backgroundImages = [bg1, bg2, bg3, bg4]

// 随机选择一张背景图片
const randomBackground = computed(() => {
  const randomIndex = Math.floor(Math.random() * backgroundImages.length)
  return backgroundImages[randomIndex]
})

// 连接WebSocket
onMounted(async () => {
  if (!userStore.token) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    // 加载历史消息
    const res = await getHistory()
    messages.value = res.data || []
    scrollToBottom()

    // 用户上线
    await userOnline()

    // 加载在线用户
    await loadOnlineUsers()

    // 连接WebSocket
    ChatWebSocket.connect(onMessageReceived, onUserStatusChanged, currentUserId)
    
    // 监听页面关闭/刷新事件
    window.addEventListener('beforeunload', handleBeforeUnload)
  } catch (error) {
    console.error('初始化聊天室失败:', error)
    alert('连接聊天室失败')
  }
})

onUnmounted(() => {
  cleanup()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 页面关闭前的清理
const handleBeforeUnload = (e) => {
  // 只需要断开WebSocket，后端会自动检测到断开并处理下线
  ChatWebSocket.disconnect()
}

// 清理函数
const cleanup = () => {
  try {
    // 断开WebSocket连接，后端会自动处理用户下线
    ChatWebSocket.disconnect()
  } catch (error) {
    console.error('断开连接失败:', error)
  }
}

// 接收消息
const onMessageReceived = (msg) => {
  // 只显示已通过审核的消息
  if (msg.status === 'approved') {
    messages.value.push(msg)
    scrollToBottom()
  } else if (msg.userId === currentUserId) {
    // 如果是当前用户发送的消息且包含敏感词
    if (msg.status === 'pending') {
      Modal.warning({
        title: '消息审核提示',
        content: '您的消息包含敏感词，已提交审核，管理员审核通过后将显示在聊天室中。',
        centered: true,
        okText: '我知道了'
      })
    }
  }
}

// 用户状态变化
const onUserStatusChanged = async () => {
  await loadOnlineUsers()
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  ChatWebSocket.sendMessage({
    content: inputMessage.value.trim()
  }, currentUserId)

  inputMessage.value = ''
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageArea.value) {
      messageArea.value.scrollTop = messageArea.value.scrollHeight
    }
  })
}

// 加载在线用户
const loadOnlineUsers = async () => {
  try {
    const res = await getOnlineUsers()
    onlineUsers.value = res.data || []
  } catch (error) {
    console.error('加载在线用户失败:', error)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }

  // 昨天
  if (diff < 48 * 60 * 60 * 1000) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }

  // 更早
  return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.chatroom-page {
  min-height: calc(100vh - 64px);
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  padding: 20px;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      linear-gradient(135deg, rgba(64, 158, 255, 0.6) 0%, rgba(102, 177, 255, 0.6) 100%),
      radial-gradient(circle at 20% 50%, rgba(64, 158, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(102, 177, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 40% 20%, rgba(64, 158, 255, 0.08) 0%, transparent 50%);
    pointer-events: none;
  }
}

.chatroom-container {
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.chatroom-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;

  h2 {
    margin: 0;
    font-size: 20px;
  }

  .online-count {
    font-size: 14px;
    background: rgba(255, 255, 255, 0.2);
    padding: 4px 12px;
    border-radius: 12px;
  }
}

.chatroom-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.message-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: linear-gradient(to bottom, #fafafa 0%, #f5f7fa 100%);
  position: relative;

  .empty-message {
    text-align: center;
    color: #999;
    padding: 40px;
    font-size: 14px;
  }

  .message-item {
    margin-bottom: 16px;
    animation: fadeIn 0.3s;

    &.own .user-message {
      flex-direction: row-reverse;

      .message-content {
        align-items: flex-end;
        margin-right: 12px;
        margin-left: 0;
      }

      .message-text {
        background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
        color: #fff;
      }
    }
  }

  .system-message {
    text-align: center;
    color: #999;
    font-size: 12px;
    padding: 8px;
  }

  .user-message {
    display: flex;
    align-items: flex-start;

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      flex-shrink: 0;
    }

    .message-content {
      margin-left: 12px;
      max-width: 60%;

      .message-header {
        display: flex;
        gap: 8px;
        margin-bottom: 4px;
        align-items: center;

        .nickname {
          font-weight: 500;
          font-size: 14px;
          color: #333;
        }

        .time {
          color: #999;
          font-size: 12px;
        }
      }

      .message-text {
        background: #fff;
        padding: 10px 14px;
        border-radius: 8px;
        word-break: break-word;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        line-height: 1.5;
      }
    }
  }
}

.online-users {
  width: 240px;
  border-left: 1px solid #eee;
  padding: 20px;
  overflow-y: auto;
  background: #fff;

  h3 {
    font-size: 14px;
    margin: 0 0 16px 0;
    color: #333;
    font-weight: 600;
  }

  .user-list {
    .user-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 8px;
      transition: all 0.3s;

      &:hover {
        background: #f5f5f5;
      }

      .user-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .user-nickname {
        font-size: 14px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.input-area {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  background: #fff;

  input {
    flex: 1;
    padding: 12px 16px;
    border: 1px solid #ddd;
    border-radius: 20px;
    font-size: 14px;
    transition: all 0.3s;

    &:focus {
      outline: none;
      border-color: #409eff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
    }
  }

  button {
    padding: 12px 32px;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: #fff;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .chatroom-body {
    flex-direction: column;
  }

  .online-users {
    width: 100%;
    max-height: 150px;
    border-left: none;
    border-top: 1px solid #eee;
  }

  .message-area .user-message .message-content {
    max-width: 80%;
  }
}
</style>
