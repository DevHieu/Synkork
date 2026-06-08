import axiosClient from "@/lib/axiosClient"
import { ReportFilterParams, ReportPageResponse, ReportStatus } from "@/types/Reports"

export const fetchReports = async (
  params: ReportFilterParams = {}
): Promise<ReportPageResponse> => {
  // Loại bỏ các key có giá trị rỗng/undefined để URL gọn
  const query: Record<string, string | number> = {}
 
  if (params.search?.trim())   query.search     = params.search.trim()
  if (params.status)           query.status     = params.status
  if (params.reportType)       query.reportType = params.reportType
  if (params.dateFrom)         query.dateFrom   = params.dateFrom
  if (params.dateTo)           query.dateTo     = params.dateTo
  if (params.page !== undefined) query.page     = params.page
  if (params.size  !== undefined) query.size    = params.size
 
  const res = await axiosClient.get('/api/manage/reports', { params: query })
  return res.data as ReportPageResponse
}
 
// ── Admin cập nhật trạng thái một report ─────────────────────────────
export const updateReportStatus = async (
  reportId: string,
  status: ReportStatus
): Promise<void> => {
  await axiosClient.patch(`/api/manage/reports/${reportId}/status`, { status })
}