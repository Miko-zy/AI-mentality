import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { UserInfo } from '@/types'

// Token存储键
const TOKEN_KEY = 'ai_mental_token'
const USER_INFO_KEY = 'ai_mental_user_info'

// 用户状态管理
export const useUserStore = defineStore('user', () => {
  // Token
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')

  // 用户信息（直接解析localStorage中的JSON；不可将函数传给ref，ref不会自动执行函数）
  const storedUserInfo = localStorage.getItem(USER_INFO_KEY)
  const userInfo = ref<UserInfo | null>(
    storedUserInfo ? JSON.parse(storedUserInfo) : null
  )

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 是否管理员
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  // 设置Token
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  // 设置用户信息
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    setToken,
    setUserInfo,
    logout
  }
})
