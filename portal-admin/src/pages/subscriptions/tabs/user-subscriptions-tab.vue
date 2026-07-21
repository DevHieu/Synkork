<script setup lang="ts">
import { LoaderIcon, ReceiptText, Search, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'
import type { AppDateRange } from '@/types/Date'

import BaseTable from '@/components/base-table.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import Pagination from '@/components/pagination.vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type { SubscriptionSearchParams, UserSubscription } from '../types/invoiceTypes'

import { subscriptionService } from '../service/subscriptionService'

const props = defineProps<{
  keyword?: string
}>()

const emit = defineEmits<{
  viewInvoice: [invoiceId: string]
}>()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const totalElement = ref(0)
const totalPages = ref(0)
const subscriptionsData = ref<UserSubscription[]>([])

const searchKeyword = ref('')
const selectedPlan = ref('ALL')
const selectedStatus = ref('ALL')
const selectedCurrent = ref('ALL')
const expiresRange = ref<AppDateRange>(null)

const debounceSearchKeyword = refDebounced(searchKeyword, 450)

const planOptions = [
  { value: 'ALL', label: 'Tất cả gói' },
  { value: 'FREE', label: 'Free' },
  { value: 'TEAM', label: 'Team' },
  { value: 'BUSINESS', label: 'Business' },
]

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'EXPIRED', label: 'Expired' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'PENDING', label: 'Pending' },
]

const currentOptions = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'true', label: 'Đang dùng' },
  { value: 'false', label: 'Lịch sử' },
]

function formatMoney(amount?: number | string | null) {
  if (amount === null || amount === undefined)
    return 'N/A'

  const value = typeof amount === 'string' ? Number(amount) : amount
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function renderPlan(plan: string) {
  let badgeClass = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold '
  if (plan === 'BUSINESS')
    badgeClass += 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300 border border-purple-200 dark:border-purple-800'
  else if (plan === 'TEAM')
    badgeClass += 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300 border border-blue-200 dark:border-blue-800'
  else
    badgeClass += 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border border-slate-200 dark:border-slate-700'

  return h('span', { class: `${badgeClass}` }, plan)
}

function renderSubscriptionStatus(status: string) {
  const config = {
    ACTIVE: {
      label: 'Đang hoạt động',
      class: 'border-emerald-300 bg-emerald-50 px-3 text-emerald-700 dark:border-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
    },
    EXPIRED: {
      label: 'Hết hạn',
      class: 'border-slate-300 bg-slate-50 px-3 text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-300',
    },
    CANCELLED: {
      label: 'Đã huỷ',
      class: 'border-red-300 bg-red-50 px-3 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-300',
    },
    PENDING: {
      label: 'Chờ kích hoạt',
      class: 'border-amber-300 bg-amber-50 px-3 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300',
    },
  }[status]

  return h(Badge, { variant: 'outline', class: config?.class }, () => config?.label ?? status)
}

function renderCurrent(current: boolean) {
  return h(
    Badge,
    {
      variant: 'outline',
      class: current
        ? 'border-primary/30 bg-primary/10 px-3 text-primary'
        : 'border-slate-300 bg-slate-50 px-3 text-slate-600 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-300',
    },
    () => current ? 'Current' : 'History',
  )
}

function buildParams(): SubscriptionSearchParams {
  const params: SubscriptionSearchParams = {
    page: currentPage.value - 1,
    size: pageSize,
  }

  const search = debounceSearchKeyword.value.trim()

  if (search)
    params.search = search
  if (selectedPlan.value !== 'ALL')
    params.plan = selectedPlan.value
  if (selectedStatus.value !== 'ALL')
    params.status = selectedStatus.value
  if (selectedCurrent.value !== 'ALL')
    params.current = selectedCurrent.value === 'true'

  if (expiresRange.value?.from)
    params.expiresFrom = formatToISODateTime(expiresRange.value.from)

  if (expiresRange.value?.to)
    params.expiresTo = formatToISODateTime(expiresRange.value.to, true)

  return params
}

async function fetchSubscriptions() {
  loading.value = true
  try {
    const res = await subscriptionService.getSubscriptions({ params: buildParams() })
    subscriptionsData.value = res.data || []
    totalElement.value = res.meta.totalElements || 0
    totalPages.value = res.meta.totalPages || 0
  }
  catch (err) {
    console.error('Lỗi khi tải danh sách user subscription:', err)
    subscriptionsData.value = []
    totalElement.value = 0
  }
  finally {
    loading.value = false
  }
}

const hasActiveFilter = computed(() =>
  !!searchKeyword.value
  || selectedPlan.value !== 'ALL'
  || selectedStatus.value !== 'ALL'
  || selectedCurrent.value !== 'ALL'
  || expiresRange.value !== null,
)

function clearFilters() {
  searchKeyword.value = ''
  selectedPlan.value = 'ALL'
  selectedStatus.value = 'ALL'
  selectedCurrent.value = 'ALL'
  expiresRange.value = null
}

watch([debounceSearchKeyword, selectedPlan, selectedStatus, selectedCurrent, expiresRange], () => {
  currentPage.value = 1
  fetchSubscriptions()
})

watch(currentPage, () => {
  fetchSubscriptions()
})

watch(() => props.keyword, (keyword) => {
  if (!keyword || keyword === searchKeyword.value)
    return

  searchKeyword.value = keyword
  currentPage.value = 1
})

onMounted(() => {
  if (props.keyword) {
    searchKeyword.value = props.keyword
    return
  }

  fetchSubscriptions()
})

const columns = computed<TableColumn<UserSubscription>[]>(() => [
  {
    header: 'Username',
    minWidth: 150,
    render: row => row.username || 'N/A',
  },
  {
    header: 'Email',
    minWidth: 220,
    render: row => row.userEmail || 'N/A',
  },
  {
    header: 'Gói',
    minWidth: 130,
    render: row => renderPlan(row.plan),
  },
  {
    header: 'Trạng thái',
    minWidth: 160,
    render: row => renderSubscriptionStatus(row.status),
  },
  {
    header: 'Current',
    minWidth: 120,
    render: row => renderCurrent(row.current),
  },
  {
    header: 'Bắt đầu',
    minWidth: 170,
    render: row => row.startedAt ? formatTimestamp(row.startedAt) : 'N/A',
  },
  {
    header: 'Hết hạn',
    minWidth: 170,
    render: row => row.expiresAt ? formatTimestamp(row.expiresAt) : 'N/A',
  },
  {
    header: 'Invoice',
    minWidth: 240,
    render: row => row.invoiceId || 'N/A',
  },
  {
    header: 'Tiền invoice',
    minWidth: 140,
    render: row => formatMoney(row.invoiceAmount),
  },
  {
    header: 'Payment',
    minWidth: 140,
    render: row => row.paymentMethod || 'N/A',
  },
  {
    header: 'Ngày tạo',
    minWidth: 170,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Thao tác',
    minWidth: 130,
    render: row => h(UiButton, {
      variant: 'outline',
      size: 'sm',
      class: 'h-8 gap-1.5 px-2 text-xs',
      disabled: !row.invoiceId,
      onClick: () => {
        if (row.invoiceId)
          emit('viewInvoice', row.invoiceId)
      },
    }, () => [
      h(ReceiptText, { class: 'h-3.5 w-3.5' }),
      'Xem đơn',
    ]),
  },
])
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput v-model="searchKeyword" placeholder="Tìm theo user, email hoặc mã invoice..." class="h-9 pl-8" />
      </div>

      <div class="w-[160px]">
        <UiSelect v-model="selectedPlan">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Gói" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="item in planOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div class="w-[180px]">
        <UiSelect v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div class="w-[150px]">
        <UiSelect v-model="selectedCurrent">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Current" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="item in currentOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div><DateRangePicker v-model="expiresRange" /></div>

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
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable :columns="columns" :data="subscriptionsData" />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPages"
        :total-count="totalElement"
        :per-page="pageSize"
      />
    </div>
  </div>
</template>
