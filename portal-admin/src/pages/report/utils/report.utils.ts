import type { ReportReason, ReportSeverity } from '@/pages/report/types/Reports'

export const REASON_LABEL_MAP: Record<ReportReason, string> = {
  SPAM: 'Spam / Quảng cáo',
  HARASSMENT: 'Quấy rối / Đe dọa',
  INAPPROPRIATE: 'Nội dung không phù hợp',
  HATE_SPEECH: 'Ngôn từ thù ghét',
  OTHER: 'Lý do khác',
}

export const SEVERITY_CONFIG: Record<ReportSeverity, { label: string, class: string }> = {
  LOW: {
    label: 'Thấp',
    class: 'border-slate-300 bg-slate-50 text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-300',
  },
  MEDIUM: {
    label: 'Trung bình',
    class: 'border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300',
  },
  HIGH: {
    label: 'Cao',
    class: 'border-orange-300 bg-orange-50 text-orange-700 dark:border-orange-700 dark:bg-orange-950 dark:text-orange-300',
  },
  CRITICAL: {
    label: 'Nghiêm trọng',
    class: 'border-red-300 bg-red-50 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-300',
  },
}
