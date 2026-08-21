<script setup lang="ts">
import { Activity } from '@lucide/vue'
import { VisAxis, VisGroupedBar, VisXYContainer } from '@unovis/vue'
import { storeToRefs } from 'pinia'
import { computed, ref, watch } from 'vue'

import type { ChartConfig } from '@/components/ui/chart'

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  ChartContainer,
  ChartCrosshair,
  ChartTooltip,
  ChartTooltipContent,
  componentToString,
} from '@/components/ui/chart'

import type {
  ReportChartLegendRow,
  ReportChartResponse,
  ReportTrendPoint,
} from '../../types/report-overview.types'

import { dashboardService } from '../../services/dashboardService'
import { useDashboardFilterStore } from '../../stores/dashboard-filter'
import {
  REPORT_CHART_COLORS,
} from '../../types/report-overview.constants'

const MIN_Y_MAX = 5 // tối thiểu của trục Y biểu đồ

const dashboardFilterStore = useDashboardFilterStore()
const { dateRange, dateRangeParams } = storeToRefs(dashboardFilterStore)

const trendData = ref<ReportChartResponse[]>([])
const isLoading = ref(false)

const chartConfig = {
  user: { label: 'User', color: REPORT_CHART_COLORS.user },
  room: { label: 'Room', color: REPORT_CHART_COLORS.room },
} satisfies ChartConfig

const svgDefs = `
<linearGradient id="fillReportUser" x1="0" y1="0" x2="0" y2="1">
  <stop offset="5%" stop-color="${REPORT_CHART_COLORS.user}" stop-opacity="0.8"/>
  <stop offset="95%" stop-color="${REPORT_CHART_COLORS.user}" stop-opacity="0.05"/>
</linearGradient>
<linearGradient id="fillReportRoom" x1="0" y1="0" x2="0" y2="1">
  <stop offset="5%" stop-color="${REPORT_CHART_COLORS.room}" stop-opacity="0.8"/>
  <stop offset="95%" stop-color="${REPORT_CHART_COLORS.room}" stop-opacity="0.05"/>
</linearGradient>
`

const chartPoint = computed<ReportTrendPoint[]>(() =>
  trendData.value.map(item => ({
    date: new Date(item.date),
    user: item.userReports,
    room: item.roomReports,
  })),
)

const yDomain = computed<[number, number]>(() => {
  const maxValue = chartPoint.value.reduce(
    (max, d) => Math.max(max, d.user ?? 0, d.room ?? 0),
    0,
  )

  return [0, Math.max(maxValue, MIN_Y_MAX)]
})

const typeRows = computed<ReportChartLegendRow[]>(() => {
  const totalUser = chartPoint.value.reduce((sum, d) => sum + (d.user ?? 0), 0)
  const totalRoom = chartPoint.value.reduce((sum, d) => sum + (d.room ?? 0), 0)

  return [
    { name: 'Người dùng', value: totalUser, color: REPORT_CHART_COLORS.user },
    { name: 'Phòng', value: totalRoom, color: REPORT_CHART_COLORS.room },
  ]
})

async function fetchData() {
  isLoading.value = true
  try {
    const data = await dashboardService.getReportChartData(dateRangeParams.value)
    trendData.value = Array.isArray(data) ? data : []
  }
  catch (err) {
    console.error('Failed to load report reasons:', err)
    trendData.value = []
  }
  finally {
    isLoading.value = false
  }
}

watch(dateRange, fetchData, { immediate: true })
</script>

<template>
  <Card>
    <CardHeader class="flex items-center justify-between">
      <div>
        <CardTitle class="flex items-center gap-2 text-base">
          <Activity class="h-4 w-4 text-muted-foreground" />
          Xu hướng tố cáo
        </CardTitle>
      </div>
    </CardHeader>

    <CardContent>
      <div v-if="isLoading" class="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
        Đang tải...
      </div>
      <ChartContainer v-else-if="chartPoint.length" :config="chartConfig" class="h-[260px] w-full" :cursor="false">
        <VisXYContainer :data="chartPoint" :svg-defs="svgDefs" :x-domain="[-0.5, chartPoint.length - 0.5]" :y-domain="yDomain">
          <VisGroupedBar
            :x="(_: ReportTrendPoint, i: number) => i"
            :y="[(d: ReportTrendPoint) => d.user, (d: ReportTrendPoint) => d.room]"
            :color="(_: ReportTrendPoint, i: number) => ['url(#fillReportUser)', 'url(#fillReportRoom)'][i]"
            :rounded-corners="6"
            :group-padding="0.25"
            :bar-padding="0.15"
          />

          <VisAxis
            type="x"
            :x="(_: ReportTrendPoint, i: number) => i"
            :num-ticks="6"
            :tick-line="false"
            :domain-line="false"
            :grid-line="false"
            :tick-format="(i: number) => chartPoint[i] ? new Date(chartPoint[i].date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''"
          />

          <VisAxis type="y" :tick-line="false" :domain-line="false" :grid-line="true" />

          <ChartTooltip />

          <ChartCrosshair
            :template="componentToString(chartConfig, ChartTooltipContent)"
            :color="(_: ReportTrendPoint, i: number) => [REPORT_CHART_COLORS.user, REPORT_CHART_COLORS.room][i]"
          />
        </VisXYContainer>
      </ChartContainer>
      <div v-else class="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
        Không có dữ liệu
      </div>

      <div class="mt-4 flex items-center justify-center gap-8">
        <div v-for="row in typeRows" :key="row.name" class="flex items-center gap-2">
          <div class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: row.color }" />
          <p class="text-xs text-muted-foreground">
            Tố cáo {{ row.name }} <strong>({{ row.value.toLocaleString() }})</strong>
          </p>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
