export type ReportType = 'USER' | 'ROOM'

export type ReportStatus =
  | 'PENDING'
  | 'REVIEWED'
  | 'RESOLVED'
  | 'DISMISSED'

export interface Report {
  id: string
  reporterId: string

  targetUserId?: string | null
  targetRoomId?: string | null

  targetName: string

  reason: string
  description?: string | null

  reportType: ReportType
  status: ReportStatus

  createdAt: string
  updatedAt: string
}

export interface ReportFilterParams {
  search?: string
  status?: ReportStatus | ''
  reportType?: ReportType | ''
  dateFrom?: string   // yyyy-MM-dd
  dateTo?: string     // yyyy-MM-dd
  page?: number       // 0-based
  size?: number
}
 
// ── Kết quả trả về từ server (phân trang) ────────────────────────────
export interface ReportPageResponse {
  content: Report[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}