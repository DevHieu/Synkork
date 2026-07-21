<script setup lang="ts">
import { CheckCircle2, Clock, Eye, LayoutGrid, ShieldAlert, User, XCircle } from '@lucide/vue'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref, watch } from 'vue'

import type { Report, ReportFilterParams, ReportStatus } from '@/pages/report/types/Reports'

import ConfirmDialog from '@/components/confirm-dialog.vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import ReportDetail from '@/pages/report/components/ReportDetail.vue'
import { getReports, updateReportStatus } from '@/pages/report/service/reportService'
import { roomService } from '@/pages/rooms/service/roomService'
import { userService } from '@/pages/users/services/userService'
import { formatTimestamp } from '@/utils/date.utils'

import { useDashboardFilterStore } from '../../stores/dashboard-filter'

const loading = ref(false)
const reports = ref<Report[]>([])

const dashboardFilterStore = useDashboardFilterStore()
const { dateRangeParams } = storeToRefs(dashboardFilterStore)

const reportDateParams = computed(() => {
  if (!dateRangeParams.value)
    return {}

  return {
    fromDate: dateRangeParams.value.dateFrom,
    toDate: dateRangeParams.value.dateTo,
  }
})

const selectedReport = ref<Report | null>(null)
const isDetailOpen = ref(false)
const isLockConfirmOpen = ref(false)
const isLockingTarget = ref(false)
const pendingLockTarget = ref<{ reportType: 'USER' | 'ROOM', targetId: string } | null>(null)

const lockTargetLabel = computed(() =>
  pendingLockTarget.value?.reportType === 'USER' ? 'user' : 'room',
)

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
      ...reportDateParams.value,
    }
    const res = await getReports({ params })
    reports.value = res.data
  }
  catch (error) {
    console.error('Lỗi tải reports:', error)
  }
  finally {
    loading.value = false
  }
}

function handleViewDetail(report: Report) {
  selectedReport.value = report
  isDetailOpen.value = true
}

async function handleUpdateReportStatus({ id, status, note }: { id: string, status: ReportStatus, note?: string }) {
  try {
    await updateReportStatus(id, status, note)
    reports.value = reports.value.filter(r => r.id !== id)
    isDetailOpen.value = false
  }
  catch (error) {
    console.error('Lỗi cập nhật report:', error)
  }
}

function handleLockTarget(payload: { reportType: 'USER' | 'ROOM', targetId: string }) {
  pendingLockTarget.value = payload
  isLockConfirmOpen.value = true
}

async function confirmLockTarget() {
  if (!pendingLockTarget.value)
    return

  try {
    isLockingTarget.value = true

    if (pendingLockTarget.value.reportType === 'USER') {
      await userService.updateStatus(pendingLockTarget.value.targetId, 'BANNED')
    }
    else {
      await roomService.changeRoomStatus(pendingLockTarget.value.targetId, 'LOCKED')
    }

    isLockConfirmOpen.value = false
    pendingLockTarget.value = null
  }
  catch (error) {
    console.error('Lỗi khóa đối tượng:', error)
  }
  finally {
    isLockingTarget.value = false
  }
}

onMounted(fetchRecentReports)
watch(reportDateParams, fetchRecentReports)
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
      <p class="text-sm">
        Không có report nào đang chờ xử lý.
      </p>
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
        <RouterLink to="/report">
          Xem tất cả reports
        </RouterLink>
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

  <ConfirmDialog
    v-model:open="isLockConfirmOpen"
    :is-loading="isLockingTarget"
    destructive
    confirm-button-text="Khóa"
    cancel-button-text="Hủy"
    @confirm="confirmLockTarget"
  >
    <template #title>
      Khóa {{ lockTargetLabel }}
    </template>
    <template #description>
      <p>
        Bạn có chắc muốn khóa {{ lockTargetLabel }} này?
      </p>
    </template>
  </ConfirmDialog>
</template>
