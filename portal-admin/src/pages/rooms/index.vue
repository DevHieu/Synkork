<script lang="ts" setup>
import { Eye, LoaderIcon, Pencil, PlusIcon, RefreshCwIcon, Search, Trash2 } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
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
  RoomDetail,
  RoomFormPayload,
  RoomParams,
  UserOption,
} from './types/RoomTypes'

import RoomDetailDialog from './components/RoomDetailDialog.vue'
import { roomService } from './service/roomService'

// ===================== Table & filters =====================
const selectedRoom = ref<Room | null>(null)
const isDetailOpen = ref(false)

const loading = ref(false)
const roomsData = ref<Room[]>([])
const totalCount = ref(0)

const currentPage = ref(1)
const pageSize = 20

const searchKeyword = ref('')
const selectedStatus = ref<string>('ALL')
const selectedType = ref<string>('ALL')

const debounceSearchKeyword = refDebounced(searchKeyword, 500)

const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))

function handleViewDetail(room: Room) {
  selectedRoom.value = room
  isDetailOpen.value = true
}

// ===================== Form (create/edit) =====================
const isFormOpen = ref(false)
const editingRoom = ref<Room | RoomDetail | null>(null)
const isEdit = computed(() => !!editingRoom.value)
const isDmRoom = computed(() => editingRoom.value?.type === 'DM')

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
  editingRoom.value = null
  resetForm()
  isFormOpen.value = true
}

function handleEdit(room: Room | RoomDetail) {
  editingRoom.value = room
  resetForm()

  form.value = {
    name: room.name,
    description: room.description || '',
    status: room.status,
    ownerId: room.ownerId,
  }

  selectedOwner.value = room.ownerId
    ? {
        id: room.ownerId,
        username: room.ownerUsername || (('owner' in room && room.owner?.username) || ''),
        email: ('owner' in room && room.owner?.email) || '',
      }
    : null

  isFormOpen.value = true
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

async function handleSubmitForm() {
  formError.value = ''

  if (!isDmRoom.value && !form.value.name.trim()) {
    formError.value = 'Tên room không được để trống'
    return
  }

  if (!isEdit.value && !form.value.ownerId) {
    formError.value = 'Vui lòng chọn owner cho room'
    return
  }

  isSubmitting.value = true

  try {
    if (isEdit.value && editingRoom.value) {
      await roomService.updateRoom(editingRoom.value.id, form.value)
    }
    else {
      await roomService.createRoom(form.value)
    }

    isFormOpen.value = false
    fetchRooms()
  }
  catch (error: any) {
    formError.value = error?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại'
  }
  finally {
    isSubmitting.value = false
  }
}

// ===================== Delete =====================
const isDeleteOpen = ref(false)
const deletingRoom = ref<Room | null>(null)
const isDeleting = ref(false)
const deleteError = ref('')

function handleDelete(room: Room) {
  deletingRoom.value = room
  deleteError.value = ''
  isDeleteOpen.value = true
}

async function confirmDelete() {
  if (!deletingRoom.value)
    return

  isDeleting.value = true
  deleteError.value = ''

  try {
    await roomService.deleteRoom(deletingRoom.value.id)
    isDeleteOpen.value = false
    fetchRooms()
  }
  catch (error: any) {
    deleteError.value = error?.response?.data?.message || 'Không thể xóa room này'
  }
  finally {
    isDeleting.value = false
  }
}

// ===================== Columns =====================
const columns = computed<TableColumn<any>[]>(() => [
  {
    header: 'Name',
    accessor: 'name',
    minWidth: 200,
    render: row => row.type === 'DM' ? 'Direct Message' : row.name,
  },
  {
    header: 'Owner',
    accessor: 'ownerUsername',
    minWidth: 140,
    render: row => row.ownerUsername || '—',
  },
  {
    header: 'Invite Code',
    accessor: 'inviteCode',
    minWidth: 140,
    render: row => row.inviteCode || '—',
  },
  {
    header: 'Type',
    accessor: 'type',
    minWidth: 100,
  },
  {
    header: 'Status',
    accessor: 'status',
    minWidth: 100,
  },
  {
    header: 'Members',
    accessor: 'memberCount',
    minWidth: 90,
  },
  {
    header: 'Action',
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
        h(
          UiButton,
          {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs',
            onClick: () => handleEdit(row),
          },
          () => [h(Pencil, { class: 'h-3.5 w-3.5' }), 'Sửa'],
        ),
        h(
          UiButton,
          {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs text-red-500 hover:bg-red-50 hover:text-red-600',
            disabled: row.type === 'DM',
            onClick: () => handleDelete(row),
          },
          () => [h(Trash2, { class: 'h-3.5 w-3.5' })],
        ),
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

    if (selectedType.value !== 'ALL')
      queryParams.type = selectedType.value

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
  [debounceSearchKeyword, selectedStatus, selectedType],
  () => {
    currentPage.value = 1
    fetchRooms()
  },
)

watch(currentPage, () => {
  fetchRooms()
})

onMounted(() => {
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
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">
              All Status
            </SelectItem>
            <SelectItem value="OPEN">
              OPEN
            </SelectItem>
            <SelectItem value="CLOSED">
              CLOSED
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div class="w-[180px]">
        <Select v-model="selectedType">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Type" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">
              All Types
            </SelectItem>
            <SelectItem value="GROUP">
              GROUP
            </SelectItem>
            <SelectItem value="DM">
              DM
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
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

  <!-- Detail dialog -->
  <RoomDetailDialog
    v-if="selectedRoom"
    v-model:open="isDetailOpen"
    :room-id="selectedRoom.id"
    @edit="handleEdit"
  />

  <!-- Create / Edit dialog -->
  <Dialog v-model:open="isFormOpen">
    <DialogContent class="max-w-[520px]">
      <DialogHeader>
        <DialogTitle>
          {{ isEdit ? 'Chỉnh sửa Room' : 'Tạo Room mới' }}
        </DialogTitle>
        <DialogDescription class="sr-only">
          {{ isEdit ? 'Form chỉnh sửa thông tin room' : 'Form tạo room mới' }}
        </DialogDescription>
      </DialogHeader>

      <div class="flex flex-col gap-4 py-2">
        <p
          v-if="isDmRoom"
          class="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-[12px] text-amber-700"
        >
          Đây là room DM (do hệ thống tự tạo giữa 2 user). Admin chỉ có thể
          thay đổi trạng thái của room này.
        </p>

        <!-- Name -->
        <div v-if="!isDmRoom" class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Tên Room <span class="text-red-500">*</span>
          </label>
          <UiInput
            v-model="form.name"
            placeholder="VD: Team Marketing"
          />
        </div>

        <!-- Description -->
        <div v-if="!isDmRoom" class="flex flex-col gap-1.5">
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
        <div v-if="!isDmRoom" class="flex flex-col gap-1.5">
          <label class="text-[12px] font-medium text-muted-foreground">
            Owner <span v-if="!isEdit" class="text-red-500">*</span>
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
            Status
          </label>
          <Select v-model="form.status">
            <SelectTrigger class="h-9 w-full">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="OPEN">
                OPEN
              </SelectItem>
              <SelectItem value="CLOSED">
                CLOSED
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <p v-if="formError" class="text-[12px] text-red-500">
          {{ formError }}
        </p>
      </div>

      <DialogFooter>
        <UiButton variant="outline" :disabled="isSubmitting" @click="isFormOpen = false">
          Hủy
        </UiButton>
        <UiButton :disabled="isSubmitting" @click="handleSubmitForm">
          {{ isSubmitting ? 'Đang lưu...' : 'Lưu' }}
        </UiButton>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <!-- Delete confirm -->
  <AlertDialog v-model:open="isDeleteOpen">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>Xóa room này?</AlertDialogTitle>
        <AlertDialogDescription>
          Hành động này sẽ xóa vĩnh viễn room
          <strong>{{ deletingRoom?.name }}</strong> cùng toàn bộ space, thành
          viên liên quan. Không thể hoàn tác.
        </AlertDialogDescription>
      </AlertDialogHeader>

      <p v-if="deleteError" class="text-[12px] text-red-500">
        {{ deleteError }}
      </p>

      <AlertDialogFooter>
        <AlertDialogCancel :disabled="isDeleting">
          Hủy
        </AlertDialogCancel>
        <AlertDialogAction :disabled="isDeleting" @click.prevent="confirmDelete">
          {{ isDeleting ? 'Đang xóa...' : 'Xóa' }}
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>