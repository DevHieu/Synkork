<script setup lang="ts">
import { VisArea, VisAxis, VisDonut, VisLine, VisSingleContainer, VisXYContainer } from '@unovis/vue'
import { Activity, CheckCircle2, Clock3, Flag, ShieldAlert, XCircle } from '@lucide/vue'

import type { ChartConfig } from '@/components/ui/chart'

import { ChartContainer, ChartCrosshair, ChartTooltip, ChartTooltipContent, componentToString } from '@/components/ui/chart'

import DataCard from '../components/data-card.vue'
import { dashboardService } from '../services/dashboardService'

interface ReportStats {
  totalReports: number
  pendingReports: number
  resolvedReports: number
  dismissedReports: number
  userReports: number
  roomReports: number
}

interface TrendPoint {
  date: Date
  user: number
  room: number
}

interface ChartRow {
  name: string
  value: number
  color: string
}

type TimeRange = 'weekly' | 'monthly' | 'quarterly' | 'yearly'

const PERIOD_MAP: Record<TimeRange, 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY'> = {
  weekly: 'WEEKLY',
  monthly: 'MONTHLY',
  quarterly: 'QUARTERLY',
  yearly: 'YEARLY',
}

const TIME_RANGE_OPTIONS: { value: TimeRange; label: string }[] = [
  { value: 'weekly', label: 'Tuần' },
  { value: 'monthly', label: 'Tháng' },
  { value: 'quarterly', label: 'Quý' },
  { value: 'yearly', label: 'Năm' },
]

const CHART_COLORS = {
  user: 'var(--chart-1)',
  room: 'var(--chart-2)',
  pending: 'var(--chart-1)',
  resolved: 'var(--chart-2)',
  dismissed: 'var(--chart-3)',
}

const chartConfig = {
  user: { label: 'User', color: CHART_COLORS.user },
  room: { label: 'Room', color: CHART_COLORS.room },
} satisfies ChartConfig

const svgDefs = `
<linearGradient id="fillUser" x1="0" y1="0" x2="0" y2="1">
  <stop offset="5%" stop-color="${CHART_COLORS.user}" stop-opacity="0.8"/>
  <stop offset="95%" stop-color="${CHART_COLORS.user}" stop-opacity="0.05"/>
</linearGradient>
<linearGradient id="fillRoom" x1="0" y1="0" x2="0" y2="1">
  <stop offset="5%" stop-color="${CHART_COLORS.room}" stop-opacity="0.8"/>
  <stop offset="95%" stop-color="${CHART_COLORS.room}" stop-opacity="0.05"/>
</linearGradient>
`

const stats = ref<ReportStats | null>(null)
const trendData = ref<TrendPoint[]>([])
const timeRange = ref<TimeRange>('weekly')
const isLoadingStats = ref(false)
const isLoadingChart = ref(false)
const errorMessage = ref<string | null>(null)

const statusRows = computed<ChartRow[]>(() => [
  { name: 'Chờ xử lý', value: stats.value?.pendingReports ?? 0, color: CHART_COLORS.pending },
  { name: 'Đã giải quyết', value: stats.value?.resolvedReports ?? 0, color: CHART_COLORS.resolved },
  { name: 'Đã bác bỏ', value: stats.value?.dismissedReports ?? 0, color: CHART_COLORS.dismissed },
])

const typeRows = computed<ChartRow[]>(() => [
  { name: 'Người dùng', value: stats.value?.userReports ?? 0, color: CHART_COLORS.user },
  { name: 'Phòng', value: stats.value?.roomReports ?? 0, color: CHART_COLORS.room },
])

function formatNumber(value: number | undefined): string {
  return value?.toLocaleString() ?? '-'
}

async function fetchStats() {
  isLoadingStats.value = true
  try {
    stats.value = await dashboardService.getReportStatsData()
  } catch (err) {
    console.error(err)
    errorMessage.value = 'Failed to load report stats.'
  } finally {
    isLoadingStats.value = false
  }
}

async function fetchChart() {
  isLoadingChart.value = true
  try {
    const data = await dashboardService.getReportChartData(PERIOD_MAP[timeRange.value])
    trendData.value = data.map((row: { date: string; userReports: number; roomReports: number }) => ({
      date: new Date(row.date),
      user: row.userReports,
      room: row.roomReports,
    }))
  } catch (err) {
    console.error(err)
    errorMessage.value = 'Failed to load report trend.'
  } finally {
    isLoadingChart.value = false
  }
}

watch(timeRange, fetchChart)

onMounted(() => {
  fetchStats()
  fetchChart()
})
</script>

<template>
  <!-- Cards -->
  <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <DataCard title="Tổng tố cáo" :data="formatNumber(stats?.totalReports)" :icon="Flag" />
    <DataCard title="Chờ xử lý" :data="formatNumber(stats?.pendingReports)" :icon="Clock3" />
    <DataCard title="Đã giải quyết" :data="formatNumber(stats?.resolvedReports)" :icon="CheckCircle2" />
    <DataCard title="Đã bác bỏ" :data="formatNumber(stats?.dismissedReports)" :icon="XCircle" />
  </div>

  <div class="grid gap-4 lg:grid-cols-2">
    <!-- Status -->
    <UiCard>
      <UiCardHeader>
        <UiCardTitle class="flex items-center gap-2 text-base">
          <ShieldAlert class="h-4 w-4 text-muted-foreground" />
          Trạng thái tố cáo
        </UiCardTitle>
        <UiCardDescription>
          Số lượng tố cáo theo trạng thái xử lý
        </UiCardDescription>
      </UiCardHeader>

      <UiCardContent>
        <div class="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
          <div class="h-[220px]">
            <VisSingleContainer :data="statusRows" class="h-full">
              <VisDonut
                :value="(d: ChartRow) => d.value" :color="(d: ChartRow) => d.color" :arc-width="30"
                :corner-radius="6" :pad-angle="0.04" central-label="Tố cáo"
                :central-sub-label="formatNumber(stats?.totalReports)"
              />
            </VisSingleContainer>
          </div>

          <div class="space-y-3">
            <div
              v-for="row in statusRows" :key="row.name"
              class="flex min-w-36 items-center justify-between gap-6 text-sm"
            >
              <div class="flex items-center gap-2">
                <span class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: row.color }" />
                <span class="text-muted-foreground">{{ row.name }}</span>
              </div>
              <span class="font-medium">{{ row.value.toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </UiCardContent>
    </UiCard>

    <!-- Report Trend -->
    <UiCard>
      <UiCardHeader class="flex items-center justify-between">
        <div>
          <UiCardTitle class="flex items-center gap-2 text-base">
            <Activity class="h-4 w-4 text-muted-foreground" />
            Xu hướng tố cáo
          </UiCardTitle>
          <UiCardDescription>
            Tố cáo người dùng và phòng theo thời gian
          </UiCardDescription>
        </div>

        <UiSelect v-model="timeRange">
          <UiSelectTrigger class="w-[140px]">
            <UiSelectValue />
          </UiSelectTrigger>
          <UiSelectContent>
            <UiSelectItem v-for="opt in TIME_RANGE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </UiSelectItem>
          </UiSelectContent>
        </UiSelect>
      </UiCardHeader>

      <UiCardContent>
        <ChartContainer :config="chartConfig" class="h-[260px] w-full" :cursor="false">
          <VisXYContainer :data="trendData" :svg-defs="svgDefs">
            <VisArea
              :x="(d: TrendPoint) => d.date" :y="[(d: TrendPoint) => d.user, (d: TrendPoint) => d.room]"
              :color="(_: TrendPoint, i: number) => ['url(#fillUser)', 'url(#fillRoom)'][i]" :opacity="0.55"
            />

            <VisLine
              :x="(d: TrendPoint) => d.date" :y="[(d: TrendPoint) => d.user, (d: TrendPoint) => d.room]"
              :color="(_: TrendPoint, i: number) => [CHART_COLORS.user, CHART_COLORS.room][i]" :line-width="2"
            />

            <VisAxis
              type="x" :x="(d: TrendPoint) => d.date" :num-ticks="6" :tick-line="false" :domain-line="false"
              :grid-line="false"
              :tick-format="(d: Date) => new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })"
            />

            <VisAxis type="y" :tick-line="false" :domain-line="false" />

            <ChartTooltip />

            <ChartCrosshair
              :template="componentToString(chartConfig, ChartTooltipContent)"
              :color="(_: TrendPoint, i: number) => [CHART_COLORS.user, CHART_COLORS.room][i]"
            />
          </VisXYContainer>
        </ChartContainer>

        <div class="mt-4 flex items-center justify-center gap-8">
          <div v-for="row in typeRows" :key="row.name" class="flex items-center gap-2">
            <div class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: row.color }" />
            <div>
              <p class="text-xs text-muted-foreground">
                Tố cáo {{ row.name }}
              </p>
              <p class="font-semibold">
                {{ row.value.toLocaleString() }}
              </p>
            </div>
          </div>
        </div>
      </UiCardContent>
    </UiCard>
  </div>
</template>