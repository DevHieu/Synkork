<script setup lang="ts">
import { VisArea, VisAxis, VisLine, VisXYContainer } from '@unovis/vue'
import { storeToRefs } from 'pinia'
import { onMounted, ref } from 'vue'

import type { ChartConfig } from '@/components/ui/chart'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {

  ChartContainer,
  ChartCrosshair,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
  componentToString,
} from '@/components/ui/chart'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useTimeRangeStore } from '@/stores/time-range'

import { dashboardService } from '../../services/dashboardService'

const timeRangeStore = useTimeRangeStore()
const { timeRange } = storeToRefs(timeRangeStore)
const rawData = ref<any[]>([])

async function fetchData() {
  try {
    const data = await dashboardService.getOverviewChartData(timeRange.value)
    rawData.value = Array.isArray(data) ? data : []
  }
  catch (err) {
    console.error('Failed to fetch overview:', err)
    rawData.value = []
  }
}

onMounted(() => fetchData())

// Watch khi đổi timeRange thì fetch lại
watch(timeRange, () => fetchData())

const chartData = computed(() => {
  if (!Array.isArray(rawData.value))
    return []
  return rawData.value.map(item => ({
    date: new Date(item.date).getTime(),
    users: item.totalUser,
    rooms: item.totalRooms,
    subscriptions: item.totalSubscriptions,
  }))
})

const chartConfig = {
  users: {
    label: 'Users',
    color: 'var(--chart-1)',
  },
  rooms: {
    label: 'Rooms',
    color: 'var(--chart-2)',
  },
  subscriptions: {
    label: 'Subscriptions',
    color: 'var(--chart-3)',
  },
} satisfies ChartConfig

const svgDefs = `
  <linearGradient id="fillUsers" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-users)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-users)" stop-opacity="0.1" />
  </linearGradient>

  <linearGradient id="fillRooms" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-rooms)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-rooms)" stop-opacity="0.1" />
  </linearGradient>

  <linearGradient id="fillMessages" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-messages)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-messages)" stop-opacity="0.1" />
  </linearGradient>
`

const yMax = computed(() => {
  if (!chartData.value.length)
    return 10
  const max = Math.max(...chartData.value.flatMap(d => [d.users, d.rooms, d.subscriptions]))
  return max === 0 ? 10 : Math.ceil(max * 1.3) // thêm 30% padding trên
})
</script>

<template>
  <Card class="pt-0">
    <CardHeader class="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
      <div class="grid flex-1 gap-1">
        <CardTitle>Tổng quan tăng trưởng</CardTitle>
        <CardDescription>
          Users, Rooms và Subscriptions theo
          <b>
            {{ timeRange === 'WEEKLY' ? 'tuần' : timeRange === 'MONTHLY' ? 'tháng'
              : timeRange === 'QUARTERLY' ? 'quý' : 'năm' }}
          </b>
        </CardDescription>
      </div>
      <Select v-model="timeRange">
        <SelectTrigger class="hidden w-[160px] rounded-lg sm:ml-auto sm:flex" aria-label="Select a value">
          <SelectValue placeholder="Monthly" />
        </SelectTrigger>
        <SelectContent class="rounded-xl">
          <SelectItem value="WEEKLY" class="rounded-lg">
            Tuần
          </SelectItem>
          <SelectItem value="MONTHLY" class="rounded-lg">
            Tháng
          </SelectItem>
          <SelectItem value="QUARTERLY" class="rounded-lg">
            Quý
          </SelectItem>
          <SelectItem value="YEARLY" class="rounded-lg">
            Năm
          </SelectItem>
        </SelectContent>
      </Select>
    </CardHeader>
    <CardContent class="px-2 pt-4 sm:px-6 sm:pt-6 pb-4">
      <ChartContainer
        v-if="chartData.length > 0" :config="chartConfig" class="aspect-auto h-[250px] w-full"
        :cursor="false"
      >
        <VisXYContainer :data="chartData" :svg-defs="svgDefs" :margin="{ left: -40 }" :y-domain="[0, yMax]">
          <VisArea :x="(d) => d.date" :y="(d) => d.users" color="url(#fillUsers)" :opacity="0.4" />
          <VisLine :x="(d) => d.date" :y="(d) => d.users" :color="chartConfig.users.color" :line-width="2" />

          <!-- Rooms -->
          <VisArea :x="(d) => d.date" :y="(d) => d.rooms" color="url(#fillRooms)" :opacity="0.4" />
          <VisLine :x="(d) => d.date" :y="(d) => d.rooms" :color="chartConfig.rooms.color" :line-width="2" />

          <!-- Subscriptions -->
          <VisArea :x="(d) => d.date" :y="(d) => d.subscriptions" color="url(#fillMessages)" :opacity="0.6" />
          <VisLine
            :x="(d) => d.date" :y="(d) => d.subscriptions" :color="chartConfig.subscriptions.color"
            :line-width="2"
          />

          <VisAxis
            type="x" :x="(d) => d.date" :tick-line="false" :domain-line="false" :grid-line="false" :num-ticks="6"
            :tick-format="(d: number, _index: number) => {
              const date = new Date(d)
              return date.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
              })
            }"
          />
          <VisAxis
            type="y" :num-ticks="3" :tick-line="false" :domain-line="false"
            :tick-format="(d: number) => Math.round(d).toString()"
          />
          <ChartTooltip />
          <ChartCrosshair
            :template="componentToString(chartConfig, ChartTooltipContent, {
              labelFormatter: (d) => {
                return new Date(d).toLocaleDateString('en-US', {
                  month: 'short',
                  day: 'numeric',
                })
              },
            })"
            :color="(_d, i) => [chartConfig.rooms.color, chartConfig.users.color, chartConfig.subscriptions.color][i % 3]"
          />
        </VisXYContainer>

        <ChartLegendContent />
      </ChartContainer>
    </CardContent>
  </Card>
</template>
