import axiosClient from '@/lib/axiosClient'

import type { LogParams } from '../types/LogTypes'

export const logService = {
  async getLogs(params: { params: LogParams }) {
    const res = await axiosClient.get('/api/manage/admin/logs', params)

    return res.data
  },

  async getLogById(id: string) {
    const res = await axiosClient.get(`/api/manage/admin/logs/${id}`)

    return res.data
  },
}
