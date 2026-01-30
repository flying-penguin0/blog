import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是 200，则认为是错误
    if (res.code !== 200) {
      // 401 错误不显示错误消息，由各个页面自行处理
      if (res.code !== 401) {
        ElMessage.error(res.message || '请求失败')
      }
      
      // 401: 未登录或登录过期
      // 只有在需要认证的页面才跳转到登录页
      if (res.code === 401) {
        const userStore = useUserStore()
        const currentPath = window.location.pathname
        const publicPaths = ['/', '/categories', '/tags', '/article', '/guestbook', '/login', '/register']
        const isPublicPath = publicPaths.some(path => currentPath.startsWith(path))
        
        // 如果不是公开页面，才跳转到登录页
        if (!isPublicPath) {
          userStore.logout()
          window.location.href = '/login'
        }
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未登录或登录已过期'
          const userStore = useUserStore()
          const currentPath = window.location.pathname
          const publicPaths = ['/', '/categories', '/tags', '/article', '/guestbook', '/login', '/register']
          const isPublicPath = publicPaths.some(path => currentPath.startsWith(path))
          
          // 如果不是公开页面，才跳转到登录页
          if (!isPublicPath) {
            userStore.logout()
            window.location.href = '/login'
          }
          break
        case 403:
          message = '权限不足'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    }
    
    // 401 错误不显示错误消息
    if (error.response?.status !== 401) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
