import axiosClient from '@/lib/axiosClient'

import type { PasswordResetParams } from '../types/passwordResetTypes'

const baseUrl = '/api/manage/admin/change-password-request'

export const passwordResetService = {
  async getAll(params: PasswordResetParams) {
    const response = await axiosClient.get(baseUrl, { params })
    return response.data
  },

  async approve(id: string) {
    const response = await axiosClient.post(`${baseUrl}/${id}/approve`)
    return response.data
  },

  async reject(id: string) {
    const response = await axiosClient.post(`${baseUrl}/${id}/reject`)
    return response.data
  },
}
