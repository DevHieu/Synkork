import axiosClient from '@/lib/axiosClient'
import { removeCookie, setCookie } from '@/lib/cookies'

import type { ChangePasswordData } from '../types/ChangePasswordData'
import type { LoginData } from '../types/LoginData'
import type { UpdateProfileData } from '../types/UpdateProfileData'

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

  async updateProfile(data: UpdateProfileData) {
    const res = await axiosClient.patch('/api/manage/auth/me', data)
    return res.data
  },

  async changePassword(data: ChangePasswordData) {
    const res = await axiosClient.patch('/api/manage/auth/me/password', data)
    return res.data
  },

  async login(loginData: LoginData) {
    const res = await axiosClient.post('/api/manage/auth/login', loginData)

    setCookie('accessToken', res.data.accessToken, 60 * 60 * 15) // 15 minutes

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
