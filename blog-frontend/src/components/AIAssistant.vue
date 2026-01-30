<template>
  <div class="ai-assistant">
    <div class="ai-header">
      <span class="ai-title">AI 写作助手</span>
      <el-button text @click="$emit('close')" class="close-btn">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    
    <div class="ai-content">
      <el-tabs v-model="activeTab" class="ai-tabs">
        <el-tab-pane label="生成内容" name="content">
          <div class="tab-content">
            <div class="input-section">
              <el-input
                v-model="outline"
                type="textarea"
                :rows="8"
                placeholder="请输入文章大纲或主题...&#10;&#10;例如：&#10;# Spring Boot 入门指南&#10;## 1. 环境搭建&#10;## 2. 创建项目"
                :disabled="isGenerating"
                class="input-textarea"
              />
            </div>
            <el-button 
              type="primary" 
              @click="handleGenerateContent"
              :loading="isGenerating"
              :disabled="!outline.trim()"
              size="large"
              class="action-btn"
            >
              <el-icon v-if="!isGenerating"><MagicStick /></el-icon>
              {{ isGenerating ? '生成中...' : '生成内容' }}
            </el-button>
            
            <div class="result-section">
              <div class="result-header">
                <span class="result-title">
                  {{ isGenerating ? '正在生成...' : (generatedContent ? '生成结果' : '生成结果') }}
                </span>
                <el-button 
                  v-if="!isGenerating && generatedContent"
                  type="primary" 
                  size="small" 
                  @click="insertToEditor"
                  :icon="Plus"
                >
                  插入到编辑器
                </el-button>
              </div>
              <div class="result-body">
                <div v-if="!generatedContent && !isGenerating" class="empty-tip">
                  点击"生成内容"按钮开始创作
                </div>
                <div v-else-if="!generatedContent && isGenerating" class="generating-tip">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>AI 正在思考中，请稍候...</span>
                </div>
                <MdPreview 
                  v-else
                  :model-value="generatedContent"
                  :preview-theme="'github'"
                  :code-theme="'github'"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="内容续写" name="continue">
          <div class="tab-content">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="tip-alert"
            >
              <template #title>
                <span class="tip-text">AI 将基于编辑器中的当前内容进行续写</span>
              </template>
            </el-alert>
            
            <el-button 
              type="primary" 
              @click="handleContinueWriting"
              :loading="isGenerating"
              :disabled="!props.editorContent.trim()"
              size="large"
              class="action-btn"
            >
              <el-icon v-if="!isGenerating"><EditPen /></el-icon>
              {{ isGenerating ? '续写中...' : '开始续写' }}
            </el-button>
            
            <div class="result-section">
              <div class="result-header">
                <span class="result-title">
                  {{ isGenerating ? '正在续写...' : (generatedContent ? '续写内容' : '续写内容') }}
                </span>
                <el-button 
                  v-if="!isGenerating && generatedContent"
                  type="primary" 
                  size="small" 
                  @click="insertToEditor"
                  :icon="Plus"
                >
                  插入到编辑器
                </el-button>
              </div>
              <div class="result-body">
                <div v-if="!generatedContent && !isGenerating" class="empty-tip">
                  点击"开始续写"按钮继续创作
                </div>
                <div v-else-if="!generatedContent && isGenerating" class="generating-tip">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>AI 正在思考中，请稍候...</span>
                </div>
                <MdPreview 
                  v-else
                  :model-value="generatedContent"
                  :preview-theme="'github'"
                  :code-theme="'github'"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="内容优化" name="optimize">
          <div class="tab-content">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="tip-alert"
            >
              <template #title>
                <span class="tip-text">AI 将优化编辑器中的当前内容</span>
              </template>
            </el-alert>
            
            <el-button 
              type="primary" 
              @click="handleOptimize"
              :loading="isGenerating"
              :disabled="!props.editorContent.trim()"
              size="large"
              class="action-btn"
            >
              <el-icon v-if="!isGenerating"><Star /></el-icon>
              {{ isGenerating ? '优化中...' : '开始优化' }}
            </el-button>
            
            <div class="result-section">
              <div class="result-header">
                <span class="result-title">
                  {{ isGenerating ? '正在优化...' : (generatedContent ? '优化后的内容' : '优化后的内容') }}
                </span>
                <el-button 
                  v-if="!isGenerating && generatedContent"
                  type="warning" 
                  size="small" 
                  @click="replaceEditor"
                  :icon="RefreshRight"
                >
                  替换编辑器内容
                </el-button>
              </div>
              <div class="result-body">
                <div v-if="!generatedContent && !isGenerating" class="empty-tip">
                  点击"开始优化"按钮优化内容
                </div>
                <div v-else-if="!generatedContent && isGenerating" class="generating-tip">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>AI 正在思考中，请稍候...</span>
                </div>
                <MdPreview 
                  v-else
                  :model-value="generatedContent"
                  :preview-theme="'github'"
                  :code-theme="'github'"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, MagicStick, EditPen, Star, Plus, RefreshRight, Loading } from '@element-plus/icons-vue'
import { 
  generateContent, 
  continueWriting, 
  optimizeContent 
} from '@/api/ai'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const props = defineProps({
  editorContent: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['insert', 'replace', 'close'])

const activeTab = ref('content')
const isGenerating = ref(false)
const outline = ref('')
const generatedContent = ref('')
const resultBodyRef = ref(null)

// 监听生成内容的变化，自动滚动到底部
watch(generatedContent, async () => {
  if (generatedContent.value && isGenerating.value) {
    await nextTick()
    const resultBody = document.querySelector('.result-body')
    if (resultBody) {
      resultBody.scrollTop = resultBody.scrollHeight
    }
  }
})

// 生成内容
const handleGenerateContent = () => {
  if (!outline.value.trim() || isGenerating.value) return

  generatedContent.value = ''
  isGenerating.value = true

  generateContent(
    outline.value,
    (text) => {
      generatedContent.value += text
    },
    (error) => {
      console.error('生成内容失败:', error)
      ElMessage.error('生成内容失败，请重试')
      isGenerating.value = false
    },
    () => {
      isGenerating.value = false
      ElMessage.success('内容生成完成')
    }
  )
}

// 续写
const handleContinueWriting = () => {
  if (!props.editorContent.trim() || isGenerating.value) return

  generatedContent.value = ''
  isGenerating.value = true

  continueWriting(
    props.editorContent,
    (text) => {
      generatedContent.value += text
    },
    (error) => {
      console.error('续写失败:', error)
      ElMessage.error('续写失败，请重试')
      isGenerating.value = false
    },
    () => {
      isGenerating.value = false
      ElMessage.success('续写完成')
    }
  )
}

// 优化
const handleOptimize = () => {
  if (!props.editorContent.trim() || isGenerating.value) return

  generatedContent.value = ''
  isGenerating.value = true

  optimizeContent(
    props.editorContent,
    (text) => {
      generatedContent.value += text
    },
    (error) => {
      console.error('优化失败:', error)
      ElMessage.error('优化失败，请重试')
      isGenerating.value = false
    },
    () => {
      isGenerating.value = false
      ElMessage.success('优化完成')
    }
  )
}

// 插入到编辑器
const insertToEditor = () => {
  emit('insert', generatedContent.value)
  ElMessage.success('已插入到编辑器')
}

// 替换编辑器内容
const replaceEditor = () => {
  emit('replace', generatedContent.value)
  ElMessage.success('已替换编辑器内容')
}

// 切换标签时清空生成的内容
watch(activeTab, () => {
  generatedContent.value = ''
})
</script>

<style scoped lang="scss">
.ai-assistant {
  height: 1060px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #409eff;
  flex-shrink: 0;
  
  .ai-title {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
  }
  
  .close-btn {
    color: #fff;
    
    &:hover {
      color: #f0f0f0;
    }
  }
}

.ai-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.ai-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 20px;
    background: #f5f7fa;
    flex-shrink: 0;
  }
  
  :deep(.el-tabs__nav-wrap) {
    padding: 10px 0;
  }
  
  :deep(.el-tabs__item) {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    flex: 1;
    text-align: center;
    justify-content: center;
    
    &.is-active {
      color: #409eff;
    }
  }
  
  :deep(.el-tabs__nav) {
    width: 100%;
    display: flex;
  }
  
  :deep(.el-tabs__active-bar) {
    transition: all 0.3s;
  }
  
  :deep(.el-tabs__content) {
    flex: 1;
    overflow: hidden;
  }
  
  :deep(.el-tab-pane) {
    height: 100%;
  }
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
  overflow: hidden;
}

.input-section {
  margin-bottom: 10px;
  flex-shrink: 0;
  
  .input-textarea {
    :deep(.el-textarea__inner) {
      font-size: 14px;
      line-height: 1.6;
      border-radius: 8px;
      border: 2px solid #e4e7ed;
      transition: all 0.3s;
      resize: vertical;
      min-height: 100px;
      max-height: 200px;
      
      &:focus {
        border-color: #409eff;
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
      }
    }
  }
}

.tip-alert {
  margin-bottom: 10px;
  border-radius: 8px;
  flex-shrink: 0;
  
  .tip-text {
    font-size: 13px;
    color: #606266;
  }
}

.action-btn {
  width: 100%;
  height: 38px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  margin-bottom: 10px;
  flex-shrink: 0;
  
  .el-icon {
    margin-right: 6px;
  }
}

.result-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  min-height: 0;
  
  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8eaf0 100%);
    border-bottom: 1px solid #e4e7ed;
    flex-shrink: 0;
    
    .result-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .result-body {
    flex: 1;
    padding: 10px;
    overflow-y: auto;
    background: #fff;
    min-height: 0;
    
    .empty-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;
      font-size: 14px;
    }
    
    .generating-tip {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;
      
      .el-icon {
        font-size: 32px;
        margin-bottom: 12px;
        color: #409eff;
      }
      
      span {
        font-size: 14px;
      }
    }
    
    :deep(.md-editor-preview-wrapper) {
      padding: 0;
      background: transparent;
    }
    
    :deep(.md-editor-preview) {
      color: #303133;
      font-size: 14px;
      line-height: 1.8;
      
      h1 {
        font-size: 28px;
        margin-top: 1.5em;
        margin-bottom: 0.8em;
        font-weight: 600;
        color: #1f2937;
        border-bottom: 2px solid #e5e7eb;
        padding-bottom: 0.3em;
      }
      
      h2 {
        font-size: 24px;
        margin-top: 1.5em;
        margin-bottom: 0.8em;
        font-weight: 600;
        color: #1f2937;
        border-bottom: 1px solid #e5e7eb;
        padding-bottom: 0.3em;
      }
      
      h3 {
        font-size: 20px;
        margin-top: 1.5em;
        margin-bottom: 0.8em;
        font-weight: 600;
        color: #1f2937;
      }
      
      h4, h5, h6 {
        font-size: 16px;
        margin-top: 1.5em;
        margin-bottom: 0.8em;
        font-weight: 600;
        color: #1f2937;
      }
      
      p {
        margin-bottom: 1em;
        line-height: 1.8;
      }
      
      ul, ol {
        margin: 1em 0;
        padding-left: 2em;
        
        li {
          margin: 0.5em 0;
        }
      }
      
      code {
        background: #f3f4f6;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 0.9em;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        color: #e83e8c;
      }
      
      pre {
        background: #1f2937;
        border-radius: 8px;
        padding: 16px;
        margin: 1em 0;
        overflow-x: auto;
        
        code {
          background: transparent;
          padding: 0;
          color: #e5e7eb;
          font-size: 14px;
        }
      }
      
      blockquote {
        border-left: 4px solid #409eff;
        padding-left: 1em;
        margin: 1em 0;
        color: #606266;
        background: #f5f7fa;
        padding: 0.5em 1em;
        border-radius: 4px;
      }
      
      table {
        border-collapse: collapse;
        width: 100%;
        margin: 1em 0;
        
        th, td {
          border: 1px solid #dcdfe6;
          padding: 8px 12px;
          text-align: left;
        }
        
        th {
          background: #f5f7fa;
          font-weight: 600;
        }
      }
      
      a {
        color: #409eff;
        text-decoration: none;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      img {
        max-width: 100%;
        height: auto;
        border-radius: 4px;
        margin: 1em 0;
      }
      
      hr {
        border: none;
        border-top: 2px solid #e5e7eb;
        margin: 2em 0;
      }
    }
  }
}

// 滚动条样式
.tab-content,
.result-body {
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
    
    &:hover {
      background: #a8a8a8;
    }
  }
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
  color: #909399;
  font-size: 14px;
}
</style>
