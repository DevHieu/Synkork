import VueCookies from 'vue-cookies'

import axiosClient from '@/lib/axiosClient'

interface LoginData {
  username: string
  password: string
}

export async function login(loginData: LoginData) {
  const res = await axiosClient.post('/api/auth/login', loginData)
  VueCookies.set('accessToken', res.data, '15m')
  return res.data
}

export async function logout() {
  try {
    await axiosClient.post('/api/auth/logout')
  }
  catch (error) {
    console.error('Error during logout:', error)
  }
  finally {
    VueCookies.remove('accessToken')
    window.location.href = '/auth'
  }
}
