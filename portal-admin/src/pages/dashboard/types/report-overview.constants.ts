import type { TimeRangeType } from '@/types/Date'

import type { ReportTimeRangeOption } from './report-overview.types'

export const REPORT_REASON_LABELS: Record<string, string> = {
  SPAM: 'Spam',
  INAPPROPRIATE: 'Nội dung không phù hợp',
  HARASSMENT: 'Quấy rối',
  HATE_SPEECH: 'Ngôn từ thù ghét',
  OTHER: 'Khác',
}

export const REPORT_REASON_COLORS: Record<string, string> = {
  SPAM: 'var(--chart-1)',
  INAPPROPRIATE: 'var(--chart-2)',
  HARASSMENT: 'var(--chart-3)',
  HATE_SPEECH: 'var(--chart-4)',
  OTHER: 'var(--chart-5)',
}

export const REPORT_TIME_RANGE_OPTIONS: ReportTimeRangeOption[] = [
  { value: 'WEEKLY', label: 'Tuần' },
  { value: 'MONTHLY', label: 'Tháng' },
  { value: 'QUARTERLY', label: 'Quý' },
  { value: 'YEARLY', label: 'Năm' },
]

export const REPORT_TIME_RANGE_LABELS: Record<TimeRangeType, string> = {
  WEEKLY: 'tuần',
  MONTHLY: 'tháng',
  QUARTERLY: 'quý',
  YEARLY: 'năm',
}

export const REPORT_CHART_COLORS = {
  user: 'var(--chart-3)',
  room: 'var(--chart-4)',
}
