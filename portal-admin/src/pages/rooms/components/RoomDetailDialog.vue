<script lang="ts" setup>
import {
  Calendar,
  Crown,
  KeyRound,
  Layers,
  Users,
} from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
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
import { formatTimestamp } from '@/utils/date.utils'

import type { RoomDetail, RoomFormPayload, UserOption } from '../types/RoomTypes'

import { roomService } from '../service/roomService'
import MemberDialog from './MemberDialog.vue'
import SpacesDialog from './SpacesDialog.vue'

const props = defineProps<{
  roomId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'updated': []
}>()

const router = useRouter()

const isOpen = computed({
  get: () => props.open,
  set: val => emit('update:open', val),
})

const room = ref<RoomDetail | null>(null)
const isLoading = ref(false)
const loadError = ref('')

const isMembersOpen = ref(false)
const isSpacesOpen = ref(false)

const isPendingRemoval = computed(() => room.value?.status === 'PENDING_REMOVAL')

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

function syncFormFromRoom() {
  if (!room.value)
    return

  form.value = {
    name: room.value.name,
    description: room.value.description || '',
    status: room.value.status === 'PENDING_REMOVAL' ? 'OPEN' : room.value.status,
    ownerId: room.value.ownerId,
  }

  selectedOwner.value = room.value.owner
    ? {
        id: room.value.owner.id,
        username: room.value.owner.username,
        email: room.value.owner.email,
      }
    : null

  ownerKeyword.value = ''
  ownerOptions.value = []
  showOwnerDropdown.value = false
  formError.value = ''
}

async function handleSubmitForm() {
  if (!room.value)
    return

  formError.value = ''

  if (!form.value.name.trim()) {
    formError.value = 'Tên room không được để trống'
    return
  }

  isSubmitting.value = true

  try {
    // Room đang Chờ xóa: không cho phép đổi status qua API
    const payload = isPendingRemoval.value
      ? { ...form.value, status: undefined as any }
      : form.value

    await roomService.updateRoom(room.value.id, payload)

    room.value = await roomService.getRoomDetail(room.value.id)
    syncFormFromRoom()
    emit('updated')
  }
  catch (error: any) {
    formError.value = error?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại'
  }
  finally {
    isSubmitting.value = false
  }
}

watch(() => props.open, async (opened) => {
  if (!opened || !props.roomId)
    return

  isLoading.value = true
  loadError.value = ''
  room.value = null

  try {
    room.value = await roomService.getRoomDetail(props.roomId)
    syncFormFromRoom()
  }
  catch (error) {
    console.error('Lỗi khi tải chi tiết room:', error)
    loadError.value = 'Không thể tải chi tiết room này'
  }
  finally {
    isLoading.value = false
  }
}, { immediate: true })

function showReportDetail(userEmail: string) {
  router.push(`/report?keyword=${userEmail}`)
}
</script>

<template>
  <!-- Main dialog -->
  <Dialog v-model:open="isOpen">
    <DialogContent class="max-w-[720px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-5">
        <div class="flex items-center justify-between">
          <div>
            <DialogTitle class="text-[15px] font-semibold">
              Chi tiết Room
            </DialogTitle>

            <DialogDescription class="sr-only">
              Thông tin chi tiết của room
            </DialogDescription>

            <p
              v-if="room"
              class="mt-1 font-mono text-[11px] text-muted-foreground"
            >
              ID: {{ room.id }}
            </p>
          </div>
        </div>
      </DialogHeader>

      <!-- Loading -->
      <div
        v-if="isLoading"
        class="flex flex-col gap-4 px-6 py-5"
      >
        <div class="grid grid-cols-2 gap-2.5">
          <div
            v-for="i in 4"
            :key="i"
            class="h-14 animate-pulse rounded-lg bg-muted"
          />
        </div>
        <div class="h-px bg-border" />
        <div class="h-28 animate-pulse rounded-lg bg-muted" />
      </div>

      <!-- Error -->
      <div
        v-else-if="loadError"
        class="px-6 py-10 text-center text-sm text-red-500"
      >
        {{ loadError }}
      </div>

      <template v-else-if="room">
        <div class="flex max-h-[70vh] flex-col gap-5 overflow-y-auto px-6 py-5">
          <!-- Badges -->
          <div class="flex items-center gap-2">
            <span
              class="inline-flex items-center rounded-full border border-primary/20 bg-primary/10 px-2.5 py-0.5 text-[11px] font-semibold text-primary"
            >
              {{ room.type }}
            </span>

            <span
              class="inline-flex items-center rounded-full border px-2.5 py-0.5 text-[11px] font-semibold"
              :class="room.status === 'OPEN'
                ? 'border-emerald-200 bg-emerald-100 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-300'
                : room.status === 'LOCKED'
                  ? 'border-rose-200 bg-rose-100 text-rose-800 dark:border-rose-800 dark:bg-rose-900/30 dark:text-rose-300'
                  : 'border-amber-300 bg-amber-50 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300'"
            >
              {{ room.status }}
            </span>
          </div>

          <!-- Room info (luôn chỉnh sửa được) -->
          <div>
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Thông tin Room
            </p>

            <div class="grid grid-cols-2 gap-2.5">
              <!-- Tên Room -->
              <div class="col-span-2 rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Tên Room
                </p>
                <UiInput
                  v-model="form.name"
                  class="h-8 text-[13px]"
                  placeholder="VD: Team Marketing"
                />
              </div>

              <!-- Trạng thái -->
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Trạng thái
                </p>
                <Select v-model="form.status" :disabled="isPendingRemoval">
                  <SelectTrigger class="h-8 w-full text-[13px]">
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
                <p v-if="isPendingRemoval" class="mt-1 text-[11px] text-amber-600">
                  Room đang Chờ xóa — không thể đổi trạng thái.
                </p>
              </div>

              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Số lần bị tố cáo
                </p>
                <div class="h-8 text-[13px] font-medium flex items-center justify-between">
                  {{ room.warning }}
                  <UiButton v-if="room.warning > 0" size="sm" variant="outline" @click="showReportDetail(room.name)">
                    Chi tiết
                  </UiButton>
                </div>
              </div>

              <!-- Mô tả -->
              <div class="col-span-2 rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Mô tả
                </p>
                <UiTextarea
                  v-model="form.description"
                  rows="3"
                  class="text-[13px]"
                  placeholder="Mô tả ngắn về room này..."
                />
              </div>

              <!-- Mã mời -->
              <div class="col-span-2 rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <div class="flex items-center gap-2">
                  <KeyRound class="h-4 w-4 text-muted-foreground" />
                  <p class="text-[13px] font-medium">
                    Mã mời:
                    <span class="font-mono">{{ room.inviteCode || '—' }}</span>
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div class="border-t border-border" />

          <!-- Owner (luôn chỉnh sửa được) -->
          <div>
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Chủ phòng
            </p>

            <div
              v-if="selectedOwner"
              class="flex items-center justify-between rounded-lg border border-border bg-muted/40 px-3 py-2"
            >
              <div class="flex items-center gap-3">
                <Crown class="h-4 w-4 text-amber-500" />
                <div>
                  <p class="text-[13px] font-medium">
                    {{ selectedOwner.username }}
                  </p>
                  <p v-if="selectedOwner.email" class="text-[11px] text-muted-foreground">
                    {{ selectedOwner.email }}
                  </p>
                </div>
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

          <div class="border-t border-border" />

          <!-- Members: count + button -->
          <div>
            <div class="mb-2.5 flex items-center justify-between">
              <p class="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
                <Users class="h-3.5 w-3.5" />
                Thành viên ({{ room.members?.length ?? 0 }})
              </p>

              <UiButton
                v-if="room.members?.length"
                variant="outline"
                size="sm"
                class="h-7 px-2.5 text-xs"
                @click="isMembersOpen = true"
              >
                Xem danh sách
              </UiButton>
            </div>

            <p
              v-if="!room.members?.length"
              class="text-sm text-muted-foreground"
            >
              Không có thành viên
            </p>
          </div>

          <!-- Spaces: count + button -->
          <div>
            <div class="mb-2.5 flex items-center justify-between">
              <p class="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
                <Layers class="h-3.5 w-3.5" />
                Spaces ({{ room.spaces?.length ?? 0 }})
              </p>

              <UiButton
                v-if="room.spaces?.length"
                variant="outline"
                size="sm"
                class="h-7 px-2.5 text-xs"
                @click="isSpacesOpen = true"
              >
                Xem danh sách
              </UiButton>
            </div>

            <p
              v-if="!room.spaces?.length"
              class="text-sm text-muted-foreground"
            >
              Không có spaces
            </p>
          </div>

          <!-- Timestamps -->
          <div class="border-t border-border pt-4">
            <div class="flex items-center gap-5 text-[12px] text-muted-foreground">
              <div class="flex items-center gap-1">
                <Calendar class="h-3.5 w-3.5" />
                Tạo lúc: {{ formatTimestamp(room.createdAt) }}
              </div>
              <div class="flex items-center gap-1">
                <Calendar class="h-3.5 w-3.5" />
                Cập nhật lúc: {{ formatTimestamp(room.updatedAt) }}
              </div>
            </div>
          </div>

          <p v-if="formError" class="text-[12px] text-red-500">
            {{ formError }}
          </p>
        </div>

        <!-- Footer -->
        <div class="flex justify-end gap-2 border-t border-border px-6 py-4">
          <UiButton variant="outline" size="sm" :disabled="isSubmitting" @click="isOpen = false">
            Đóng
          </UiButton>
          <UiButton size="sm" :disabled="isSubmitting" @click="handleSubmitForm">
            {{ isSubmitting ? 'Đang lưu...' : 'Lưu' }}
          </UiButton>
        </div>
      </template>
    </DialogContent>
  </Dialog>

  <!-- Members sub-dialog -->
  <MemberDialog
    v-model:open="isMembersOpen"
    :members="room?.members ?? []"
  />

  <SpacesDialog
    v-model:open="isSpacesOpen"
    :spaces="room?.spaces ?? []"
  />
</template>
