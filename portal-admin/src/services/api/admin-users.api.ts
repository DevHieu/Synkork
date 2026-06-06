import axiosClient from '@/lib/axiosClient'

import type { User } from './schema'

import { mapSynkorkUser } from './users'

// ── Filter & Page types ───────────────────────────────────────────────────────

export interface IAdminUserFilter {
  keyword?: string
  role?: string
  status?: string
  page?: number // 0-indexed
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

// ── Service ───────────────────────────────────────────────────────────────────

export const adminUserService = {

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

    const res = await axiosClient.get(`/api/manage/admin/users?${params.toString()}`)
    const page: IAdminUserPage = res.data
    return {
      users: page.content.map(mapSynkorkUser),
      totalElements: page.totalElements,
    }
  },

  async getById(id: string): Promise<User> {
    const res = await axiosClient.get(`/api/manage/admin/users/${id}`)
    return mapSynkorkUser(res.data)
  },

  async create(data: {
    username: string
    email: string
    firstName: string
    lastName: string
    phoneNumber?: string
    role: string
    status: string
  }): Promise<User> {
    const res = await axiosClient.post('/api/manage/admin/users', {
      ...data,
      displayName: `${data.firstName} ${data.lastName}`.trim(),
    })
    return mapSynkorkUser(res.data)
  },

  async update(id: string, data: Partial<User>): Promise<User> {
    const res = await axiosClient.patch(`/api/manage/admin/users/${id}`, {
      ...data,
      displayName: data.firstName && data.lastName
        ? `${data.firstName} ${data.lastName}`.trim()
        : undefined,
    })
    return mapSynkorkUser(res.data)
  },

  async delete(id: string): Promise<void> {
    await axiosClient.delete(`/api/manage/admin/users/${id}`)
  },

  async invite(data: { email: string, role: string, description?: string }): Promise<void> {
    await axiosClient.post('/api/manage/admin/users/invite', data)
  },

  async updateStatus(id: string, status: string): Promise<User> {
    const res = await axiosClient.patch(`/api/manage/admin/users/${id}/status`, { status })
    return mapSynkorkUser(res.data)
  },
}
