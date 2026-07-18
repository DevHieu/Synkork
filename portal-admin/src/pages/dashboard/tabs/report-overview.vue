<script setup lang="ts">
import { onMounted, ref } from 'vue'

import type { ReportStats } from '../types/report-overview.types.ts'

import ReportReasonCard from '../components/report/report-reason-card.vue'
import ReportStatsCards from '../components/report/report-stats-cards.vue'
import ReportTrendCard from '../components/report/report-trend-card.vue'
import { dashboardService } from '../services/dashboardService.ts'

const stats = ref<ReportStats | null>(null)
const isLoadingStats = ref(false)

async function fetchStats() {
  isLoadingStats.value = true
  try {
    stats.value = await dashboardService.getReportStatsData()
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
</script>

<template>
  <ReportStatsCards :stats="stats" :is-loading="isLoadingStats" />

  <div class="grid gap-4 lg:grid-cols-2">
    <ReportReasonCard />
    <ReportTrendCard :stats="stats" />
  </div>
</template>
