export type ReportType = 'USER' | 'ROOM'

export type ReportStatus =
  | 'PENDING'
  | 'REVIEWED'
  | 'RESOLVED'
  | 'DISMISSED'

export interface Report {
  id: string
  reporterId: string
  reporterName?: string | null
  reporterEmail: string
  targetUserId?: string | null
  targetRoomId?: string | null
  targetName: string
  targetEmail?: string | null
  reason: string
  note: string
  description?: string | null
  reportType: ReportType
  status: ReportStatus
  createdAt: string
}

export interface ReportFilterParams {
  search?: string
  status?: ReportStatus | ''
  reportType?: ReportType | ''
  fromDate?: string   
  toDate?: string
  page?: number
  size?: number
}

export interface UpdateReportStatusPayload {
  status: ReportStatus
  reason?: string
}
