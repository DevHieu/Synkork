<script lang="ts" setup>
import OverviewChart from '../components/overview-chart.vue'
import RecentSales from '../components/recent-sales.vue'
import DataCard from '../components/data-card.vue';

import { Activity, MessagesSquare, Server, Users } from '@lucide/vue';
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
      :icon="MessagesSquare"
      :day-growth="stats?.subscriptionDayGrowth"
      :month-growth="stats?.subscriptionMonthGrowth"
    />
  </div>

  <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
    <OverviewChart class="col-span-1 lg:col-span-4" />
    <UiCard class="col-span-1 lg:col-span-3">
      <UiCardHeader>
        <UiCardTitle>Recent Sales</UiCardTitle>
        <UiCardDescription>
          You made 265 sales this month.
        </UiCardDescription>
      </UiCardHeader>
      <UiCardContent>
        <RecentSales />
      </UiCardContent>
    </UiCard>
  </div>
</template>
