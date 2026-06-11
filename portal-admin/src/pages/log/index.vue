<script lang="ts" setup>
import { Eye, LoaderIcon, Search } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type { AuditLog, LogParams } from './types/LogTypes'

import AuditLogDetailDialog from './components/AuditLogDetailDialog.vue'
import { logService } from './service/logService'

const selectedLog = ref<AuditLog | null>(null)
const isDetailOpen = ref(false)

const loading = ref(false)
const logsData = ref<AuditLog[]>([])
const currentPage = ref(1)
const pageSize = 20

const searchKeyword = ref('')
const actionKeyword = ref('')
const selectedEntityType = ref<string>('ALL')
const dateRange = ref(defaultDateRange())

const debounceSearchKeyword = refDebounced(searchKeyword, 500)
const debounceActionKeyword = refDebounced(actionKeyword, 500)
const totalCount = ref(0)
const totalPage = ref(0)

const columns = computed<TableColumn<any>[]>(() => [
  { header: 'Hành động', accessor: 'action', minWidth: 150 },
  { header: 'Đối tượng', accessor: 'entityType', minWidth: 120 },
  { header: 'Tên đối tượng', accessor: 'entityName', minWidth: 160 },
  { header: 'Người thực hiện', accessor: 'actorEmail', minWidth: 200 },
  {
    header: 'Thời gian',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Hành động',
    minWidth: 120,
    render: row => h(UiButton, {
      variant: 'outline',
      size: 'sm',
      class: 'h-8 gap-1 px-2 text-xs',
      onClick: () => handleViewDetail(row),
    }, () => [
      h(Eye, { class: 'h-3.5 w-3.5' }),
      'Chi tiết',
    ]),
  },
])

async function fetchLogs() {
  loading.value = true
  try {
    const queryParams: LogParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (searchKeyword.value.trim()) {
      queryParams.search = searchKeyword.value.trim()
    }

    if (actionKeyword.value.trim()) {
      queryParams.action = actionKeyword.value.trim()
    }

    if (selectedEntityType.value !== 'ALL') {
      queryParams.entityType = selectedEntityType.value
    }

    if (dateRange.value?.from) {
      const dateFrom = typeof dateRange.value.from === 'string' ? new Date(dateRange.value.from) : dateRange.value.from
      queryParams.dateFrom = formatToISODateTime(dateFrom)
    }

    if (dateRange.value?.to) {
      const dateTo = typeof dateRange.value.to === 'string' ? new Date(dateRange.value.to) : dateRange.value.to
      queryParams.dateTo = formatToISODateTime(dateTo, true) // true để lấy 23:59:59
    }
    const response = await logService.getLogs({ params: queryParams })

    logsData.value = response.data || []
    totalCount.value = response.meta.totalElements || 0
    totalPage.value = response.meta.totalPages || 0
  }
  catch (error) {
    console.error('Lỗi khi tải danh sách hệ thống log:', error)
  }
  finally {
    loading.value = false
  }
}

function handleViewDetail(log: AuditLog) {
  selectedLog.value = log
  isDetailOpen.value = true
}

watch([debounceSearchKeyword, debounceActionKeyword, selectedEntityType, dateRange], () => {
  currentPage.value = 1
  fetchLogs()
})

watch(currentPage, () => {
  fetchLogs()
})

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <BasicPage
    title="System Log"
    description="Quản lý và theo dõi nhật ký hoạt động hệ thống"
    sticky
  >
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="searchKeyword"
          type="text"
          placeholder="Tìm theo email, nội dung log..."
          class="pl-8 h-9"
        />
      </div>

      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="actionKeyword"
          type="text"
          placeholder="Tìm theo hành động"
          class="pl-8 h-9"
        />
      </div>

      <div class="w-[180px]">
        <UiSelect v-model="selectedEntityType">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Loại đối tượng" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">
              Tất cả đối tượng
            </SelectItem>
            <SelectItem value="USER">
              User (Người dùng)
            </SelectItem>
            <SelectItem value="WORKSPACE">
              Workspace
            </SelectItem>
            <SelectItem value="REPORT">
              Report (Báo cáo)
            </SelectItem>
            <SelectItem value="SUBSCRIPTION">
              Subscription
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div>
        <DateRangePicker v-model="dateRange" />
      </div>
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <!-- Loading Overlay -->
      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable
          :columns="columns"
          :data="logsData"
        />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <AuditLogDetailDialog
    v-if="selectedLog"
    v-model:open="isDetailOpen"
    :log-id="selectedLog.id"
  />
</template>
