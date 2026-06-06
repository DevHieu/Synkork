import axiosClient from '@/lib/axiosClient'
import { removeCookie, setCookie } from '@/lib/cookies'

import type { LoginData } from '../types/LoginData'

export const authService = {
  async checkAuth() {
    const res = await axiosClient.get('/api/manage/auth/check')
    return res.data
  },

  async getUserInfo() {
    try {
      const response = await axiosClient.get('/api/users/me')
      return response.data
    }
    catch (error) {
      console.error('Error fetching user info:', error)
      throw error
    }
  },

  async login(loginData: LoginData) {
    const res = await axiosClient.post('/api/manage/auth/login', loginData)

    setCookie('accessToken', res.data, 60 * 60 * 15) // 15 minutes

    return res.data
  },

  async logout() {
    try {
      await axiosClient.post('/api/auth/logout')
    }
    catch (error) {
      console.error('Error during logout:', error)
    }
    finally {
      removeCookie('accessToken')
      window.location.href = '/auth'
    }
  },

  async requestPasswordReset(email: string) {
    const res = await axiosClient.post('/api/manage/auth/reset-password-request', { email })
    return res.data
  },

  async verifyOtp(email: string, otpCode: string, password: string) {
    const res = await axiosClient.post('/api/manage/auth/reset-password', { email, otpCode, password })
    return res.data
  },

}
