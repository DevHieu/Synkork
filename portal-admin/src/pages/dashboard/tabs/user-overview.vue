<script setup lang="ts">
import {
  CalendarPlus,
  CircleUserRound,
  ShieldCheck,
  TrendingUp,
  Users,
} from '@lucide/vue'
import { VisDonut, VisSingleContainer } from '@unovis/vue'
import dayjs from 'dayjs'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'

import DataCard from '../components/overview/data-card.vue'
import { dashboardService } from '../services/dashboardService'
import { useDashboardFilterStore } from '../stores/dashboard-filter.ts'

interface UserStats {
  totalUsers: number
  newUsersToday: number
  userGrowth: number
}

interface UserStatusCount {
  count: number
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED'
}

interface UserPlanCount {
  count: number
  plan: 'FREE' | 'TEAM' | 'BUSINESS'
}

interface UserChart {
  statusCounts: UserStatusCount[]
  planCounts: UserPlanCount[]
}

interface ChartRow {
  name: string
  value: number
  color: string
}

const dashboardFilterStore = useDashboardFilterStore()
const { dateRange, dateRangeLabel, dateRangeParams } = storeToRefs(dashboardFilterStore)

const stats = ref<UserStats | null>(null)
const chart = ref<UserChart | null>(null)
const isLoadingStats = ref(false)
const isLoadingChart = ref(false)

const statusConfig: Array<{ name: string, status: UserStatusCount['status'], color: string }> = [
  { name: 'Active', status: 'ACTIVE', color: 'var(--chart-1)' },
  { name: 'Inactive', status: 'INACTIVE', color: 'var(--chart-2)' },
  { name: 'Banned', status: 'BANNED', color: 'var(--chart-3)' },
]

const planConfig: Array<{ name: string, plan: UserPlanCount['plan'], color: string }> = [
  { name: 'Free', plan: 'FREE', color: 'var(--chart-1)' },
  { name: 'Team', plan: 'TEAM', color: 'var(--chart-2)' },
  { name: 'Business', plan: 'BUSINESS', color: 'var(--chart-3)' },
]

const statusRows = computed<ChartRow[]>(() =>
  statusConfig.map(item => ({
    name: item.name,
    value: chart.value?.statusCounts.find(row => row.status === item.status)?.count ?? 0,
    color: item.color,
  })),
)

const planRows = computed<ChartRow[]>(() =>
  planConfig.map(item => ({
    name: item.name,
    value: chart.value?.planCounts.find(row => row.plan === item.plan)?.count ?? 0,
    color: item.color,
  })),
)

const totalStatusUsers = computed(() =>
  statusRows.value.reduce((total, row) => total + row.value, 0),
)

const totalPlanUsers = computed(() =>
  planRows.value.reduce((total, row) => total + row.value, 0),
)

const growthDataClass = computed(() => {
  const value = stats.value?.userGrowth ?? 0
  if (value < 0)
    return 'text-red-500'
  if (value > 0)
    return 'text-green-500'
  return ''
})

const comparisonRangeLabel = computed(() => {
  let previousFrom: dayjs.Dayjs
  let previousTo: dayjs.Dayjs

  if (dateRange.value) {
    const currentFrom = dayjs(dateRange.value.from)
    const currentTo = dayjs(dateRange.value.to)
    const periodLength = currentTo.diff(currentFrom)
    previousFrom = currentFrom.subtract(periodLength, 'millisecond')
    previousTo = currentFrom
  }
  else {
    const now = dayjs()
    previousFrom = now.subtract(2, 'month')
    previousTo = now.subtract(1, 'month')
  }

  return `so từ ${previousFrom.format('DD/MM/YYYY')} đến ${previousTo.format('DD/MM/YYYY')}`
})

function formatNumber(value?: number) {
  return value?.toLocaleString() ?? '-'
}

function formatPercent(value?: number) {
  const numericValue = value ?? 0
  const sign = numericValue > 0 ? '+' : ''
  return `${sign}${numericValue.toFixed(1)}%`
}

async function fetchUserStats() {
  isLoadingStats.value = true
  try {
    stats.value = await dashboardService.getUserStatsData(dateRangeParams.value)
  }
  catch (err) {
    console.error('Failed to load user stats:', err)
    stats.value = null
  }
  finally {
    isLoadingStats.value = false
  }
}

async function fetchUserChart() {
  isLoadingChart.value = true
  try {
    chart.value = await dashboardService.getUserChartData(dateRangeParams.value)
  }
  catch (err) {
    console.error('Failed to load user chart:', err)
    chart.value = null
  }
  finally {
    isLoadingChart.value = false
  }
}

function fetchUserDashboard() {
  void Promise.all([
    fetchUserStats(),
    fetchUserChart(),
  ])
}

onMounted(fetchUserDashboard)
watch(dateRange, fetchUserDashboard)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Thống kê người dùng
        </h2>
        <p class="text-sm text-muted-foreground">
          Dữ liệu trong khoảng: {{ dateRangeLabel }}
        </p>
      </div>

      <div class="w-full sm:w-[280px]">
        <DateRangePicker v-model="dateRange" />
      </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <DataCard
        title="Tổng người dùng"
        :data="isLoadingStats ? '-' : formatNumber(stats?.totalUsers)"
        :icon="Users"
      />
      <DataCard
        title="Tăng trưởng"
        :data="isLoadingStats ? '-' : formatPercent(stats?.userGrowth)"
        :icon="TrendingUp"
        :data-class="isLoadingStats ? '' : growthDataClass"
        :description="comparisonRangeLabel"
      />
      <DataCard
        title="Người dùng mới hôm nay"
        :data="isLoadingStats ? '-' : formatNumber(stats?.newUsersToday)"
        :icon="CalendarPlus"
      />
    </div>

    <div class="grid gap-4 lg:grid-cols-2">
      <UiCard>
        <UiCardHeader>
          <UiCardTitle class="flex items-center gap-2 text-base">
            <ShieldCheck class="h-4 w-4 text-muted-foreground" />
            Status chart
          </UiCardTitle>
          <UiCardDescription>
            User count by account status.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-4">
          <div v-if="isLoadingChart" class="flex h-[220px] items-center justify-center text-sm text-muted-foreground">
            Đang tải...
          </div>
          <div v-else class="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
            <div class="h-[220px]">
              <VisSingleContainer :data="statusRows" class="h-full">
                <VisDonut
                  :value="(d: ChartRow) => d.value"
                  :color="(d: ChartRow) => d.color"
                  :arc-width="30"
                  :corner-radius="6"
                  :pad-angle="0.04"
                  central-label="Status"
                  :central-sub-label="totalStatusUsers.toLocaleString()"
                />
              </VisSingleContainer>
            </div>

            <div class="space-y-3">
              <div v-for="row in statusRows" :key="row.name" class="flex min-w-36 items-center justify-between gap-6 text-sm">
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

      <UiCard>
        <UiCardHeader>
          <UiCardTitle class="flex items-center gap-2 text-base">
            <CircleUserRound class="h-4 w-4 text-muted-foreground" />
            Plan chart
          </UiCardTitle>
          <UiCardDescription>
            User count by subscription plan.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-4">
          <div v-if="isLoadingChart" class="flex h-[220px] items-center justify-center text-sm text-muted-foreground">
            Đang tải...
          </div>
          <div v-else class="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
            <div class="h-[220px]">
              <VisSingleContainer :data="planRows" class="h-full">
                <VisDonut
                  :value="(d: ChartRow) => d.value"
                  :color="(d: ChartRow) => d.color"
                  :arc-width="30"
                  :corner-radius="6"
                  :pad-angle="0.04"
                  central-label="Plan"
                  :central-sub-label="totalPlanUsers.toLocaleString()"
                />
              </VisSingleContainer>
            </div>

            <div class="space-y-3">
              <div v-for="row in planRows" :key="row.name" class="flex min-w-36 items-center justify-between gap-6 text-sm">
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
    </div>
  </div>
</template>
