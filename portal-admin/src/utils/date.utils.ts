import dayjs from 'dayjs'

import type { AppDateRange } from '@/types/Date'

export const today = new Date()

export function defaultDateRange(): AppDateRange {
  return null
}
export const toStartTime = (value: Date) => dayjs(value).startOf('day').valueOf()

export const toEndTime = (value: Date) => dayjs(value).endOf('day').valueOf()

export function formatTimestamp(value: number | string | null | undefined) {
  if (!value)
    return '-'

  const d = dayjs(value)

  return d.isValid() ? d.format('HH:mm DD/MM/YYYY') : '-'
}

export function formatToISODateTime(date: Date, isEndOfDay = false) {
  if (!date)
    return undefined

  const d = dayjs(date)
  return isEndOfDay
    ? d.endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    : d.format('YYYY-MM-DDTHH:mm:ss')
}
