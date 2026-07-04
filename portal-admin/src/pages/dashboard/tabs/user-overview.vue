<script setup lang="ts">
import { VisDonut, VisSingleContainer } from '@unovis/vue'
import {
  Activity,
  CalendarPlus,
  CircleUserRound,
  ShieldCheck,
  UserPlus,
  Users,
} from '@lucide/vue'

import DataCard from '../components/data-card.vue'
import { dashboardService } from '../services/dashboardService'

interface UserStats {
  totalUsers: number
  newUsersToday: number
  newUsersThisMonth: number
  activeUsers: number
  inactiveUsers: number
  bannedUsers: number
  freeUsers: number
  teamUsers: number
  businessUsers: number
}

interface ChartRow {
  name: string
  value: number
  color: string
}

const stats = ref<UserStats | null>(null)

const statusRows = computed<ChartRow[]>(() => [
  { name: 'Active', value: stats.value?.activeUsers ?? 0, color: 'var(--chart-1)' },
  { name: 'Inactive', value: stats.value?.inactiveUsers ?? 0, color: 'var(--chart-2)' },
  { name: 'Banned', value: stats.value?.bannedUsers ?? 0, color: 'var(--chart-3)' },
])

const planRows = computed<ChartRow[]>(() => [
  { name: 'Free', value: stats.value?.freeUsers ?? 0, color: 'var(--chart-1)' },
  { name: 'Team', value: stats.value?.teamUsers ?? 0, color: 'var(--chart-2)' },
  { name: 'Business', value: stats.value?.businessUsers ?? 0, color: 'var(--chart-3)' },
])

onMounted(async () => {
  try {
    stats.value = await dashboardService.getUserStatsData()
  }
  catch (err) {
    console.error(err)
  }
})
</script>

<template>
  <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
    <DataCard title="Total users" :data="stats?.totalUsers?.toLocaleString() ?? '-'" :icon="Users" />
    <DataCard title="New today" :data="stats?.newUsersToday?.toLocaleString() ?? '-'" :icon="UserPlus" />
    <DataCard title="New this month" :data="stats?.newUsersThisMonth?.toLocaleString() ?? '-'" :icon="CalendarPlus" />
    <DataCard title="Active users" :data="stats?.activeUsers?.toLocaleString() ?? '-'" :icon="Activity" />
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
        <div class="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
          <div class="h-[220px]">
            <VisSingleContainer :data="statusRows" class="h-full">
              <VisDonut
                :value="d => d.value"
                :color="d => d.color"
                :arc-width="30"
                :corner-radius="6"
                :pad-angle="0.04"
                central-label="Status"
                :central-sub-label="stats?.totalUsers?.toLocaleString() ?? '0'"
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
        <div class="grid gap-4 sm:grid-cols-[1fr_auto] sm:items-center">
          <div class="h-[220px]">
            <VisSingleContainer :data="planRows" class="h-full">
              <VisDonut
                :value="d => d.value"
                :color="d => d.color"
                :arc-width="30"
                :corner-radius="6"
                :pad-angle="0.04"
                central-label="Plan"
                :central-sub-label="stats?.totalUsers?.toLocaleString() ?? '0'"
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
</template>
