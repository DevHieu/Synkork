import axiosClient from '@/lib/axiosClient'

import type { RoomParams } from '../types/RoomTypes'

export const roomService = {
  async getRooms(params: { params: RoomParams }) {
    const res = await axiosClient.get(
      '/manage/rooms',
      params,
    )

    return res.data
  },

  async getRoomDetail(roomId: string) {
    const res = await axiosClient.get(
      `/api/manage/rooms/${roomId}`,
    )

    return res.data
  },

  async lockRoom(roomId: string, status: string){
    const res = await axiosClient.patch(`/api/manage/rooms/${roomId}/status`, { status })
    return res.data
  }
}