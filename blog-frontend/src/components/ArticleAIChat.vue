<template>
  <div class="article-ai-chat">
    <div
      v-if="!showDialog"
      class="ai-float-button"
      @click="showDialog = true"
    >
      <div class="button-icon">
        <img src="@/assets/images/dp.png" alt="AI" class="deepseek-icon" />
      </div>
      <div class="button-text">AI助手</div>
    </div>

    <el-dialog
      v-model="showDialog"
      width="680px"
      top="3vh"
      :close-on-click-modal="false"
      :append-to-body="true"
      :z-index="10000"
      class="ai-dialog"
    >
      <template #header>
        <div class="custom-header">
          <div class="header-left">
            <div class="header-icon-wrap">
              <img src="@/assets/images/dp.png" alt="AI" class="deepseek-icon-header" />
            </div>
            <div class="header-info">
              <div class="header-title">AI 智能助手</div>
              <div class="header-subtitle">基于文章内容的智能问答</div>
            </div>
          </div>
          <div class="header-tag">Article Q&amp;A</div>
        </div>
      </template>

      <div class="chat-body">
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-panel">
            <div class="welcome-icon">💬</div>
            <h3 class="welcome-title">你好！我是AI助手</h3>
            <p class="welcome-desc">我可以帮你理解这篇文章，试试下面的问题吧</p>
          </div>

          <div class="question-group">
            <div class="question-grid">
              <button
                v-for="(q, index) in quickQuestions"
                :key="index"
                type="button"
                class="question-card"
                @click="askQuestion(q.text)"
              >
                <div class="question-icon">{{ q.icon }}</div>
                <div class="question-text">{{ q.text }}</div>
              </button>
            </div>
          </div>
        </div>

        <div v-else class="chat-area">
          <div class="messages-wrapper" ref="messagesRef">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              :class="['message-item', msg.role]"
            >
              <div class="message-avatar">
                <el-avatar v-if="msg.role === 'user'" :size="36" :src="userAvatar">
                  <span>U</span>
                </el-avatar>
                <span v-else class="ai-avatar">
                  <img src="@/assets/images/dp.png" alt="AI" class="bubble-ai-icon" />
                </span>
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

            <div v-if="isGenerating" class="message-item assistant">
              <div class="message-avatar">
                <span class="ai-avatar">
                  <img src="@/assets/images/dp.png" alt="AI" class="bubble-ai-icon" />
                </span>
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

        <div class="input-section">
          <div class="input-shell">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="输入你的问题..."
              @keydown.enter.prevent="handleEnter"
              :disabled="isGenerating"
              class="message-input"
            />
            <div class="input-footer">
              <div class="input-hint">Enter 发送，Shift + Enter 换行</div>
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
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Promotion } from '@element-plus/icons-vue'
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

const userAvatar = computed(() => userStore.user?.avatar || '/default-avatar.png')

const quickQuestions = [
  { icon: '📝', text: '总结这篇文章' },
  { icon: '💡', text: '文章的主要观点是什么？' },
  { icon: '🔑', text: '有哪些关键知识点？' },
  { icon: '📖', text: '能否举个例子说明？' }
]

const sendMessage = () => {
  if (!inputMessage.value.trim() || isGenerating.value) return
  askQuestion(inputMessage.value.trim())
}

const handleEnter = (e) => {
  if (e.shiftKey) return
  e.preventDefault()
  sendMessage()
}

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

const clearChat = () => {
  if (messages.value.length === 0) return
  messages.value = []
  ElMessage.success('对话已清空')
}

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
  right: 24px;
  bottom: 24px;
  width: 68px;
  height: 68px;
  background: #fff;
  border: 1px solid #e5eef8;
  border-radius: 20px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.14);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  z-index: 9999;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  .button-icon {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .deepseek-icon {
    width: 34px;
    height: 34px;
    object-fit: contain;
  }

  .button-text {
    font-size: 11px;
    color: #3278d8;
    font-weight: 600;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 44px rgba(15, 23, 42, 0.18);
  }
}

.ai-dialog {
  :deep(.el-dialog) {
    width: min(680px, 92vw);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 28px 80px rgba(15, 23, 42, 0.22);
  }

  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
    border-bottom: 1px solid #edf2f7;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__headerbtn) {
    top: 18px;
    right: 18px;
  }
}

.custom-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 22px 16px;
  background:
    radial-gradient(circle at top left, rgba(139, 92, 246, 0.14), transparent 34%),
    linear-gradient(135deg, #f8fbff 0%, #eef6ff 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.header-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffffff 0%, #e7f1ff 100%);
  border: 1px solid rgba(91, 141, 239, 0.16);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.deepseek-icon-header {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.header-info {
  min-width: 0;
}

.header-title {
  font-size: 20px;
  line-height: 1.2;
  font-weight: 700;
  color: #1f2a37;
}

.header-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #66758a;
}

.header-tag {
  flex-shrink: 0;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #3278d8;
  background: rgba(80, 142, 255, 0.1);
  border: 1px solid rgba(80, 142, 255, 0.16);
}

.chat-body {
  display: flex;
  flex-direction: column;
  height: 72vh;
  min-height: 540px;
  max-height: 760px;
  background: #f7f9fc;
}

.welcome-screen {
  flex: 1;
  padding: 18px 22px 14px;
  overflow-y: auto;
}

.welcome-panel {
  padding: 20px 18px;
  border-radius: 16px;
  text-align: center;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #e8eef6;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
}

.welcome-icon {
  font-size: 30px;
  line-height: 1;
  margin-bottom: 10px;
}

.welcome-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2a37;
}

.welcome-desc {
  margin: 8px auto 0;
  max-width: 400px;
  font-size: 13px;
  line-height: 1.6;
  color: #6b778c;
}

.question-group {
  margin-top: 14px;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.question-card {
  width: 100%;
  min-height: 84px;
  padding: 14px;
  border: 1px solid #e7edf5;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #d4e5ff;
    box-shadow: 0 14px 28px rgba(50, 120, 216, 0.12);
  }
}

.question-icon {
  font-size: 20px;
  line-height: 1;
}

.question-text {
  font-size: 13px;
  line-height: 1.4;
  font-weight: 600;
  color: #334155;
}

.chat-area {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.messages-wrapper {
  flex: 1;
  min-height: 0;
  padding: 16px 22px 12px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  flex-shrink: 0;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ffffff 0%, #eef5ff 100%);
  border: 1px solid #dce8f8;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.06);
}

.bubble-ai-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.message-bubble {
  max-width: min(78%, 560px);
  padding: 12px 14px;
  border-radius: 14px;
}

.message-item.user .message-bubble {
  color: #fff;
  background: linear-gradient(135deg, #4d9cff 0%, #3278d8 100%);
  box-shadow: 0 14px 28px rgba(50, 120, 216, 0.18);
}

.message-item.assistant .message-bubble {
  background: #fff;
  border: 1px solid #e8eef6;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.message-text {
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-markdown {
  font-size: 13px;

  :deep(.md-editor-preview-wrapper) {
    padding: 0;
    background: transparent;
  }

  :deep(.md-editor-preview) {
    font-size: 13px;
    color: #243244;
    line-height: 1.75;
  }

  :deep(p) {
    margin: 0.65em 0;
  }

  :deep(pre) {
    margin: 0.9em 0;
    border-radius: 10px;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 1.5em;
  }
}

.typing-indicator {
  display: inline-flex;
  gap: 6px;
  padding: 6px 0;

  span {
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: #5b8def;
    animation: typing 1.2s infinite ease-in-out;
  }

  span:nth-child(2) {
    animation-delay: 0.2s;
  }

  span:nth-child(3) {
    animation-delay: 0.4s;
  }
}

.quick-actions {
  padding: 8px 22px 0;
  display: flex;
  justify-content: flex-end;
}

.input-section {
  padding: 12px 22px 18px;
}

.input-shell {
  border-radius: 16px;
  border: 1px solid #dfe8f3;
  background: #fff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.06);
  padding: 8px;
}

.message-input {
  :deep(.el-textarea__inner) {
    min-height: 68px !important;
    border: 0;
    box-shadow: none;
    resize: none;
    padding: 8px 8px 0;
    font-size: 13px;
    line-height: 1.6;
    color: #243244;
  }
}

.input-footer {
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.input-hint {
  font-size: 11px;
  color: #8a94a6;
  padding-left: 8px;
}

.send-btn {
  min-width: 92px;
  height: 36px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #4d9cff 0%, #3278d8 100%);
  box-shadow: 0 12px 24px rgba(50, 120, 216, 0.18);
  font-weight: 600;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.45;
  }

  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .chat-body {
    height: 78vh;
    min-height: 520px;
  }

  .custom-header,
  .welcome-screen,
  .messages-wrapper,
  .quick-actions,
  .input-section {
    padding-left: 18px;
    padding-right: 18px;
  }

  .custom-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-title {
    font-size: 22px;
  }

  .question-grid {
    grid-template-columns: 1fr;
  }

  .message-bubble {
    max-width: calc(100% - 24px);
  }

  .input-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .input-hint {
    padding-left: 0;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
