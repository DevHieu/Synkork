<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue'
import { LoaderIcon, Eye, ShieldAlert, Search, X } from '@lucide/vue'
import { useDebounceFn } from '@vueuse/core'

import { BasicPage } from '@/components/global-layout'
import type { TableColumn } from '@/components/base-table.vue'
import { Button as UiButton } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import DateRangePicker from '@/components/date-range-picker.vue'

import type { Report, ReportStatus, ReportType, ReportFilterParams } from '@/types/Reports'
import ReportDetail from './components/ReportDetail.vue'
import { fetchReports, updateReportStatus } from './service/reportService'

// ── State ──────────────────────────────────────────────────────────────
const loading    = ref(false)
const currentPage = ref(0)   // 0-based (server-side)
const pageSize    = 10
const totalElements = ref(0)
const totalPages    = ref(0)

const pagedData = ref<Report[]>([])

// Filter state
const searchKeyword  = ref('')
const filterStatus   = ref<ReportStatus | ''>('')
const filterType     = ref<ReportType | ''>('')
interface AppDateRange { from: Date; to: Date }
const dateRange = ref<AppDateRange | null>(null)

// DateRangePicker require non-null modelValue — dùng today làm placeholder khi chưa chọn
const dateRangeModel = computed<AppDateRange>({
  get: () => dateRange.value ?? { from: new Date(), to: new Date() },
  set: (val) => { dateRange.value = val },
})

const selectedReport = ref<Report | null>(null)
const isDetailOpen   = ref(false)

// ── Fetch ──────────────────────────────────────────────────────────────
const fetchData = async () => {
  loading.value = true
  try {
    const params: ReportFilterParams = {
      page: currentPage.value,
      size: pageSize,
    }

    if (searchKeyword.value.trim()) params.search     = searchKeyword.value.trim()
    if (filterStatus.value)         params.status     = filterStatus.value
    if (filterType.value)           params.reportType = filterType.value
    if (dateRange.value?.from) params.dateFrom = dateRange.value.from.toISOString().slice(0, 10)
    if (dateRange.value?.to)   params.dateTo   = dateRange.value.to.toISOString().slice(0, 10)

    const res = await fetchReports(params)

    pagedData.value     = res.content
    totalElements.value = res.totalElements
    totalPages.value    = res.totalPages
  } catch (error) {
    console.error('Error fetching reports:', error)
  } finally {
    loading.value = false
  }
}

// Reset về trang 0 và fetch lại khi filter thay đổi
const resetAndFetch = () => {
  currentPage.value = 0
  fetchData()
}

// Debounce search để tránh gọi API liên tục khi user đang gõ
const debouncedSearch = useDebounceFn(resetAndFetch, 400)

watch(searchKeyword, () => debouncedSearch())
watch([filterStatus, filterType, dateRange], resetAndFetch, { deep: true })
watch(currentPage, fetchData)

onMounted(fetchData)

// ── Clear filters ──────────────────────────────────────────────────────
const hasActiveFilter = computed(() =>
  !!searchKeyword.value || !!filterStatus.value || !!filterType.value ||
  !!dateRange.value?.from
)

function clearFilters() {
  searchKeyword.value = ''
  filterStatus.value  = ''
  filterType.value    = ''
  dateRange.value     = null
}

// ── Detail modal ───────────────────────────────────────────────────────
function handleViewDetail(report: Report) {
  selectedReport.value = report
  isDetailOpen.value   = true
}

// ── Update status (gọi API thật) ───────────────────────────────────────
async function handleUpdateReportStatus({ id, status }: { id: string; status: ReportStatus }) {
  try {
    loading.value = true

    // 1. Gọi API backend
    await updateReportStatus(id, status)

    // 2. Cập nhật UI cục bộ ngay lập tức
    const item = pagedData.value.find(r => r.id === id)
    if (item) {
      item.status    = status
      item.updatedAt = new Date().toISOString()
    }

    // 3. Cập nhật report đang mở trong modal (nếu có)
    if (selectedReport.value?.id === id) {
      selectedReport.value = { ...selectedReport.value, status, updatedAt: new Date().toISOString() }
    }

    isDetailOpen.value = false
  } catch (error) {
    console.error('Lỗi cập nhật:', error)
  } finally {
    loading.value = false
  }
}

// ── Table columns ──────────────────────────────────────────────────────
const statusVariantMap: Record<string, 'default' | 'secondary' | 'destructive' | 'outline'> = {
  PENDING:  'secondary',
  RESOLVED: 'default',
  DISMISSED:'destructive',
  REVIEWED: 'outline',
}

const columns = computed<TableColumn<Report>[]>(() => [
  { header: 'ID',          accessor: 'id',          minWidth: 100 },
  {
    header: 'Type',
    accessor: 'reportType',
    minWidth: 100,
    render: (row) =>
      h(Badge, { variant: row.reportType === 'USER' ? 'outline' : 'secondary' }, () => row.reportType),
  },
  { header: 'Reason',      accessor: 'reason',      minWidth: 200 },
  { header: 'Description', accessor: 'description', minWidth: 240 },
  {
    header: 'Status',
    accessor: 'status',
    minWidth: 120,
    render: (row) =>
      h(Badge, { variant: statusVariantMap[row.status] ?? 'default' }, () => row.status),
  },
  { header: 'Reporter ID', accessor: 'reporterId',  minWidth: 160 },
  {
    header: 'Created At',
    accessor: 'createdAt',
    minWidth: 160,
    render: (row) => new Date(row.createdAt).toLocaleDateString('vi-VN'),
  },
  {
    header: 'Updated At',
    accessor: 'updatedAt',
    minWidth: 160,
    render: (row) => new Date(row.updatedAt).toLocaleDateString('vi-VN'),
  },
  {
    header: 'Actions',
    minWidth: 140,
    render: (row) =>
      h(
        UiButton,
        {
          variant: 'outline',
          size: 'sm',
          class: 'h-8 gap-1 px-2 text-xs',
          onClick: () => handleViewDetail(row),
        },
        () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'View Detail'],
      ),
  },
])
</script>

<template>
  <BasicPage title="Reports" description="Manage user and room reports" sticky>
    <template #actions>
      <DateRangePicker v-model="dateRangeModel" />
    </template>

    <!-- ── Toolbar: Search + Filter ──────────────────────────────── -->
    <div class="flex flex-wrap items-center gap-3 mb-4">

      <!-- Search -->
      <div class="relative flex-1 min-w-[200px] max-w-sm">
        <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
        <Input
          v-model="searchKeyword"
          placeholder="Search reason or description…"
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

      <!-- Filter: Status -->
      <Select v-model="filterStatus">
        <SelectTrigger class="h-9 w-[150px] text-sm">
          <SelectValue placeholder="All Status" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">All Status</SelectItem>
          <SelectItem value="PENDING">Pending</SelectItem>
          <SelectItem value="REVIEWED">Reviewed</SelectItem>
          <SelectItem value="RESOLVED">Resolved</SelectItem>
          <SelectItem value="DISMISSED">Dismissed</SelectItem>
        </SelectContent>
      </Select>

      <!-- Filter: Type -->
      <Select v-model="filterType">
        <SelectTrigger class="h-9 w-[140px] text-sm">
          <SelectValue placeholder="All Types" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">All Types</SelectItem>
          <SelectItem value="USER">User</SelectItem>
          <SelectItem value="ROOM">Room</SelectItem>
        </SelectContent>
      </Select>

      <!-- Clear filters -->
      <UiButton
        v-if="hasActiveFilter"
        variant="ghost"
        size="sm"
        class="h-9 gap-1.5 text-sm text-muted-foreground"
        @click="clearFilters"
      >
        <X class="h-3.5 w-3.5" />
        Clear filters
      </UiButton>

      <!-- Summary badge -->
      <span v-if="!loading" class="ml-auto text-xs text-muted-foreground whitespace-nowrap">
        {{ totalElements }} report{{ totalElements !== 1 ? 's' : '' }} found
      </span>
    </div>

    <!-- ── Table ──────────────────────────────────────────────────── -->
    <div class="relative">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div
        v-if="!loading && pagedData.length === 0"
        class="flex flex-col items-center justify-center py-20 text-muted-foreground gap-2"
      >
        <ShieldAlert class="h-10 w-10 opacity-40" />
        <p class="text-sm">No reports found.</p>
        <UiButton v-if="hasActiveFilter" variant="link" size="sm" @click="clearFilters">
          Clear filters to see all
        </UiButton>
      </div>

      <BaseTable v-else :columns="columns" :data="pagedData" />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPages"
        :total-count="totalElements"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <!-- Detail dialog -->
  <ReportDetail
    v-if="selectedReport"
    v-model:open="isDetailOpen"
    :report="selectedReport"
    @action="handleUpdateReportStatus"
  />
</template>