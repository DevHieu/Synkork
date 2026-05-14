import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'

import axios from 'axios'
import VueCookies from 'vue-cookies'

import { getFreshToken } from '@/utils/auth'

const cookies = VueCookies as any

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_SERVER_API_URL as string,
  timeout: 10000,
  withCredentials: true,
})

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = cookies.get('accessToken')
    const url = config.url ?? ''
    if (!url.includes('/auth')) {
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

    // Token mà không hợp lệ thì về trang đăng nhập
    if (
      error.response?.status === 401
      && error.response?.data?.error === 'INVALID_TOKEN'
    ) {
      cookies.remove('accessToken')
      cookies.remove('refreshToken')
      window.location.href = '/auth'
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
        cookies.remove('accessToken')
        window.location.href = '/auth'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export default axiosClient
