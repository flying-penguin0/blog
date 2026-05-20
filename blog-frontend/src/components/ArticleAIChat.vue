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
      <div class="button-text">{{ ui.buttonText }}</div>
    </div>

    <el-dialog
      v-model="showDialog"
      width="680px"
      top="3vh"
      :close-on-click-modal="false"
      :append-to-body="true"
      :z-index="10000"
      :show-close="false"
      class="ai-dialog"
    >
      <div class="chat-body">
        <button
          type="button"
          class="dialog-close"
          :aria-label="ui.closeLabel"
          @click="showDialog = false"
        >
          <el-icon><Close /></el-icon>
        </button>

        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-panel">
            <img src="@/assets/images/dp.png" alt="DeepSeek" class="welcome-logo" />
            <h3 class="welcome-title">{{ ui.welcomeTitle }}</h3>
            <p class="welcome-desc">{{ ui.welcomeDesc }}</p>
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
              {{ ui.clearText }}
            </el-button>
          </div>
        </div>

        <div class="input-section">
          <div class="input-shell">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :placeholder="ui.placeholder"
              @keydown.enter="handleEnter"
              :disabled="isGenerating"
              class="message-input"
            />
            <div class="input-footer">
              <div class="input-hint">{{ ui.inputHint }}</div>
              <el-button
                type="primary"
                :loading="isGenerating"
                :disabled="!inputMessage.trim()"
                @click="sendMessage"
                class="send-btn"
              >
                <el-icon v-if="!isGenerating"><Promotion /></el-icon>
                {{ isGenerating ? ui.sendingText : ui.sendText }}
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
import { Close, Delete, Promotion } from '@element-plus/icons-vue'
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

const ui = {
  buttonText: '\u0041\u0049\u52a9\u624b',
  welcomeTitle: '\u4f60\u597d\uff01\u6211\u662f\u0041\u0049\u52a9\u624b',
  welcomeDesc: '\u6211\u53ef\u4ee5\u5e2e\u4f60\u7406\u89e3\u8fd9\u7bc7\u6587\u7ae0\uff0c\u8bd5\u8bd5\u4e0b\u9762\u7684\u95ee\u9898\u5427',
  clearText: '\u6e05\u7a7a\u5bf9\u8bdd',
  clearSuccess: '\u5bf9\u8bdd\u5df2\u6e05\u7a7a',
  placeholder: '\u8f93\u5165\u4f60\u7684\u95ee\u9898...',
  inputHint: 'Enter \u53d1\u9001\uff0cShift + Enter \u6362\u884c',
  sendingText: '\u751f\u6210\u4e2d...',
  sendText: '\u53d1\u9001',
  closeLabel: '\u5173\u95ed',
  errorTitle: '\u0041\u0049\u95ee\u7b54\u5931\u8d25',
  errorRetry: '\u0041\u0049\u95ee\u7b54\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5',
  iconSummary: '\uD83D\uDCDD',
  iconOpinion: '\uD83D\uDCA1',
  iconKnowledge: '\uD83D\uDD11',
  iconExample: '\uD83D\uDCD6',
  quickSummary: '\u603b\u7ed3\u8fd9\u7bc7\u6587\u7ae0',
  quickOpinion: '\u6587\u7ae0\u7684\u4e3b\u8981\u89c2\u70b9\u662f\u4ec0\u4e48\uff1f',
  quickKnowledge: '\u6709\u54ea\u4e9b\u5173\u952e\u77e5\u8bc6\u70b9\uff1f',
  quickExample: '\u80fd\u5426\u4e3e\u4e2a\u4f8b\u5b50\u8bf4\u660e\uff1f'
}

const quickQuestions = [
  { icon: ui.iconSummary, text: ui.quickSummary },
  { icon: ui.iconOpinion, text: ui.quickOpinion },
  { icon: ui.iconKnowledge, text: ui.quickKnowledge },
  { icon: ui.iconExample, text: ui.quickExample }
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
      console.error(ui.errorTitle + ':', error)
      ElMessage.error(ui.errorRetry)
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
  ElMessage.success(ui.clearSuccess)
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

.chat-body {
  position: relative;
  display: flex;
  flex-direction: column;
  height: min(82vh, 820px);
  min-height: 620px;
  background: linear-gradient(180deg, #f8fbff 0%, #f4f8fc 100%);
  max-width: 620px;
  margin: 0 auto;
  padding-top: 12px;
}

.dialog-close {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #8a97ab;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.18s ease, color 0.18s ease;

  &:hover {
    background: rgba(15, 23, 42, 0.06);
    color: #334155;
  }

  :deep(.el-icon) {
    font-size: 16px;
    font-weight: 600;
  }
}

.welcome-screen {
  flex: 1;
  padding: 26px 22px 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 12px;
}

.welcome-panel {
  padding: 24px 24px 20px;
  border-radius: 20px;
  text-align: center;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #e8eef6;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.welcome-logo {
  width: 34px;
  height: 34px;
  display: block;
  margin: 0 auto 10px;
  object-fit: contain;
}

.welcome-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2a37;
}

.welcome-desc {
  margin: 10px auto 0;
  max-width: 420px;
  font-size: 13px;
  line-height: 1.6;
  color: #6b778c;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.question-card {
  width: 100%;
  min-height: 84px;
  padding: 14px 16px;
  border: 1px solid #e7edf5;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 14px;
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
  line-height: 1.5;
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
  padding: 44px 22px 10px;
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
  border-radius: 16px;
}

.message-item.user .message-bubble {
  color: #fff;
  background: #409eff;
  box-shadow: 0 14px 28px rgba(64, 158, 255, 0.22);
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
  padding: 6px 22px 0;
  display: flex;
  justify-content: flex-end;
}

.input-section {
  padding: 4px 22px 18px;
}

.input-shell {
  border-radius: 18px;
  border: 1px solid #dfe8f3;
  background: #fff;
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.08);
  padding: 8px;
}

.message-input {
  :deep(.el-textarea__inner) {
    min-height: 72px !important;
    border: 0;
    box-shadow: none;
    resize: none;
    padding: 8px 10px 0;
    font-size: 13px;
    line-height: 1.6;
    color: #243244;
    background: transparent;
  }
}

.input-footer {
  margin-top: 8px;
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
  min-width: 108px;
  height: 42px;
  border: 0;
  border-radius: 999px;
  background: #409eff;
  box-shadow: 0 12px 24px rgba(64, 158, 255, 0.22);
  font-weight: 600;

  &:hover,
  &:focus-visible {
    background: #409eff;
  }
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
    height: 84vh;
    min-height: 600px;
  }

  .dialog-close {
    top: 8px;
    right: 8px;
  }

  .welcome-screen,
  .messages-wrapper,
  .quick-actions,
  .input-section {
    padding-left: 18px;
    padding-right: 18px;
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

<style lang="scss">
.ai-dialog.el-dialog {
  width: min(720px, 92vw);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 32px 88px rgba(15, 23, 42, 0.2);
}

.ai-dialog .el-dialog__header {
  display: none !important;
  height: 0 !important;
  min-height: 0 !important;
  padding: 0 !important;
  margin: 0 !important;
  border: 0 !important;
}

.ai-dialog .el-dialog__body {
  padding: 0 !important;
}
</style>

