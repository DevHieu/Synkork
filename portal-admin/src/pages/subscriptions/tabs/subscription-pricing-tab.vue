<script setup lang="ts">
import { LoaderIcon, RefreshCcw, Save } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import NumberField from '@/components/number-field.vue'
import { Button as UiButton } from '@/components/ui/button'

import type { BillingCycle, PaidPlanCode, PlanPricing } from '../types/invoiceTypes'

import { subscriptionService } from '../service/subscriptionService'

const pricingLoading = ref(false)
const savingPriceKey = ref<string | null>(null)
const planPricings = ref<PlanPricing[]>([])
const priceDrafts = ref<Record<string, number | undefined>>({})

const managedPriceCombos: Array<{
  key: string
  plan: PaidPlanCode
  billingCycle: BillingCycle
  cycleLabel: string
}> = [
  { key: 'TEAM-MONTHLY', plan: 'TEAM', billingCycle: 'MONTHLY', cycleLabel: 'Tháng' },
  { key: 'TEAM-YEARLY', plan: 'TEAM', billingCycle: 'YEARLY', cycleLabel: 'Năm' },
  { key: 'BUSINESS-MONTHLY', plan: 'BUSINESS', billingCycle: 'MONTHLY', cycleLabel: 'Tháng' },
  { key: 'BUSINESS-YEARLY', plan: 'BUSINESS', billingCycle: 'YEARLY', cycleLabel: 'Năm' },
]

function formatMoney(amount?: number | string | null) {
  const value = typeof amount === 'string' ? Number(amount) : amount ?? 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function pricingKey(plan: PaidPlanCode, billingCycle: BillingCycle) {
  return `${plan}-${billingCycle}`
}

function getPricing(plan: PaidPlanCode, billingCycle: BillingCycle) {
  return planPricings.value.find(item => item.plan === plan && item.billingCycle === billingCycle)
}

function isDirtyPrice(plan: PaidPlanCode, billingCycle: BillingCycle) {
  const key = pricingKey(plan, billingCycle)
  const current = Number(getPricing(plan, billingCycle)?.amount ?? 0)
  const draft = priceDrafts.value[key]
  return typeof draft === 'number' && Number.isFinite(draft) && draft !== current
}

function syncPriceDrafts() {
  for (const combo of managedPriceCombos) {
    const current = getPricing(combo.plan, combo.billingCycle)
    priceDrafts.value[combo.key] = current ? Number(current.amount) : undefined
  }
}

function planBadgeClass(plan: PaidPlanCode) {
  const base = 'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold '
  if (plan === 'BUSINESS')
    return `${base}border-purple-200 bg-purple-100 text-purple-800 dark:border-purple-800 dark:bg-purple-900/30 dark:text-purple-300`
  return `${base}border-blue-200 bg-blue-100 text-blue-800 dark:border-blue-800 dark:bg-blue-900/30 dark:text-blue-300`
}

async function fetchPlanPricings() {
  pricingLoading.value = true
  try {
    const res = await subscriptionService.getPlanPricings()
    planPricings.value = Array.isArray(res) ? res : res.data || []
    syncPriceDrafts()
  }
  catch (err) {
    console.error('Failed to load plan pricings:', err)
    planPricings.value = []
    toast.error('Không tải được bảng giá gói')
  }
  finally {
    pricingLoading.value = false
  }
}

async function savePlanPrice(plan: PaidPlanCode, billingCycle: BillingCycle) {
  const key = pricingKey(plan, billingCycle)
  const amount = priceDrafts.value[key]

  if (typeof amount !== 'number' || !Number.isFinite(amount) || amount < 0) {
    toast.error('Giá gói phải là số lớn hơn hoặc bằng 0')
    return
  }

  savingPriceKey.value = key
  try {
    const updated = await subscriptionService.updatePlanPricing({ plan, billingCycle, amount })
    const normalized = updated?.data ?? updated
    const index = planPricings.value.findIndex(item => item.plan === plan && item.billingCycle === billingCycle)

    if (index >= 0)
      planPricings.value[index] = normalized
    else
      planPricings.value.push(normalized)

    syncPriceDrafts()
    toast.success('Đã cập nhật giá gói')
  }
  catch (err) {
    console.error('Failed to update plan pricing:', err)
    toast.error('Không cập nhật được giá gói')
  }
  finally {
    savingPriceKey.value = null
  }
}

onMounted(fetchPlanPricings)
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="text-base font-semibold">
          Bảng giá gói
        </h2>
        <p class="text-sm text-muted-foreground">
          Chỉnh giá niêm yết cho TEAM và BUSINESS theo tháng hoặc năm.
        </p>
      </div>

      <UiButton variant="outline" size="sm" class="h-9 gap-2" :disabled="pricingLoading" @click="fetchPlanPricings">
        <RefreshCcw class="h-4 w-4" :class="{ 'animate-spin': pricingLoading }" />
        Tải lại
      </UiButton>
    </div>

    <div class="relative overflow-hidden rounded-md border border-neutral-200 dark:border-neutral-800">
      <div v-if="pricingLoading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <table class="w-full min-w-[760px] caption-bottom text-sm">
          <thead class="border-b bg-muted/40">
            <tr>
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Gói
              </th>
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Chu kỳ
              </th>
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Giá hiện tại
              </th>
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Giá mới
              </th>
              <th class="h-10 px-4 text-right font-medium text-muted-foreground">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="combo in managedPriceCombos" :key="combo.key" class="border-b last:border-0 hover:bg-muted/30">
              <td class="px-4 py-3">
                <span :class="planBadgeClass(combo.plan)">
                  {{ combo.plan }}
                </span>
              </td>
              <td class="px-4 py-3 font-medium">
                {{ combo.cycleLabel }}
              </td>
              <td class="px-4 py-3">
                {{ getPricing(combo.plan, combo.billingCycle) ? formatMoney(getPricing(combo.plan, combo.billingCycle)?.amount) : 'Chưa có giá' }}
              </td>
              <td class="px-4 py-3">
                <div class="flex max-w-xs items-center gap-2">
                  <NumberField
                    v-model="priceDrafts[combo.key]"
                    :min="0"
                    placeholder="Nhập giá"
                    class="w-[180px]"
                  />
                  <span class="whitespace-nowrap text-xs text-muted-foreground">VND</span>
                </div>
              </td>
              <td class="px-4 py-3 text-right">
                <UiButton
                  size="sm"
                  class="h-9 gap-2"
                  :disabled="savingPriceKey === combo.key || pricingLoading || !isDirtyPrice(combo.plan, combo.billingCycle)"
                  @click="savePlanPrice(combo.plan, combo.billingCycle)"
                >
                  <LoaderIcon v-if="savingPriceKey === combo.key" class="h-4 w-4 animate-spin" />
                  <Save v-else class="h-4 w-4" />
                  Lưu
                </UiButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
