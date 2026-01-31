<template>
  <div class="editor-page">
    <!-- 顶部操作栏 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <el-button @click="goBack" text>
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <el-divider direction="vertical" />
          <span class="page-title">{{ articleId ? '编辑文章' : '新建文章' }}</span>
        </div>
        <div class="header-right">
          <el-button @click="showAIAssistant = !showAIAssistant" :type="showAIAssistant ? 'primary' : ''">
            <el-icon><ChatDotRound /></el-icon>
            AI 助手
          </el-button>
          <el-button type="primary" @click="showPublishDialog = true">
            <el-icon><Check /></el-icon>
            {{ articleId ? '保存' : '发布' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <div class="editor-container">
      <!-- 左侧编辑区 -->
      <div class="editor-main">
        <!-- 文章信息卡片 -->
        <el-card class="info-card" shadow="never">
          <el-form :model="form" label-position="top">
            <el-form-item label="文章标题" required>
              <el-input 
                v-model="form.title" 
                placeholder="请输入文章标题..." 
                size="large"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="文章分类" required>
                  <el-select 
                    v-model="form.categoryId" 
                    placeholder="请选择分类" 
                    style="width: 100%" 
                    size="large"
                    @change="handleCategoryChange"
                  >
                    <el-option 
                      v-for="category in categories" 
                      :key="category.id" 
                      :label="category.name" 
                      :value="category.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="8">
                <el-form-item label="文章标签">
                  <el-select 
                    v-model="form.tagIds" 
                    multiple 
                    placeholder="请先选择分类" 
                    style="width: 100%"
                    size="large"
                    :disabled="!form.categoryId"
                  >
                    <el-option 
                      v-for="tag in tags" 
                      :key="tag.id" 
                      :label="tag.name" 
                      :value="tag.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              
              <el-col :span="8">
                <el-form-item label="文章状态">
                  <el-radio-group v-model="form.status" size="large">
                    <el-radio value="published">公开</el-radio>
                    <el-radio value="private">私密</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="封面图片">
                  <el-upload
                    class="cover-uploader-compact"
                    :show-file-list="false"
                    :on-change="handleCoverUpload"
                    :auto-upload="false"
                    accept="image/*"
                  >
                    <img v-if="form.coverImage" :src="form.coverImage" class="cover-image-compact" />
                    <div v-else class="cover-placeholder-compact">
                      <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                      <div class="cover-text">点击上传封面</div>
                    </div>
                  </el-upload>
                </el-form-item>
              </el-col>
              
              <el-col :span="11">
                <el-form-item>
                  <template #label>
                    <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                      <span>文章摘要</span>
                      <el-button 
                        type="primary" 
                        size="small" 
                        :loading="generatingSummary"
                        @click="generateAISummary"
                        :disabled="!form.content"
                        style="margin-left: 10px;"
                      >
                        <el-icon><MagicStick /></el-icon>
                        AI 提取摘要
                      </el-button>
                    </div>
                  </template>
                  <el-input 
                    v-model="form.summary" 
                    type="textarea" 
                    :rows="7"
                    placeholder="可使用AI根据文章内容提取摘要"
                    maxlength="300"
                    show-word-limit
                    class="summary-textarea"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <!-- 编辑器卡片 -->
        <el-card class="editor-card" shadow="never">
          <MdEditor 
            v-model="form.content"
            :language="'zh-CN'"
            :theme="'light'"
            :preview-theme="'github'"
            :code-theme="'github'"
            :toolbars="toolbars"
            :on-upload-img="handleUploadImg"
            @on-save="handleSave"
            style="height: 600px;"
          />
        </el-card>
      </div>

      <!-- 右侧 AI 助手面板 -->
      <div class="ai-panel" :class="{ 'open': showAIAssistant }">
        <AIAssistant 
          :editor-content="form.content"
          @insert="handleAIInsert"
          @replace="handleAIReplace"
          @close="showAIAssistant = false"
          class="ai-assistant-wrapper"
        />
      </div>
    </div>

    <!-- 发布确认对话框 -->
    <el-dialog 
      v-model="showPublishDialog" 
      :title="articleId ? '保存文章' : '发布文章'" 
      width="500px"
    >
      <el-alert
        title="发布前请确认"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <div style="line-height: 1.8;">
            <div>标题：{{ form.title || '未填写' }}</div>
            <div>分类：{{ getCategoryName(form.categoryId) || '未选择' }}</div>
            <div>标签：{{ getTagNames(form.tagIds) || '未选择' }}</div>
            <div>状态：{{ form.status === 'published' ? '公开' : '私密' }}</div>
          </div>
        </template>
      </el-alert>
      
      <el-alert
        v-if="!form.title || !form.categoryId || !form.content"
        title="请完善必填信息"
        type="warning"
        :closable="false"
      >
        <template #default>
          <div v-if="!form.title">• 文章标题不能为空</div>
          <div v-if="!form.categoryId">• 请选择文章分类</div>
          <div v-if="!form.content">• 文章内容不能为空</div>
        </template>
      </el-alert>

      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="publishArticle" 
          :loading="publishing"
          :disabled="!form.title || !form.categoryId || !form.content"
        >
          确认{{ articleId ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Plus, Check, ArrowLeft, Close, MagicStick } from '@element-plus/icons-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle, getArticleDetail } from '@/api/article'
import { getCategoryList, getTagList } from '@/api/article'
import { uploadFile } from '@/api/file'
import { extractSummary } from '@/api/ai'
import AIAssistant from '@/components/AIAssistant.vue'

const router = useRouter()
const route = useRoute()

const form = ref({
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  status: 'published'
})

const categories = ref([])
const tags = ref([])
const showPublishDialog = ref(false)
const showAIAssistant = ref(false)
const publishing = ref(false)
const articleId = ref(null)
const autoSaveTimer = ref(null)
const generatingSummary = ref(false)

// 工具栏配置
const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  '-',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  'revoke',
  'next',
  'save',
  '=',
  'pageFullscreen',
  'fullscreen',
  'preview',
  'htmlPreview',
  'catalog',
]

// 返回
const goBack = () => {
  if (form.value.title || form.value.content) {
    ElMessageBox.confirm(
      '当前有未保存的内容，确定要离开吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

// 获取分类名称
const getCategoryName = (id) => {
  const category = categories.value.find(c => c.id === id)
  return category ? category.name : ''
}

// 获取标签名称
const getTagNames = (ids) => {
  if (!ids || ids.length === 0) return ''
  return ids.map(id => {
    const tag = tags.value.find(t => t.id === id)
    return tag ? tag.name : ''
  }).filter(Boolean).join('、')
}

// 处理图片上传
const handleUploadImg = async (files, callback) => {
  const res = await Promise.all(
    files.map(async (file) => {
      try {
        const uploadRes = await uploadFile(file)
        return uploadRes.data
      } catch (error) {
        console.error('上传失败:', error)
        ElMessage.error(`图片上传失败`)
        return null
      }
    })
  )
  
  // 过滤掉上传失败的图片
  const urls = res.filter(url => url !== null)
  callback(urls)
}

// 保存到 LocalStorage
const saveToLocalStorage = () => {
  localStorage.setItem('draft_article', JSON.stringify({
    title: form.value.title,
    content: form.value.content,
    summary: form.value.summary,
    coverImage: form.value.coverImage,
    categoryId: form.value.categoryId,
    tagIds: form.value.tagIds,
    timestamp: Date.now()
  }))
}

// 从 LocalStorage 恢复
const restoreFromLocalStorage = () => {
  const draft = localStorage.getItem('draft_article')
  if (draft) {
    try {
      const data = JSON.parse(draft)
      if (Date.now() - data.timestamp < 3600000) {
        ElMessageBox.confirm(
          '检测到未保存的草稿，是否恢复？',
          '提示',
          {
            confirmButtonText: '恢复',
            cancelButtonText: '放弃',
            type: 'info'
          }
        ).then(() => {
          form.value.title = data.title || ''
          form.value.content = data.content || ''
          form.value.summary = data.summary || ''
          form.value.coverImage = data.coverImage || ''
          form.value.categoryId = data.categoryId || null
          form.value.tagIds = data.tagIds || []
        }).catch(() => {
          localStorage.removeItem('draft_article')
        })
      }
    } catch (e) {
      console.error('恢复草稿失败:', e)
    }
  }
}

// 处理保存快捷键
const handleSave = () => {
  saveToLocalStorage()
  ElMessage.success('草稿已保存')
}

// 发布文章
const publishArticle = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择文章分类')
    return
  }
  if (!form.value.content) {
    ElMessage.warning('请输入文章内容')
    return
  }

  publishing.value = true
  try {
    const data = {
      ...form.value
    }

    if (articleId.value) {
      await updateArticle(articleId.value, data)
      ElMessage.success('文章保存成功')
    } else {
      await createArticle(data)
      ElMessage.success('文章发布成功')
    }

    showPublishDialog.value = false
    localStorage.removeItem('draft_article')
    
    setTimeout(() => {
      router.push('/admin/articles')
    }, 1000)
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    publishing.value = false
  }
}

// 获取分类和标签
const fetchCategoriesAndTags = async () => {
  try {
    const categoryRes = await getCategoryList()
    categories.value = categoryRes.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 根据分类获取标签
const fetchTagsByCategory = async (categoryId) => {
  if (!categoryId) {
    tags.value = []
    return
  }
  
  try {
    const tagRes = await getTagList({ categoryId })
    tags.value = tagRes.data || []
  } catch (error) {
    console.error('获取标签失败:', error)
    tags.value = []
  }
}

// 处理分类变化
const handleCategoryChange = (categoryId) => {
  // 清空已选标签
  form.value.tagIds = []
  // 加载该分类下的标签
  fetchTagsByCategory(categoryId)
}

// 处理封面图片上传
const handleCoverUpload = async (file) => {
  try {
    const res = await uploadFile(file.raw)
    form.value.coverImage = res.data
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  }
}

// 处理 AI 插入
const handleAIInsert = (content) => {
  form.value.content = form.value.content + '\n\n' + content
  ElMessage.success('内容已插入')
}

// 处理 AI 替换
const handleAIReplace = (content) => {
  form.value.content = content
  ElMessage.success('内容已替换')
}

// 加载文章（编辑模式）
const loadArticle = async () => {
  if (route.query.id) {
    try {
      const res = await getArticleDetail(route.query.id)
      const article = res.data
      
      form.value.title = article.title
      form.value.content = article.content
      form.value.summary = article.summary
      form.value.coverImage = article.coverImage
      form.value.categoryId = article.categoryId
      form.value.tagIds = article.tags?.map(tag => tag.id) || []
      form.value.status = article.status
      
      articleId.value = article.id
      
      // 加载该分类下的标签
      if (article.categoryId) {
        await fetchTagsByCategory(article.categoryId)
      }
    } catch (error) {
      console.error('加载文章失败:', error)
    }
  }
}

// 自动保存
const startAutoSave = () => {
  autoSaveTimer.value = setInterval(() => {
    if (form.value.title && form.value.content) {
      saveToLocalStorage()
    }
  }, 30000)
}

onMounted(() => {
  fetchCategoriesAndTags()
  
  setTimeout(() => {
    loadArticle()
    if (!route.query.id) {
      restoreFromLocalStorage()
    }
  }, 500)
  
  startAutoSave()
})

onBeforeUnmount(() => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value)
  }
})

// AI 生成摘要
const generateAISummary = () => {
  if (!form.value.content) {
    ElMessage.warning('请先输入文章内容')
    return
  }
  
  console.log('开始生成摘要...')
  generatingSummary.value = true
  form.value.summary = '' // 清空现有摘要，准备实时渲染
  let isFirstChunk = true
  
  extractSummary(
    form.value.content,
    (text) => {
      // 接收流式响应，实时更新摘要
      console.log('收到AI响应片段:', text)
      
      // 如果是第一个片段，检查并移除可能的"摘要："前缀
      if (isFirstChunk) {
        text = text.replace(/^摘要[：:]\s*/, '')
        isFirstChunk = false
      }
      
      form.value.summary += text
    },
    (error) => {
      console.error('生成摘要失败:', error)
      ElMessage.error('生成摘要失败，请重试')
      generatingSummary.value = false
    },
    () => {
      // 完成回调
      console.log('摘要生成完成')
      // 再次检查并清理可能的"摘要："前缀
      form.value.summary = form.value.summary.replace(/^摘要[：:]\s*/, '').trim()
      
      // 如果超过300字，截断
      if (form.value.summary.length > 300) {
        form.value.summary = form.value.summary.substring(0, 300)
      }
      
      if (form.value.summary) {
        ElMessage.success('摘要生成成功')
      } else {
        ElMessage.warning('未能生成摘要，请重试')
      }
      generatingSummary.value = false
    }
  )
}

onBeforeUnmount(() => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value)
  }
})
</script>

<style scoped lang="scss">
.editor-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 20px;
}

.header-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 15px 20px;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .header-right {
    display: flex;
    gap: 10px;
  }
}

.editor-container {
  display: flex;
  gap: 20px;
  padding: 0 20px;
  align-items: flex-start;
  max-width: 1800px;
  margin: 0 auto;
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0; // 防止flex子元素溢出
  transition: all 0.3s ease;
}

.info-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  // 标签选择器选中项样式
  :deep(.el-select) {
    .el-tag {
      background-color: #409eff !important;
      border-color: #409eff !important;
      color: #fff !important;
      
      .el-tag__close {
        color: #fff !important;
        
        &:hover {
          background-color: #66b1ff !important;
        }
      }
    }
  }
  
  .cover-uploader-compact {
    width: 100%;
    
    :deep(.el-upload) {
      width: 100%;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
    }
    
    .cover-placeholder-compact {
      width: 100%;
      height: 165px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: #fafafa;
    }
    
    .cover-uploader-icon {
      font-size: 35px;
      color: #8c939d;
      margin-bottom: 8px;
    }
    
    .cover-text {
      font-size: 13px;
      color: #8c939d;
    }
    
    .cover-image-compact {
      width: 100%;
      height: 165px;
      display: block;
      object-fit: cover;
    }
  }
  
  .summary-textarea {
    :deep(.el-textarea__inner) {
      height: 165px !important;
      resize: none;
    }
  }
}

.editor-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.ai-panel {
  width: 0;
  overflow: hidden;
  transition: all 0.3s ease;
  flex-shrink: 0;
  opacity: 0;
  
  &.open {
    width: 400px;
    opacity: 1;
  }
  
  .ai-assistant-wrapper {
    height: 1060px;
    width: 400px;
  }
}

@media (max-width: 1200px) {
  .editor-container {
    flex-direction: column;
  }
  
  .ai-panel {
    &.open {
      width: 100%;
      height: 400px;
    }
  }
}
</style>
