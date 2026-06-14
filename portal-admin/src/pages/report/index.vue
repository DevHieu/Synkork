<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue'
import { LoaderIcon, Eye, ShieldAlert, Search, X, Trash2 } from '@lucide/vue'
import { BasicPage } from '@/components/global-layout'
import type { TableColumn } from '@/components/base-table.vue'
import { Button as UiButton } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import DateRangePicker from '@/components/date-range-picker.vue'
import type { Report, ReportStatus, ReportType, ReportFilterParams } from '@/pages/report/types/Reports.ts'
import ReportDetail from './components/ReportDetail.vue'
import { getReports, updateReportStatus, deleteReport } from './service/reportService'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'
import { refDebounced } from '@vueuse/core'

const loading    = ref(false)
const currentPage = ref(1) 
const pageSize    = 20
const totalCount = ref(0)
const totalPages    = ref(0)

const pagedData = ref<Report[]>([])

const searchKeyword  = ref('')
const filterStatus   = ref<ReportStatus | 'ALL'>('ALL')
const filterType     = ref<ReportType | 'ALL'>('ALL')
const dateRange = ref(defaultDateRange())
const debouncedSearch = refDebounced(searchKeyword, 500)

const selectedReport = ref<Report | null>(null)
const isDetailOpen   = ref(false)

const fetchReports = async () => {
  loading.value = true
  try {
    const params: ReportFilterParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (searchKeyword.value.trim()) params.search = searchKeyword.value.trim()
    if (filterStatus.value && filterStatus.value !== 'ALL') params.status = filterStatus.value
    if (filterType.value && filterType.value !== 'ALL') params.reportType = filterType.value
    if (dateRange.value?.from) {
      const fromDate = typeof dateRange.value.from === 'string' ? new Date(dateRange.value.from) : dateRange.value.from
      params.fromDate = formatToISODateTime(fromDate)
    }

    if (dateRange.value?.to) {
      const toDate = typeof dateRange.value.to === 'string' ? new Date(dateRange.value.to) : dateRange.value.to
      params.toDate = formatToISODateTime(toDate, true) 
    }
    
    const res = await getReports({ params: params })

    pagedData.value = res.data
    totalCount.value = res.meta.totalElements || 0
    totalPages.value = res.meta.totalPages
  } catch (error) {
    console.error('Error fetching reports:', error)
  } finally {
    loading.value = false
  }
}

const hasActiveFilter = computed(() =>
  !!searchKeyword.value || !!filterStatus.value && filterStatus.value !== 'ALL' || !!filterType.value && filterType.value !== 'ALL'
)

function clearFilters() {
  searchKeyword.value = ''
  filterStatus.value  = 'ALL'
  filterType.value    = 'ALL'
  dateRange.value     = defaultDateRange()
}

function handleViewDetail(report: Report) {
  selectedReport.value = report
  isDetailOpen.value   = true
}

async function handleUpdateReportStatus({ id, status, note }: { id: string; status: ReportStatus; note?: string }) {
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
  } catch (error) {
    console.error('Lỗi cập nhật:', error)
  } finally {
    loading.value = false
  }
}

async function handleDeleteReport(reportId: string){
  if(!confirm('Bạn có chắc muốn xó cái nì khum?')) return

  try {
    loading.value = true
    await deleteReport(reportId)
  } catch (error) {
    console.error('Lỗi xóa report:', error)
  } finally {
    loading.value = false
    fetchReports()
  }
}

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
  { header: 'Reporter Email', accessor: 'reporterEmail',  minWidth: 160 },
  {
    header: 'Created At',
    accessor: 'createdAt',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Actions',
    minWidth: 140,
    render: (row) =>
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
          'View',
        ],
      ),

      h(
        UiButton,
        {
          variant: 'destructive',
          size: 'sm',
          class: 'h-8 gap-1 px-2 text-xs',
          onClick: () => handleDeleteReport(row.id),
        },
        () => [
          h(Trash2, { class: 'h-3.5 w-3.5' }),
          'Delete',
        ],
      ),
    ]),
  },
])

watch([debouncedSearch, filterStatus, filterType, dateRange], () => {
  currentPage.value = 1
  fetchReports()
})
watch(currentPage, fetchReports)

onMounted(fetchReports)
</script>

<template>
  <BasicPage title="Reports" description="Manage user and room reports" sticky>
    <div class="flex flex-wrap items-center gap-3 mb-4">
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
        Clear filters
      </UiButton>

      <span v-if="!loading" class="ml-auto text-xs text-muted-foreground whitespace-nowrap">
        {{ totalCount }} report{{ totalCount !== 1 ? 's' : '' }} found
      </span>
    </div>

    <div class="relative">
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
        <p class="text-sm">No reports found.</p>
        <UiButton v-if="hasActiveFilter" variant="link" size="sm" @click="clearFilters">
          Clear filters to see all
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