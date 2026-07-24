<script setup lang="ts">
import { Check, LoaderIcon, Search, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import ConfirmDialog from '@/components/confirm-dialog.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import Pagination from '@/components/pagination.vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type {
  PasswordResetParams,
  PasswordResetRequest,
  PasswordResetStatus,
} from '../types/passwordResetTypes'

import { passwordResetService } from '../services/passwordResetService'

const loading = ref(false)
const requests = ref<PasswordResetRequest[]>([])
const keyword = ref('')
const selectedStatus = ref<string>('PENDING')
const dateRange = ref(defaultDateRange())
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)
const totalPage = ref(0)

const debouncedKeyword = refDebounced(keyword, 400)
const actionTarget = ref<PasswordResetRequest>()
const actionType = ref<'approve' | 'reject'>('approve')
const showActionDialog = ref(false)
const actionReason = ref('')

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Từ chối' },
  { value: 'NOT_VERIFIED', label: 'Chưa xác minh OTP' },
] as const

const hasActiveFilter = computed(() =>
  !!keyword.value
  || selectedStatus.value !== 'PENDING'
  || dateRange.value !== null,
)

async function fetchData() {
  loading.value = true
  try {
    const params: PasswordResetParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (debouncedKeyword.value.trim())
      params.search = debouncedKeyword.value.trim()
    if (selectedStatus.value !== 'ALL')
      params.status = selectedStatus.value as PasswordResetStatus
    if (dateRange.value?.from)
      params.dateFrom = formatToISODateTime(dateRange.value.from)
    if (dateRange.value?.to)
      params.dateTo = formatToISODateTime(dateRange.value.to, true)

    const response = await passwordResetService.getAll(params)
    requests.value = response.data ?? []
    totalCount.value = response.meta?.totalElements ?? 0
    totalPage.value = response.meta?.totalPages ?? 0
  }
  catch (error: any) {
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || 'Không thể tải danh sách yêu cầu đổi mật khẩu'
    toast.error(message)
    requests.value = []
    totalCount.value = 0
    totalPage.value = 0
  }
  finally {
    loading.value = false
  }
}

function clearFilters() {
  keyword.value = ''
  selectedStatus.value = 'PENDING'
  dateRange.value = defaultDateRange()
}

function openActionDialog(request: PasswordResetRequest, type: 'approve' | 'reject') {
  actionTarget.value = request
  actionType.value = type
  actionReason.value = ''
  showActionDialog.value = true
}

async function confirmAction() {
  if (!actionTarget.value)
    return

  loading.value = true
  try {
    if (actionType.value === 'approve') {
      await passwordResetService.approve(actionTarget.value.id)
      toast.success(`Đã duyệt yêu cầu của ${actionTarget.value.email}`)
    }
    else {
      await passwordResetService.reject(actionTarget.value.id)
      toast.success(`Đã từ chối yêu cầu của ${actionTarget.value.email}`)
    }

    showActionDialog.value = false
    fetchData()
  }
  catch (error: any) {
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || error?.message || 'Không thể xử lý yêu cầu'
    toast.error(message)
  }
  finally {
    loading.value = false
  }
}

function renderStatus(status: PasswordResetStatus) {
  const config = {
    PENDING: { label: 'Chờ duyệt', class: 'border-amber-200 bg-amber-100 text-amber-800 dark:border-amber-800 dark:bg-amber-900/30 dark:text-amber-300' },
    APPROVED: { label: 'Đã duyệt', class: 'border-emerald-200 bg-emerald-100 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300' },
    REJECTED: { label: 'Từ chối', class: 'border-rose-200 bg-rose-100 text-rose-800 dark:border-rose-800 dark:bg-rose-900/30 dark:text-rose-300' },
    NOT_VERIFIED: { label: 'Chưa xác minh', class: 'border-slate-200 bg-slate-100 text-slate-800 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300' },
  }[status]

  return h(Badge, {
    variant: 'outline',
    class: `text-xs font-semibold ${config.class}`,
  }, () => config.label)
}

const columns = computed<TableColumn<PasswordResetRequest>[]>(() => [
  {
    header: 'Tài khoản',
    minWidth: 240,
    render: row => h('div', { class: 'space-y-0.5 text-left' }, [
      h('div', { class: 'font-medium text-foreground' }, row.displayName || row.username),
      h('div', { class: 'text-xs text-muted-foreground' }, row.email),
    ]),
  },
  {
    header: 'Vai trò',
    minWidth: 110,
    render: row => h(Badge, { variant: 'secondary', class: 'text-xs font-semibold uppercase' }, () => row.role),
  },
  {
    header: 'Trạng thái',
    minWidth: 140,
    render: row => renderStatus(row.status),
  },
  {
    header: 'Ngày gửi',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Cập nhật',
    minWidth: 160,
    render: row => formatTimestamp(row.updatedAt),
  },
  {
    header: 'Thao tác',
    minWidth: 190,
    render: row => row.status === 'PENDING'
      ? h('div', { class: 'flex justify-center gap-1.5' }, [
          h(UiButton, {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-900/20',
            onClick: () => openActionDialog(row, 'approve'),
          }, () => [h(Check, { class: 'h-3.5 w-3.5' }), 'Duyệt']),
          h(UiButton, {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
            onClick: () => openActionDialog(row, 'reject'),
          }, () => [h(X, { class: 'h-3.5 w-3.5' }), 'Từ chối']),
        ])
      : h('span', { class: 'text-xs text-muted-foreground' }, 'Đã xử lý'),
  },
])

watch(currentPage, fetchData)
watch([debouncedKeyword, selectedStatus, dateRange], () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  fetchData()
})

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="keyword"
          type="text"
          placeholder="Tìm theo tên, username hoặc email..."
          class="h-9 pl-8"
        />
      </div>

      <div class="w-[190px]">
        <UiSelect v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

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

      <div class="overflow-x-auto">
        <BaseTable :columns="columns" :data="requests" />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </div>

  <ConfirmDialog
    v-model:open="showActionDialog"
    v-model:reason="actionReason"
    :destructive="actionType === 'reject'"
    :require-reason="false"
    :close-on-confirm="false"
    cancel-button-text="Hủy"
    :confirm-button-text="actionType === 'approve' ? 'Duyệt yêu cầu' : 'Từ chối'"
    :is-loading="loading"
    @confirm="confirmAction"
  >
    <template #title>
      {{ actionType === 'approve' ? 'Duyệt yêu cầu đổi mật khẩu?' : 'Từ chối yêu cầu đổi mật khẩu?' }}
    </template>

    <template #description>
      <p>
        {{
          actionType === 'approve'
            ? `Mật khẩu mới của ${actionTarget?.email} sẽ được áp dụng ngay sau khi duyệt.`
            : `Yêu cầu của ${actionTarget?.email} sẽ được đánh dấu là từ chối.`
        }}
      </p>
    </template>
  </ConfirmDialog>
</template>
