import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/Categories.vue'),
      meta: { title: '分类' }
    },
    {
      path: '/tags',
      name: 'tags',
      component: () => import('@/views/Tags.vue'),
      meta: { title: '标签' }
    },
    {
      path: '/guestbook',
      name: 'guestbook',
      component: () => import('@/views/Guestbook.vue'),
      meta: { title: '留言板' }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/About.vue'),
      meta: { title: '关于' }
    },
    {
      path: '/chatroom',
      name: 'chatroom',
      component: () => import('@/views/ChatRoom.vue'),
      meta: { title: '聊天室' }
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: () => import('@/views/ArticleDetail.vue'),
      meta: { title: '文章详情' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册' }
    },
    {
      path: '/editor',
      name: 'editor',
      component: () => import('@/views/Editor.vue'),
      meta: { title: '文章编辑', requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/Layout.vue'),
      meta: { title: '后台管理', requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '数据统计' }
        },
        {
          path: 'articles',
          name: 'admin-articles',
          component: () => import('@/views/admin/Articles.vue'),
          meta: { title: '文章管理' }
        },
        {
          path: 'editor',
          name: 'admin-editor',
          component: () => import('@/views/Editor.vue'),
          meta: { title: '文章编辑' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/Users.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'comments',
          name: 'admin-comments',
          component: () => import('@/views/admin/Comments.vue'),
          meta: { title: '评论管理' }
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/Categories.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: 'tags',
          name: 'admin-tags',
          component: () => import('@/views/admin/Tags.vue'),
          meta: { title: '标签管理' }
        },
        {
          path: 'announcements',
          name: 'admin-announcements',
          component: () => import('@/views/admin/Announcements.vue'),
          meta: { title: '公告管理' }
        },
        {
          path: 'messages',
          name: 'admin-messages',
          component: () => import('@/views/admin/Messages.vue'),
          meta: { title: '留言管理' }
        },
        {
          path: 'chatroom',
          name: 'admin-chatroom',
          component: () => import('@/views/admin/ChatRoom.vue'),
          meta: { title: '聊天室管理' }
        },
        {
          path: 'sensitive-words',
          name: 'admin-sensitive-words',
          component: () => import('@/views/admin/SensitiveWords.vue'),
          meta: { title: '敏感词管理' }
        }
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/user/Layout.vue'),
      meta: { title: '个人中心', requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'user-dashboard',
          component: () => import('@/views/user/Dashboard.vue'),
          meta: { title: '数据统计' }
        },
        {
          path: 'articles',
          name: 'user-articles',
          component: () => import('@/views/user/Articles.vue'),
          meta: { title: '我的文章' }
        },
        {
          path: 'editor',
          name: 'user-editor',
          component: () => import('@/views/Editor.vue'),
          meta: { title: '文章编辑' }
        },
        {
          path: 'comments',
          name: 'user-comments',
          component: () => import('@/views/user/Comments.vue'),
          meta: { title: '我的评论' }
        },
        {
          path: 'messages',
          name: 'user-messages',
          component: () => import('@/views/user/Messages.vue'),
          meta: { title: '我的留言' }
        },
        {
          path: 'chat-messages',
          name: 'user-chat-messages',
          component: () => import('@/views/user/ChatMessages.vue'),
          meta: { title: '我的聊天' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 基于DeepSeek开放平台的个人知识博客` : '基于DeepSeek开放平台的个人知识博客'
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !userStore.token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && userStore.user?.role !== 'admin') {
    next({ name: 'home' })
    return
  }
  
  next()
})

export default router
