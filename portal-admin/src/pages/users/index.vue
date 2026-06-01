<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue'
import { LoaderIcon, Eye } from '@lucide/vue'

import { BasicPage } from '@/components/global-layout'
import type { TableColumn } from '@/components/base-table.vue'
import { Button as UiButton } from '@/components/ui/button'

import UserCreate from './components/user-create.vue'
import UserInvite from './components/user-invite.vue'
import type { User } from './data/schema'
import { users as allUsers } from './data/users'
import DateRangePicker from '@/components/date-range-picker.vue'

const loading = ref(false)
const dateRange = ref();
const currentPage = ref(1)
const pageSize = 10
const totalCount = ref(allUsers.length)
const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))

const pagedData = ref<User[]>([])

watch(dateRange, (newDate) => {
  console.log('Selected date range:', newDate);
})

function fetchData() {
  loading.value = true
  // Mock API call simulation
  setTimeout(() => {
    const start = (currentPage.value - 1) * pageSize
    const end = start + pageSize
    pagedData.value = allUsers.slice(start, end)
    loading.value = false
  }, 500)
}

watch(currentPage, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})

function handleViewDetail(user: User) {
  console.log('View user detail:', user)
}

const columns = computed<TableColumn<User>[]>(() => [
  { header: 'ID', accessor: 'id', minWidth: 100 },
  { header: 'Username', accessor: 'username', minWidth: 150 },
  {
    header: 'Full Name',
    render: (row) => `${row.firstName} ${row.lastName}`,
    minWidth: 180,
  },
  { header: 'Role', accessor: 'role', minWidth: 120 },
  { header: 'Status', accessor: 'status', minWidth: 120 },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Actions',
    minWidth: 140,
    render: (row) => h(UiButton, {
      variant: 'outline',
      size: 'sm',
      class: 'h-8 gap-1 px-2 text-xs',
      onClick: () => handleViewDetail(row),
    }, () => [
      h(Eye, { class: 'h-3.5 w-3.5' }),
      'View Detail',
    ]),
  },
])
</script>

<template>
  <BasicPage
    title="Users"
    description="Users description"
    sticky
  >
    <template #actions>
      <UserInvite />
      <UserCreate />
      <DateRangePicker v-model="dateRange"  />
    </template>

    <div class="relative">
      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <BaseTable
        :columns="columns"
        :data="pagedData"
      />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>
</template>
