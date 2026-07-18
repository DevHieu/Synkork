<script setup lang="ts">
import { ShieldAlert } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs'

import type {
  ReportReasonRow,
  ReportReasonScope,
  ReportReasonStat,
} from '../../types/report-overview.types'

import { dashboardService } from '../../services/dashboardService'
import {
  REPORT_REASON_COLORS,
  REPORT_REASON_LABELS,
} from '../../types/report-overview.constants'

const reasonScope = ref<ReportReasonScope>('all')
const reasonStats = ref<ReportReasonStat[]>([])
const isLoading = ref(false)

async function fetchReasonStats() {
  isLoading.value = true
  try {
    reasonStats.value = await dashboardService.getReportReasonStats()
  }
  catch (err) {
    console.error('Failed to load report reasons:', err)
    reasonStats.value = []
  }
  finally {
    isLoading.value = false
  }
}

const reasonRows = computed<ReportReasonRow[]>(() => {
  const filtered = reasonScope.value === 'all'
    ? reasonStats.value
    : reasonStats.value.filter(row => row.reportType === reasonScope.value.toUpperCase())

  const totals = new Map<string, number>()
  for (const row of filtered) {
    totals.set(row.reason, (totals.get(row.reason) ?? 0) + row.count)
  }

  return Array.from(totals.entries())
    .map(([reason, count]) => ({ reason, count }))
    .sort((a, b) => b.count - a.count)
})

const maxReasonCount = computed(() =>
  Math.max(1, ...reasonRows.value.map(row => row.count)),
)

onMounted(fetchReasonStats)
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle class="flex items-center gap-2 text-base">
        <ShieldAlert class="h-4 w-4 text-muted-foreground" />
        Top lý do tố cáo
      </CardTitle>
      <CardDescription>
        Các lý do tố cáo phổ biến nhất
      </CardDescription>
    </CardHeader>

    <CardContent>
      <Tabs v-model="reasonScope">
        <TabsList class="mb-4 grid w-full grid-cols-3">
          <TabsTrigger value="all">
            Tất cả
          </TabsTrigger>
          <TabsTrigger value="user">
            Người dùng
          </TabsTrigger>
          <TabsTrigger value="room">
            Phòng
          </TabsTrigger>
        </TabsList>

        <TabsContent :value="reasonScope">
          <div v-if="isLoading" class="py-8 text-center text-sm text-muted-foreground">
            Đang tải...
          </div>
          <div v-else-if="reasonRows.length" class="space-y-3">
            <div v-for="row in reasonRows" :key="row.reason" class="space-y-1">
              <div class="flex items-center justify-between text-sm">
                <span class="text-muted-foreground">{{ REPORT_REASON_LABELS[row.reason] ?? row.reason }}</span>
                <span class="font-medium">{{ row.count.toLocaleString() }}</span>
              </div>
              <div class="h-2 w-full overflow-hidden rounded-full bg-muted">
                <div
                  class="h-full rounded-full transition-all"
                  :style="{
                    width: `${(row.count / maxReasonCount) * 100}%`,
                    backgroundColor: REPORT_REASON_COLORS[row.reason] ?? 'var(--chart-1)',
                  }"
                />
              </div>
            </div>
          </div>
          <p v-else class="py-8 text-center text-sm text-muted-foreground">
            Không có dữ liệu
          </p>
        </TabsContent>
      </Tabs>
    </CardContent>
  </Card>
</template>
