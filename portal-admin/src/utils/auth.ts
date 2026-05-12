import VueCookies from 'vue-cookies'

import axiosClient from '@/lib/axiosClient'

const cookies = VueCookies as any

export async function getFreshToken(): Promise<string> {
  const response = await axiosClient.post(
    '/api/auth/refresh',
    {},
    { withCredentials: true },
  )
  const accessToken = response.data
  cookies.set('accessToken', accessToken, '15m')
  return accessToken
}
