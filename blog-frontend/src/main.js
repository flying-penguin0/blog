import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import 'normalize.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import VueDanmaku from 'vue-danmaku'

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
app.use(ElementPlus, {
  // 配置消息组件的 z-index，确保在导航栏之上
  zIndex: 3000
})
app.use(Antd)
app.use(VueDanmaku)

// 初始化用户信息
const userStore = useUserStore()
if (userStore.token) {
  userStore.fetchUserInfo().catch(() => {
    // 如果获取用户信息失败，清除token
    userStore.logout()
  })
}

app.mount('#app')
