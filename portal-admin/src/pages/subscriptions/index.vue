<script setup lang="ts">
import { LoaderIcon, Search, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type { Invoice, InvoiceSearchParams } from './types/invoiceTypes'

import { subscriptionService } from './service/subscriptionService'

const { t } = useI18n()

const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const totalElement = ref(0)
const totalPages = ref(0)
const invoicesData = ref<Invoice[]>([])

const searchKeyword = ref('')
const selectedStatus = ref('ALL')
const selectedPlan = ref('ALL')
const selectedPaymentMethod = ref('ALL')
const dateRange = ref(defaultDateRange())

const debounceSearchKeyword = refDebounced(searchKeyword, 450)

const selectedInvoice = ref<Invoice | null>(null)
const isOpen = ref(false)
const showComponent = shallowRef<Component | null>(null)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'PAID', label: 'Paid' },
  { value: 'FAILED', label: 'Failed' },
]

const planOptions = [
  { value: 'ALL', label: 'Tất cả gói' },
  { value: 'FREE', label: 'Free' },
  { value: 'TEAM', label: 'Team' },
  { value: 'BUSINESS', label: 'Business' },
]

const paymentMethodOptions = [
  { value: 'ALL', label: 'Tất cả phương thức' },
  { value: 'MOMO', label: 'MoMo' },
  { value: 'VNPAY', label: 'VNPay' },
]

function statusMeta(status?: string | null) {
  const normalized = (status || 'PENDING').toUpperCase()
  if (normalized === 'PAID')
    return { label: 'Paid', tone: 'green' }
  if (normalized === 'FAILED')
    return { label: 'Failed', tone: 'red' }
  if (normalized === 'CANCELLED')
    return { label: 'Cancelled', tone: 'gray' }
  return { label: 'Pending', tone: 'orange' }
}

function formatMoney(amount?: number | string | null) {
  const value = typeof amount === 'string' ? Number(amount) : amount ?? 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function buildParams(): InvoiceSearchParams {
  const params: InvoiceSearchParams = {
    page: currentPage.value - 1,
    size: pageSize,
  }

  const search = debounceSearchKeyword.value.trim()

  if (search)
    params.search = search
  if (selectedStatus.value !== 'ALL')
    params.status = selectedStatus.value
  if (selectedPlan.value !== 'ALL')
    params.plan = selectedPlan.value
  if (selectedPaymentMethod.value !== 'ALL')
    params.paymentMethod = selectedPaymentMethod.value

  if (dateRange.value?.from) {
    params.dateFrom = formatToISODateTime(dateRange.value.from)
  }

  if (dateRange.value?.to) {
    params.dateTo = formatToISODateTime(dateRange.value.to, true)
  }

  return params
}

async function fetchInvoices() {
  loading.value = true
  try {
    const res = await subscriptionService.getInvoices({ params: buildParams() })
    invoicesData.value = res.data || []
    totalElement.value = res.meta.totalElements || 0
    totalPages.value = res.meta.totalPages || 0
  }
  catch (err) {
    console.error('Lỗi khi tải danh sách invoice:', err)
    invoicesData.value = []
    totalElement.value = 0
  }
  finally {
    loading.value = false
  }
}

async function handleSelectDetail(invoice: Invoice) {
  try {
    const { default: component } = await import('./components/billing-history/billing-detail.vue')
    showComponent.value = component
    selectedInvoice.value = invoice
    isOpen.value = true
  }
  catch (err) {
    console.error('Failed to load billing detail', err)
  }
}

const hasActiveFilter = computed(() =>
  !!searchKeyword.value
  || (selectedStatus.value !== 'ALL')
  || (selectedPlan.value !== 'ALL')
  || (selectedPaymentMethod.value !== 'ALL')
  || dateRange.value !== null, // null = tất cả = không active, có value = đang filter
)

function clearFilters() {
  searchKeyword.value = ''
  selectedStatus.value = 'ALL'
  selectedPlan.value = 'ALL'
  selectedPaymentMethod.value = 'ALL'
  dateRange.value = defaultDateRange()
}

watch([debounceSearchKeyword, selectedStatus, selectedPlan, selectedPaymentMethod, dateRange], () => {
  currentPage.value = 1
  fetchInvoices()
})

watch(currentPage, () => {
  fetchInvoices()
})

onMounted(() => {
  fetchInvoices()
})

const columns = computed<TableColumn<Invoice>[]>(() => [
  {
    header: t('subscriptions.username'),
    minWidth: 150,
    render: row => row.username || 'N/A',
  },
  {
    header: t('subscriptions.email'),
    minWidth: 200,
    render: row => row.userEmail || 'N/A',
  },
  {
    header: t('subscriptions.plan'),
    minWidth: 130,
    render: row => row.plan,
  },
  {
    header: t('subscriptions.planExpiry'),
    minWidth: 170,
    render: row => row.planExpiresAt ? formatTimestamp(row.planExpiresAt) : 'N/A',
  },
  {
    header: t('subscriptions.amount'),
    minWidth: 140,
    render: row => formatMoney(row.amount),
  },
  {
    header: t('subscriptions.status'),
    minWidth: 150,
    render: (row) => {
      const meta = statusMeta(row.status)
      return h(Badge, {
        class: 'flex max-w-[120px] items-center',
        style: { color: meta.tone },
        variant: 'secondary',
      }, () => meta.label)
    },
  },
  {
    header: t('subscriptions.payment'),
    minWidth: 140,
    render: row => row.paymentMethod,
  },
  {
    header: t('subscriptions.paidAt'),
    minWidth: 170,
    render: row => row.paidAt ? formatTimestamp(row.paidAt) : 'N/A',
  },
  {
    header: t('subscriptions.created'),
    minWidth: 170,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: t('subscriptions.actions'),
    minWidth: 110,
    render: row => h(UiButton, {
      variant: 'outline',
      size: 'sm',
      class: 'h-8 gap-1 px-2 text-xs',
      onClick: () => handleSelectDetail(row),
    }, () => t('subscriptions.details')),
  },
])
</script>

<template>
  <BasicPage
    title="Gói đăng ký"
    description="Quản lý hóa đơn và lịch sử thanh toán theo kiểu bảng vận hành."
    sticky
  >
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput v-model="searchKeyword" placeholder="Tìm theo id hoặc email..." class="h-9 pl-8" />
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
        <UiSelect v-model="selectedPaymentMethod">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Phương thức" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="item in paymentMethodOptions" :key="item.value" :value="item.value">
              {{ item.label }}
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
        Clear filters
      </UiButton>
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable :columns="columns" :data="invoicesData" />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPages"
        :total-count="totalElement"
        :per-page="pageSize"
      />
    </div>

    <Modal v-model:open="isOpen">
      <ModalContent>
        <component :is="showComponent" v-if="selectedInvoice" :billing="selectedInvoice" />
      </ModalContent>
    </Modal>
  </BasicPage>
</template>
