<script setup lang="ts">
import { VisArea, VisAxis, VisLine, VisXYContainer } from '@unovis/vue'
import { computed, onMounted, ref, watch } from 'vue'

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
import { dashboardService } from '../../services/dashboardService'

interface ChartPoint {
  date: number
  total: number
  open: number
  locked: number
}

const rawData = ref<any[]>([])
const timeRange = ref<'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY'>('WEEKLY')

async function fetchData() {
  try {
    const data = await dashboardService.getRoomChartData(timeRange.value)
    rawData.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to fetch room chart:', err)
    rawData.value = []
  }
}

onMounted(fetchData)
watch(timeRange, fetchData)

const chartData = computed<ChartPoint[]>(() => {
  return rawData.value.map(item => ({
    date: new Date(item.date).getTime(),
    total: item.totalRooms,
    open: item.openRooms,
    locked: item.lockedRooms,
  }))
})

const getDate = (d: ChartPoint) => d.date
const getTotal = (d: ChartPoint) => d.total
const getOpen = (d: ChartPoint) => d.open
const getLocked = (d: ChartPoint) => d.locked

const chartConfig = {
  total: {
    label: 'Total Rooms',
    color: 'var(--chart-1)',
  },
  open: {
    label: 'Open',
    color: 'var(--chart-2)',
  },
  locked: {
    label: 'Locked',
    color: 'var(--chart-3)',
  },
} satisfies ChartConfig

const svgDefs = `
  <linearGradient id="fillTotal" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-total)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-total)" stop-opacity="0.1" />
  </linearGradient>

  <linearGradient id="fillOpen" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-open)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-open)" stop-opacity="0.1" />
  </linearGradient>

  <linearGradient id="fillLocked" x1="0" y1="0" x2="0" y2="1">
    <stop offset="5%" stop-color="var(--color-locked)" stop-opacity="0.8" />
    <stop offset="95%" stop-color="var(--color-locked)" stop-opacity="0.1" />
  </linearGradient>
`

const yMax = computed(() => {
  if (!chartData.value.length) return 10

  const max = Math.max(
    ...chartData.value.flatMap(d => [
      d.total,
      d.open,
      d.locked,
    ]),
  )

  return max === 0 ? 10 : Math.ceil(max * 1.3)
})

const periodLabel = computed(() => {
  const map = {
    WEEKLY: 'tuần',
    MONTHLY: 'tháng',
    QUARTERLY: 'quý',
    YEARLY: 'năm',
  }

  return map[timeRange.value]
})
</script>

<template>
  <Card class="pt-0">
    <CardHeader
      class="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row"
    >
      <div class="grid flex-1 gap-1">
        <CardTitle>Tăng trưởng Rooms</CardTitle>

        <CardDescription>
          Tổng, Open và Locked theo
          <b>{{ periodLabel }}</b>
        </CardDescription>
      </div>

      <Select v-model="timeRange">
        <SelectTrigger
          class="hidden w-[160px] rounded-lg sm:ml-auto sm:flex"
        >
          <SelectValue placeholder="Weekly" />
        </SelectTrigger>

        <SelectContent class="rounded-xl">
          <SelectItem value="WEEKLY" class="rounded-lg">
            Weekly
          </SelectItem>

          <SelectItem value="MONTHLY" class="rounded-lg">
            Monthly
          </SelectItem>

          <SelectItem value="QUARTERLY" class="rounded-lg">
            Quarterly
          </SelectItem>

          <SelectItem value="YEARLY" class="rounded-lg">
            Yearly
          </SelectItem>
        </SelectContent>
      </Select>
    </CardHeader>

    <CardContent class="px-2 pt-4 pb-4 sm:px-6 sm:pt-6">
      <ChartContainer
        v-if="chartData.length > 0"
        :config="chartConfig"
        class="aspect-auto h-[250px] w-full"
        :cursor="false"
      >
        <VisXYContainer
          :data="chartData"
          :svg-defs="svgDefs"
          :margin="{ left: -40 }"
          :y-domain="[0, yMax]"
        >
          <!-- Total -->
          <VisArea
            :x="getDate"
            :y="getTotal"
            color="url(#fillTotal)"
            :opacity="0.4"
          />
          <VisLine
            :x="getDate"
            :y="getTotal"
            :color="chartConfig.total.color"
            :line-width="2"
          />

          <!-- Open -->
          <VisArea
            :x="getDate"
            :y="getOpen"
            color="url(#fillOpen)"
            :opacity="0.4"
          />
          <VisLine
            :x="getDate"
            :y="getOpen"
            :color="chartConfig.open.color"
            :line-width="2"
          />

          <!-- Locked -->
          <VisArea
            :x="getDate"
            :y="getLocked"
            color="url(#fillLocked)"
            :opacity="0.4"
          />
          <VisLine
            :x="getDate"
            :y="getLocked"
            :color="chartConfig.locked.color"
            :line-width="2"
          />

          <VisAxis
            type="x"
            :x="getDate"
            :tick-line="false"
            :domain-line="false"
            :grid-line="false"
            :num-ticks="6"
            :tick-format="
              (d: number) =>
                new Date(d).toLocaleDateString('en-US', {
                  month: 'short',
                  day: 'numeric',
                })
            "
          />

          <VisAxis
            type="y"
            :num-ticks="3"
            :tick-line="false"
            :domain-line="false"
            :tick-format="(d: number) => Math.round(d).toString()"
          />

          <ChartTooltip />

          <ChartCrosshair
            :template="
              componentToString(
                chartConfig,
                ChartTooltipContent,
                {
                  labelFormatter: (d) =>
                    new Date(d).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                    }),
                }
              )
            "
            :color="
              (_d: unknown, i: number) => [
                chartConfig.total.color,
                chartConfig.open.color,
                chartConfig.locked.color,
              ][i % 3]
            "
          />
        </VisXYContainer>

        <ChartLegendContent />
      </ChartContainer>

      <div
        v-else
        class="flex h-[250px] items-center justify-center text-sm text-muted-foreground"
      >
        Không có dữ liệu
      </div>
    </CardContent>
  </Card>
</template>