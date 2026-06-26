<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { LayoutGrid, LockKeyhole, DoorOpen, MessageSquare } from '@lucide/vue'

import DataCard from '../components/data-card.vue'
import RoomChart from '../tabs/room/room-card.vue'
import TopRooms from '../tabs/room/top-room.vue'
import { dashboardService } from '../services/dashboardService'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

const stats = ref<any>(null)

onMounted(async () => {
  try {
    stats.value = await dashboardService.getRoomStatsData()
  } catch (err) {
    console.error('Failed to load room stats:', err)
  }
})
</script>

<template>
  <div class="space-y-4">
    <!-- Stats cards -->
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <DataCard
        title="Tổng Rooms"
        :data="stats?.totalRooms?.toLocaleString() ?? '—'"
        :icon="LayoutGrid"
        :day-growth="stats?.dayGrowth"
        :month-growth="stats?.monthGrowth"
      />
      <DataCard
        title="Đang mở"
        :data="stats?.openRooms?.toLocaleString() ?? '—'"
        :icon="DoorOpen"
      />
      <DataCard
        title="Đã khoá"
        :data="stats?.lockedRooms?.toLocaleString() ?? '—'"
        :icon="LockKeyhole"
      />
      <DataCard
        title="Group Rooms"
        :data="stats?.groupRooms?.toLocaleString() ?? '—'"
        :icon="MessageSquare"
      />
    </div>

    <!-- Chart + Top Rooms -->
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