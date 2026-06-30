<script lang="ts" setup>
import OverviewChart from '../components/overview-chart.vue'
import RecentReports from '../components/recent-reports.vue'
import DataCard from '../components/data-card.vue';

import { Activity, CreditCard, Server, Users } from '@lucide/vue';
import { dashboardService } from '../services/dashboardService';

const stats = ref<any>(null)

onMounted(async () => {
  try {
    stats.value = await dashboardService.getOverviewStatsData()
  } catch (err) {
    console.error(err)
  }
})
</script>

<template>
  <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <DataCard
      title="Tổng người dùng"
      :data="stats?.totalUsers?.toLocaleString() ?? '—'"
      :icon="Users"
      :day-growth="stats?.userDayGrowth"
      :month-growth="stats?.userMonthGrowth"
    />
    <DataCard
      title="Đang online"
      :data="stats?.userOnlines?.toLocaleString() ?? '—'"
      :icon="Activity"
      :day-growth="stats?.onlineDayGrowth"
    />
    <DataCard
      title="Tổng rooms"
      :data="stats?.totalRooms?.toLocaleString() ?? '—'"
      :icon="Server"
      :day-growth="stats?.roomDayGrowth"
      :month-growth="stats?.roomMonthGrowth"
    />
    <DataCard
      title="Tổng subscriptions"
      :data="stats?.totalSubscriptions?.toLocaleString() ?? '—'"
      :icon="CreditCard"
      :day-growth="stats?.subscriptionDayGrowth"
      :month-growth="stats?.subscriptionMonthGrowth"
    />
  </div>

  <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
    <OverviewChart class="col-span-1 lg:col-span-4" />
    <UiCard class="col-span-1 lg:col-span-3">
      <UiCardHeader>
        <UiCardTitle class="flex items-center gap-2">
          <Flag class="h-4 w-4 text-destructive" />
          Tố cáo gần đây
        </UiCardTitle>
        <UiCardDescription>Các report đang chờ xử lý gần đây.</UiCardDescription>
      </UiCardHeader>
      <UiCardContent>
        <RecentReports />
      </UiCardContent>
    </UiCard>
  </div>
</template>
