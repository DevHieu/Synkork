<script setup lang="ts">
import { ChartPie } from '@lucide/vue'
import { VisDonut, VisSingleContainer } from '@unovis/vue'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

import { dashboardService } from '../../services/dashboardService'
import { useDashboardFilterStore } from '../../stores/dashboard-filter'

interface RoomStatusCount {
  count: number
  status: 'OPEN' | 'LOCKED' | 'PENDING_REMOVAL'
}

interface StatusRow {
  name: string
  status: RoomStatusCount['status']
  value: number
  color: string
}

const dashboardFilterStore = useDashboardFilterStore()
const { dateRangeParams } = storeToRefs(dashboardFilterStore)

const rawData = ref<RoomStatusCount[]>([])
const isLoading = ref(false)

const ROOM_STATUS_COLORS = {
  OPEN: '#14b8a6',
  LOCKED: '#f97316',
  PENDING_REMOVAL: '#eab308',
} as const

const statusConfig: Array<Omit<StatusRow, 'value'>> = [
  { name: 'Đang mở', status: 'OPEN', color: ROOM_STATUS_COLORS.OPEN },
  { name: 'Đã khóa', status: 'LOCKED', color: ROOM_STATUS_COLORS.LOCKED },
  { name: 'Chờ xóa', status: 'PENDING_REMOVAL', color: ROOM_STATUS_COLORS.PENDING_REMOVAL },
]

const statusRows = computed<StatusRow[]>(() =>
  statusConfig.map(item => ({
    ...item,
    value: rawData.value.find(row => row.status === item.status)?.count ?? 0,
  })),
)

const totalRooms = computed(() =>
  statusRows.value.reduce((total, row) => total + row.value, 0),
)

async function fetchData() {
  isLoading.value = true
  try {
    const data = await dashboardService.getRoomChartData(dateRangeParams.value)
    rawData.value = Array.isArray(data) ? data : []
  }
  catch (err) {
    console.error('Failed to fetch room status chart:', err)
    rawData.value = []
  }
  finally {
    isLoading.value = false
  }
}

function getStatusValue(row: StatusRow) {
  return row.value
}

function getStatusColor(row: StatusRow) {
  return row.color
}

function getStatusPercent(value: number) {
  if (totalRooms.value === 0)
    return '0%'

  return `${Math.round((value / totalRooms.value) * 100)}%`
}

onMounted(fetchData)
watch(dateRangeParams, fetchData)
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle class="flex items-center gap-2 text-base">
        <ChartPie class="h-4 w-4 text-muted-foreground" />
        Trạng thái rooms
      </CardTitle>
      <CardDescription>
        Phân bổ room theo trạng thái trong khoảng đã chọn.
      </CardDescription>
    </CardHeader>

    <CardContent class="space-y-4">
      <div v-if="isLoading" class="flex h-[260px] items-center justify-center text-sm text-muted-foreground">
        Đang tải...
      </div>
      <div v-else-if="totalRooms === 0" class="flex h-[260px] items-center justify-center rounded-lg border border-dashed text-sm text-muted-foreground">
        Không có dữ liệu
      </div>
      <div v-else class="grid gap-6 sm:grid-cols-[1fr_auto] sm:items-center">
        <div class="h-[260px]">
          <VisSingleContainer :data="statusRows" class="h-full">
            <VisDonut
              :value="getStatusValue"
              :color="getStatusColor"
              :arc-width="34"
              :corner-radius="6"
              :pad-angle="0.04"
              central-label="Rooms"
              :central-sub-label="totalRooms.toLocaleString()"
            />
          </VisSingleContainer>
        </div>

        <div class="space-y-3">
          <div
            v-for="row in statusRows"
            :key="row.status"
            class="flex min-w-44 items-center justify-between gap-8 rounded-lg border border-border/60 p-3"
          >
            <div class="flex items-center gap-2">
              <span class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: row.color }" />
              <span class="text-sm font-medium">{{ row.name }}</span>
            </div>
            <div class="text-right">
              <div class="text-sm font-semibold">
                {{ row.value.toLocaleString() }}
              </div>
              <div class="text-xs text-muted-foreground">
                {{ getStatusPercent(row.value) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
