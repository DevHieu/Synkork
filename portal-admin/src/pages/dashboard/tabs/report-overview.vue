<script setup lang="ts">
import { VisAxis, VisGroupedBar, VisXYContainer } from '@unovis/vue'
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

interface ReasonStatRow {
  reason: string
  reportType: 'USER' | 'ROOM'
  count: number
}

interface ReasonRow {
  reason: string
  count: number
}

type ReasonScope = 'all' | 'user' | 'room'

const REASON_LABELS: Record<string, string> = {
  SPAM: 'Spam',
  INAPPROPRIATE: 'Nội dung không phù hợp',
  HARASSMENT: 'Quấy rối',
  HATE_SPEECH: 'Ngôn từ thù ghét',
  OTHER: 'Khác',
}

const REASON_COLORS: Record<string, string> = {
  SPAM: 'var(--chart-1)',
  INAPPROPRIATE: 'var(--chart-2)',
  HARASSMENT: 'var(--chart-3)',
  HATE_SPEECH: 'var(--chart-4)',
  OTHER: 'var(--chart-5)',
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
  user: 'var(--chart-3)',
  room: 'var(--chart-4)',
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

const reasonScope = ref<ReasonScope>('all')
const reasonStats = ref<ReasonStatRow[]>([])
const isLoadingReasons = ref(false)

async function fetchReasonStats() {
  isLoadingReasons.value = true
  try {
    const data = await dashboardService.getReportReasonStats()
    reasonStats.value = data
  } catch (err) {
    console.error(err)
    errorMessage.value = 'Failed to load report reasons.'
  } finally {
    isLoadingReasons.value = false
  }
}

const reasonRows = computed<ReasonRow[]>(() => {
  const filtered = reasonScope.value === 'all'
    ? reasonStats.value
    : reasonStats.value.filter(row => row.reportType === reasonScope.value.toUpperCase())

  const totals = new Map<string, number>()
  for (const row of filtered) {
    totals.set(row.reason, (totals.get(row.reason) ?? 0) + row.count)
  }

  return Array.from(totals.entries())
    .map(([reason, count]) => ({ reason, count }))
    .sort((a, b) => b.count - a.count)
})

const maxReasonCount = computed(() =>
  Math.max(1, ...reasonRows.value.map(r => r.count)),
)

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
  fetchReasonStats()
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
    <!-- Top Reason -->
    <UiCard>
      <UiCardHeader>
        <UiCardTitle class="flex items-center gap-2 text-base">
          <ShieldAlert class="h-4 w-4 text-muted-foreground" />
          Top lý do tố cáo
        </UiCardTitle>
        <UiCardDescription>
          Các lý do tố cáo phổ biến nhất
        </UiCardDescription>
      </UiCardHeader>

      <UiCardContent>
        <UiTabs v-model="reasonScope">
          <UiTabsList class="mb-4 grid w-full grid-cols-3">
            <UiTabsTrigger value="all">
              Tất cả
            </UiTabsTrigger>
            <UiTabsTrigger value="user">
              Người dùng
            </UiTabsTrigger>
            <UiTabsTrigger value="room">
              Phòng
            </UiTabsTrigger>
          </UiTabsList>

          <UiTabsContent :value="reasonScope">
            <div v-if="isLoadingReasons" class="py-8 text-center text-sm text-muted-foreground">
              Đang tải...
            </div>
            <div v-else-if="reasonRows.length" class="space-y-3">
              <div v-for="row in reasonRows" :key="row.reason" class="space-y-1">
                <div class="flex items-center justify-between text-sm">
                  <span class="text-muted-foreground">{{ REASON_LABELS[row.reason] ?? row.reason }}</span>
                  <span class="font-medium">{{ row.count.toLocaleString() }}</span>
                </div>
                <div class="h-2 w-full overflow-hidden rounded-full bg-muted">
                  <div
                    class="h-full rounded-full transition-all"
                    :style="{
                      width: `${(row.count / maxReasonCount) * 100}%`,
                      backgroundColor: REASON_COLORS[row.reason] ?? 'var(--chart-1)',
                    }"
                  />
                </div>
              </div>
            </div>
            <p v-else class="py-8 text-center text-sm text-muted-foreground">
              Không có dữ liệu
            </p>
          </UiTabsContent>
        </UiTabs>
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
          <VisXYContainer :data="trendData" :svg-defs="svgDefs" :x-domain="[-0.5, trendData.length - 0.5]">
            <VisGroupedBar
              :x="(_: TrendPoint, i: number) => i" :y="[(d: TrendPoint) => d.user, (d: TrendPoint) => d.room]"
              :color="(_: TrendPoint, i: number) => ['url(#fillUser)', 'url(#fillRoom)'][i]"
              :rounded-corners="6" :group-padding="0.25" :bar-padding="0.15"
            />

            <VisAxis
              type="x" :x="(_: TrendPoint, i: number) => i" :num-ticks="6" :tick-line="false" :domain-line="false"
              :grid-line="false"
              :tick-format="(i: number) => trendData[i] ? new Date(trendData[i].date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''"
            />

            <VisAxis type="y" :tick-line="false" :domain-line="false" :grid-line="true" />

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
                Tố cáo {{ row.name }} <strong>({{ row.value.toLocaleString() }})</strong>
              </p>
            </div>
          </div>
        </div>
      </UiCardContent>
    </UiCard>
  </div>
</template>