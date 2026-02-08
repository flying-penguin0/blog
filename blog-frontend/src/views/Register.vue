<template>
  <div class="register-page">
    <!-- 头部导航栏 -->
    <header class="auth-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <!-- <span class="logo-icon">📝</span> -->
          <span class="logo-text">个人知识博客</span>
        </div>
        <nav class="nav-links">
          <router-link to="/" class="nav-link">
            <span>首页</span>
          </router-link>
          <router-link to="/login" class="nav-link">
            <span>登录</span>
          </router-link>
        </nav>
      </div>
    </header>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="register-container">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <h1 class="welcome-title">加入我们</h1>
          <p class="welcome-subtitle">开启AI驱动的知识管理之旅</p>
          <div class="feature-list">
            <div class="feature-item">
              <img src="@/assets/images/dp.png" alt="DeepSeek" class="deepseek-logo" />
              <span>DeepSeek AI助手</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📚</span>
              <span>智能知识整理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌟</span>
              <span>知识分享平台</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💬</span>
              <span>互动交流社区</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-box">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>填写信息完成注册</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="用户名" 
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><user /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码" 
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="确认密码" 
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input 
                v-model="form.email" 
                placeholder="邮箱" 
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="emailCode">
              <div class="code-input-wrapper">
                <el-input 
                  v-model="form.emailCode" 
                  placeholder="邮箱验证码" 
                  size="large"
                  class="custom-input code-input"
                  maxlength="6"
                >
                  <template #prefix>
                    <el-icon><key /></el-icon>
                  </template>
                </el-input>
                <el-button 
                  type="primary" 
                  size="large"
                  class="send-code-btn"
                  :disabled="countdown > 0 || !form.username || !form.email || sendingCode"
                  :loading="sendingCode"
                  @click="sendEmailCode"
                >
                  <span v-if="sendingCode">发送中...</span>
                  <span v-else-if="countdown > 0">{{ countdown }}秒</span>
                  <span v-else>发送</span>
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item prop="nickname">
              <el-input 
                v-model="form.nickname" 
                placeholder="昵称（选填）" 
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon><user /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                class="register-btn"
                :loading="loading" 
                @click="handleRegister"
              >
                <span v-if="!loading">注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>已有账号？</span>
            <router-link to="/login" class="link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { sendEmailCode as sendEmailCodeApi } from '@/api/captcha'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  emailCode: '',
  nickname: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度必须为6位', trigger: 'blur' }
  ],
  nickname: [
    { min: 2, max: 20, message: '昵称长度必须在2-20之间', trigger: 'blur' }
  ]
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  // 验证用户名
  if (!form.value.username) {
    ElMessage.warning('请先输入用户名')
    return
  }
  
  if (form.value.username.length < 3 || form.value.username.length > 20) {
    ElMessage.warning('用户名长度必须在3-20之间')
    return
  }
  
  // 验证邮箱格式
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.value.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  
  sendingCode.value = true
  try {
    await sendEmailCodeApi(form.value.username, form.value.email)
    ElMessage.success('验证码已发送，请查收邮件')
    
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  await formRef.value.validate()
  
  loading.value = true
  try {
    await userStore.register({
      username: form.value.username,
      password: form.value.password,
      email: form.value.email,
      emailCode: form.value.emailCode,
      nickname: form.value.nickname || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  position: relative;
  overflow: hidden;
  padding: 20px 0;
}

// 头部导航栏
.auth-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    height: 64px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
    
    .logo-icon {
      font-size: 28px;
    }
    
    .logo-text {
      font-size: 20px;
      font-weight: 700;
      color: white;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
  
  .nav-links {
    display: flex;
    gap: 24px;
    
    .nav-link {
      color: white;
      text-decoration: none;
      font-size: 15px;
      font-weight: 500;
      padding: 8px 16px;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.15);
      }
      
      &.router-link-active {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

// 背景装饰
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 20s infinite ease-in-out;
    
    &.circle-1 {
      width: 300px;
      height: 300px;
      top: -100px;
      left: -100px;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 200px;
      height: 200px;
      bottom: -50px;
      right: 10%;
      animation-delay: 5s;
    }
    
    &.circle-3 {
      width: 150px;
      height: 150px;
      top: 50%;
      right: -75px;
      animation-delay: 10s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.register-container {
  flex: 1;
  display: flex;
  width: 900px;
  max-width: 95%;
  margin: 0 auto;
  margin-top: 80px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  animation: slideIn 0.5s ease-out;
  position: relative;
  z-index: 1;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 左侧欢迎区域
.welcome-section {
  flex: 1;
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></svg>');
    opacity: 0.3;
  }
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

.feature-list {
  .feature-item {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    animation: fadeInUp 0.6s ease-out both;
    
    &:nth-child(1) { animation-delay: 0.4s; }
    &:nth-child(2) { animation-delay: 0.5s; }
    &:nth-child(3) { animation-delay: 0.6s; }
    
    .feature-icon {
      font-size: 24px;
      margin-right: 12px;
    }
    
    .deepseek-logo {
      width: 32px;
      height: 32px;
      margin-right: 12px;
      object-fit: contain;
      background: white;
      border-radius: 6px;
      padding: 4px;
    }
    
    span:last-child {
      font-size: 15px;
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 右侧表单区域
.form-section {
  flex: 1;
  padding: 30px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
  max-height: 90vh;
}

.form-box {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
    color: #333;
    margin-bottom: 4px;
  }
  
  p {
    color: #666;
    font-size: 13px;
  }
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 8px 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }
    
    &.is-focus {
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
    }
  }
  
  :deep(.el-input__prefix) {
    color: #409EFF;
  }
}

.code-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: stretch;
  width: 100%;
  
  .code-input {
    flex: 1;
    min-width: 0;
  }
  
  .send-code-btn {
    flex-shrink: 0;
    width: 90px;
    border-radius: 10px;
    font-size: 13px;
    font-weight: 600;
    padding: 0 8px;
    height: auto;
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.register-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: #409EFF;
  border: none;
  transition: all 0.3s;
  margin-top: 4px;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
    background: #66b1ff;
  }
  
  &:active {
    transform: translateY(0);
  }
}

.form-footer {
  text-align: center;
  margin-top: 12px;
  color: #666;
  font-size: 13px;
  
  .link {
    color: #409EFF;
    text-decoration: none;
    font-weight: 600;
    margin-left: 8px;
    transition: color 0.3s;
    
    &:hover {
      color: #66b1ff;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .auth-header {
    .logo-text {
      display: none;
    }
  }
  
  .register-container {
    flex-direction: column;
    width: 90%;
    margin-top: 80px;
  }
  
  .welcome-section {
    padding: 40px 30px;
    
    .welcome-title {
      font-size: 28px;
    }
    
    .feature-list {
      display: none;
    }
  }
  
  .form-section {
    padding: 40px 30px;
    max-height: none;
  }
}
</style>
