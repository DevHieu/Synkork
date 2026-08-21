import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'

import axios from 'axios'

import { getFreshToken } from '@/utils/auth'

import { getCookie, removeCookie } from './cookies'

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_SERVER_API_URL as string,
  timeout: 10000,
  withCredentials: true,
})

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getCookie('accessToken')
    const url = config.url ?? ''
    if (!url.includes('auth/')) {
      if (token) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    return config
  },
  error => Promise.reject(error),
)

axiosClient.interceptors.response.use(
  response => response,
  async (error) => {
    const originalRequest = error.config

    if (originalRequest.url.includes('/auth/refresh')) {
      return Promise.reject(error)
    }

    // 403: FORBIDDEN: Không có quyền -> cút ra đăng nhập luôn
    if (error.response?.status === 403 && !originalRequest.url.includes('/auth')) {
      removeCookie('accessToken')
      removeCookie('refreshToken')
      window.location.href = '/auth/sign-in'
    }

    // Token mà không hợp lệ thì về trang đăng nhập
    if (
      error.response?.status === 401
      && ['INVALID_TOKEN', 'ACCOUNT_LOCKED'].includes(error.response?.data?.error)
    ) {
      removeCookie('accessToken')
      removeCookie('refreshToken')
      window.location.href = '/auth/sign-in'
    }

    if (
      error.response?.status === 401
      && error.response?.data?.error === 'TOKEN_EXPIRED'
      && !originalRequest._retry
    ) {
      originalRequest._retry = true

      try {
        const newAccessToken = await getFreshToken()

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return axiosClient(originalRequest)
      }
      catch (refreshError: any) {
        removeCookie('accessToken')
        window.location.href = '/auth/sign-in'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export default axiosClient
