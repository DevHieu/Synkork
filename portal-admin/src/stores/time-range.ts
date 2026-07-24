import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { TimeRangeType } from '@/types/Date'

export const useTimeRangeStore = defineStore('timeRange', () => {
  const timeRange = ref<TimeRangeType>('WEEKLY')

  function changeTimeRange(newRange: TimeRangeType) {
    timeRange.value = newRange
    return true
  }

  return { timeRange, changeTimeRange }
})
