import axiosClient from "@/lib/axiosClient"
import type { ReportRequest } from "@/types/Report"

function buildFormData(data: ReportRequest, evidence: File | null) {
  const formData = new FormData()
  formData.append('targetId', data.targetId)
  formData.append('reason', data.reason)
  if (data.description) formData.append('description', data.description)
  if (evidence) formData.append('evidence', evidence)
  return formData
}

export const createUserReport = async (data: ReportRequest, evidence: File | null = null) => {
  const res = await axiosClient.post('/api/reports/users', buildFormData(data, evidence), {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return res
}

export const createRoomReport = async (data: ReportRequest, evidence: File | null = null) => {
  const res = await axiosClient.post('/api/reports/rooms', buildFormData(data, evidence), {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return res
}