import type { TimeRangeType } from '@/types/Date'

export interface ReportStats {
  totalReports: number
  pendingReports: number
  resolvedReports: number
  dismissedReports: number
  userReports: number
  roomReports: number
}

export interface ReportChartResponse {
  date: string
  userReports: number
  roomReports: number
}

export interface ReportTrendPoint {
  date: Date
  user: number
  room: number
}

export interface ReportReasonStat {
  reason: string
  reportType: 'USER' | 'ROOM'
  count: number
}

export interface ReportReasonRow {
  reason: string
  count: number
}

export interface ReportChartLegendRow {
  name: string
  value: number
  color: string
}

export type ReportReasonScope = 'all' | 'user' | 'room'

export interface ReportTimeRangeOption {
  value: TimeRangeType
  label: string
}
