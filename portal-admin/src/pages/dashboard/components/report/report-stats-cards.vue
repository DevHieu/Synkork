<script setup lang="ts">
import { CheckCircle2, Clock3, Flag, XCircle } from '@lucide/vue'

import DataCard from '../../../components/data-card.vue'

import type { ReportStats } from '../../types/report-overview.types.ts'

defineProps<{
  stats: ReportStats | null
  isLoading?: boolean
}>()

function formatNumber(value: number | undefined): string {
  return value?.toLocaleString() ?? '-'
}
</script>

<template>
  <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <DataCard title="Tổng tố cáo" :data="isLoading ? '-' : formatNumber(stats?.totalReports)" :icon="Flag" />
    <DataCard title="Chờ xử lý" :data="isLoading ? '-' : formatNumber(stats?.pendingReports)" :icon="Clock3" />
    <DataCard title="Đã giải quyết" :data="isLoading ? '-' : formatNumber(stats?.resolvedReports)" :icon="CheckCircle2" />
    <DataCard title="Đã bác bỏ" :data="isLoading ? '-' : formatNumber(stats?.dismissedReports)" :icon="XCircle" />
  </div>
</template>
