<template>
  <a-layout class="admin-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="256"
      theme="dark"
      class="sider"
    >
      <div class="logo">
        <h2 v-if="!collapsed">后台管理</h2>
        <h2 v-else>后台</h2>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="/admin/dashboard">
          <template #icon>
            <DashboardOutlined />
          </template>
          <span>数据统计</span>
        </a-menu-item>
        <a-menu-item key="/admin/articles">
          <template #icon>
            <FileTextOutlined />
          </template>
          <span>文章管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/users">
          <template #icon>
            <UserOutlined />
          </template>
          <span>用户管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/comments">
          <template #icon>
            <CommentOutlined />
          </template>
          <span>评论管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/categories">
          <template #icon>
            <FolderOutlined />
          </template>
          <span>分类管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/tags">
          <template #icon>
            <TagsOutlined />
          </template>
          <span>标签管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/announcements">
          <template #icon>
            <BellOutlined />
          </template>
          <span>公告管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/messages">
          <template #icon>
            <MessageOutlined />
          </template>
          <span>留言板管理</span>
        </a-menu-item>
        <a-menu-item key="/admin/chatroom">
          <template #icon>
            <CommentOutlined />
          </template>
          <span>聊天室管理</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout>
      <!-- 顶部导航 -->
      <a-layout-header class="header">
        <div class="header-left">
          <menu-unfold-outlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <menu-fold-outlined
            v-else
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item>
              <router-link to="/admin/dashboard">首页</router-link>
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentPageTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-button type="link" @click="goHome">
            <template #icon>
              <HomeOutlined />
            </template>
            返回前台
          </a-button>
          <a-dropdown>
            <div class="user-info">
              <a-avatar :size="32" :src="userStore.user?.avatar">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
            </div>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item key="profile">
                  <UserOutlined />
                  个人中心
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content :class="['content', { 'editor-content': isEditorPage }]">
        <router-view />
      </a-layout-content>
    </a-layout>

    <!-- 个人中心对话框 -->
    <a-modal
      v-model:open="showProfileDialog"
      title="个人中心"
      :width="500"
      @ok="saveProfile"
      :confirmLoading="savingProfile"
      okText="确认"
      cancelText="取消"
    >
      <a-form :model="profileForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item :label-col="{ span: 0 }" :wrapper-col="{ span: 24 }">
          <div class="avatar-upload">
            <a-avatar :size="80" :src="profileForm.avatar || '/default-avatar.png'">
              <template #icon><UserOutlined /></template>
            </a-avatar>
            <a-upload
              :show-upload-list="false"
              :before-upload="handleAvatarChange"
              accept="image/*"
            >
              <a-button style="margin-left: 20px">更换头像</a-button>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="profileForm.username" disabled />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="profileForm.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="个性签名">
          <a-textarea 
            v-model:value="profileForm.signature" 
            placeholder="请输入个性签名" 
            :rows="3"
            :maxlength="100"
            show-count
          />
        </a-form-item>
        <a-form-item>
          <a-button type="link" @click="showPasswordModal = true" style="padding: 0;">
            修改密码
          </a-button>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="showPasswordModal"
      title="修改密码"
      :width="400"
      @ok="changePassword"
      :confirmLoading="changingPassword"
      okText="确认"
      cancelText="取消"
    >
      <a-form :model="passwordForm" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="原密码">
          <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
        </a-form-item>
        <a-form-item label="新密码">
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认密码">
          <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword as changePasswordApi } from '@/api/user'
import { uploadFile } from '@/api/file'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  FileTextOutlined,
  UserOutlined,
  CommentOutlined,
  FolderOutlined,
  TagsOutlined,
  BellOutlined,
  MessageOutlined,
  HomeOutlined,
  LogoutOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const selectedKeys = ref([route.path])
const showProfileDialog = ref(false)
const savingProfile = ref(false)
const avatarFile = ref(null)
const profileForm = ref({
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  signature: ''
})

// 修改密码相关
const showPasswordModal = ref(false)
const changingPassword = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 监听路由变化更新选中菜单
watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

const currentPageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '数据统计',
    '/admin/articles': '文章管理',
    '/admin/editor': '文章编辑',
    '/admin/users': '用户管理',
    '/admin/comments': '评论管理',
    '/admin/categories': '分类管理',
    '/admin/tags': '标签管理',
    '/admin/announcements': '公告管理',
    '/admin/messages': '留言管理'
  }
  return titles[route.path] || '管理后台'
})

const isEditorPage = computed(() => route.path === '/admin/editor')

const handleMenuClick = ({ key }) => {
  router.push(key)
}

const goHome = () => {
  router.push('/')
}

const handleUserMenuClick = ({ key }) => {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    showProfileDialog.value = true
    console.log('用户信息:', userStore.user)
    profileForm.value = {
      username: userStore.user?.username || '',
      email: userStore.user?.email || '',
      nickname: userStore.user?.nickname || userStore.user?.username || '',
      avatar: userStore.user?.avatar || '',
      signature: userStore.user?.signature || '生活总要活埋了我，不料我是一粒种子'
    }
    console.log('表单数据:', profileForm.value)
    avatarFile.value = null
  }
}

// 处理头像选择
const handleAvatarChange = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }

  avatarFile.value = file
  // 预览头像
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.value.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

// 保存个人信息
const saveProfile = async () => {
  savingProfile.value = true
  try {
    let avatarUrl = profileForm.value.avatar

    // 如果有新头像，先上传
    if (avatarFile.value) {
      const uploadRes = await uploadFile(avatarFile.value)
      avatarUrl = uploadRes.data
    }

    // 更新用户信息
    await updateUserInfo({
      nickname: profileForm.value.nickname,
      email: profileForm.value.email,
      avatar: avatarUrl,
      signature: profileForm.value.signature
    })

    // 更新本地用户信息
    await userStore.fetchUserInfo()
    
    message.success('个人信息更新成功')
    showProfileDialog.value = false
  } catch (error) {
    console.error('更新失败:', error)
    // 错误已经在拦截器中显示，这里不需要再次显示
  } finally {
    savingProfile.value = false
  }
}

// 修改密码
const changePassword = async () => {
  // 验证表单
  if (!passwordForm.value.oldPassword) {
    message.error('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    message.error('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    message.error('新密码长度不能少于6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }

  changingPassword.value = true
  try {
    await changePasswordApi({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    message.success('密码修改成功，请重新登录')
    showPasswordModal.value = false
    
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    
    // 延迟退出登录
    setTimeout(() => {
      logout()
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  
  .sider {
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
    position: relative;
    z-index: 10;
    
    .logo {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.05);
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      h2 {
        color: #fff;
        font-size: 20px;
        margin: 0;
        font-weight: 600;
      }
    }
    
    :deep(.ant-menu-dark) {
      background: transparent;
      
      .ant-menu-item {
        margin: 8px 12px;
        border-radius: 8px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.08);
        }
        
        &.ant-menu-item-selected {
          background: #1890ff;
          
          &::after {
            display: none;
          }
        }
      }
    }
  }
  
  .header {
    background: #fff;
    padding: 0 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 9;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .trigger {
        font-size: 18px;
        cursor: pointer;
        transition: color 0.3s;
        
        &:hover {
          color: #1890ff;
        }
      }
      
      .breadcrumb {
        :deep(.ant-breadcrumb-link) {
          a {
            color: #1890ff;
            
            &:hover {
              color: #40a9ff;
            }
          }
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        padding: 0px 16px;
        border-radius: 20px;
        transition: all 0.3s;
        
        &:hover {
          background: rgba(24, 144, 255, 0.08);
        }
        
        .username {
          font-size: 14px;
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
  
  .content {
    background: #fff;
    min-height: calc(100vh - 64px);
    
    &.editor-content {
      margin: 0;
      padding: 0;
      border-radius: 0;
    }
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}
</style>
