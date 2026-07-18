<script setup lang="ts">
import { Activity } from '@lucide/vue'
import { VisAxis, VisGroupedBar, VisXYContainer } from '@unovis/vue'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'

import type { ChartConfig } from '@/components/ui/chart'

import {
  Card,
  CardContent,
  CardDescription,
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useTimeRangeStore } from '@/stores/time-range'

import type {
  ReportChartLegendRow,
  ReportStats,
  ReportTrendPoint,
} from '../../types/report-overview.types'

import { dashboardService } from '../../services/dashboardService'
import {
  REPORT_CHART_COLORS,
  REPORT_TIME_RANGE_LABELS,
  REPORT_TIME_RANGE_OPTIONS,
} from '../../types/report-overview.constants'

const props = defineProps<{
  stats: ReportStats | null
}>()

const timeRangeStore = useTimeRangeStore()
const { timeRange } = storeToRefs(timeRangeStore)

const trendData = ref<ReportTrendPoint[]>([])
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

const typeRows = computed<ReportChartLegendRow[]>(() => [
  { name: 'Người dùng', value: props.stats?.userReports ?? 0, color: REPORT_CHART_COLORS.user },
  { name: 'Phòng', value: props.stats?.roomReports ?? 0, color: REPORT_CHART_COLORS.room },
])

const periodLabel = computed(() => REPORT_TIME_RANGE_LABELS[timeRange.value])

async function fetchChart() {
  isLoading.value = true
  try {
    trendData.value = await dashboardService.getReportChartData(timeRange.value)
  }
  catch (err) {
    console.error('Failed to load report trend:', err)
    trendData.value = []
  }
  finally {
    isLoading.value = false
  }
}

onMounted(fetchChart)
watch(timeRange, fetchChart)
</script>

<template>
  <Card>
    <CardHeader class="flex items-center justify-between">
      <div>
        <CardTitle class="flex items-center gap-2 text-base">
          <Activity class="h-4 w-4 text-muted-foreground" />
          Xu hướng tố cáo
        </CardTitle>
        <CardDescription>
          Tố cáo người dùng và phòng theo {{ periodLabel }}
        </CardDescription>
      </div>

      <Select v-model="timeRange">
        <SelectTrigger class="w-[140px]">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem v-for="opt in REPORT_TIME_RANGE_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </SelectItem>
        </SelectContent>
      </Select>
    </CardHeader>

    <CardContent>
      <div v-if="isLoading" class="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
        Đang tải...
      </div>
      <ChartContainer v-else-if="trendData.length" :config="chartConfig" class="h-[260px] w-full" :cursor="false">
        <VisXYContainer :data="trendData" :svg-defs="svgDefs" :x-domain="[-0.5, trendData.length - 0.5]">
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
            :tick-format="(i: number) => trendData[i] ? new Date(trendData[i].date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''"
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
