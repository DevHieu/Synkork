<script lang="ts" setup>
import { Eye, LoaderIcon, Lock, PlusIcon, RefreshCwIcon, Search, Unlock, X } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import ConfirmDialog from '@/components/confirm-dialog.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input as UiInput } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea as UiTextarea } from '@/components/ui/textarea'

import type {
  Room,
  RoomFormPayload,
  RoomParams,
  UserOption,
} from './types/RoomTypes'

import RoomDetailDialog from './components/RoomDetailDialog.vue'
import { roomService } from './service/roomService'

const route = useRoute()

const route = useRoute()

const route = useRoute()
const keywordParam = (route.query.keyword as string) ?? ''

const selectedRoom = ref<Room | null>(null)
const isDetailOpen = ref(false)

const loading = ref(false)
const roomsData = ref<Room[]>([])
const totalCount = ref(0)

const currentPage = ref(1)
const pageSize = 20

const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const selectedStatus = ref<string>('ALL')

const debounceSearchKeyword = refDebounced(searchKeyword, 500)

const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))
const hasActiveFilter = computed(() =>
  !!searchKeyword.value
  || selectedStatus.value !== 'ALL',
)

function handleViewDetail(room: Room) {
  selectedRoom.value = room
  isDetailOpen.value = true
}

function clearFilters() {
  searchKeyword.value = ''
  selectedStatus.value = 'ALL'
}

// ===================== Create form =====================
const isCreateOpen = ref(false)

const form = ref<RoomFormPayload>({
  name: '',
  description: '',
  status: 'OPEN',
  ownerId: undefined,
})

const ownerKeyword = ref('')
const ownerOptions = ref<UserOption[]>([])
const selectedOwner = ref<UserOption | null>(null)
const isSearchingOwner = ref(false)
const showOwnerDropdown = ref(false)
const debouncedOwnerKeyword = refDebounced(ownerKeyword, 400)

const isSubmitting = ref(false)
const formError = ref('')

function resetForm() {
  form.value = {
    name: '',
    description: '',
    status: 'OPEN',
    ownerId: undefined,
  }
  selectedOwner.value = null
  ownerKeyword.value = ''
  ownerOptions.value = []
  showOwnerDropdown.value = false
  formError.value = ''
}

function handleCreate() {
  resetForm()
  isCreateOpen.value = true
}

watch(debouncedOwnerKeyword, async (keyword) => {
  if (!keyword.trim()) {
    ownerOptions.value = []
    return
  }

  isSearchingOwner.value = true
  try {
    ownerOptions.value = await roomService.searchOwners(keyword.trim())
  }
  finally {
    isSearchingOwner.value = false
  }
})

function pickOwner(user: UserOption) {
  selectedOwner.value = user
  form.value.ownerId = user.id
  ownerKeyword.value = ''
  ownerOptions.value = []
  showOwnerDropdown.value = false
}

function clearOwner() {
  selectedOwner.value = null
  form.value.ownerId = undefined
}

async function handleSubmitCreate() {
  formError.value = ''

  if (!form.value.name.trim()) {
    formError.value = 'Tên room không được để trống'
    return
  }

  if (!form.value.ownerId) {
    formError.value = 'Vui lòng chọn owner cho room'
    return
  }

  isSubmitting.value = true

  try {
    await roomService.createRoom(form.value)
    isCreateOpen.value = false
    fetchRooms()
  }
  catch (error: any) {
    formError.value = error?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại'
  }
  finally {
    isSubmitting.value = false
  }
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

// ===================== Columns =====================
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

// ===================== Fetch =====================
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
  [debounceSearchKeyword, selectedStatus],
  () => {
    currentPage.value = 1
    fetchRooms()
  },
)

watch(currentPage, () => {
  fetchRooms()
})

onMounted(() => {
  if (keywordParam !== '') {
    return searchKeyword.value = keywordParam
  }
  fetchRooms()
})
</script>

<template>
  <BasicPage
    title="Rooms"
    description="Quản lý tất cả room trong hệ thống"
    sticky
  >
    <template #actions>
      <div class="flex items-center gap-2">
        <UiButton variant="outline" @click="fetchRooms">
          <RefreshCwIcon class="mr-2 h-4 w-4" />
          Refresh
        </UiButton>

        <UiButton @click="handleCreate">
          <PlusIcon class="mr-2 h-4 w-4" />
          Tạo Room
        </UiButton>
      </div>
    </template>

    <!-- filters -->
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

  <!-- Detail + Edit dialog (gộp làm 1) -->
  <RoomDetailDialog
    v-if="selectedRoom"
    v-model:open="isDetailOpen"
    :room-id="selectedRoom.id"
    @updated="fetchRooms"
  />

  <!-- Create dialog -->
  <Dialog v-model:open="isCreateOpen">
    <DialogContent class="max-w-[520px]">
      <DialogHeader>
        <DialogTitle>
          Tạo Room mới
        </DialogTitle>
        <DialogDescription class="sr-only">
          Form tạo room mới
        </DialogDescription>
      </DialogHeader>

      <div class="flex flex-col gap-4 py-2">
        <!-- Name -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Tên Room <span class="text-red-500">*</span>
          </label>
          <UiInput
            v-model="form.name"
            placeholder="VD: Team Marketing"
          />
        </div>

        <!-- Description -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Description
          </label>
          <UiTextarea
            v-model="form.description"
            rows="3"
            placeholder="Mô tả ngắn về room này..."
          />
        </div>

        <!-- Owner -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Owner <span class="text-red-500">*</span>
          </label>

          <div
            v-if="selectedOwner"
            class="flex items-center justify-between rounded-lg border border-border bg-muted/40 px-3 py-2"
          >
            <div>
              <p class="text-[13px] font-medium">
                {{ selectedOwner.username }}
              </p>
              <p v-if="selectedOwner.email" class="text-[11px] text-muted-foreground">
                {{ selectedOwner.email }}
              </p>
            </div>
            <UiButton variant="ghost" size="sm" @click="clearOwner">
              Đổi
            </UiButton>
          </div>

          <div v-else class="relative">
            <UiInput
              v-model="ownerKeyword"
              placeholder="Tìm theo username hoặc email..."
              @focus="showOwnerDropdown = true"
            />

            <div
              v-if="showOwnerDropdown && (ownerOptions.length || isSearchingOwner)"
              class="absolute z-10 mt-1 w-full rounded-lg border border-border bg-background shadow-md"
            >
              <p v-if="isSearchingOwner" class="px-3 py-2 text-[12px] text-muted-foreground">
                Đang tìm...
              </p>

              <button
                v-for="user in ownerOptions"
                :key="user.id"
                type="button"
                class="flex w-full flex-col items-start px-3 py-2 text-left hover:bg-muted/60"
                @click="pickOwner(user)"
              >
                <span class="text-[13px] font-medium">{{ user.username }}</span>
                <span class="text-[11px] text-muted-foreground">{{ user.email }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Status -->
        <div class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Trạng thái
          </label>
          <Select v-model="form.status">
            <SelectTrigger class="h-9 w-full">
              <SelectValue placeholder="Trạng thái" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="OPEN">
                Đang mở
              </SelectItem>
              <SelectItem value="LOCKED">
                Đã khoá
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <p v-if="formError" class="text-[12px] text-red-500">
          {{ formError }}
        </p>
      </div>

      <DialogFooter>
        <UiButton variant="outline" :disabled="isSubmitting" @click="isCreateOpen = false">
          Hủy
        </UiButton>
        <UiButton :disabled="isSubmitting" @click="handleSubmitCreate">
          {{ isSubmitting ? 'Đang lưu...' : 'Lưu' }}
        </UiButton>
      </DialogFooter>
    </DialogContent>
  </Dialog>

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
