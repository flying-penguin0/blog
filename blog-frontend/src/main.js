import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import 'normalize.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Vue3Danmaku from 'vue3-danmaku'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import './assets/styles/main.scss'
import './assets/styles/admin.scss'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(Antd)
app.use(Vue3Danmaku)

// 初始化用户信息
const userStore = useUserStore()
if (userStore.token) {
  userStore.fetchUserInfo().catch(() => {
    // 如果获取用户信息失败，清除token
    userStore.logout()
  })
}

app.mount('#app')
