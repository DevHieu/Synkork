import { defineStore } from 'pinia'

import type { Account } from '@/types/Account'

import { authService } from '@/pages/auth/services/authService'

export const useAuthStore = defineStore('user', () => {
  const isLogin = ref(false)
  const user = ref<Account | null>(null)
  const loading = ref(true)

  async function getUserInfo() {
    loading.value = true
    try {
      const data = await authService.getUserInfo()
      console.log('getUserInfo data:', data)
      user.value = data
      isLogin.value = true
    }
    catch (err) {
      console.error('getUserInfo error:', err)
      isLogin.value = false
      user.value = null
    }
    finally {
      loading.value = false
    }
  }

  getUserInfo()

  return { isLogin, user, loading, getUserInfo }
})
