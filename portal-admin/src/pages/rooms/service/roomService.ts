import axiosClient from '@/lib/axiosClient'

import type { RoomFormPayload, RoomParams, UserOption } from '../types/RoomTypes'

export const roomService = {
  async getRooms(queryParams: RoomParams) {
    const res = await axiosClient.get('/api/manage/rooms', { params: queryParams })
    return res.data
  },

  async getRoomDetail(roomId: string) {
    const res = await axiosClient.get(`/api/manage/rooms/${roomId}`)
    return res.data.data
  },

  async getRoomMembers(roomId: string) {
    const res = await axiosClient.get(`/api/manage/rooms/${roomId}/members`)

    return res.data.data
  },

  async getRoomSpaces(roomId: string) {
    const res = await axiosClient.get(`/api/manage/rooms/${roomId}/spaces`)

    return res.data.data
  },

  async createRoom(payload: RoomFormPayload) {
    const res = await axiosClient.post('/api/manage/rooms', payload)
    return res.data
  },

  async updateRoom(roomId: string, payload: RoomFormPayload) {
    const res = await axiosClient.put(`/api/manage/rooms/${roomId}`, payload)
    return res.data
  },

  async searchOwners(keyword: string): Promise<UserOption[]> {
    if (!keyword.trim())
      return []

    const res = await axiosClient.get('/api/manage/rooms/owners/search', {
      params: { keyword },
    })

    return res.data.data || []
  },

  async changeRoomStatus(roomId: string, status: string, reason?: string) {
    const res = await axiosClient.patch(`/api/manage/rooms/${roomId}/status`, {
      status,
      ...(reason ? { reason } : {}),
    })
    return res.data
  },

  async warnRoom(roomId: string) {
    const res = await axiosClient.patch(`/api/manage/rooms/${roomId}/warn`)
    return res.data
  },
}
