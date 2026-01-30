<template>
  <header 
    class="header" 
    :class="{ 'header-transparent': transparent }"
    :style="transparent ? 'background: rgba(255, 255, 255, 0.02) !important;' : ''"
  >
    <div class="container" :style="transparent ? 'background: transparent !important;' : ''">
      <div class="header-content" :style="transparent ? 'background: transparent !important;' : ''">
        <h1 class="logo" @click="$router.push('/')">Ruyu-博客</h1>
        <nav class="nav">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/categories" class="nav-item" :class="{ active: $route.path === '/categories' }">
            <el-icon><FolderOpened /></el-icon>
            <span>分类</span>
          </router-link>
          <router-link to="/tags" class="nav-item" :class="{ active: $route.path === '/tags' }">
            <el-icon><PriceTag /></el-icon>
            <span>标签</span>
          </router-link>
          <a href="#" class="nav-item">
            <el-icon><Calendar /></el-icon>
            <span>归档</span>
          </a>
          <router-link to="/guestbook" class="nav-item" :class="{ active: $route.path === '/guestbook' }">
            <el-icon><ChatDotRound /></el-icon>
            <span>留言板</span>
          </router-link>
          <a href="#" class="nav-item">
            <el-icon><User /></el-icon>
            <span>关于</span>
          </a>
        </nav>
        <div class="header-actions">
          <template v-if="userStore.token">
            <el-dropdown>
              <el-avatar :size="32" :src="userStore.user?.avatar" style="cursor: pointer;">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToBackend">进入后台</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  Calendar, 
  FolderOpened, 
  PriceTag, 
  ChatDotRound, 
  User
} from '@element-plus/icons-vue'

defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const userStore = useUserStore()

const goToBackend = () => {
  if (userStore.user?.role === 'admin') {
    router.push('/admin/dashboard')
  } else {
    router.push('/user/dashboard')
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped lang="scss">
.header {
  position: sticky;
  top: 0;
  z-index: 9999;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  
  &.header-transparent {
    background: transparent !important;
    backdrop-filter: blur(10px);
    box-shadow: none;
    border-bottom: none;
    
    .container {
      background: transparent !important;
      padding: 0 20px !important;
    }
    
    .header-content {
      background: transparent !important;
    }
    
    .logo {
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
      font-weight: 700;
    }
    
    .nav-item {
      color: rgba(255, 255, 255, 0.95);
      text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
      }
      
      &.active {
        background: rgba(255, 255, 255, 0.15);
        color: #fff;
      }
    }
    
    .el-button {
      background: rgba(64, 158, 255, 0.8) !important;
      border-color: rgba(64, 158, 255, 0.8) !important;
      
      &:hover {
        background: rgba(64, 158, 255, 1) !important;
        border-color: rgba(64, 158, 255, 1) !important;
      }
    }
    
    .el-avatar {
      background: rgba(64, 158, 255, 0.8) !important;
    }
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 60px;
    gap: 40px;
    background: #ffffff;
  }
  
  .logo {
    font-size: 20px;
    font-weight: 600;
    color: #333;
    white-space: nowrap;
    margin: 0;
    cursor: pointer;
  }
  
  .nav {
    display: flex;
    gap: 5px;
    align-items: center;
    flex: 1;
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      color: #666;
      font-size: 14px;
      border-radius: 6px;
      transition: all 0.3s;
      text-decoration: none;
      white-space: nowrap;
      
      .el-icon {
        font-size: 16px;
      }
      
      &:hover {
        background: #f5f7fa;
        color: #409eff;
      }
      
      &.active {
        background: #e8f4ff;
        color: #409eff;
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .action-btn {
      background: #f5f7fa;
      border: none;
      color: #666;
      
      &:hover {
        background: #e8f4ff;
        color: #409eff;
      }
    }
  }
}

@media (max-width: 768px) {
  .header {
    .header-content {
      height: auto;
      padding: 10px 0;
      flex-wrap: wrap;
    }
    
    .logo {
      font-size: 18px;
    }
    
    .nav {
      order: 3;
      width: 100%;
      justify-content: space-around;
      padding-top: 10px;
      border-top: 1px solid #f0f0f0;
      
      .nav-item {
        padding: 6px 12px;
        font-size: 13px;
        
        .el-icon {
          font-size: 14px;
        }
        
        span {
          display: none;
        }
      }
    }
  }
}
</style>
