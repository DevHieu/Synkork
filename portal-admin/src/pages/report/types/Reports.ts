export type ReportType = 'USER' | 'ROOM'

export type ReportStatus
  = | 'PENDING'
    | 'REVIEWED'
    | 'RESOLVED'
    | 'DISMISSED'

export type ReportReason
  = | 'SPAM'
    | 'HARASSMENT'
    | 'INAPPROPRIATE'
    | 'HATE_SPEECH'
    | 'OTHER'

export type ReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface Report {
  id: string
  reporterId: string
  reporterName?: string | null
  reporterEmail: string
  targetUserId?: string | null
  targetRoomId?: string | null
  targetName: string
  targetEmail?: string | null
  reason: ReportReason
  severity: ReportSeverity
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
  severity?: ReportSeverity | ''
  fromDate?: string
  toDate?: string
  page?: number
  size?: number
}

export interface UpdateReportStatusPayload {
  status: ReportStatus
  reason?: string
}
