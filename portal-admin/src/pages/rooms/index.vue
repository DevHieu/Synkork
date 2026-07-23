<script lang="ts" setup>
import { Eye, LoaderIcon, Lock, PlusIcon, Search, Unlock, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import ConfirmDialog from '@/components/confirm-dialog.vue'
import { BasicPage } from '@/components/global-layout'
import NumberField from '@/components/number-field.vue'
import Pagination from '@/components/pagination.vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

import type { Room, RoomParams } from './types/RoomTypes'

import CreateRoomDialog from './components/CreateRoomDialog.vue'
import RoomDetailDialog from './components/RoomDetailDialog.vue'
import { roomService } from './service/roomService'

const route = useRoute()
const keywordParam = (route.query.keyword as string) ?? ''

const selectedRoom = ref<Room | null>(null)
const isDetailOpen = ref(false)

const loading = ref(false)
const roomsData = ref<Room[]>([])
const totalCount = ref(0)

const currentPage = ref(1)
const pageSize = 20

const searchKeyword = ref('')
const selectedStatus = ref<string>('ALL')
const minMembers = ref<number>()
const maxMembers = ref<number>()
const minWarning = ref<number>()
const maxWarning = ref<number>()

const debounceSearchKeyword = refDebounced(searchKeyword, 500)
const debounceMinMembers = refDebounced(minMembers, 500)
const debounceMaxMembers = refDebounced(maxMembers, 500)
const debounceMinWarning = refDebounced(minWarning, 500)
const debounceMaxWarning = refDebounced(maxWarning, 500)

const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))
const hasActiveFilter = computed(() =>
  !!searchKeyword.value
  || selectedStatus.value !== 'ALL'
  || !!minMembers.value
  || !!maxMembers.value
  || !!minWarning.value
  || !!maxWarning.value,
)

function clearFilters() {
  searchKeyword.value = ''
  selectedStatus.value = 'ALL'
  minMembers.value = undefined
  maxMembers.value = undefined
  minWarning.value = undefined
  maxWarning.value = undefined
}

function handleViewDetail(room: Room) {
  selectedRoom.value = room
  isDetailOpen.value = true
}

const isCreateOpen = ref(false)

function handleCreate() {
  isCreateOpen.value = true
}

const isLockConfirmOpen = ref(false)
const lockTargetRoom = ref<Room | null>(null)
const isLocking = ref(false)
const lockError = ref('')
const lockReason = ref('')
const isLockAction = computed(() => lockTargetRoom.value?.status !== 'LOCKED')

function handleToggleLock(room: Room) {
  lockTargetRoom.value = room
  lockError.value = ''
  lockReason.value = ''
  isLockConfirmOpen.value = true
}

async function confirmToggleLock(reason: string) {
  if (!lockTargetRoom.value)
    return

  isLocking.value = true
  lockError.value = ''

  const nextStatus = lockTargetRoom.value.status === 'LOCKED' ? 'OPEN' : 'LOCKED'

  try {
    await roomService.changeRoomStatus(lockTargetRoom.value.id, nextStatus, reason)
    isLockConfirmOpen.value = false
    fetchRooms()
  }
  catch (error: any) {
    lockError.value = error?.response?.data?.message || 'Không thể cập nhật trạng thái room này'
  }
  finally {
    isLocking.value = false
  }
}

function renderRoomStatus(status: string) {
  const config = {
    OPEN: {
      label: 'Đang mở',
      class: 'border-emerald-200 bg-emerald-100 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300',
    },
    LOCKED: {
      label: 'Đã khóa',
      class: 'border-rose-200 bg-rose-100 text-rose-800 dark:border-rose-800 dark:bg-rose-900/30 dark:text-rose-300',
    },
    PENDING_REMOVAL: {
      label: 'Chờ xóa',
      class: 'border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300',
    },
  }[status]

  return h(Badge, {
    variant: 'outline',
    class: `text-xs font-semibold ${config?.class ?? ''}`,
  }, () => config?.label ?? status)
}

async function fetchRooms() {
  loading.value = true

  try {
    const queryParams: RoomParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (searchKeyword.value.trim())
      queryParams.search = searchKeyword.value.trim()

    if (selectedStatus.value !== 'ALL')
      queryParams.status = selectedStatus.value

    queryParams.minMembers = minMembers.value
    queryParams.maxMembers = maxMembers.value
    queryParams.minWarning = minWarning.value
    queryParams.maxWarning = maxWarning.value

    const response = await roomService.getRooms(queryParams)

    roomsData.value = response.data || []
    totalCount.value = response.meta?.totalElements || 0
  }
  catch (error) {
    console.error('Lỗi khi tải rooms:', error)
  }
  finally {
    loading.value = false
  }
}

watch(
  [debounceSearchKeyword, selectedStatus, debounceMinMembers, debounceMaxMembers, debounceMinWarning, debounceMaxWarning],
  () => {
    currentPage.value = 1
    fetchRooms()
  },
)

watch(() => route.query.keyword, (value) => {
  const nextKeyword = typeof value === 'string' ? value : ''
  if (nextKeyword && nextKeyword !== searchKeyword.value) {
    searchKeyword.value = nextKeyword
    currentPage.value = 1
  }
})

watch(currentPage, () => {
  fetchRooms()
})

onMounted(() => {
  if (keywordParam !== '') {
    return searchKeyword.value = keywordParam
  }
  fetchRooms()
})

const columns = computed<TableColumn<any>[]>(() => [
  {
    header: 'Tên Room',
    accessor: 'name',
    minWidth: 200,
    render: row => row.type === 'DM' ? 'Tin nhắn trực tiếp' : row.name,
  },
  {
    header: 'Chủ phòng',
    accessor: 'ownerUsername',
    minWidth: 140,
    render: row => row.ownerUsername || '—',
  },
  {
    header: 'Mã mời',
    accessor: 'inviteCode',
    minWidth: 140,
    render: row => row.inviteCode || '—',
  },
  {
    header: 'Trạng thái',
    accessor: 'status',
    minWidth: 130,
    render: row => renderRoomStatus(row.status),
  },
  {
    header: 'Thành viên',
    accessor: 'memberCount',
    minWidth: 90,
  },
  {
    header: 'Số lần bị cảnh báo',
    accessor: 'warning',
    minWidth: 90,
  },
  {
    header: 'Thao tác',
    minWidth: 180,
    render: row =>
      h('div', { class: 'flex items-center gap-1.5' }, [
        h(
          UiButton,
          {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs',
            onClick: () => handleViewDetail(row),
          },
          () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'Chi tiết'],
        ),
        row.status === 'LOCKED'
          ? h(UiButton, {
              variant: 'outline',
              size: 'sm',
              class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10 hover:text-destructive border-destructive/20 hover:border-destructive/30',
              onClick: () => handleToggleLock(row),
            }, () => [h(Lock, { class: 'h-3.5 w-3.5' }), 'Khóa'])
          : h(UiButton, {
              variant: 'outline',
              size: 'sm',
              class: 'h-8 gap-1 px-2 text-xs text-emerald-600 hover:bg-emerald-50 hover:text-emerald-700 dark:text-emerald-300 dark:hover:bg-emerald-900/20',
              onClick: () => handleToggleLock(row),
            }, () => [h(Unlock, { class: 'h-3.5 w-3.5' }), 'Mở']),
      ]),
  },
])
</script>

<template>
  <BasicPage
    title="Rooms"
    description="Quản lý tất cả room trong hệ thống"
    sticky
  >
    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="searchKeyword"
          type="text"
          placeholder="Tìm room..."
          class="h-9 pl-8"
        />
      </div>

      <div class="w-[180px]">
        <Select v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">
              Tất cả trạng thái
            </SelectItem>
            <SelectItem value="OPEN">
              Đang mở
            </SelectItem>
            <SelectItem value="LOCKED">
              Đã khoá
            </SelectItem>
            <SelectItem value="PENDING_REMOVAL">
              Chờ xoá
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div class="flex items-center gap-1.5">
        <span class="whitespace-nowrap text-[13px] text-foreground">Thành viên</span>
        <NumberField
          v-model="minMembers"
          placeholder="Từ"
          class="w-[90px]"
        />
        <span class="text-muted-foreground">-</span>
        <NumberField
          v-model="maxMembers"
          placeholder="Đến"
          class="w-[90px]"
        />
      </div>

      <div class="flex items-center gap-1.5">
        <span class="whitespace-nowrap text-[13px] text-foreground">Cảnh báo</span>
        <NumberField
          v-model="minWarning"
          placeholder="Từ"
          class="w-[90px]"
        />
        <span class="text-muted-foreground">-</span>
        <NumberField
          v-model="maxWarning"
          placeholder="Đến"
          class="w-[90px]"
        />
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

      <!-- <div class="ml-auto">
        <UiButton @click="handleCreate">
          <PlusIcon class="mr-2 h-4 w-4" />
          Tạo Room
        </UiButton>
      </div> -->
    </div>

    <!-- table -->
    <div class="relative rounded-md border border-neutral-200 dark:border-neutral-800">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <div class="overflow-x-auto">
        <BaseTable :columns="columns" :data="roomsData" />
      </div>

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <RoomDetailDialog
    v-if="selectedRoom"
    v-model:open="isDetailOpen"
    :room-id="selectedRoom.id"
    @updated="fetchRooms"
  />

  <CreateRoomDialog
    v-model:open="isCreateOpen"
    @created="fetchRooms"
  />

  <ConfirmDialog
    v-model:open="isLockConfirmOpen"
    v-model:reason="lockReason"
    :destructive="isLockAction"
    :require-reason="isLockAction"
    :close-on-confirm="false"
    cancel-button-text="Hủy"
    :confirm-button-text="isLockAction ? 'Khóa' : 'Mở khóa'"
    reason-label="Nội dung thông báo"
    reason-placeholder="Nhập nội dung/lý do khóa room"
    reason-error="Vui lòng nhập nội dung khóa room"
    :is-loading="isLocking"
    @confirm="confirmToggleLock"
  >
    <template #title>
      {{ isLockAction ? 'Khóa room này?' : 'Mở khóa room này?' }}
    </template>

    <template #description>
      <p>
        Room <strong>{{ lockTargetRoom?.name }}</strong>
        {{
          isLockAction
            ? ' sẽ bị khóa, thành viên sẽ không thể tương tác trong room này.'
            : ' sẽ được mở khóa và hoạt động trở lại bình thường.'
        }}
      </p>
    </template>

    <p v-if="lockError" class="text-[12px] text-red-500">
      {{ lockError }}
    </p>
  </ConfirmDialog>
</template>
