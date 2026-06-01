<script setup lang="ts">
import { ref, computed, onMounted, watch, h, shallowRef } from 'vue'
import { LoaderIcon } from '@lucide/vue'

import { BasicPage } from '@/components/global-layout'
import type { TableColumn } from '@/components/base-table.vue'
import { Badge } from '@/components/ui/badge'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Button as UiButton } from '@/components/ui/button'

import type { Billing } from './components/billing-history/data/schema'
import { billingSchema } from './components/billing-history/data/schema'
import billingsData from './components/billing-history/data/billings.json'
import { statuses } from './components/billing-history/data/data'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = 10
const totalCount = ref(billingsData.length)
const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))
const pagedData = ref<Billing[]>([])

const selectedBilling = ref<Billing | null>(null)
const isOpen = ref(false)
const showComponent = shallowRef<Component | null>(null)

async function handleSelectDetail(billing: Billing) {
  try {
    const { default: component } = await import('./components/billing-history/billing-detail.vue')
    showComponent.value = component
    selectedBilling.value = billingSchema.parse(billing)
    isOpen.value = true
  }
  catch (e) {
    console.error('Failed to load billing detail', e)
  }
}


function fetchData() {
  loading.value = true
  setTimeout(() => {
    const start = (currentPage.value - 1) * pageSize
    const end = start + pageSize
    pagedData.value = (billingsData as unknown as Billing[]).slice(start, end)
    loading.value = false
  }, 500)
}

watch(currentPage, () => { fetchData() })
onMounted(() => { fetchData() })

const columns = computed<TableColumn<Billing>[]>(() => [
  { header: 'ID', accessor: 'id', minWidth: 80 },
  { 
    header: 'Amount', 
    accessor: 'amount', 
    minWidth: 100,
    render: (row) => `$${row.amount.toFixed(2)}`
  },
  { header: 'Billing Date', accessor: 'date', minWidth: 150 },
  { header: 'Billing Plan', accessor: 'plan', minWidth: 150 },
  {
    header: 'Status',
    minWidth: 150,
    render: (row) => {
      const status = statuses.find(s => s.value === row.status)
      if (!status) return row.status
      return h(Badge, {
        class: 'flex max-w-[120px] items-center',
        style: { color: status.color },
        variant: 'secondary',
      }, () => [status.icon, h('span', { class: 'ml-2' }, status.label)])
    }
  },
  { 
    header: 'Order ID', 
    accessor: 'orderId', 
    minWidth: 150,
    render: (row) => row.orderId || 'N/A'
  },
  {
  header: 'Actions',
  minWidth: 100,
  render: (row) => h(UiButton, {
    variant: 'ghost',
    class: 'h-8 px-3 text-xs',
    onClick: () => handleSelectDetail(row),
  }, () => 'Xem')
}
])
</script>

<template>
  <BasicPage title="Plans & Billing" description="Manage your plan and billing history here.">
    <div class="mt-10 relative">
      <h3 class="text-lg font-medium mb-4">Billing History</h3>

      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <BaseTable :columns="columns" :data="pagedData" />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>

    <Modal v-model:open="isOpen">
      <ModalContent>
        <component :is="showComponent" :billing="selectedBilling" />
      </ModalContent>
    </Modal>
  </BasicPage>
</template>