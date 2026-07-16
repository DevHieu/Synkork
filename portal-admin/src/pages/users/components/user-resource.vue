<script lang="ts" setup>
import { DoorOpen, Eye, LoaderCircle, Users } from '@lucide/vue'
import { computed, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import ConfirmDialog from '@/components/confirm-dialog.vue'
import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import { Button as UiButton } from '@/components/ui/button'
import RoomDetailDialog from '@/pages/rooms/components/RoomDetailDialog.vue'

import type { User, UserRoom } from '../types/userTypes'

import { userService } from '../services/userService'
import UserForm from './user-form.vue'

const props = defineProps<{
  user?: User
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const detail = ref<User | null>(null)
const isLoadingDetail = ref(false)
const loadError = ref('')
const selectedRoomId = ref('')
const isRoomDetailOpen = ref(false)
const kickTarget = ref<UserRoom | null>(null)
const isKickConfirmOpen = ref(false)
const isKicking = ref(false)

const currentUser = computed(() => detail.value ?? props.user)
const title = computed(() => currentUser.value?.id ? 'Chi tiết người dùng' : 'Tạo người dùng mới')
const description = computed(() => currentUser.value?.id
  ? `Thông tin và các phòng ${currentUser.value.username} đang tham gia`
  : 'Tạo tài khoản người dùng mới')

async function loadDetail() {
  if (!props.user?.id)
    return

  isLoadingDetail.value = true
  loadError.value = ''
  try {
    detail.value = await userService.getById(props.user.id)
  }
  catch (error) {
    console.error(error)
    loadError.value = 'Không thể tải danh sách phòng của người dùng.'
  }
  finally {
    isLoadingDetail.value = false
  }
}

watch(() => props.user?.id, loadDetail, { immediate: true })

function viewRoom(room: UserRoom) {
  selectedRoomId.value = room.roomId
  isRoomDetailOpen.value = true
}

function askKick(room: UserRoom) {
  kickTarget.value = room
  isKickConfirmOpen.value = true
}

async function confirmKick() {
  if (!props.user?.id || !kickTarget.value)
    return

  isKicking.value = true
  try {
    await userService.kickFromRoom(props.user.id, kickTarget.value.membershipId)
    toast.success('Đã đuổi người dùng khỏi phòng')
    isKickConfirmOpen.value = false
    await loadDetail()
  }
  catch (error: any) {
    toast.error(error?.response?.data?.message || 'Không thể đuổi người dùng khỏi phòng')
  }
  finally {
    isKicking.value = false
  }
}

function onSaved(user: User) {
  detail.value = { ...detail.value, ...user } as User
  emit('saved', user)
}
</script>

<template>
  <div>
    <ModalHeader class="border-b px-6 py-5">
      <ModalTitle>{{ title }}</ModalTitle>
      <ModalDescription>{{ description }}</ModalDescription>
    </ModalHeader>

    <div class="grid max-h-[75vh] gap-6 overflow-y-auto p-6 md:grid-cols-[minmax(0,1fr)_minmax(280px,0.9fr)]">
      <UserForm :user="currentUser ?? undefined" @close="$emit('close')" @saved="onSaved" />

      <section v-if="currentUser?.id" class="space-y-3 border-t pt-5 md:border-l md:border-t-0 md:pl-6 md:pt-0">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="flex items-center gap-2 text-sm font-semibold">
              <Users class="h-4 w-4" />
              Phòng đang tham gia
            </h3>
            <p class="mt-1 text-xs text-muted-foreground">
              Kick chỉ đổi trạng thái membership, dữ liệu cũ vẫn được giữ lại.
            </p>
          </div>
          <span class="rounded-full bg-muted px-2 py-0.5 text-xs font-medium">
            {{ currentUser.rooms?.length ?? 0 }}
          </span>
        </div>

        <div v-if="isLoadingDetail" class="flex items-center justify-center gap-2 py-10 text-sm text-muted-foreground">
          <LoaderCircle class="h-4 w-4 animate-spin" />
          Đang tải...
        </div>
        <p v-else-if="loadError" class="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          {{ loadError }}
        </p>
        <p v-else-if="!currentUser.rooms?.length" class="rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground">
          Người dùng hiện không tham gia phòng nào.
        </p>
        <div v-else class="space-y-2">
          <div
            v-for="room in currentUser.rooms"
            :key="room.membershipId"
            class="rounded-lg border bg-muted/20 p-3"
          >
            <div class="mb-3 flex items-start justify-between gap-3">
              <div class="min-w-0">
                <p class="truncate text-sm font-medium">
                  {{ room.type === 'DM' ? 'Tin nhắn trực tiếp' : (room.name || 'Phòng không tên') }}
                </p>
                <p class="mt-0.5 text-xs text-muted-foreground">
                  {{ room.role }} · {{ room.roomStatus }}
                </p>
              </div>
              <span class="rounded-full bg-emerald-100 px-2 py-0.5 text-[11px] font-medium text-emerald-700">
                {{ room.memberStatus }}
              </span>
            </div>
            <div class="flex gap-2">
              <UiButton variant="outline" size="sm" class="h-8 flex-1 gap-1 text-xs" @click="viewRoom(room)">
                <Eye class="h-3.5 w-3.5" />
                Chi tiết phòng
              </UiButton>
              <UiButton variant="destructive" size="sm" class="h-8 flex-1 gap-1 text-xs" @click="askKick(room)">
                <DoorOpen class="h-3.5 w-3.5" />
                Đuổi khỏi phòng
              </UiButton>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>

  <RoomDetailDialog
    v-if="selectedRoomId"
    v-model:open="isRoomDetailOpen"
    :room-id="selectedRoomId"
  />

  <ConfirmDialog
    v-model:open="isKickConfirmOpen"
    destructive
    :close-on-confirm="false"
    :is-loading="isKicking"
    cancel-button-text="Hủy"
    confirm-button-text="Đuổi khỏi phòng"
    @confirm="confirmKick"
  >
    <template #title>
      Đuổi {{ currentUser?.username }} khỏi phòng?
    </template>
    <template #description>
      Membership sẽ chuyển sang KICKED. Tin nhắn, task và lịch liên quan vẫn được giữ nguyên.
    </template>
  </ConfirmDialog>
</template>
