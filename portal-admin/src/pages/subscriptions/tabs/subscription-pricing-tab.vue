<script setup lang="ts">
import { LoaderIcon, RefreshCcw, Save } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import NumberField from '@/components/number-field.vue'
import { Button as UiButton } from '@/components/ui/button'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'

import type { BillingCycle, DiscountType, PaidPlanCode, PlanPricing } from '../types/invoiceTypes'

import { subscriptionService } from '../service/subscriptionService'

const pricingLoading = ref(false)
const savingPriceKey = ref<string | null>(null)
const planPricings = ref<PlanPricing[]>([])
const priceDrafts = ref<Record<string, number | undefined>>({})
const discountTypeDrafts = ref<Record<string, DiscountDraftType>>({})
const discountValueDrafts = ref<Record<string, number | undefined>>({})

type DiscountDraftType = DiscountType | 'NONE'

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

function normalizeNumber(value?: number | string | null) {
  if (value === null || value === undefined || value === '')
    return undefined

  const numberValue = typeof value === 'string' ? Number(value) : value
  return Number.isFinite(numberValue) ? numberValue : undefined
}

function currentDiscountType(pricing?: PlanPricing): DiscountDraftType {
  return pricing?.discountType ?? 'NONE'
}

function currentDiscountValue(pricing?: PlanPricing) {
  return pricing?.discountType ? normalizeNumber(pricing.discountValue) : undefined
}

function calculateDiscountAmount(amount?: number, discountType?: DiscountDraftType, discountValue?: number) {
  if (typeof amount !== 'number' || !Number.isFinite(amount) || amount <= 0)
    return 0

  if (!discountType || discountType === 'NONE' || typeof discountValue !== 'number' || !Number.isFinite(discountValue))
    return 0

  const discountAmount = discountType === 'PERCENTAGE'
    ? Math.round(amount * discountValue / 100)
    : discountValue

  return Math.min(Math.max(discountAmount, 0), amount)
}

function getDraftFinalAmount(key: string) {
  const amount = priceDrafts.value[key] ?? 0
  const discountAmount = calculateDiscountAmount(
    amount,
    discountTypeDrafts.value[key],
    discountValueDrafts.value[key],
  )

  return Math.max(amount - discountAmount, 0)
}

function formatDiscount(pricing?: PlanPricing) {
  if (!pricing?.discountType || !normalizeNumber(pricing.discountValue))
    return 'Không giảm'

  const discountValue = normalizeNumber(pricing.discountValue) ?? 0
  if (pricing.discountType === 'PERCENTAGE')
    return `${discountValue}% (${formatMoney(pricing.discountAmount)})`

  return formatMoney(discountValue)
}

function isDirtyPrice(plan: PaidPlanCode, billingCycle: BillingCycle) {
  const key = pricingKey(plan, billingCycle)
  const currentPricing = getPricing(plan, billingCycle)
  const current = Number(currentPricing?.amount ?? 0)
  const draft = priceDrafts.value[key]
  const currentType = currentDiscountType(currentPricing)
  const draftType = discountTypeDrafts.value[key] ?? 'NONE'
  const currentValue = currentDiscountValue(currentPricing)
  const draftValue = draftType === 'NONE' ? undefined : discountValueDrafts.value[key]

  return (
    typeof draft === 'number'
    && Number.isFinite(draft)
    && (
      draft !== current
      || draftType !== currentType
      || (draftValue ?? undefined) !== (currentValue ?? undefined)
    )
  )
}

function syncPriceDrafts() {
  for (const combo of managedPriceCombos) {
    const current = getPricing(combo.plan, combo.billingCycle)
    priceDrafts.value[combo.key] = current ? Number(current.amount) : undefined
    discountTypeDrafts.value[combo.key] = currentDiscountType(current)
    discountValueDrafts.value[combo.key] = currentDiscountValue(current)
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
  const discountType = discountTypeDrafts.value[key] ?? 'NONE'
  const discountValue = discountValueDrafts.value[key]

  if (typeof amount !== 'number' || !Number.isFinite(amount) || amount < 0) {
    toast.error('Giá gói phải là số lớn hơn hoặc bằng 0')
    return
  }

  if (discountType !== 'NONE') {
    if (typeof discountValue !== 'number' || !Number.isFinite(discountValue) || discountValue < 0) {
      toast.error('Giá trị giảm giá phải là số lớn hơn hoặc bằng 0')
      return
    }

    if (discountType === 'PERCENTAGE' && discountValue > 100) {
      toast.error('Giảm giá phần trăm không được vượt quá 100%')
      return
    }
  }

  savingPriceKey.value = key
  try {
    const updated = await subscriptionService.updatePlanPricing({
      plan,
      billingCycle,
      amount,
      discountType: discountType === 'NONE' ? null : discountType,
      discountValue: discountType === 'NONE' ? null : discountValue!,
    })
    const normalized = updated?.data ?? updated
    const index = planPricings.value.findIndex(item => item.plan === plan && item.billingCycle === billingCycle)

    if (index >= 0)
      planPricings.value[index] = normalized
    else
      planPricings.value.push(normalized)

    syncPriceDrafts()
    toast.success('Đã cập nhật giá và giảm giá gói')
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
          Chỉnh giá niêm yết và giảm giá cho TEAM và BUSINESS theo tháng hoặc năm.
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
        <table class="w-full min-w-[1120px] caption-bottom text-sm">
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
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Giảm giá
              </th>
              <th class="h-10 px-4 text-left font-medium text-muted-foreground">
                Sau giảm
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
                <div v-if="getPricing(combo.plan, combo.billingCycle)" class="space-y-1">
                  <div class="font-medium">
                    {{ formatMoney(getPricing(combo.plan, combo.billingCycle)?.amount) }}
                  </div>
                  <div class="text-xs text-muted-foreground">
                    Giảm: {{ formatDiscount(getPricing(combo.plan, combo.billingCycle)) }}
                  </div>
                  <div class="text-xs font-medium text-emerald-600 dark:text-emerald-400">
                    Sau giảm: {{ formatMoney(getPricing(combo.plan, combo.billingCycle)?.finalAmount ?? getPricing(combo.plan, combo.billingCycle)?.amount) }}
                  </div>
                </div>
                <span v-else>Chưa có giá</span>
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
              <td class="px-4 py-3">
                <div class="flex min-w-[320px] items-center gap-2">
                  <UiSelect v-model="discountTypeDrafts[combo.key]">
                    <SelectTrigger class="h-9 w-[128px] bg-background">
                      <SelectValue placeholder="Kiểu giảm" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="NONE">
                        Không giảm
                      </SelectItem>
                      <SelectItem value="PERCENTAGE">
                        Theo %
                      </SelectItem>
                      <SelectItem value="FIXED">
                        Số tiền
                      </SelectItem>
                    </SelectContent>
                  </UiSelect>

                  <NumberField
                    v-model="discountValueDrafts[combo.key]"
                    :min="0"
                    :max="discountTypeDrafts[combo.key] === 'PERCENTAGE' ? 100 : undefined"
                    :placeholder="discountTypeDrafts[combo.key] === 'PERCENTAGE' ? 'Nhập %' : 'Nhập số tiền'"
                    :class="discountTypeDrafts[combo.key] === 'NONE' ? 'w-[160px] opacity-50' : 'w-[160px]'"
                  />
                  <span class="w-8 text-xs text-muted-foreground">
                    {{ discountTypeDrafts[combo.key] === 'PERCENTAGE' ? '%' : 'VND' }}
                  </span>
                </div>
              </td>
              <td class="px-4 py-3 font-medium text-emerald-600 dark:text-emerald-400">
                {{ formatMoney(getDraftFinalAmount(combo.key)) }}
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
