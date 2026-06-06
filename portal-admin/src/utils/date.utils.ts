import dayjs from 'dayjs'

export const today = new Date()

export function defaultDateRange() {
  return {
    from: dayjs(today).subtract(30, 'day').startOf('day').toDate(),
    to: dayjs(today).endOf('day').toDate(),
  }
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
