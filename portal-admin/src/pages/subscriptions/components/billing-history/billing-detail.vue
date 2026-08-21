<script lang="ts" setup>
import { VisuallyHidden } from 'reka-ui'
import { computed } from 'vue'

import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import { formatTimestamp } from '@/utils/date.utils'

import type { Invoice } from '../../types/invoiceTypes'

import TransactionCard from '../transaction-card/index.vue'

const props = defineProps<{
  billing: Invoice
}>()

const normalizedState = computed(() => {
  const status = props.billing.status?.toLowerCase()
  if (status === 'paid')
    return 'paid'
  if (status === 'failed')
    return 'failed'
  if (status === 'pending')
    return 'pending'
  return 'pending'
})

function formatMoney(amount?: number | string | null) {
  const value = typeof amount === 'string' ? Number(amount) : amount ?? 0
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

const updatedAt = computed(() => props.billing.paidAt || props.billing.updatedAt || props.billing.createdAt)

const billingDescription = computed(() => [
  props.billing.userEmail || 'Unknown user',
  props.billing.paymentMethod || 'N/A',
  props.billing.plan || 'N/A',
  props.billing.billingCycle || 'N/A',
].join(' · '))
</script>

<template>
  <div>
    <ModalHeader>
      <VisuallyHidden as-child>
        <ModalTitle />
      </VisuallyHidden>
      <VisuallyHidden as-child>
        <ModalDescription :aria-describedby="undefined" />
      </VisuallyHidden>
    </ModalHeader>

    <TransactionCard
      :card-no="billing.id.length"
      :order-id="billing.transactionId || billing.id"
      :price="formatMoney(billing.amount)"
      currency="₫"
      :state="normalizedState"
      :updated-at="formatTimestamp(updatedAt)"
      :invoice-no="billing.id"
      :description="billingDescription"
    />
  </div>
</template>
