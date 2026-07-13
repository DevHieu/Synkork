<script setup lang="ts">
import { Eye, LoaderIcon, Lock, Search, ShieldAlert, Trash2, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'
import type { Report, ReportFilterParams, ReportReason, ReportSeverity, ReportStatus, ReportType } from '@/pages/report/types/Reports.ts'

import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import ReportDetail from './components/ReportDetail.vue'
import { deleteReport, getReports, updateReportStatus } from './service/reportService'
import { REASON_LABEL_MAP, SEVERITY_CONFIG } from './utils/report.utils.ts'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)
const totalPages = ref(0)

const pagedData = ref<Report[]>([])

const searchKeyword = ref('')
const filterStatus = ref<ReportStatus | 'ALL'>('ALL')
const filterType = ref<ReportType | 'ALL'>('ALL')
const filterSeverity = ref<ReportSeverity | 'ALL'>('ALL')
const dateRange = ref(defaultDateRange())
const debouncedSearch = refDebounced(searchKeyword, 500)

const selectedReport = ref<Report | null>(null)
const isDetailOpen = ref(false)

async function fetchReports() {
  loading.value = true
  try {
    const params: ReportFilterParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (searchKeyword.value.trim())
      params.search = searchKeyword.value.trim()
    if (filterStatus.value && filterStatus.value !== 'ALL')
      params.status = filterStatus.value
    if (filterType.value && filterType.value !== 'ALL')
      params.reportType = filterType.value
    if (filterSeverity.value && filterSeverity.value !== 'ALL')
      params.severity = filterSeverity.value
    if (dateRange.value?.from) {
      const fromDate = typeof dateRange.value.from === 'string' ? new Date(dateRange.value.from) : dateRange.value.from
      params.fromDate = formatToISODateTime(fromDate)
    }

    if (dateRange.value?.to) {
      const toDate = typeof dateRange.value.to === 'string' ? new Date(dateRange.value.to) : dateRange.value.to
      params.toDate = formatToISODateTime(toDate, true)
    }

    const res = await getReports({ params })

    pagedData.value = res.data
    totalCount.value = res.meta.totalElements || 0
    totalPages.value = res.meta.totalPages
  }
  catch (error) {
    console.error('Error fetching reports:', error)
  }
  finally {
    loading.value = false
  }
}

const typeLabelMap: Record<string, string> = {
  USER: 'Người dùng',
  ROOM: 'Phòng',
}

function renderSeverity(severity: string) {
  const config = SEVERITY_CONFIG[severity as keyof typeof SEVERITY_CONFIG]
  return h(Badge, { variant: 'outline', class: config ? `px-3 ${config.class}` : '' }, () => config?.label ?? severity)
}

const hasActiveFilter = computed(() =>
  !!searchKeyword.value
  || (filterStatus.value !== 'ALL')
  || (filterType.value !== 'ALL')
  || (filterSeverity.value !== 'ALL')
  || dateRange.value !== null, // null = tất cả = không active, có value = đang filter
)

function clearFilters() {
  searchKeyword.value = ''
  filterStatus.value = 'ALL'
  filterType.value = 'ALL'
  filterSeverity.value = 'ALL'
  dateRange.value = defaultDateRange()
}

function handleViewDetail(report: Report) {
  selectedReport.value = report
  isDetailOpen.value = true
}

async function handleUpdateReportStatus({ id, status, note }: { id: string, status: ReportStatus, note?: string }) {
  try {
    loading.value = true
    await updateReportStatus(id, status, note)

    const item = pagedData.value.find(r => r.id === id)
    if (item) {
      item.status = status
    }
    if (selectedReport.value?.id === id) {
      selectedReport.value = { ...selectedReport.value, status }
    }
    isDetailOpen.value = false
  }
  catch (error) {
    console.error('Lỗi cập nhật:', error)
  }
  finally {
    loading.value = false
  }
}

async function handleDeleteReport(reportId: string) {
  if (!confirm('Bạn có chắc chắn muốn xóa cái này?'))
    return

  try {
    loading.value = true
    await deleteReport(reportId)
  }
  catch (error) {
    console.error('Lỗi xóa report:', error)
  }
  finally {
    loading.value = false
    fetchReports()
  }
}

function renderStatus(status: string) {
  const config = {
    PENDING: {
      label: 'Chờ xử lý',
      class:
        'border-amber-300 bg-amber-50 px-3 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300',
    },
    REVIEWED: {
      label: 'Đang xem xét',
      class:
        'border-blue-300 bg-blue-50 px-3 text-blue-700 dark:border-blue-700 dark:bg-blue-950 dark:text-blue-300',
    },
    RESOLVED: {
      label: 'Đã giải quyết',
      class:
        'border-emerald-300 bg-emerald-50 px-3 text-emerald-700 dark:border-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
    },
    DISMISSED: {
      label: 'Đã bác bỏ',
      class:
        'border-red-300 bg-red-50 px-3 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-300',
    },
  }[status]

  return h(
    Badge,
    {
      variant: 'outline',
      class: config?.class,
    },
    () => config?.label ?? status,
  )
}

const columns = computed<TableColumn<Report>[]>(() => [
  {
    header: 'Loại',
    accessor: 'reportType',
    minWidth: 110,
    render: row =>
      h(Badge, { variant: row.reportType === 'USER' ? 'outline' : 'secondary' }, () => typeLabelMap[row.reportType] ?? row.reportType),
  },
  {
    header: 'Lý do',
    accessor: 'reason',
    minWidth: 180,
    render: row => REASON_LABEL_MAP[row.reason] ?? row.reason,
  },
  {
    header: 'Mức độ',
    accessor: 'severity',
    minWidth: 130,
    render: row => renderSeverity(row.severity),
  },
  { header: 'Mô tả', accessor: 'description', minWidth: 220 },
  {
    header: 'Trạng thái',
    accessor: 'status',
    minWidth: 150,
    render: row => renderStatus(row.status),
  },
  { header: 'Email người báo cáo', accessor: 'reporterEmail', minWidth: 180 },
  {
    header: 'Ngày tạo',
    accessor: 'createdAt',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Thao tác',
    minWidth: 140,
    render: row =>
      h('div', { class: 'flex gap-2' }, [
        h(
          UiButton,
          {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs',
            onClick: () => handleViewDetail(row),
          },
          () => [
            h(Eye, { class: 'h-3.5 w-3.5' }),
            'Xem',
          ],
        ),
        h(UiButton, {
          variant: 'outline',
          size: 'sm',
          class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
          onClick: () => handleDeleteReport(row.id),
        }, () => [h(Lock, { class: 'h-3.5 w-3.5' }), 'Khóa']),
      ]),
  },
])

watch([debouncedSearch, filterStatus, filterType, filterSeverity, dateRange], () => {
  currentPage.value = 1
  fetchReports()
})
watch(currentPage, fetchReports)

onMounted(fetchReports)
</script>

<template>
  <BasicPage title="Báo cáo vi phạm" description="Quản lý các báo cáo người dùng và phòng" sticky>
    <div class="flex flex-wrap items-center gap-3 mb-4">
      <div class="relative flex-1 min-w-[200px] max-w-sm">
        <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
        <Input
          v-model="searchKeyword"
          placeholder="Tìm theo lý do hoặc mô tả…"
          class="pl-8 pr-8 h-9 text-sm"
        />
        <button
          v-if="searchKeyword"
          class="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
          @click="searchKeyword = ''"
        >
          <X class="h-3.5 w-3.5" />
        </button>
      </div>

      <Select v-model="filterStatus">
        <SelectTrigger class="h-9 w-[160px] text-sm">
          <SelectValue placeholder="Tất cả trạng thái" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">
            Tất cả trạng thái
          </SelectItem>
          <SelectItem value="PENDING">
            Chờ xử lý
          </SelectItem>
          <SelectItem value="REVIEWED">
            Đang xem xét
          </SelectItem>
          <SelectItem value="RESOLVED">
            Đã giải quyết
          </SelectItem>
          <SelectItem value="DISMISSED">
            Đã bác bỏ
          </SelectItem>
        </SelectContent>
      </Select>

      <Select v-model="filterType">
        <SelectTrigger class="h-9 w-[150px] text-sm">
          <SelectValue placeholder="Tất cả loại" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">
            Tất cả loại
          </SelectItem>
          <SelectItem value="USER">
            Người dùng
          </SelectItem>
          <SelectItem value="ROOM">
            Phòng
          </SelectItem>
        </SelectContent>
      </Select>

      <Select v-model="filterSeverity">
        <SelectTrigger class="h-9 w-[150px] text-sm">
          <SelectValue placeholder="Tất cả mức độ" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">
            Tất cả mức độ
          </SelectItem>
          <SelectItem v-for="(config, key) in SEVERITY_CONFIG" :key="key" :value="key">
            {{ config.label }}
          </SelectItem>
        </SelectContent>
      </Select>

      <div>
        <DateRangePicker v-model="dateRange" />
      </div>

      <UiButton
        v-if="hasActiveFilter"
        variant="ghost"
        size="sm"
        class="h-9 gap-1.5 text-sm text-muted-foreground"
        @click="clearFilters"
      >
        <X class="h-3.5 w-3.5" />
        Xóa bộ lọc
      </UiButton>
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div
        v-if="!loading && pagedData?.length === 0"
        class="flex flex-col items-center justify-center py-20 text-muted-foreground gap-2"
      >
        <ShieldAlert class="h-10 w-10 opacity-40" />
        <p class="text-sm">
          Không tìm thấy báo cáo nào.
        </p>
        <UiButton v-if="hasActiveFilter" variant="link" size="sm" @click="clearFilters">
          Xóa bộ lọc để xem tất cả
        </UiButton>
      </div>

      <BaseTable v-else :columns="columns" :data="pagedData" />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPages"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <ReportDetail v-if="selectedReport" v-model:open="isDetailOpen" :report="selectedReport" @action="handleUpdateReportStatus" />
</template>
