<script setup lang="ts">
import { Eye, LoaderIcon, Search, Trash2 } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import { defaultDateRange, formatToISODateTime } from '@/utils/date.utils.ts'

import type { User, UserParams, UserPlan, UserStatus } from './types/userTypes.ts'

import UserDelete from './components/user-delete.vue'
import UserResource from './components/user-resource.vue'
import { userService } from './services/userService.ts'

const loading = ref(false)
const allUsers = ref<User[]>([])
const keyword = ref('')
const dateRange = ref(defaultDateRange())
const selectedStatus = ref<string>('ALL')
const selectedPlan = ref<string>('ALL')
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)

const debounceKeyword = refDebounced(keyword, 500)
const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))

const editTarget = ref<User | null>(null)
const deleteTarget = ref<User | null>(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
  { value: 'BANNED', label: 'Bị khóa' },
] as const

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
      queryParams.keyword = keyword.value.trim()
    }

    if (selectedStatus.value !== 'ALL') {
      queryParams.status = selectedStatus.value as UserStatus
    }

    if (selectedPlan.value !== 'ALL') {
      queryParams.plan = selectedPlan.value as UserPlan
    }

    if (dateRange.value?.from) {
      const fromDate = typeof dateRange.value.from === 'string' ? new Date(dateRange.value.from) : dateRange.value.from
      queryParams.fromDate = formatToISODateTime(fromDate)
    }

    if (dateRange.value?.to) {
      const toDate = typeof dateRange.value.to === 'string' ? new Date(dateRange.value.to) : dateRange.value.to
      queryParams.toDate = formatToISODateTime(toDate, true)

      const response = await userService.getAll({ params: queryParams })
      allUsers.value = response.content || []
      totalCount.value = response.totalElements || 0
    }
  }
  catch (err) {
    console.error('Failed to fetch users:', err)
    allUsers.value = []
    totalCount.value = 0
  }
  finally {
    loading.value = false
  }
}

watch(currentPage, () => fetchData())

watch([debounceKeyword, dateRange, selectedStatus, selectedPlan], () => {
  fetchData()
})

onMounted(() => fetchData())

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

function renderRole(role: string) {
  const label = role
    ? role.charAt(0).toUpperCase() + role.slice(1).toLowerCase()
    : '-'

  return h(Badge, {
    variant: 'secondary',
    class: 'border-0 bg-neutral-100 px-3 text-neutral-950 dark:bg-neutral-800 dark:text-neutral-50',
  }, () => label)
}

function renderStatus(status: UserStatus) {
  const normalizedStatus = status?.toUpperCase() as UserStatus
  const config = {
    ACTIVE: {
      label: 'Hoạt động',
      class: 'border-emerald-300 bg-emerald-50 px-3 text-emerald-700 dark:border-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
    },
    INACTIVE: {
      label: 'Ngừng hoạt động',
      class: 'border-amber-300 bg-amber-50 px-3 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300',
    },
    BANNED: {
      label: 'Bị khóa',
      class: 'border-red-300 bg-red-50 px-3 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-300',
    },
  }[normalizedStatus] ?? {
    label: status || '-',
    class: 'border-neutral-300 bg-neutral-50 px-3 text-neutral-700 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-300',
  }

  return h(Badge, {
    variant: 'outline',
    class: config.class,
  }, () => config.label)
}

const columns = computed<TableColumn<User>[]>(() => [
  { header: 'ID', accessor: 'id', minWidth: 100 },
  { header: 'Username', accessor: 'username', minWidth: 150 },
  {
    header: 'Full Name',
    render: row => `${row.displayName}`,
    minWidth: 180,
  },
  {
    header: 'Role',
    minWidth: 120,
    render: row => renderRole(row.role),
  },
  {
    header: 'Status',
    minWidth: 150,
    render: row => renderStatus(row.status),
  },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Actions',
    minWidth: 160,
    render: row => h('div', { class: 'flex gap-1' }, [
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs',
        onClick: () => handleViewDetail(row),
      }, () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'View Detail']),
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10',
        onClick: () => handleDelete(row),
      }, () => [h(Trash2, { class: 'h-3.5 w-3.5' }), 'Delete']),
    ]),
  },
])
</script>

<template>
  <BasicPage
    title="System Log"
    description="Quản lý và theo dõi nhật ký hoạt động hệ thống"
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
            <SelectValue placeholder="Trạng thái" />
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

  <!-- Delete Modal -->
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
