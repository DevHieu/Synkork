import axiosClient from '@/lib/axiosClient'

import type { User, UserParams } from '../types/userTypes'

export const userService = {

  async getAll(params: { params: UserParams }) {
    const res = await axiosClient.get(`/api/manage/users`, params)
    return res.data
  },

  // Lấy 1 user theo id
  async getById(id: string) {
    const res = await axiosClient.get(`/api/manage/users/${id}`)
    return res.data
  },

  // Tạo user mới
  async create(data: {
    firstName: string
    lastName: string
    username: string
    email: string
    status: string
    plan: string
  }) {
    const res = await axiosClient.post('/api/manage/users', data)
    return res.data
  },

  // Cập nhật user
  async update(id: string, data: Partial<User>) {
    const res = await axiosClient.patch(`/api/manage/users/${id}`, data)
    return res.data
  },

  // Xóa user
  async delete(id: string) {
    await axiosClient.delete(`/api/manage/users/${id}`)
  },

  // Cập nhật trạng thái user
  async updateStatus(id: string, status: string) {
    const res = await axiosClient.patch(`/api/manage/users/${id}/status`, { status })
    return res.data
  },
}
