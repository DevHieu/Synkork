import { LoginData } from './../pages/auth/types/LoginData';
import { storeToRefs } from 'pinia'

import { useAuthStore } from '@/stores/auth'
import { authService } from '@/pages/auth/services/authService'

export function useAuth() {
  const router = useRouter()

  const authStore = useAuthStore()
  const { isLogin } = storeToRefs(authStore)
  const loading = ref(false)

  async function logout() {
    await authService.logout()
  }

  function toHome() {
    router.push({ path: '/dashboard' })
  }

  async function login(data: LoginData) {
    loading.value = true
    
    await authService.login(data)

    // mock login
    isLogin.value = true
    loading.value = false

    const redirect = router.currentRoute.value.query.redirect as string
    if (!redirect || redirect.startsWith('//')) {
      toHome()
    }
    else {
      router.push(redirect)
    }
  }

  return {
    loading,
    logout,
    login,
  }
}
