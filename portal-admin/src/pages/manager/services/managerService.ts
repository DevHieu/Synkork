import axiosClient from '@/lib/axiosClient'

import type {
  CreateManagerPayload,
  ManagerAccount,
  ManagerParams,
  UpdateManagerPayload,
} from '../types/managerTypes'

const baseUrl = '/api/manage/admin/manager'

export const managerService = {
  async getAll(params: ManagerParams) {
    const response = await axiosClient.get(baseUrl, { params })
    return response.data
  },

  async getById(id: string) {
    const response = await axiosClient.get<ManagerAccount>(`${baseUrl}/${id}`)
    return response.data
  },

  async create(data: CreateManagerPayload) {
    const response = await axiosClient.post<ManagerAccount>(baseUrl, data)
    return response.data
  },

  async update(id: string, data: UpdateManagerPayload) {
    const response = await axiosClient.patch<ManagerAccount>(`${baseUrl}/${id}`, data)
    return response.data
  },

  async lock(id: string, reason: string) {
    await axiosClient.delete(`${baseUrl}/${id}`, {
      data: { reason },
    })
  },
}
