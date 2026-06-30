import axiosClient from '@/lib/axiosClient'

import type { User, UserParams, UserRole } from '../types/userTypes'

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
    role: 'user'
  }) {
    const res = await axiosClient.post('/api/manage/users', data)
    return res.data
  },

  // Cập nhật user
  async update(id: string, data: {
    displayName?: string
    email?: string
    status?: string
    plan?: string
    role?: UserRole
  }) {
    const res = await axiosClient.patch(`/api/manage/users/${id}`, data)
    return res.data
  },

  // Khóa mềm user và gửi lý do thông báo cho user
  async delete(id: string, reason: string) {
    await axiosClient.delete(`/api/manage/users/${id}`, {
      data: { reason },
    })
  },

  // Cập nhật trạng thái user
  async updateStatus(id: string, status: string) {
    const res = await axiosClient.patch(`/api/manage/users/${id}/status`, { status })
    return res.data
  },

  async warnUser(id: string){
    const res = await axiosClient.patch(`/api/manage/users/${id}/warn`)
    return res.data
  }
}
