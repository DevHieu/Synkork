import axiosClient from '@/lib/axiosClient'

import type { RoomFormPayload, RoomParams, UserOption } from '../types/RoomTypes'

const PREFIX = import.meta.env.VITE_SERVER_API_PREFIX as string

export const roomService = {
  async getRooms(queryParams: RoomParams) {
    const res = await axiosClient.get(`${PREFIX}/manage/rooms`, { params: queryParams })
    return res.data
  },

  async getRoomDetail(roomId: string) {
    const res = await axiosClient.get(`${PREFIX}/manage/rooms/${roomId}`)
    return res.data.data
  },

  async createRoom(payload: RoomFormPayload) {
    const res = await axiosClient.post(`${PREFIX}/manage/rooms`, payload)
    return res.data
  },

  async updateRoom(roomId: string, payload: RoomFormPayload) {
    const res = await axiosClient.put(`${PREFIX}/manage/rooms/${roomId}`, payload)
    return res.data
  },

  async deleteRoom(roomId: string) {
    const res = await axiosClient.delete(`${PREFIX}/manage/rooms/${roomId}`)
    return res.data
  },

  async searchOwners(keyword: string): Promise<UserOption[]> {
    if (!keyword.trim())
      return []

    const res = await axiosClient.get(`${PREFIX}/manage/rooms/owners/search`, {
      params: { keyword },
    })

    return res.data.data || []
  },
}