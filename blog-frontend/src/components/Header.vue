<template>
  <header 
    class="header" 
    :class="{ 'header-transparent': transparent }"
  >
    <div class="container">
      <div class="header-content">
        <!-- <h1 class="logo" @click="$router.push('/')">基于DeepSeek开放平台的个人知识博客</h1> -->
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
          <router-link to="/chatroom" class="nav-item" :class="{ active: $route.path === '/chatroom' }">
            <el-icon><ChatDotRound /></el-icon>
            <span>聊天室</span>
          </router-link>
          <router-link to="/guestbook" class="nav-item" :class="{ active: $route.path === '/guestbook' }">
            <el-icon><Message /></el-icon>
            <span>留言板</span>
          </router-link>
          <router-link to="/about" class="nav-item" :class="{ active: $route.path === '/about' }">
            <el-icon><User /></el-icon>
            <span>关于</span>
          </router-link>
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
            <el-button class="auth-btn" @click="$router.push('/login')">登录/注册</el-button>
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
  Message,
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
  z-index: 1000;
  background: #ffffff;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  transition: all 0.3s;
  
  &.header-transparent {
    position: absolute;
    left: 0;
    right: 0;
    background: linear-gradient(180deg, rgba(25, 40, 62, 0.28) 0%, rgba(25, 40, 62, 0.12) 68%, rgba(25, 40, 62, 0) 100%);
    backdrop-filter: blur(8px);
    box-shadow: none;
    border-bottom: none;
    
    .container {
      background: transparent;
      padding: 0 20px !important;
    }
    
    .header-content {
      background: transparent;
    }
    
    .logo {
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
      font-weight: 700;
    }
    
    .nav {
      padding: 6px 0;
      gap: 8px;

      .nav-item {
        min-height: 38px;
        padding: 0 11px;
        gap: 7px;
        font-size: 14px;
        border-radius: 10px;
        color: rgba(255, 255, 255, 0.92);
        text-shadow: 0 1px 6px rgba(0, 0, 0, 0.28);
        border: 1px solid transparent;
        
        &:hover {
          background: rgba(18, 30, 46, 0.16);
          border-color: rgba(255, 255, 255, 0.06);
          color: #fff;
        }
        
        &.active {
          background: rgba(18, 30, 46, 0.22);
          border-color: rgba(255, 255, 255, 0.08);
          color: #fff;
          box-shadow: 0 8px 20px rgba(17, 28, 45, 0.1);
        }

        .el-icon {
          font-size: 15px;
          color: rgba(255, 255, 255, 0.74);
        }
      }
    }
    
    .el-button {
      background: rgba(18, 30, 46, 0.2) !important;
      border-color: rgba(255, 255, 255, 0.08) !important;
      color: #fff !important;
      
      &:hover {
        background: rgba(18, 30, 46, 0.26) !important;
        border-color: rgba(255, 255, 255, 0.12) !important;
      }
    }
    
    .el-avatar {
      box-shadow: 0 6px 18px rgba(17, 28, 45, 0.18);
    }
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 72px;
    gap: 36px;
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
    gap: 10px;
    align-items: center;
    flex: 1;
    padding: 6px 0;
    min-width: 0;
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 8px;
      min-height: 42px;
      padding: 0 14px;
      color: #526174;
      font-size: 15px;
      font-weight: 600;
      border-radius: 12px;
      border: 1px solid transparent;
      letter-spacing: 0;
      transition: all 0.24s ease;
      text-decoration: none;
      white-space: nowrap;
      
      .el-icon {
        font-size: 16px;
        color: #7a889b;
        transition: color 0.24s ease, transform 0.24s ease;
      }

      span {
        line-height: 1;
      }
      
      &:hover {
        background: rgba(64, 158, 255, 0.12);
        border-color: rgba(64, 158, 255, 0.16);
        color: #409eff;

        .el-icon {
          color: #409eff;
          transform: translateY(-0.5px);
        }
      }
      
      &.active {
        background: #409eff;
        border-color: #409eff;
        color: #ffffff;
        box-shadow: 0 10px 24px rgba(64, 158, 255, 0.24);

        .el-icon {
          color: #ffffff;
        }
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 14px;

    .auth-btn {
      min-height: 40px;
      padding: 0 16px;
      border-radius: 12px;
      border: 1px solid rgba(64, 158, 255, 0.16);
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0;
      box-shadow: none;
      transition: all 0.24s ease;

      &:hover {
        background: #409eff;
        border-color: #409eff;
        color: #fff;
      }
    }
    
    :deep(.el-avatar) {
      width: 38px;
      height: 38px;
      border: 2px solid rgba(255, 255, 255, 0.9);
      box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
    }
  }
}

@media (max-width: 768px) {
  .header {
    .header-content {
      height: auto;
      padding: 12px 0;
      flex-wrap: wrap;
    }
    
    .logo {
      font-size: 18px;
    }
    
    .nav {
      order: 3;
      width: 100%;
      justify-content: space-around;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;
      
      .nav-item {
        min-height: 38px;
        padding: 0 10px;
        font-size: 13px;
        font-weight: 600;
        border-radius: 10px;
        
        .el-icon {
          font-size: 15px;
        }
        
        span {
          display: none;
        }
      }
    }

    &.header-transparent {
      .nav {
        border-top: none;
      }
    }
  }
}
</style>
