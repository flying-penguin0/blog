import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  // 登录
  const login = async (username, password) => {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    return res
  }
  
  // 注册
  const register = async (data) => {
    const res = await registerApi(data)
    return res
  }
  
  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    const res = await getUserInfo()
    user.value = res.data
  }
  
  // 登出
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }
  
  return {
    token,
    user,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
