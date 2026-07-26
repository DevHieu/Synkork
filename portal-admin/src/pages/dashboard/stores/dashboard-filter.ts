import dayjs from 'dayjs'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import type { AppDateRange } from '@/types/Date'

import { formatToISODateTime } from '@/utils/date.utils'

export interface DashboardDateRangeParams {
  dateFrom?: string
  dateTo?: string
}

export const useDashboardFilterStore = defineStore('dashboardFilter', () => {
  const dateRange = ref<AppDateRange>(null)

  const dateRangeParams = computed<DashboardDateRangeParams | undefined>(() => {
    if (!dateRange.value)
      return undefined

    return {
      dateFrom: formatToISODateTime(dateRange.value.from),
      dateTo: formatToISODateTime(dateRange.value.to, true),
    }
  })

  const dateRangeLabel = computed(() => {
    if (!dateRange.value)
      return 'Tất cả thời gian'

    return `${dayjs(dateRange.value.from).format('DD/MM/YYYY')} - ${dayjs(dateRange.value.to).format('DD/MM/YYYY')}`
  })

  return {
    dateRange,
    dateRangeLabel,
    dateRangeParams,
  }
})
