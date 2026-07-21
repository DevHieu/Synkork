<script setup lang="ts">
import { AlertTriangle, LayoutGrid, TrendingUp, UserRoundPlus } from '@lucide/vue'
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
import RoomChart from '../components/room/room-card.vue'
import TopRooms from '../components/room/top-room.vue'
import { dashboardService } from '../services/dashboardService'
import { useDashboardFilterStore } from '../stores/dashboard-filter'

interface RoomStats {
  totalRooms: number
  newRooms: number
  roomGrowth: number
  averageMembersPerRoom: number
  warnedRooms: number
}

const dashboardFilterStore = useDashboardFilterStore()
const { dateRange, dateRangeLabel, dateRangeParams } = storeToRefs(dashboardFilterStore)

const stats = ref<RoomStats | null>(null)
const isLoadingStats = ref(false)

async function fetchRoomStats() {
  isLoadingStats.value = true
  try {
    stats.value = await dashboardService.getRoomStatsData(dateRangeParams.value)
  }
  catch (err) {
    console.error('Failed to load room stats:', err)
    stats.value = null
  }
  finally {
    isLoadingStats.value = false
  }
}

function formatNumber(value?: number) {
  return value?.toLocaleString() ?? '-'
}

function formatAverage(value?: number) {
  return value !== undefined ? value.toFixed(1) : '-'
}

onMounted(fetchRoomStats)
watch(dateRange, fetchRoomStats)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Thống kê rooms
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
        title="Tổng rooms"
        :data="isLoadingStats ? '-' : formatNumber(stats?.totalRooms)"
        :icon="LayoutGrid"
        :day-growth="stats?.roomGrowth"
        day-growth-label="so với kỳ trước"
      />
      <DataCard
        title="Room mới"
        :data="isLoadingStats ? '-' : formatNumber(stats?.newRooms)"
        :icon="TrendingUp"
      />
      <DataCard
        title="TB thành viên/room"
        :data="isLoadingStats ? '-' : `${formatAverage(stats?.averageMembersPerRoom)}%`"
        :icon="UserRoundPlus"
      />
      <DataCard
        title="Room bị cảnh báo"
        :data="isLoadingStats ? '-' : formatNumber(stats?.warnedRooms)"
        :icon="AlertTriangle"
      />
    </div>

    <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
      <RoomChart class="col-span-1 lg:col-span-4" />

      <Card class="col-span-1 lg:col-span-3">
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <LayoutGrid class="h-4 w-4 text-primary" />
            Top Rooms
          </CardTitle>
          <CardDescription>5 rooms có nhiều thành viên nhất.</CardDescription>
        </CardHeader>
        <CardContent>
          <TopRooms />
        </CardContent>
      </Card>
    </div>
  </div>
</template>
