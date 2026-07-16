<script setup lang="ts">
import { LoaderIcon, LockKeyhole, Pencil, Plus, Search, Unlock, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import ConfirmDialog from '@/components/confirm-dialog.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import Pagination from '@/components/pagination.vue'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import {
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Select as UiSelect,
} from '@/components/ui/select'
import { defaultDateRange, formatTimestamp, formatToISODateTime } from '@/utils/date.utils'

import type {
  ManagementRole,
  ManagerAccount,
  ManagerParams,
  ManagerStatus,
} from '../types/managerTypes'

import { managerService } from '../services/managerService'
import ManagerResource from './manager-resource.vue'

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
const lockReason = ref('')
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
      params.search = debouncedKeyword.value.trim()
    if (selectedStatus.value !== 'ALL')
      params.status = selectedStatus.value as ManagerStatus
    if (selectedRole.value !== 'ALL')
      params.role = selectedRole.value as ManagementRole
    if (dateRange.value?.from)
      params.dateFrom = formatToISODateTime(dateRange.value.from)
    if (dateRange.value?.to)
      params.dateTo = formatToISODateTime(dateRange.value.to, true)

    const response = await managerService.getAll(params)
    accounts.value = response.data ?? []
    totalCount.value = response.meta?.totalElements ?? 0
    totalPage.value = response.meta?.totalPages ?? 0
  }
  catch (error: any) {
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || 'Không thể tải danh sách tài khoản quản trị'
    toast.error(message)
    accounts.value = []
    totalCount.value = 0
    totalPage.value = 0
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
  lockReason.value = ''
  showLockModal.value = true
}

function refreshAfterChange() {
  showResourceModal.value = false
  showLockModal.value = false
  fetchData()
}

const isLockingManager = computed(() => lockTarget.value?.status === 'active')

async function confirmManagerStatusAction(reason: string) {
  if (!lockTarget.value)
    return

  loading.value = true
  try {
    if (isLockingManager.value) {
      await managerService.lock(lockTarget.value.id, reason)
      toast.success(`Đã khóa tài khoản ${lockTarget.value.username}`)
    }
    else {
      await managerService.update(lockTarget.value.id, { status: 'active' })
      toast.success(`Đã mở khóa tài khoản ${lockTarget.value.username}`)
    }

    showLockModal.value = false
    fetchData()
  }
  catch (error: any) {
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || error?.message || 'Không thể cập nhật trạng thái tài khoản'
    toast.error(message)
  }
  finally {
    loading.value = false
  }
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
        class: row.status === 'active'
          ? 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30'
          : 'h-8 gap-1 px-2 text-xs text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-900/20',
        onClick: () => openLockModal(row),
      }, () => [
        h(row.status === 'active' ? LockKeyhole : Unlock, { class: 'h-3.5 w-3.5' }),
        row.status === 'active' ? 'Khóa' : 'Mở',
      ]),
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
  <div class="space-y-4">
    <div class="flex flex-wrap items-center gap-3">
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

      <div><DateRangePicker v-model="dateRange" /></div>

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

      <div class="ml-auto">
        <UiButton @click="openCreateModal">
          <Plus />
          Tạo tài khoản
        </UiButton>
      </div>
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
  </div>

  <Modal v-model:open="showResourceModal">
    <ModalContent class="overflow-hidden p-0 sm:max-w-2xl">
      <ManagerResource
        :account="editTarget"
        @close="showResourceModal = false"
        @saved="refreshAfterChange"
      />
    </ModalContent>
  </Modal>

  <ConfirmDialog
    v-model:open="showLockModal"
    v-model:reason="lockReason"
    :destructive="isLockingManager"
    :require-reason="isLockingManager"
    :close-on-confirm="false"
    cancel-button-text="Hủy"
    :confirm-button-text="isLockingManager ? 'Khóa tài khoản' : 'Mở khóa'"
    reason-label="Lý do khóa tài khoản"
    reason-placeholder="Nhập lý do để gửi mail cho manager/admin"
    reason-error="Vui lòng nhập lý do khóa tài khoản"
    :is-loading="loading"
    @confirm="confirmManagerStatusAction"
  >
    <template #title>
      {{ isLockingManager ? `Khóa tài khoản ${lockTarget?.username}?` : `Mở khóa tài khoản ${lockTarget?.username}?` }}
    </template>

    <template #description>
      <p>
        {{
          isLockingManager
            ? 'Tài khoản sẽ chuyển sang trạng thái bị khóa và không thể tiếp tục đăng nhập.'
            : 'Tài khoản sẽ được chuyển về hoạt động và có thể đăng nhập trở lại.'
        }}
      </p>
    </template>
  </ConfirmDialog>
</template>
