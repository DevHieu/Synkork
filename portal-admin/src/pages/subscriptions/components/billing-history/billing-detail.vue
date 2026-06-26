<script lang="ts" setup>
import { VisuallyHidden } from 'reka-ui'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import { formatTimestamp } from '@/utils/date.utils'

import type { Invoice } from '../../types/invoiceTypes'

import TransactionCard from '../transaction-card/index.vue'

const props = defineProps<{
  billing: Invoice
}>()

const { t } = useI18n()

function planLabel(plan?: string | null) {
  const normalized = (plan || '').toUpperCase()
  if (normalized === 'FREE')
    return t('subscriptions.planFree')
  if (normalized === 'TEAM')
    return t('subscriptions.planTeam')
  if (normalized === 'BUSINESS')
    return t('subscriptions.planBusiness')
  return plan || 'N/A'
}

function paymentMethodLabel(method?: string | null) {
  const normalized = (method || '').toUpperCase()
  if (normalized === 'MOMO')
    return t('subscriptions.methodMomo')
  if (normalized === 'VNPAY')
    return t('subscriptions.methodVnpay')
  if (normalized === 'BANK_TRANSFER')
    return t('subscriptions.methodBankTransfer')
  return method || 'N/A'
}

const normalizedState = computed(() => {
  const status = props.billing.status?.toLowerCase()
  if (status === 'paid')
    return 'paid'
  if (status === 'failed')
    return 'cancelled'
  if (status === 'cancelled')
    return 'cancelled'
  return 'unpaid'
})

const updatedAt = computed(() => props.billing.paidAt || props.billing.updatedAt || props.billing.createdAt)
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
      :price="billing.amount"
      currency="₫"
      :state="normalizedState"
      :updated-at="formatTimestamp(updatedAt)"
      :invoice-no="billing.id"
      :description="`${billing.userEmail || 'Không rõ email'} · ${paymentMethodLabel(billing.paymentMethod)} · ${planLabel(billing.plan)}`"
    />
  </div>
</template>
