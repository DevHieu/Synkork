<script setup lang="ts">
import { VisArea, VisAxis, VisLine, VisXYContainer } from '@unovis/vue'
import { computed } from 'vue'

import type { ChartConfig } from '@/components/ui/chart'

import {
  Card,
  CardContent,
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

interface OverviewRawPoint {
  date: string
  totalUser: number
  totalRooms: number
  totalSubscriptions: number
}

interface OverviewChartPoint {
  date: number
  users: number
  rooms: number
  subscriptions: number
}

const props = withDefaults(defineProps<{
  chartData: OverviewRawPoint[]
  isLoading?: boolean
}>(), {
  isLoading: false,
})

const chartData = computed<OverviewChartPoint[]>(() =>
  props.chartData.map(item => ({
    date: new Date(item.date).getTime(),
    users: item.totalUser,
    rooms: item.totalRooms,
    subscriptions: item.totalSubscriptions,
  })),
)

const getDate = (row: OverviewChartPoint) => row.date
const getUsers = (row: OverviewChartPoint) => row.users
const getRooms = (row: OverviewChartPoint) => row.rooms
const getSubscriptions = (row: OverviewChartPoint) => row.subscriptions

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

  <linearGradient id="fillSubscriptions" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-subscriptions)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-subscriptions)" stop-opacity="0.1" />
  </linearGradient>
`

const yMax = computed(() => {
  if (!chartData.value.length)
    return 10

  const max = Math.max(...chartData.value.flatMap(row => [row.users, row.rooms, row.subscriptions]))
  return max === 0 ? 10 : Math.ceil(max * 1.3)
})
</script>

<template>
  <Card class="pt-0">
    <CardHeader class="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
      <div class="grid flex-1 gap-1">
        <CardTitle>Tổng quan tăng trưởng</CardTitle>
      </div>
    </CardHeader>
    <CardContent class="px-2 pt-4 pb-4 sm:px-6 sm:pt-6">
      <ChartContainer
        v-if="chartData.length > 0"
        :config="chartConfig"
        class="aspect-auto h-[250px] w-full"
        :cursor="false"
      >
        <VisXYContainer :data="chartData" :svg-defs="svgDefs" :margin="{ left: -40 }" :y-domain="[0, yMax]">
          <VisArea :x="getDate" :y="getUsers" color="url(#fillUsers)" :opacity="0.4" />
          <VisLine :x="getDate" :y="getUsers" :color="chartConfig.users.color" :line-width="2" />

          <VisArea :x="getDate" :y="getRooms" color="url(#fillRooms)" :opacity="0.4" />
          <VisLine :x="getDate" :y="getRooms" :color="chartConfig.rooms.color" :line-width="2" />

          <VisArea :x="getDate" :y="getSubscriptions" color="url(#fillSubscriptions)" :opacity="0.6" />
          <VisLine
            :x="getDate"
            :y="getSubscriptions"
            :color="chartConfig.subscriptions.color"
            :line-width="2"
          />

          <VisAxis
            type="x"
            :x="getDate"
            :tick-line="false"
            :domain-line="false"
            :grid-line="false"
            :num-ticks="6"
            :tick-format="(value: number) => new Date(value).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })"
          />
          <VisAxis
            type="y"
            :num-ticks="3"
            :tick-line="false"
            :domain-line="false"
            :tick-format="(value: number) => Math.round(value).toString()"
          />
          <ChartTooltip />
          <ChartCrosshair
            :template="componentToString(chartConfig, ChartTooltipContent, {
              labelFormatter: (value: number) => new Date(value).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
              }),
            })"
            :color="(_row: OverviewChartPoint, index: number) => [chartConfig.users.color, chartConfig.rooms.color, chartConfig.subscriptions.color][index % 3]"
          />
        </VisXYContainer>

        <ChartLegendContent />
      </ChartContainer>
      <div v-else class="flex h-[250px] items-center justify-center text-sm text-muted-foreground">
        <span v-if="isLoading">Đang tải...</span>
        <span v-else>Không có dữ liệu</span>
      </div>
    </CardContent>
  </Card>
</template>
