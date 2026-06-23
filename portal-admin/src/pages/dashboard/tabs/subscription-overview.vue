<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CreditCard, DollarSign, Users, Clock, CheckCircle2, AlertCircle, ArrowRight, TrendingUp } from '@lucide/vue'
import { dashboardService } from '../services/dashboardService'
import DataCard from '../components/data-card.vue'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import dayjs from 'dayjs'

const loading = ref(false)
const subData = ref<any>(null)

async function fetchSubscriptionData() {
  loading.value = true
  try {
    subData.value = await dashboardService.getSubscriptionDashboardData()
  } catch (err) {
    console.error('Lỗi khi lấy dữ liệu subscription dashboard:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSubscriptionData()
})

function formatMoney(amount?: number | string | null) {
  const value = typeof amount === 'string' ? Number(amount) : amount ?? 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function formatDate(dateStr?: string | null) {
  if (!dateStr) return '—'
  return dayjs(dateStr).format('DD/MM/YYYY HH:mm')
}

function statusMeta(status?: string | null) {
  const normalized = (status || 'PENDING').toUpperCase()
  if (normalized === 'PAID') return { label: 'Paid', color: 'text-green-500 bg-green-500/10 border-green-500/20' }
  if (normalized === 'FAILED') return { label: 'Failed', color: 'text-red-500 bg-red-500/10 border-red-500/20' }
  return { label: 'Pending', color: 'text-orange-500 bg-orange-500/10 border-orange-500/20' }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Top Stats Cards -->
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <DataCard
        title="Tổng doanh thu"
        :data="formatMoney(subData?.totalRevenue)"
        :icon="DollarSign"
      />
      <DataCard
        title="Doanh thu tháng này"
        :data="formatMoney(subData?.revenueThisMonth)"
        :icon="TrendingUp"
      />
      <DataCard
        title="Gói đang hoạt động"
        :data="subData?.activeSubscriptions?.toLocaleString() ?? '—'"
        :icon="Users"
      />
    </div>

    <!-- Middle Summary Grid -->
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-7">
      <!-- Status Distribution Card -->
      <Card class="col-span-1 lg:col-span-3">
        <CardHeader>
          <CardTitle>Trạng thái hóa đơn</CardTitle>
          <CardDescription>
            Phân bố trạng thái của tất cả giao dịch thanh toán
          </CardDescription>
        </CardHeader>
        <CardContent class="grid gap-4">
          <div class="flex items-center gap-4 p-3 rounded-lg border border-green-500/10 bg-green-500/5">
            <CheckCircle2 class="h-8 w-8 text-green-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">Đã thanh toán (PAID)</div>
              <div class="text-2xl font-bold text-green-500">{{ subData?.paidInvoices ?? 0 }}</div>
            </div>
          </div>
          <div class="flex items-center gap-4 p-3 rounded-lg border border-orange-500/10 bg-orange-500/5">
            <Clock class="h-8 w-8 text-orange-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">Chờ thanh toán (PENDING)</div>
              <div class="text-2xl font-bold text-orange-500">{{ subData?.pendingInvoices ?? 0 }}</div>
            </div>
          </div>
          <div class="flex items-center gap-4 p-3 rounded-lg border border-red-500/10 bg-red-500/5">
            <AlertCircle class="h-8 w-8 text-red-500" />
            <div class="flex-1">
              <div class="text-sm font-medium text-muted-foreground">Thất bại (FAILED)</div>
              <div class="text-2xl font-bold text-red-500">{{ subData?.failedInvoices ?? 0 }}</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Recent Transactions Card -->
      <Card class="col-span-1 lg:col-span-4">
        <CardHeader class="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Giao dịch gần đây</CardTitle>
            <CardDescription>5 giao dịch mua gói dịch vụ mới nhất</CardDescription>
          </div>
          <router-link to="/subscriptions" class="inline-flex items-center gap-1 text-xs text-primary hover:underline">
            Xem tất cả <ArrowRight class="h-3 w-3" />
          </router-link>
        </CardHeader>
        <CardContent>
          <div v-if="loading" class="flex h-40 items-center justify-center">
            <div class="h-6 w-6 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          </div>
          <div v-else-if="!subData?.recentTransactions || subData.recentTransactions.length === 0" class="flex h-40 items-center justify-center text-sm text-muted-foreground">
            Chưa có giao dịch nào
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="tx in subData.recentTransactions"
              :key="tx.id"
              class="flex items-center justify-between p-3 rounded-lg border border-border/50 hover:bg-muted/30 transition-all duration-200"
            >
              <div class="flex flex-col gap-1">
                <span class="text-sm font-medium">{{ tx.username || 'N/A' }}</span>
                <span class="text-xs text-muted-foreground">{{ tx.userEmail }}</span>
                <div class="flex items-center gap-2 mt-1">
                  <Badge variant="outline" class="text-[10px] py-0 px-1.5 uppercase font-semibold">
                    {{ tx.plan || 'FREE' }}
                  </Badge>
                  <span class="text-[10px] text-muted-foreground">
                    {{ tx.paymentMethod || 'BANK' }}
                  </span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-2">
                <span class="text-sm font-bold text-foreground">
                  {{ formatMoney(tx.amount) }}
                </span>
                <span class="text-[10px] font-semibold text-muted-foreground">
                  {{ formatDate(tx.createdAt) }}
                </span>
                <span
                  class="text-[10px] px-2 py-0.5 rounded-full border font-semibold"
                  :class="statusMeta(tx.status).color"
                >
                  {{ statusMeta(tx.status).label }}
                </span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
