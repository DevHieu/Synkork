<script setup lang="ts">
import { VisArea, VisAxis, VisDonut, VisLine, VisSingleContainer, VisXYContainer } from '@unovis/vue'
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
import type { ChartConfig } from '@/components/ui/chart'
import { ChartContainer, ChartCrosshair, ChartTooltip, ChartTooltipContent, componentToString } from '@/components/ui/chart'

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

interface UserTrendPoint {
  date: number
  newUsers: number
}

type Period = 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY'

const stats = ref<UserStats | null>(null)
const period = ref<Period>('WEEKLY')
const trendData = ref<UserTrendPoint[]>([])
const isLoadingTrend = ref(false)

const trendConfig = {
  newUsers: { label: 'Người dùng mới', color: 'var(--chart-1)' },
} satisfies ChartConfig

const trendSvgDefs = `
  <linearGradient id="fillNewUsers" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--chart-1)" stop-opacity="0.7" />
    <stop offset="95%" stop-color="var(--chart-1)" stop-opacity="0.05" />
  </linearGradient>
`

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

async function fetchTrend() {
  isLoadingTrend.value = true
  try {
    const data = await dashboardService.getUserChartData(period.value)
    trendData.value = data.map((row: { date: string; newUsers: number }) => ({
      date: new Date(row.date).getTime(),
      newUsers: row.newUsers,
    }))
  }
  catch (err) {
    console.error(err)
    trendData.value = []
  }
  finally {
    isLoadingTrend.value = false
  }
}

watch(period, fetchTrend)

onMounted(async () => {
  try {
    stats.value = await dashboardService.getUserStatsData()
  }
  catch (err) {
    console.error(err)
  }
  fetchTrend()
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

  <UiCard>
    <UiCardHeader class="flex items-center gap-3 space-y-0 sm:flex-row">
      <div class="grid flex-1 gap-1">
        <UiCardTitle class="text-base">
          Xu hướng người dùng mới
        </UiCardTitle>
        <UiCardDescription>Số tài khoản user được tạo theo khoảng thời gian.</UiCardDescription>
      </div>
      <UiSelect v-model="period">
        <UiSelectTrigger class="w-[140px]">
          <UiSelectValue />
        </UiSelectTrigger>
        <UiSelectContent>
          <UiSelectItem value="WEEKLY">Tuần</UiSelectItem>
          <UiSelectItem value="MONTHLY">Tháng</UiSelectItem>
          <UiSelectItem value="QUARTERLY">Quý</UiSelectItem>
          <UiSelectItem value="YEARLY">Năm</UiSelectItem>
        </UiSelectContent>
      </UiSelect>
    </UiCardHeader>
    <UiCardContent>
      <div v-if="isLoadingTrend" class="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
        Đang tải...
      </div>
      <ChartContainer v-else :config="trendConfig" class="h-[260px] w-full" :cursor="false">
        <VisXYContainer :data="trendData" :svg-defs="trendSvgDefs" :margin="{ left: -30 }">
          <VisArea :x="d => d.date" :y="d => d.newUsers" color="url(#fillNewUsers)" />
          <VisLine :x="d => d.date" :y="d => d.newUsers" :color="trendConfig.newUsers.color" :line-width="2" />
          <VisAxis
            type="x"
            :x="d => d.date"
            :tick-line="false"
            :domain-line="false"
            :grid-line="false"
            :num-ticks="6"
            :tick-format="(value: number) => new Date(value).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })"
          />
          <VisAxis type="y" :tick-line="false" :domain-line="false" :num-ticks="4" />
          <ChartTooltip />
          <ChartCrosshair
            :template="componentToString(trendConfig, ChartTooltipContent, {
              labelFormatter: value => new Date(value).toLocaleDateString('vi-VN'),
            })"
            :color="trendConfig.newUsers.color"
          />
        </VisXYContainer>
      </ChartContainer>
    </UiCardContent>
  </UiCard>
</template>
