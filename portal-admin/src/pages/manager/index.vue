<script setup lang="ts">
import { LoaderIcon, LockKeyhole, Pencil, Plus, Search, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import type { TableColumn } from '@/components/base-table.vue'

import { BasicPage } from '@/components/global-layout'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import {
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type {
  ManagementRole,
  ManagerAccount,
  ManagerParams,
  ManagerStatus,
} from './types/managerTypes'

import ManagerLock from './components/manager-lock.vue'
import ManagerResource from './components/manager-resource.vue'
import { managerService } from './services/managerService'

const loading = ref(false)
const accounts = ref<ManagerAccount[]>([])
const keyword = ref('')
const selectedStatus = ref('ALL')
const selectedRole = ref('ALL')
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)
const totalPage = ref(0)
const dateRange = ref(defaultDateRange())

const debouncedKeyword = refDebounced(keyword, 400)

const editTarget = ref<ManagerAccount>()
const lockTarget = ref<ManagerAccount>()
const showResourceModal = ref(false)
const showLockModal = ref(false)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'active', label: 'Hoạt động' },
  { value: 'inactive', label: 'Ngừng hoạt động' },
  { value: 'banned', label: 'Bị khóa' },
] as const

const roleOptions = [
  { value: 'ALL', label: 'Tất cả vai trò' },
  { value: 'admin', label: 'Quản trị viên' },
  { value: 'manager', label: 'Quản lý' },
] as const

async function fetchData() {
  loading.value = true
  try {
    const params: ManagerParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (debouncedKeyword.value.trim())
      params.keyword = debouncedKeyword.value.trim()
    if (selectedStatus.value !== 'ALL')
      params.status = selectedStatus.value as ManagerStatus
    if (selectedRole.value !== 'ALL')
      params.role = selectedRole.value as ManagementRole

    if (dateRange.value?.from) {
      params.dateFrom = formatToISODateTime(dateRange.value.from)
    }

    if (dateRange.value?.to) {
      params.dateTo = formatToISODateTime(dateRange.value.to, true)
    }

    const response = await managerService.getAll(params)
    accounts.value = response.data ?? []
    totalCount.value = response.meta.totalElements ?? 0
    totalPage.value = response.meta.totalPages ?? 0
  }
  catch (error: any) {
    console.error('Failed to fetch manager accounts:', error)
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || 'Không thể tải danh sách tài khoản quản trị'
    toast.error(message)
    accounts.value = []
    totalCount.value = 0
  }
  finally {
    loading.value = false
  }
}

function openCreateModal() {
  editTarget.value = undefined
  showResourceModal.value = true
}

function openEditModal(account: ManagerAccount) {
  editTarget.value = account
  showResourceModal.value = true
}

function openLockModal(account: ManagerAccount) {
  lockTarget.value = account
  showLockModal.value = true
}

function refreshAfterChange() {
  showResourceModal.value = false
  showLockModal.value = false
  fetchData()
}

function renderStatus(status: ManagerStatus) {
  const config = {
    active: { label: 'Hoạt động', class: 'border-emerald-200 bg-emerald-100 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300' },
    inactive: { label: 'Ngừng hoạt động', class: 'border-slate-200 bg-slate-100 text-slate-800 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300' },
    banned: { label: 'Bị khóa', class: 'border-rose-200 bg-rose-100 text-rose-800 dark:border-rose-800 dark:bg-rose-900/30 dark:text-rose-300' },
  }[status]

  return h(Badge, {
    variant: 'outline',
    class: `text-xs font-semibold ${config.class}`,
  }, () => config.label)
}

function renderRole(role: ManagementRole) {
  const config = {
    admin: { label: 'Quản trị viên', class: 'border-purple-200 bg-purple-100 text-purple-800 dark:border-purple-800 dark:bg-purple-900/30 dark:text-purple-300' },
    manager: { label: 'Quản lý', class: 'border-blue-200 bg-blue-100 text-blue-800 dark:border-blue-800 dark:bg-blue-900/30 dark:text-blue-300' },
  }[role]

  return h(Badge, {
    variant: 'outline',
    class: `text-xs font-semibold ${config.class}`,
  }, () => config.label)
}

const columns = computed<TableColumn<ManagerAccount>[]>(() => [
  { header: 'Tên đăng nhập', accessor: 'username', minWidth: 150 },
  { header: 'Tên hiển thị', accessor: 'displayName', minWidth: 180 },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Vai trò',
    minWidth: 110,
    render: row => renderRole(row.role),
  },
  {
    header: 'Trạng thái',
    minWidth: 150,
    render: row => renderStatus(row.status),
  },
  {
    header: 'Ngày tạo',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Thao tác',
    minWidth: 180,
    render: row => h('div', { class: 'flex justify-center gap-1' }, [
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs',
        onClick: () => openEditModal(row),
      }, () => [h(Pencil, { class: 'h-3.5 w-3.5' }), 'Sửa']),
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
        onClick: () => openLockModal(row),
      }, () => [h(LockKeyhole, { class: 'h-3.5 w-3.5' }), 'Khóa']),
    ]),
  },
])

watch(currentPage, fetchData)
watch([debouncedKeyword, selectedStatus, selectedRole, dateRange], () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  fetchData()
})

onMounted(fetchData)

const hasActiveFilter = computed(() =>
  !!keyword.value
  || selectedStatus.value !== 'ALL'
  || selectedRole.value !== 'ALL'
  || dateRange.value !== null,
)

function clearFilters() {
  keyword.value = ''
  selectedStatus.value = 'ALL'
  selectedRole.value = 'ALL'
  dateRange.value = defaultDateRange()
}
</script>

<template>
  <BasicPage
    title="Quản lý tài khoản quản trị"
    description="Quản lý các tài khoản quản lý và quản trị viên trong hệ thống"
    sticky
  >
    <template #actions>
      <UiButton @click="openCreateModal">
        <Plus />
        Tạo tài khoản
      </UiButton>
    </template>

    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="keyword"
          type="text"
          placeholder="Tìm theo username hoặc email..."
          class="h-9 pl-8"
        />
      </div>

      <div class="w-[190px]">
        <UiSelect v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div class="w-[170px]">
        <UiSelect v-model="selectedRole">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Vai trò" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="option in roleOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>

      <div>
        <DateRangePicker v-model="dateRange" />
      </div>

      <UiButton
        v-if="hasActiveFilter"
        variant="ghost"
        size="sm"
        class="h-9 gap-1.5 text-sm text-muted-foreground"
        @click="clearFilters"
      >
        <X class="h-3.5 w-3.5" />
        Xóa bộ lọc
      </UiButton>
    </div>

    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable :columns="columns" :data="accounts" />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <Modal v-model:open="showResourceModal">
    <ModalContent class="sm:max-w-lg">
      <ManagerResource
        :account="editTarget"
        @close="showResourceModal = false"
        @saved="refreshAfterChange"
      />
    </ModalContent>
  </Modal>

  <Modal v-model:open="showLockModal">
    <ModalContent>
      <ManagerLock
        v-if="lockTarget"
        :account="lockTarget"
        @close="showLockModal = false"
        @locked="refreshAfterChange"
      />
    </ModalContent>
  </Modal>
</template>
