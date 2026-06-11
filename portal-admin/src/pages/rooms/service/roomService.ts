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
      `/manage/rooms/${roomId}`,
    )

    return res.data
  },
}