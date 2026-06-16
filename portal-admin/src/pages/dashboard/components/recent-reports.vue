<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { User, LayoutGrid, Clock, Eye, CheckCircle2, XCircle, ShieldAlert } from '@lucide/vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'

import type { Report, ReportStatus, ReportFilterParams } from '@/pages/report/types/Reports'
import ReportDetail from '@/pages/report/components/ReportDetail.vue'
import { getReports, updateReportStatus } from '@/pages/report/service/reportService'
import { userService } from '@/pages/users/services/userService'
import { roomService } from '@/pages/rooms/service/roomService'
import { formatTimestamp } from '@/utils/date.utils'

const loading = ref(false)
const reports = ref<Report[]>([])

const selectedReport = ref<Report | null>(null)
const isDetailOpen = ref(false)

const statusVariantMap: Record<string, 'default' | 'secondary' | 'destructive' | 'outline'> = {
  PENDING: 'secondary',
  RESOLVED: 'default',
  DISMISSED: 'destructive',
  REVIEWED: 'outline',
}

const statusIconMap: Record<string, any> = {
  PENDING: Clock,
  REVIEWED: Eye,
  RESOLVED: CheckCircle2,
  DISMISSED: XCircle,
}

const typeIconMap = {
  USER: User,
  ROOM: LayoutGrid,
}

async function fetchRecentReports() {
  loading.value = true
  try {
    const params: ReportFilterParams = {
      page: 0,
      size: 5,
      status: 'PENDING',
    }
    const res = await getReports({ params })
    reports.value = res.data
  } catch (error) {
    console.error('Lỗi tải reports:', error)
  } finally {
    loading.value = false
  }
}

function handleViewDetail(report: Report) {
  selectedReport.value = report
  isDetailOpen.value = true
}

async function handleUpdateReportStatus({ id, status, note }: { id: string; status: ReportStatus; note?: string }) {
  try {
    await updateReportStatus(id, status, note)
    reports.value = reports.value.filter(r => r.id !== id)
    isDetailOpen.value = false
  } catch (error) {
    console.error('Lỗi cập nhật report:', error)
  }
}

async function handleLockTarget({ reportType, targetId }: { reportType: 'USER' | 'ROOM'; targetId: string }) {
  const confirmMsg = reportType === 'USER'
    ? 'Bạn có chắc muốn khoá user này?'
    : 'Bạn có chắc muốn khoá room này?'

  if (!confirm(confirmMsg)) return

  try {
    if (reportType === 'USER') {
      await userService.updateStatus(targetId, 'BANNED')
    } else {
      await roomService.lockRoom(targetId, 'LOCKED')
    }
  } catch (error) {
    console.error('Lỗi khoá đối tượng:', error)
  }
}

onMounted(fetchRecentReports)
</script>

<template>
  <div class="space-y-1">
    <div v-if="loading" class="flex flex-col gap-3 py-2">
      <div v-for="i in 4" :key="i" class="flex items-center gap-3 animate-pulse">
        <div class="h-9 w-9 rounded-full bg-muted shrink-0" />
        <div class="flex-1 space-y-1.5">
          <div class="h-3 w-2/3 rounded bg-muted" />
          <div class="h-2.5 w-1/3 rounded bg-muted" />
        </div>
      </div>
    </div>

    <div
      v-else-if="reports.length === 0"
      class="flex flex-col items-center justify-center py-10 text-muted-foreground gap-2"
    >
      <ShieldAlert class="h-8 w-8 opacity-40" />
      <p class="text-sm">Không có report nào đang chờ xử lý.</p>
    </div>

    <div v-else class="space-y-1">
      <button
        v-for="report in reports"
        :key="report.id"
        class="flex w-full items-center gap-3 rounded-lg px-2 py-2 text-left transition-colors hover:bg-muted/50"
        @click="handleViewDetail(report)"
      >
        <div
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground"
        >
          <component :is="typeIconMap[report.reportType]" class="h-4.5 w-4.5" />
        </div>

        <div class="flex-1 min-w-0 space-y-0.5">
          <p class="text-sm font-medium leading-tight truncate">
            {{ report.reason }}
          </p>
          <p class="text-xs text-muted-foreground leading-tight truncate">
            {{ report.reporterEmail }} · {{ formatTimestamp(report.createdAt) }}
          </p>
        </div>

        <Badge :variant="statusVariantMap[report.status] ?? 'default'" class="gap-1 shrink-0 text-[11px]">
          <component :is="statusIconMap[report.status]" class="h-3 w-3" />
          {{ report.status }}
        </Badge>
      </button>
    </div>

    <div class="pt-2 text-center">
      <UiButton variant="link" size="sm" as-child>
        <RouterLink to="/report">Xem tất cả reports</RouterLink>
      </UiButton>
    </div>
  </div>

  <ReportDetail
    v-if="selectedReport"
    v-model:open="isDetailOpen"
    :report="selectedReport"
    @action="handleUpdateReportStatus"
    @lock-target="handleLockTarget"
  />
</template>