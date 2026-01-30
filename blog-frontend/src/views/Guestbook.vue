<template>
  <div class="guestbook-page">
    <Header :transparent="true" />
    
    <div class="guestbook-container">
      <!-- 背景图片 -->
      <div class="background-image"></div>
      
      <!-- 弹幕容器 -->
      <vue3-danmaku
        ref="danmakuRef"
        v-model:danmus="danmus"
        :speeds="100"
        :channels="5"
        :loop="false"
        :useSlot="true"
        :debounce="200"
        class="danmaku-container"
      >
        <template v-slot:dm="{ danmu }">
          <div class="danmu-item">
            <el-avatar :size="30" :src="danmu.avatar">
              {{ danmu.nickname?.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="danmu-username">{{ danmu.nickname }}:</span>
            <span class="danmu-content">{{ danmu.content }}</span>
          </div>
        </template>
      </vue3-danmaku>
      
      <!-- 中心内容区域 -->
      <div class="center-content">
        <!-- 状态提示 -->
        <transition name="fade">
          <div v-if="showStatus" class="status-message">
            {{ statusMessage }}
          </div>
        </transition>
        
        <!-- 输入区域 -->
        <div class="input-wrapper" v-if="userStore.token">
          <div class="input-area">
            <el-input
              v-model="messageContent"
              placeholder="说点什么吧..."
              maxlength="100"
              show-word-limit
              class="message-input"
            >
              <template #suffix>
                <span class="word-count">{{ messageContent.length }}/100</span>
              </template>
            </el-input>
            <el-button 
              type="primary" 
              @click="submitMessage" 
              :loading="submitting"
              class="send-button"
            >
              发送
            </el-button>
          </div>
        </div>
        <div class="login-tip" v-else>
          <el-button type="primary" size="large" @click="$router.push('/login')">
            登录后留言
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { createMessage, getMessageList } from '@/api/message'
import { ElMessage } from 'element-plus'
import Header from '@/components/Header.vue'
import Vue3Danmaku from 'vue3-danmaku'

const userStore = useUserStore()
const messageContent = ref('')
const danmus = ref([])
const submitting = ref(false)
const danmakuRef = ref(null)
const statusMessage = ref('')
const showStatus = ref(false)
let loadInterval = null

const submitMessage = async () => {
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  
  submitting.value = true
  try {
    const res = await createMessage({ content: messageContent.value })
    const status = res.data
    
    // 显示状态提示
    showStatus.value = true
    if (status === 'approved') {
      statusMessage.value = '留言成功！'
      // 立即加载新留言
      await loadMessages()
    } else if (status === 'pending') {
      statusMessage.value = '留言包含敏感词，已提交审核'
    }
    
    // 3秒后隐藏提示
    setTimeout(() => {
      showStatus.value = false
    }, 3000)
    
    messageContent.value = ''
  } catch (error) {
    console.error('留言失败:', error)
    ElMessage.error(error.response?.data?.message || '留言失败')
  } finally {
    submitting.value = false
  }
}

const loadMessages = async () => {
  try {
    const res = await getMessageList({
      page: 1,
      size: 50,
      status: 'approved'
    })
    const messages = res.data.records || []
    
    // 转换为弹幕格式，每条留言只显示一次
    danmus.value = messages.map((msg, index) => ({
      id: `${msg.id}-${Date.now()}-${index}`,
      avatar: msg.avatar,
      nickname: msg.nickname || msg.username,
      content: msg.content
    }))
  } catch (error) {
    console.error('加载留言失败:', error)
  }
}

onMounted(() => {
  loadMessages()
  // 每30秒自动刷新留言
  loadInterval = setInterval(() => {
    loadMessages()
  }, 30000)
})

onUnmounted(() => {
  if (loadInterval) {
    clearInterval(loadInterval)
  }
})
</script>

<style scoped lang="scss">
.guestbook-page {
  min-height: 100vh;
  background: #e8ecf1;
}

.guestbook-container {
  position: relative;
  min-height: calc(100vh - 60px);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.background-image {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1920') center/cover;
  filter: brightness(0.8);
  z-index: 0;
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(180deg, 
      rgba(135, 206, 235, 0.3) 0%, 
      rgba(255, 182, 193, 0.3) 50%, 
      rgba(255, 218, 185, 0.3) 100%
    );
  }
}

.danmaku-container {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
}

.danmu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 14px;
  white-space: nowrap;
  
  .danmu-username {
    font-weight: 600;
    color: #409eff;
  }
  
  .danmu-content {
    color: #333;
  }
}

.center-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 550px;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-message {
  text-align: center;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  color: #409eff;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.input-wrapper {
  width: 100%;
}

.input-area {
  background: transparent;
  border-radius: 50px;
  padding: 4px 4px 4px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: none;
  backdrop-filter: none;
  border: 2px solid rgba(100, 180, 255, 0.6);
  width: 100%;
  height: 50px;
  
  .message-input {
    flex: 1;
    
    :deep(.el-input__wrapper) {
      border-radius: 0;
      box-shadow: none;
      border: none;
      padding: 0;
      background: transparent;
      font-size: 15px;
    }
    
    :deep(.el-input__inner) {
      color: #fff;
      font-weight: 400;
      padding: 0;
      
      &::placeholder {
        color: rgba(255, 255, 255, 0.5);
      }
    }
    
    :deep(.el-input__suffix) {
      display: none;
    }
    
    :deep(.el-input__count) {
      display: none;
    }
  }
  
  .word-count {
    color: rgba(255, 255, 255, 0.6);
    font-size: 13px;
    white-space: nowrap;
    padding: 0 12px;
    flex-shrink: 0;
  }
  
  .send-button {
    border-radius: 50px;
    padding: 0 28px;
    font-weight: 600;
    font-size: 15px;
    height: 42px;
    box-shadow: none;
    transition: all 0.3s;
    flex-shrink: 0;
    
    &:hover {
      transform: scale(1.05);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
}

.login-tip {
  text-align: center;
  
  .el-button {
    border-radius: 50px;
    padding: 16px 48px;
    font-size: 16px;
    font-weight: 600;
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
    backdrop-filter: none;
    background: rgba(64, 158, 255, 0.85);
    border: 2px solid rgba(100, 180, 255, 0.6);
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
      background: rgba(64, 158, 255, 1);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

@media (max-width: 768px) {
  .center-content {
    max-width: 100%;
    padding: 0 15px;
  }
  
  .input-area {
    padding: 4px 4px 4px 16px;
    height: 46px;
    gap: 6px;
    
    .message-input {
      :deep(.el-input__wrapper) {
        font-size: 14px;
      }
    }
    
    .word-count {
      font-size: 12px;
      padding: 0 8px;
    }
    
    .send-button {
      padding: 0 24px;
      font-size: 14px;
      height: 38px;
    }
  }
  
  .login-tip {
    .el-button {
      padding: 14px 40px;
      font-size: 15px;
    }
  }
}
</style>
