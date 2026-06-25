import type { ReportFilterParams, ReportStatus } from '@/pages/report/types/Reports'

import axiosClient from '@/lib/axiosClient'

export async function getReports(params: { params: ReportFilterParams }) {
  const res = await axiosClient.get('/api/manage/reports', params)
  return res.data
}

export async function updateReportStatus(reportId: string, status: ReportStatus, note?: string) {
  await axiosClient.patch(`/api/manage/reports/${reportId}/status`, { status, ...(note ? { note } : {}) })
}

export async function deleteReport(reportId: string) {
  await axiosClient.delete(`/api/manage/reports/${reportId}`)
}
