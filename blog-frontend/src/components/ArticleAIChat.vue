<template>
  <div class="article-ai-chat">
    <!-- 悬浮按钮 -->
    <div 
      v-if="!showDialog"
      class="ai-float-button" 
      @click="showDialog = true"
    >
      <div class="button-icon">🤖</div>
      <div class="button-text">AI助手</div>
    </div>

    <!-- 对话框 -->
    <el-dialog
      v-model="showDialog"
      width="650px"
      :close-on-click-modal="false"
      :append-to-body="true"
      :z-index="10000"
      class="ai-dialog"
    >
      <template #header>
        <div class="custom-header">
          <div class="header-left">
            <span class="header-icon">🤖</span>
            <div class="header-info">
              <div class="header-title">AI 智能助手</div>
              <div class="header-subtitle">基于文章内容的智能问答</div>
            </div>
          </div>
        </div>
      </template>
      
      <div class="chat-body">
        <!-- 欢迎界面 -->
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-icon">💬</div>
          <h3 class="welcome-title">你好！我是AI助手</h3>
          <p class="welcome-desc">我可以帮你理解这篇文章，试试下面的问题吧</p>
          
          <div class="question-grid">
            <div 
              v-for="(q, index) in quickQuestions" 
              :key="index"
              class="question-card"
              @click="askQuestion(q.text)"
            >
              <div class="question-icon">{{ q.icon }}</div>
              <div class="question-text">{{ q.text }}</div>
            </div>
          </div>
        </div>

        <!-- 对话区域 -->
        <div v-else class="chat-area">
          <div class="messages-wrapper" ref="messagesRef">
            <div 
              v-for="(msg, index) in messages" 
              :key="index" 
              :class="['message-item', msg.role]"
            >
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36" :src="userAvatar">
                  <span>👤</span>
                </el-avatar>
                <span v-else class="ai-avatar">🤖</span>
              </div>
              <div class="message-bubble">
                <div v-if="msg.role === 'user'" class="message-text">
                  {{ msg.content }}
                </div>
                <div v-else class="message-markdown">
                  <MdPreview 
                    :model-value="msg.content"
                    :preview-theme="'github'"
                    :code-theme="'atom-one-dark'"
                  />
                </div>
              </div>
            </div>
            
            <!-- 生成中 -->
            <div v-if="isGenerating" class="message-item assistant">
              <div class="message-avatar">
                <span class="ai-avatar">🤖</span>
              </div>
              <div class="message-bubble generating">
                <div class="message-markdown">
                  <MdPreview 
                    v-if="currentResponse"
                    :model-value="currentResponse"
                    :preview-theme="'github'"
                    :code-theme="'atom-one-dark'"
                  />
                  <div v-else class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 快捷操作 -->
          <div class="quick-actions">
            <el-button 
              size="small" 
              text
              @click="clearChat"
              :icon="Delete"
            >
              清空对话
            </el-button>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-section">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入你的问题..."
            @keydown.enter.prevent="handleEnter"
            :disabled="isGenerating"
            class="message-input"
          />
          <el-button 
            type="primary"
            :loading="isGenerating"
            :disabled="!inputMessage.trim()"
            @click="sendMessage"
            class="send-btn"
          >
            <el-icon v-if="!isGenerating"><Promotion /></el-icon>
            {{ isGenerating ? '生成中...' : '发送' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Loading, Close, Delete, Promotion } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { articleQA } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  articleContent: {
    type: String,
    required: true
  }
})

const userStore = useUserStore()
const showDialog = ref(false)
const messages = ref([])
const inputMessage = ref('')
const currentResponse = ref('')
const isGenerating = ref(false)
const messagesRef = ref(null)

// 获取用户头像
const userAvatar = computed(() => {
  return userStore.user?.avatar || '/default-avatar.png'
})

// 快捷问题
const quickQuestions = [
  { icon: '📝', text: '总结这篇文章' },
  { icon: '💡', text: '文章的主要观点是什么？' },
  { icon: '🔑', text: '有哪些关键知识点？' },
  { icon: '📖', text: '能否举个例子说明？' }
]

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || isGenerating.value) return
  const question = inputMessage.value.trim()
  askQuestion(question)
}

// 处理回车
const handleEnter = (e) => {
  if (e.shiftKey) {
    return // Shift+Enter 换行
  }
  e.preventDefault()
  sendMessage()
}

// 提问
const askQuestion = (question) => {
  messages.value.push({ role: 'user', content: question })
  inputMessage.value = ''
  currentResponse.value = ''
  isGenerating.value = true

  scrollToBottom()

  articleQA(
    props.articleContent,
    question,
    (text) => {
      currentResponse.value += text
      scrollToBottom()
    },
    (error) => {
      console.error('AI 问答失败:', error)
      ElMessage.error('AI 问答失败，请重试')
      isGenerating.value = false
    },
    () => {
      messages.value.push({ role: 'assistant', content: currentResponse.value })
      currentResponse.value = ''
      isGenerating.value = false
      scrollToBottom()
    }
  )
}

// 清空对话
const clearChat = () => {
  if (messages.value.length === 0) return
  
  messages.value = []
  ElMessage.success('对话已清空')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped lang="scss">
.ai-float-button {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  z-index: 9999;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  .button-icon {
    font-size: 28px;
    line-height: 1;
  }
  
  .button-text {
    font-size: 11px;
    color: #fff;
    font-weight: 500;
  }
  
  &:hover {
    transform: scale(1.1) rotate(5deg);
    box-shadow: 0 12px 32px rgba(64, 158, 255, 0.5);
  }
  
  &:active {
    transform: scale(1.05);
  }
}

.ai-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
  }
  
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.custom-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .header-icon {
      font-size: 32px;
      line-height: 1;
    }
    
    .header-info {
      .header-title {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
        line-height: 1.3;
      }
      
      .header-subtitle {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
        margin-top: 2px;
      }
    }
  }
}

.chat-body {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  height: 550px;
  overflow: hidden;
}

.welcome-screen {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 24px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }
  
  .welcome-icon {
    font-size: 48px;
    margin-bottom: 12px;
    margin-top: 10px;
    animation: bounce 2s infinite;
  }
  
  .welcome-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 6px;
  }
  
  .welcome-desc {
    font-size: 13px;
    color: #909399;
    margin: 0 0 20px;
  }
  
  .question-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    width: 100%;
    max-width: 500px;
    margin-bottom: 20px;
    
    .question-card {
      background: #fff;
      border-radius: 12px;
      padding: 18px;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 8px;
      
      .question-icon {
        font-size: 28px;
      }
      
      .question-text {
        font-size: 13px;
        color: #606266;
        font-weight: 500;
        line-height: 1.4;
      }
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 16px rgba(64, 158, 255, 0.2);
      }
    }
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 450px;
  
  .messages-wrapper {
    flex: 1;
    overflow-y: auto;
    padding: 20px 24px;
    min-height: 0;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 3px;
    }
    
    .message-item {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      animation: slideIn 0.3s ease;
      
      .message-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        .ai-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
      }
      
      .message-bubble {
        max-width: 75%;
        padding: 12px 16px;
        border-radius: 12px;
        
        .message-text {
          font-size: 14px;
          line-height: 1.6;
          color: #303133;
        }
        
        .message-markdown {
          font-size: 14px;
          
          :deep(.md-editor-preview-wrapper) {
            padding: 0;
            background: transparent;
            
            p {
              margin: 8px 0;
              line-height: 1.7;
            }
            
            code {
              background: rgba(0, 0, 0, 0.05);
              padding: 2px 6px;
              border-radius: 4px;
              font-size: 13px;
            }
            
            pre {
              margin: 12px 0;
              border-radius: 8px;
            }
            
            ul, ol {
              margin: 8px 0;
              padding-left: 20px;
            }
            
            li {
              margin: 4px 0;
            }
          }
        }
      }
      
      &.user {
        flex-direction: row-reverse;
        
        .message-bubble {
          background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
          color: #fff;
          
          .message-text {
            color: #fff;
          }
        }
      }
      
      &.assistant {
        .message-bubble {
          background: #fff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          
          &.generating {
            .typing-indicator {
              display: flex;
              gap: 4px;
              padding: 8px 0;
              
              span {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: #409eff;
                animation: typing 1.4s infinite;
                
                &:nth-child(2) {
                  animation-delay: 0.2s;
                }
                
                &:nth-child(3) {
                  animation-delay: 0.4s;
                }
              }
            }
          }
        }
      }
    }
  }
  
  .quick-actions {
    padding: 8px 24px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    justify-content: flex-end;
    background: #fff;
    position: relative;
    z-index: 1;
  }
}

.input-section {
  padding: 14px 24px 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
  
  .message-input {
    flex: 1;
    
    :deep(.el-textarea__inner) {
      border-radius: 12px;
      border: 2px solid #e4e7ed;
      padding: 12px;
      font-size: 14px;
      resize: none;
      
      &:focus {
        border-color: #667eea;
      }
    }
  }
  
  .send-btn {
    height: 40px;
    padding: 0 24px;
    border-radius: 20px;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    border: none;
    font-weight: 500;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}
</style>
