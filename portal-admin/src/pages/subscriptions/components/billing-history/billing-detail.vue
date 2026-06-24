<script lang="ts" setup>
import { computed } from 'vue'
import { VisuallyHidden } from 'reka-ui'

import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import { formatTimestamp } from '@/utils/date.utils'

import type { Invoice } from '../../types/invoiceTypes'

import TransactionCard from '../transaction-card/index.vue'

const props = defineProps<{
  billing: Invoice
}>()

const normalizedState = computed(() => {
  const status = props.billing.status?.toLowerCase()
  if (status === 'paid') return 'paid'
  if (status === 'failed') return 'cancelled'
  if (status === 'cancelled') return 'cancelled'
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
      :description="`${billing.userEmail || 'Unknown user'} · ${billing.paymentMethod || 'N/A'} · ${billing.plan || 'N/A'}`"
    />
  </div>
</template>
