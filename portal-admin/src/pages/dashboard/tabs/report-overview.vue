<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { onMounted, ref, watch } from 'vue'

import DateRangePicker from '@/components/date-range-picker.vue'

import type { ReportStats } from '../types/report-overview.types.ts'

import ReportReasonCard from '../components/report/report-reason-card.vue'
import ReportStatsCards from '../components/report/report-stats-cards.vue'
import ReportTrendCard from '../components/report/report-trend-card.vue'
import { dashboardService } from '../services/dashboardService.ts'
import { useDashboardFilterStore } from '../stores/dashboard-filter'

const dashboardFilterStore = useDashboardFilterStore()
const { dateRange, dateRangeLabel, dateRangeParams } = storeToRefs(dashboardFilterStore)

const stats = ref<ReportStats | null>(null)
const isLoadingStats = ref(false)

async function fetchStats() {
  isLoadingStats.value = true
  try {
    stats.value = await dashboardService.getReportStatsData(dateRangeParams.value)
  }
  catch (err) {
    console.error('Failed to load report stats:', err)
    stats.value = null
  }
  finally {
    isLoadingStats.value = false
  }
}

onMounted(fetchStats)
watch(dateRange, fetchStats)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Thống kê tố cáo
        </h2>
        <p class="text-sm text-muted-foreground">
          Dữ liệu trong khoảng: {{ dateRangeLabel }}
        </p>
      </div>

      <div class="w-full sm:w-[280px]">
        <DateRangePicker v-model="dateRange" />
      </div>
    </div>

    <ReportStatsCards :stats="stats" :is-loading="isLoadingStats" />

    <div class="grid gap-4 lg:grid-cols-2">
      <ReportReasonCard />
      <ReportTrendCard :stats="stats" />
    </div>
  </div>
</template>
