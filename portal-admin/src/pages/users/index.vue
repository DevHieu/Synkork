<script setup lang="ts">
import { Eye, LoaderIcon, Search, Trash2 } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { defaultDateRange, formatToISODateTime } from '@/utils/date.utils'

import type { User, UserParams, UserPlan, UserStatus } from './types/userTypes'

import UserCreate from './components/user-create.vue'
import UserDelete from './components/user-delete.vue'
import UserResource from './components/user-resource.vue'
import { userService } from './services/userService'

const loading = ref(false)
const allUsers = ref<User[]>([])
const keyword = ref('')
const dateRange = ref(defaultDateRange())
const selectedStatus = ref<string>('ALL')
const selectedPlan = ref<string>('ALL')
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)
const totalPage = ref(0)

const debounceKeyword = refDebounced(keyword, 500)

const editTarget = ref<User | null>(null)
const deleteTarget = ref<User | null>(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
  { value: 'BANNED', label: 'Bị khóa' },
] as const

const statusLabels: Record<UserStatus, string> = {
  ACTIVE: 'Hoạt động',
  INACTIVE: 'Ngừng hoạt động',
  BANNED: 'Bị khóa',
}

const planOptions = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'FREE', label: 'FREE' },
  { value: 'TEAM', label: 'TEAM' },
  { value: 'BUSINESS', label: 'BUSINESS' },
] as const

async function fetchData() {
  loading.value = true
  try {
    const queryParams: UserParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (debounceKeyword.value.trim()) {
      queryParams.search = debounceKeyword.value.trim()
    }

    if (selectedStatus.value !== 'ALL') {
      queryParams.status = selectedStatus.value as UserStatus
    }

    if (selectedPlan.value !== 'ALL') {
      queryParams.plan = selectedPlan.value as UserPlan
    }

    if (dateRange.value?.from) {
      const fromDate = typeof dateRange.value.from === 'string' ? new Date(dateRange.value.from) : dateRange.value.from
      queryParams.dateFrom = formatToISODateTime(fromDate)
    }

    if (dateRange.value?.to) {
      const toDate = typeof dateRange.value.to === 'string' ? new Date(dateRange.value.to) : dateRange.value.to
      queryParams.dateTo = formatToISODateTime(toDate)
    }

    const response = await userService.getAll({ params: queryParams })

    allUsers.value = response.data || []
    totalCount.value = response.meta?.totalElements || 0
    totalPage.value = response.meta?.totalPages || 0
  }
  catch (err) {
    console.error('Failed to fetch users:', err)
    allUsers.value = []
    totalCount.value = 0
    totalPage.value = 0
  }
  finally {
    loading.value = false
  }
}

watch(currentPage, () => {
  fetchData()
})

watch([debounceKeyword, selectedStatus, selectedPlan, dateRange], () => {
  currentPage.value = 1
  fetchData()
})

onMounted(() => {
  fetchData()
})

function handleViewDetail(user: User) {
  editTarget.value = user
  showEditModal.value = true
}

function handleDelete(user: User) {
  deleteTarget.value = user
  showDeleteModal.value = true
}

function onUserSaved() {
  showEditModal.value = false
  fetchData()
}

function onUserDeleted() {
  showDeleteModal.value = false
  fetchData()
}

const columns = computed<TableColumn<User>[]>(() => [
  { header: 'Username', accessor: 'username', minWidth: 150 },
  {
    header: 'Họ tên',
    render: row => `${row.displayName}`,
    minWidth: 180,
  },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Plan',
    minWidth: 120,
    render: (row) => {
      const plan = row.plan?.toUpperCase() || 'FREE'
      let badgeClass = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold '
      if (plan === 'BUSINESS')
        badgeClass += 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300 border border-purple-200 dark:border-purple-800'
      else if (plan === 'TEAM')
        badgeClass += 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300 border border-blue-200 dark:border-blue-800'
      else
        badgeClass += 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border border-slate-200 dark:border-slate-700'

      return h('span', { class: `${badgeClass}` }, plan)
    },
  },
  {
    header: 'Trạng thái',
    minWidth: 120,
    render: (row) => {
      const status = (row.status?.toUpperCase() || 'INACTIVE') as UserStatus
      let badgeClass = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold '
      if (status === 'ACTIVE')
        badgeClass += 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800'
      else if (status === 'BANNED')
        badgeClass += 'bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-300 border border-rose-200 dark:border-rose-800'
      else if (status === 'INACTIVE')
        badgeClass += 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-300 border border-slate-200 dark:border-slate-700'
      else
        badgeClass += 'bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300 border border-amber-200 dark:border-amber-800'

      return h('span', { class: badgeClass }, statusLabels[status] ?? status)
    },
  },
  {
    header: 'Thao tác',
    minWidth: 180,
    render: row => h('div', { class: 'flex justify-center gap-1.5' }, [
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs',
        onClick: () => handleViewDetail(row),
      }, () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'Chi tiết']),
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
        onClick: () => handleDelete(row),
      }, () => [h(Trash2, { class: 'h-3.5 w-3.5' }), 'Khóa']),
    ]),
  },
])
</script>

<template>
  <BasicPage
    title="Quản lý user"
    description="Quản lý và theo dõi danh sách người dùng trong hệ thống"
    sticky
  >
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="keyword"
          type="text"
          placeholder="Tìm theo email, tên của user..."
          class="pl-8 h-9"
        />
      </div>

      <div class="w-[160px]">
        <UiSelect v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div class="w-[160px]">
        <UiSelect v-model="selectedPlan">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Gói dịch vụ" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="opt in planOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div>
        <DateRangePicker v-model="dateRange" />
      </div>

      <!-- Button tạo mới user -->
      <div class="ml-auto">
        <UserCreate @saved="fetchData" />
      </div>
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <!-- Loading Overlay -->
      <div v-if="loading" class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50">
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable
          :columns="columns"
          :data="allUsers"
        />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <!-- Edit Modal -->
  <Modal v-model:open="showEditModal">
    <ModalContent>
      <UserResource
        :user="editTarget ?? undefined"
        @close="showEditModal = false"
        @saved="onUserSaved"
      />
    </ModalContent>
  </Modal>

  <!-- Modal khóa user -->
  <Modal v-model:open="showDeleteModal">
    <ModalContent>
      <UserDelete
        v-if="deleteTarget"
        :user="deleteTarget"
        @remove="onUserDeleted"
      />
    </ModalContent>
  </Modal>
</template>
