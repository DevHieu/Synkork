<script setup lang="ts">
import { Eye, LoaderIcon, Lock, Search, Unlock } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import ConfirmDialog from '@/components/confirm-dialog.vue'
import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import Badge from '@/components/ui/badge/Badge.vue'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { defaultDateRange, formatToISODateTime } from '@/utils/date.utils'

import type { User, UserParams, UserPlan, UserStatus } from './types/userTypes'

import UserCreate from './components/user-create.vue'
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
const actionTarget = ref<User | null>(null)
const actionReason = ref('')
const showEditModal = ref(false)
const showActionDialog = ref(false)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
  { value: 'BANNED', label: 'Bị chặn' },
] as const

const planOptions = [
  { value: 'ALL', label: 'Tất cả' },
  { value: 'FREE', label: 'FREE' },
  { value: 'TEAM', label: 'TEAM' },
  { value: 'BUSINESS', label: 'BUSINESS' },
] as const

/**
 * Nguồn xác định duy nhất cho việc user có đang ACTIVE hay không.
 * Mọi chỗ cần biết trạng thái active/locked đều phải qua đây,
 * tránh lặp lại `.toUpperCase() === 'ACTIVE'` rải rác gây lệch logic.
 */
function isUserActive(user: Pick<User, 'status'> | null | undefined): boolean {
  return user?.status?.toUpperCase() === 'ACTIVE'
}

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

function handleOpenUserAction(user: User) {
  actionTarget.value = user
  actionReason.value = ''
  showActionDialog.value = true
}

async function handleConfirmUserAction(reason: string) {
  if (!actionTarget.value)
    return

  loading.value = true
  try {
    if (isUserActive(actionTarget.value)) {
      await userService.delete(actionTarget.value.id, reason)
      toast.success(`Đã khóa người dùng: ${actionTarget.value.username}`)
    }
    else {
      await userService.updateStatus(actionTarget.value.id, 'ACTIVE')
      toast.success(`Đã mở khóa người dùng: ${actionTarget.value.username}`)
    }
    showActionDialog.value = false
    fetchData()
  }
  catch (err: any) {
    const message = err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || 'Cập nhật tài khoản thất bại'
    toast.error(message)
  }
  finally {
    loading.value = false
  }
}

function onUserSaved() {
  showEditModal.value = false
  fetchData()
}

function renderPlan(plan: string) {
  const normalized = plan?.toUpperCase() || 'FREE'

  const config = {
    BUSINESS: {
      class: 'border-purple-200 bg-purple-100 text-purple-800 dark:border-purple-800 dark:bg-purple-900/30 dark:text-purple-300',
    },
    TEAM: {
      class: 'border-blue-200 bg-blue-100 text-blue-800 dark:border-blue-800 dark:bg-blue-900/30 dark:text-blue-300',
    },
    FREE: {
      class: 'border-slate-200 bg-slate-100 text-slate-800 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300',
    },
  }[normalized]

  return h(
    'span',
    { class: `inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold border ${config?.class ?? ''}` },
    normalized,
  )
}

function renderStatus(status: string) {
  const normalized = (status?.toUpperCase() || 'INACTIVE') as UserStatus

  const config = {
    ACTIVE: {
      label: 'Hoạt động',
      class: 'border-emerald-200 bg-emerald-100 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300',
    },
    INACTIVE: {
      label: 'Ngừng hoạt động',
      class: 'border-slate-200 bg-slate-100 text-slate-800 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300',
    },
    BANNED: {
      label: 'Bị chặn',
      class: 'border-rose-200 bg-rose-100 text-rose-800 dark:border-rose-800 dark:bg-rose-900/30 dark:text-rose-300',
    },
  }[normalized]

  return h(
    Badge,
    {
      variant: 'outline',
      class: `text-xs font-semibold ${config?.class ?? ''}`,
    },
    () => config?.label ?? normalized,
  )
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
    render: row => renderPlan(row.plan ?? ''),
  },
  {
    header: 'Trạng thái',
    minWidth: 120,
    render: row => renderStatus(row.status ?? ''),
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
      }, () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'Xem']),
      isUserActive(row)
        ? h(UiButton, {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
            onClick: () => handleOpenUserAction(row),
          }, () => [h(Lock, { class: 'h-3.5 w-3.5' }), 'Khóa'])
        : h(UiButton, {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-900/20',
            onClick: () => handleOpenUserAction(row),
          }, () => [h(Unlock, { class: 'h-3.5 w-3.5' }), 'Mở']),
    ]),
  },
])

const isLockingUser = computed(() => isUserActive(actionTarget.value))
</script>

<template>
  <BasicPage
    title="Quản lý người dùng"
    description="Quản lý và theo dõi danh sách người dùng trong hệ thống"
    sticky
  >
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="keyword"
          type="text"
          placeholder="Tìm theo username hoặc email..."
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

      <!-- Button tạo mới người dùng -->
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
    <ModalContent class="overflow-hidden p-0 sm:max-w-2xl">
      <UserResource
        :user="editTarget ?? undefined"
        @close="showEditModal = false"
        @saved="onUserSaved"
      />
    </ModalContent>
  </Modal>

  <ConfirmDialog
    v-model:open="showActionDialog"
    v-model:reason="actionReason"
    :destructive="isLockingUser"
    :require-reason="isLockingUser"
    :close-on-confirm="false"
    cancel-button-text="Hủy"
    :confirm-button-text="isLockingUser ? 'Khóa user' : 'Mở khóa'"
    reason-label="Lý do khóa/xóa user"
    reason-placeholder="Nhập lý do để gửi mail cho user"
    reason-error="Vui lòng nhập lý do khóa tài khoản"
    :is-loading="loading"
    @confirm="handleConfirmUserAction"
  >
    <template #title>
      {{ isLockingUser ? `Khóa tài khoản: ${actionTarget?.username}?` : `Mở khóa tài khoản: ${actionTarget?.username}?` }}
    </template>

    <template #description>
      <p>
        {{
          isLockingUser
            ? 'User sẽ được chuyển sang INACTIVE, nhận email thông báo lý do, và bị xóa khỏi tất cả room đang tham gia.'
            : 'User sẽ được chuyển về ACTIVE và có thể sử dụng hệ thống trở lại.'
        }}
      </p>
    </template>
  </ConfirmDialog>
</template>
