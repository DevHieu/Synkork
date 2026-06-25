import dayjs from 'dayjs'

export function formatPickerToDate(date: Date) {
  return dayjs(date).format('YYYYMMDD')
}

export function formatTimestamp(value: number | string | null | undefined) {
  if (!value)
    return '-'
  const date = typeof value === 'number' ? new Date(value) : new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : dayjs(date).format('HH:mm dd/MM/yyyy')
}

export function formatToISODateTime(date: Date, isEndOfDay = false) {
  if (!date)
    return undefined
  const d = dayjs(date)
  return isEndOfDay
    ? d.endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    : d.format('YYYY-MM-DDTHH:mm:ss')
}
