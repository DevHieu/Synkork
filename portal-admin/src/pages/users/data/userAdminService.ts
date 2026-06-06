import axiosClient from '@/lib/axiosClient'

import type { User } from './schema'

import { mapSynkorkUser } from './users'

export interface IAdminUserFilter {
  keyword?: string
  role?: string
  status?: string
  page?: number
  size?: number
}

export interface IAdminUserPage {
  content: any[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export const adminUserService = {

  // Lấy danh sách user (có filter + phân trang)
  async getAll(filter: IAdminUserFilter = {}): Promise<{ users: User[], totalElements: number }> {
    const params = new URLSearchParams()
    if (filter.keyword)
      params.set('keyword', filter.keyword)
    if (filter.role)
      params.set('role', filter.role)
    if (filter.status)
      params.set('status', filter.status)
    params.set('page', String(filter.page ?? 0))
    params.set('size', String(filter.size ?? 20))

    const res = await axiosClient.get(`/api/manage/users?${params.toString()}`)
    const page: IAdminUserPage = res.data
    return {
      users: page.content.map(mapSynkorkUser),
      totalElements: page.totalElements,
    }
  },

  // Lấy 1 user theo id
  async getById(id: string): Promise<User> {
    const res = await axiosClient.get(`/api/manage/users/${id}`)
    return mapSynkorkUser(res.data)
  },

  // Tạo user mới
  async create(data: {
    username: string
    email: string
    firstName: string
    lastName: string

    status: string
  }): Promise<User> {
    const res = await axiosClient.post('/api/manage/users', {
      ...data,
      displayName: `${data.firstName} ${data.lastName}`.trim(),
    })
    return mapSynkorkUser(res.data)
  },

  // Cập nhật user
  async update(id: string, data: Partial<User>): Promise<User> {
    const res = await axiosClient.patch(`/api/manage/users/${id}`, {
      ...data,
      displayName: data.firstName && data.lastName
        ? `${data.firstName} ${data.lastName}`.trim()
        : undefined,
    })
    return mapSynkorkUser(res.data)
  },

  // Xóa user
  async delete(id: string): Promise<void> {
    await axiosClient.delete(`/api/manage/users/${id}`)
  },

  // Cập nhật trạng thái user
  async updateStatus(id: string, status: string): Promise<User> {
    const res = await axiosClient.patch(`/api/manage/users/${id}/status`, { status })
    return mapSynkorkUser(res.data)
  },
}
