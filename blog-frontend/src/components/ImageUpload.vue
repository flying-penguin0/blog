<template>
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      accept="image/*"
      :disabled="uploading"
    >
      <el-button :loading="uploading" type="primary" :icon="Upload">
        {{ uploading ? '上传中...' : '上传图片' }}
      </el-button>
    </el-upload>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  onSuccess: {
    type: Function,
    default: () => {}
  }
})

const uploadUrl = ref(import.meta.env.VITE_API_BASE_URL + '/file/upload')
const uploading = ref(false)

// 请求头（携带 token）
const headers = ref({
  'Authorization': localStorage.getItem('token') || ''
})

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  
  uploading.value = true
  return true
}

// 上传成功
const handleSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    ElMessage.success('上传成功')
    props.onSuccess(response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleError = (error) => {
  uploading.value = false
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}
</style>
