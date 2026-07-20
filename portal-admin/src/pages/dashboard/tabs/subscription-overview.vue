<script setup lang="ts">
import { AlertCircle, CheckCircle2, Clock, DollarSign, PackagePlus, Percent, PieChart } from '@lucide/vue'
import { VisDonut, VisSingleContainer } from '@unovis/vue'
import dayjs from 'dayjs'
import { computed, onMounted, ref, watch } from 'vue'

import type { AppDateRange } from '@/types/Date'

import DateRangePicker from '@/components/date-range-picker.vue'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatToISODateTime } from '@/utils/date.utils'

import DataCard from '../components/overview/data-card.vue'
import { dashboardService } from '../services/dashboardService'

const isLoadingStats = ref(false)
const isLoadingChart = ref(false)
const statsData = ref<SubscriptionStats | null>(null)
const chartData = ref<SubscriptionChart | null>(null)
const dateRange = ref<AppDateRange>(null)

interface SubscriptionStats {
  totalRevenue: number | string | null
  newSubscriptions: number
  renewalRate: number | string
  pendingInvoices: number
  paidInvoices: number
  failedInvoices: number
}

interface SubscriptionChart {
  teamSubscriptions: number
  businessSubscriptions: number
}

interface PlanDistributionRow {
  name: string
  value: number
  color: string
}

const rangeParams = computed(() => {
  if (!dateRange.value)
    return undefined

  return {
    dateFrom: formatToISODateTime(dateRange.value.from),
    dateTo: formatToISODateTime(dateRange.value.to, true),
  }
})

const rangeLabel = computed(() => {
  if (!dateRange.value)
    return 'Tất cả thời gian'

  return `${dayjs(dateRange.value.from).format('DD/MM/YYYY')} - ${dayjs(dateRange.value.to).format('DD/MM/YYYY')}`
})

async function fetchSubscriptionStats() {
  isLoadingStats.value = true
  try {
    statsData.value = await dashboardService.getSubscriptionStatData(rangeParams.value)
  }
  catch (err) {
    console.error('Error fetching subscription stats:', err)
    statsData.value = null
  }
  finally {
    isLoadingStats.value = false
  }
}

async function fetchSubscriptionChart() {
  isLoadingChart.value = true
  try {
    chartData.value = await dashboardService.getSubscriptionChartData(rangeParams.value)
  }
  catch (err) {
    console.error('Error fetching subscription chart:', err)
    chartData.value = null
  }
  finally {
    isLoadingChart.value = false
  }
}

function fetchSubscriptionData() {
  void Promise.all([
    fetchSubscriptionStats(),
    fetchSubscriptionChart(),
  ])
}

const planDistributionRows = computed<PlanDistributionRow[]>(() => [
  { name: 'TEAM', value: chartData.value?.teamSubscriptions ?? 0, color: 'var(--chart-2)' },
  { name: 'BUSINESS', value: chartData.value?.businessSubscriptions ?? 0, color: 'var(--chart-3)' },
])

const totalPaidPlans = computed(() =>
  planDistributionRows.value.reduce((total, row) => total + row.value, 0),
)

function getPlanValue(row: PlanDistributionRow) {
  return row.value
}

function getPlanColor(row: PlanDistributionRow) {
  return row.color
}

function getPlanPercent(value: number) {
  if (totalPaidPlans.value === 0)
    return '0%'

  return `${Math.round((value / totalPaidPlans.value) * 100)}%`
}

onMounted(() => {
  fetchSubscriptionData()
})

watch(dateRange, () => {
  fetchSubscriptionData()
})

function formatMoney(amount?: number | string | null) {
  const value = typeof amount === 'string' ? Number(amount) : amount ?? 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function formatPercent(value?: number | string | null) {
  const numericValue = typeof value === 'string' ? Number(value) : value ?? 0
  return `${numericValue.toFixed(1)}%`
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Thống kê gói đăng ký
        </h2>
        <p class="text-sm text-muted-foreground">
          Dữ liệu đơn mua gói trong khoảng: {{ rangeLabel }}
        </p>
      </div>

      <div class="w-full sm:w-[280px]">
        <DateRangePicker v-model="dateRange" />
      </div>
    </div>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      <DataCard
        title="Doanh thu"
        :data="isLoadingStats ? '-' : formatMoney(statsData?.totalRevenue)"
        :icon="DollarSign"
      />
      <DataCard
        title="Gói mới"
        :data="isLoadingStats ? '-' : statsData?.newSubscriptions?.toLocaleString() ?? '-'"
        :icon="PackagePlus"
      />
      <DataCard
        title="Tỷ lệ gia hạn"
        :data="isLoadingStats ? '-' : formatPercent(statsData?.renewalRate)"
        :icon="Percent"
      />
    </div>

    <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
      <Card class="col-span-1 lg:col-span-3">
        <CardHeader>
          <CardTitle>Invoice Status</CardTitle>
          <CardDescription>
            Status distribution in selected range
          </CardDescription>
        </CardHeader>
        <CardContent class="grid gap-4">
          <div class="flex items-center gap-4 rounded-lg border border-green-500/10 bg-green-500/5 p-3">
            <CheckCircle2 class="h-8 w-8 text-green-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">
                Paid (PAID)
              </div>
              <div class="text-2xl font-bold text-green-500">
                {{ statsData?.paidInvoices ?? 0 }}
              </div>
            </div>
          </div>
          <div class="flex items-center gap-4 rounded-lg border border-orange-500/10 bg-orange-500/5 p-3">
            <Clock class="h-8 w-8 text-orange-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">
                Pending (PENDING)
              </div>
              <div class="text-2xl font-bold text-orange-500">
                {{ statsData?.pendingInvoices ?? 0 }}
              </div>
            </div>
          </div>
          <div class="flex items-center gap-4 rounded-lg border border-red-500/10 bg-red-500/5 p-3">
            <AlertCircle class="h-8 w-8 text-red-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">
                Failed (FAILED)
              </div>
              <div class="text-2xl font-bold text-red-500">
                {{ statsData?.failedInvoices ?? 0 }}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="col-span-1 lg:col-span-4">
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <PieChart class="h-4 w-4 text-muted-foreground" />
            Phân bổ gói
          </CardTitle>
          <CardDescription>
            Số lượng gói TEAM và BUSINESS trong khoảng đã chọn
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div v-if="isLoadingChart" class="flex h-40 items-center justify-center">
            <div class="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          </div>
          <div v-else-if="totalPaidPlans === 0" class="flex h-56 items-center justify-center rounded-lg border border-dashed text-sm text-muted-foreground">
            Chưa có gói đăng ký
          </div>
          <div v-else class="grid gap-6 sm:grid-cols-[1fr_auto] sm:items-center">
            <div class="h-[260px]">
              <VisSingleContainer :data="planDistributionRows" class="h-full">
                <VisDonut
                  :value="getPlanValue"
                  :color="getPlanColor"
                  :arc-width="36"
                  :corner-radius="6"
                  :pad-angle="0.04"
                  central-label="Tổng"
                  :central-sub-label="totalPaidPlans.toLocaleString()"
                />
              </VisSingleContainer>
            </div>

            <div class="space-y-4">
              <div
                v-for="row in planDistributionRows"
                :key="row.name"
                class="flex min-w-44 items-center justify-between gap-8 rounded-lg border border-border/60 p-3"
              >
                <div class="flex items-center gap-2">
                  <span class="h-2.5 w-2.5 rounded-full" :style="{ backgroundColor: row.color }" />
                  <span class="text-sm font-medium">{{ row.name }}</span>
                </div>
                <div class="text-right">
                  <div class="text-sm font-semibold">
                    {{ row.value.toLocaleString() }}
                  </div>
                  <div class="text-xs text-muted-foreground">
                    {{ getPlanPercent(row.value) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
