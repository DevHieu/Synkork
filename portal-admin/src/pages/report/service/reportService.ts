import axiosClient from "@/lib/axiosClient"
import { ReportFilterParams, ReportStatus } from "@/pages/report/types/Reports"

export const getReports = async ( params: { params: ReportFilterParams } ) => {
  const res = await axiosClient.get('/api/manage/reports', params)
  return res.data
}
 
export const updateReportStatus = async ( reportId: string, status: ReportStatus, note?: string ) => {
  await axiosClient.patch(`/api/manage/reports/${reportId}/status`, { status, ...(note ? { note } : {}) })
}

export const deleteReport = async ( reportId: string ) => {
  await axiosClient.delete(`/api/manage/reports/${reportId}`)
}