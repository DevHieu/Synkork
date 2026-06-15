import axiosClient from '@/lib/axiosClient'

import type {
  CreateManagerPayload,
  ManagerAccount,
  ManagerPage,
  ManagerParams,
  UpdateManagerPayload,
} from '../types/managerTypes'

const baseUrl = '/api/manage/admin'

export const managerService = {
  async getAll(params: ManagerParams) {
    const response = await axiosClient.get<ManagerPage>(baseUrl, { params })
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

  async delete(id: string) {
    await axiosClient.delete(`${baseUrl}/${id}`)
  },
}
