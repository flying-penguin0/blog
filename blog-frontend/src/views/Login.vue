<template>
  <div class="login-page">
    <!-- 头部导航栏 -->
    <header class="auth-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">个人知识博客</span>
        </div>
        <nav class="nav-links">
          <router-link to="/" class="nav-link">
            <span>首页</span>
          </router-link>
          <router-link to="/register" class="nav-link">
            <span>注册</span>
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

    <div class="login-container">
      <!-- 左侧欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-content">
          <h1 class="welcome-title">AI驱动的知识博客</h1>
          <p class="welcome-subtitle">基于DeepSeek的智能知识管理平台</p>
          <div class="feature-list">
            <div class="feature-item">
              <img src="@/assets/images/dp.png" alt="DeepSeek" class="deepseek-logo" />
              <span>AI辅助知识整理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📚</span>
              <span>高效管理个人知识</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌐</span>
              <span>智能分享与交流</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💡</span>
              <span>知识价值最大化</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <div class="form-box">
          <div class="form-header">
            <h2>登录</h2>
            <p>使用您的账号登录</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名" 
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
                placeholder="请输入密码" 
                size="large"
                class="custom-input"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon><lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="captcha-container">
                <el-input 
                  v-model="form.captchaCode" 
                  placeholder="请输入验证码" 
                  size="large"
                  class="custom-input captcha-input"
                  maxlength="4"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon><key /></el-icon>
                  </template>
                </el-input>
                <div 
                  class="captcha-image-wrapper" 
                  @click="refreshCaptcha"
                  title="点击刷新验证码"
                >
                  <img 
                    v-if="captchaImage" 
                    :src="captchaImage" 
                    alt="验证码"
                    class="captcha-image"
                  />
                  <div v-else class="captcha-loading">
                    <el-icon class="is-loading"><loading /></el-icon>
                    <span>加载中...</span>
                  </div>
                  <div class="captcha-refresh-hint">
                    <el-icon><refresh /></el-icon>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                class="login-btn"
                :loading="loading" 
                @click="handleLogin"
              >
                <span v-if="!loading">登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/captcha'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const form = ref({
  username: '',
  password: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 加载验证码
const loadCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.captchaImage
    captchaKey.value = res.data.captchaKey
  } catch (error) {
    console.error('加载验证码失败:', error)
    ElMessage.error('加载验证码失败')
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  form.value.captchaCode = ''
  loadCaptcha()
}

const handleLogin = async () => {
  await formRef.value.validate()
  
  loading.value = true
  try {
    await userStore.login(
      form.value.username, 
      form.value.password,
      captchaKey.value,
      form.value.captchaCode
    )
    ElMessage.success('登录成功')
    
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
    // 登录失败后刷新验证码
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  position: relative;
  overflow: hidden;
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

.login-container {
  flex: 1;
  display: flex;
  width: 900px;
  max-width: 95%;
  margin: 0 auto;
  margin-top: 100px;
  margin-bottom: 40px;
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
  padding: 60px 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-box {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
  
  h2 {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin-bottom: 8px;
  }
  
  p {
    color: #666;
    font-size: 14px;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 12px 16px;
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

.captcha-container {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.captcha-input {
  flex: 1;
  
  :deep(.el-input__wrapper) {
    height: 48px;
  }
}

.captcha-image-wrapper {
  position: relative;
  width: 130px;
  height: 48px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #dcdfe6;
  background: #f5f7fa;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    border-color: #409EFF;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
    
    .captcha-refresh-hint {
      opacity: 1;
    }
  }
  
  .captcha-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
  
  .captcha-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #909399;
    font-size: 12px;
    
    .el-icon {
      font-size: 20px;
    }
  }
  
  .captcha-refresh-hint {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(64, 158, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
    
    .el-icon {
      font-size: 24px;
      color: white;
      animation: rotate 1s linear infinite;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: #409EFF;
  border: none;
  transition: all 0.3s;
  
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
  margin-top: 24px;
  color: #666;
  font-size: 14px;
  
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
  
  .login-container {
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
  }
}
</style>
