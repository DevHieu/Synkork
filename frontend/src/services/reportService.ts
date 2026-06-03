import axiosClient from "@/lib/axiosClient"
import type { ReportRequest } from "@/types/Report"

export const createUserReport = async (data: ReportRequest) => {
  const res = await axiosClient.post('/api/reports/users', data)
  return res
}

export const createRoomReport = async (data: ReportRequest) => {
  const res = await axiosClient.post('/api/reports/rooms', data)
  return res
}