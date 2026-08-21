<script lang="ts" setup>
import { Activity, CreditCard, Flag, Server, Users } from '@lucide/vue'
import { storeToRefs } from 'pinia'
import { onMounted, ref, watch } from 'vue'

import DateRangePicker from '@/components/date-range-picker.vue'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import DataCard from '../components/overview/data-card.vue'
import OverviewChart from '../components/overview/overview-chart.vue'
import RecentReports from '../components/overview/recent-reports.vue'
import { dashboardService } from '../services/dashboardService'
import { useDashboardFilterStore } from '../stores/dashboard-filter'

interface OverviewStatsType {
  totalUsers: number
  userOnlines: number
  totalRooms: number
  totalSubscriptions: number
  userGrowth: number
  roomGrowth: number
  subscriptionGrowth: number
  onlineGrowth: number
}

interface OverviewChartType {
  date: string
  totalUser: number
  totalRooms: number
  totalSubscriptions: number
}

const dashboardFilterStore = useDashboardFilterStore()
const { dateRange, dateRangeLabel, dateRangeParams } = storeToRefs(dashboardFilterStore)

const isLoadingStats = ref(false)
const isLoadingChart = ref(false)
const statsData = ref<OverviewStatsType | null>(null)
const chartData = ref<OverviewChartType[]>([])

async function fetchOverviewStats() {
  isLoadingStats.value = true
  try {
    statsData.value = await dashboardService.getOverviewStatsData(dateRangeParams.value)
  }
  catch (err) {
    console.error('Error fetching overview stats:', err)
    statsData.value = null
  }
  finally {
    isLoadingStats.value = false
  }
}

async function fetchOverviewChart() {
  isLoadingChart.value = true
  try {
    const data = await dashboardService.getOverviewChartData(dateRangeParams.value)
    chartData.value = Array.isArray(data) ? data : []
  }
  catch (err) {
    console.error('Error fetching overview chart:', err)
    chartData.value = []
  }
  finally {
    isLoadingChart.value = false
  }
}

function fetchOverviewData() {
  void Promise.all([
    fetchOverviewStats(),
    fetchOverviewChart(),
  ])
}

onMounted(fetchOverviewData)
watch(dateRange, fetchOverviewData)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Tổng quan dashboard
        </h2>
        <p class="text-sm text-muted-foreground">
          Dữ liệu trong khoảng: {{ dateRangeLabel }}
        </p>
      </div>

      <div class="w-full sm:w-[280px]">
        <DateRangePicker v-model="dateRange" />
      </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <DataCard
        title="Người dùng"
        :data="isLoadingStats ? '-' : statsData?.totalUsers?.toLocaleString() ?? '-'"
        :icon="Users"
        :day-growth="statsData?.userGrowth"
        day-growth-label="so với kỳ trước"
      />
      <DataCard
        title="Đang online"
        :data="isLoadingStats ? '-' : statsData?.userOnlines?.toLocaleString() ?? '-'"
        :icon="Activity"
        :day-growth="statsData?.onlineGrowth"
        day-growth-label="so với kỳ trước"
      />
      <DataCard
        title="Rooms"
        :data="isLoadingStats ? '-' : statsData?.totalRooms?.toLocaleString() ?? '-'"
        :icon="Server"
        :day-growth="statsData?.roomGrowth"
        day-growth-label="so với kỳ trước"
      />
      <DataCard
        title="Subscriptions"
        :data="isLoadingStats ? '-' : statsData?.totalSubscriptions?.toLocaleString() ?? '-'"
        :icon="CreditCard"
        :day-growth="statsData?.subscriptionGrowth"
        day-growth-label="so với kỳ trước"
      />
    </div>

    <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
      <OverviewChart class="col-span-1 lg:col-span-4" :chart-data="chartData" :is-loading="isLoadingChart" />

      <Card class="col-span-1 lg:col-span-3">
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <Flag class="h-4 w-4 text-destructive" />
            Tố cáo gần đây
          </CardTitle>
          <CardDescription>Các report đang chờ xử lý gần đây.</CardDescription>
        </CardHeader>
        <CardContent>
          <RecentReports />
        </CardContent>
      </Card>
    </div>
  </div>
</template>
