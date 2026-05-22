import { LoginData } from './../pages/auth/types/LoginData';
import { storeToRefs } from 'pinia'

import { useAuthStore } from '@/stores/auth'
import { authService } from '@/pages/auth/services/authService'

export function useAuth() {
  const router = useRouter()
  const authStore = useAuthStore()
  const { isLogin, user } = storeToRefs(authStore)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function checkAuth() {
    try {
      const data = await authService.checkAuth()
      user.value = data
      isLogin.value = true
      return true
    } catch {
      isLogin.value = false
      user.value = null
      return false
    }
  }

  async function login(data: LoginData) {
    loading.value = true
    error.value = null
    try {
      await authService.login(data)
      await authStore.getUserInfo() // load user info luôn sau login
      
      const redirect = router.currentRoute.value.query.redirect as string
      if (!redirect || redirect.startsWith('//')) {
        router.push('/dashboard')
      } else {
        router.push(redirect)
      }
    } catch (err: any) {
      error.value = err?.response?.data || 'Đăng nhập thất bại. Vui lòng thử lại.'
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    await authService.logout()
    isLogin.value = false
    user.value = null
  }

  return { loading, error, isLogin, user, checkAuth, login, logout }
}