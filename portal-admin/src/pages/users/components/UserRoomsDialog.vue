<script setup lang="ts">
import { Eye, LoaderIcon } from '@lucide/vue'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { formatTimestamp } from '@/utils/date.utils'

import type { UserJoinedRoom } from '../types/userTypes'

import { userService } from '../services/userService'

const props = defineProps<{
  open: boolean
  userId: string
  username?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const router = useRouter()

const isOpen = computed({
  get: () => props.open,
  set: value => emit('update:open', value),
})

const rooms = ref<UserJoinedRoom[]>([])
const loading = ref(false)
const errorMessage = ref('')

function roomDisplayName(room: UserJoinedRoom) {
  return room.type === 'DM' ? 'Tin nhắn trực tiếp' : room.name
}

function roomStatusLabel(status: string) {
  return {
    OPEN: 'Đang mở',
    LOCKED: 'Đã khóa',
    PENDING_REMOVAL: 'Chờ xóa',
  }[status] ?? status
}

function memberStatusLabel(status: string) {
  return {
    ACTIVE: 'Đang tham gia',
    INACTIVE: 'Ngừng tham gia',
  }[status] ?? status
}

async function fetchRooms() {
  if (!props.userId)
    return

  loading.value = true
  errorMessage.value = ''
  try {
    rooms.value = await userService.getUserRooms(props.userId)
  }
  catch (error) {
    console.error('Failed to fetch user rooms:', error)
    rooms.value = []
    errorMessage.value = 'Không thể tải danh sách phòng của user này'
  }
  finally {
    loading.value = false
  }
}

function showRoomDetail(room: UserJoinedRoom) {
  isOpen.value = false
  router.push({
    path: '/rooms',
    query: { keyword: room.name || room.id },
  })
}

watch(() => props.open, (opened) => {
  if (opened)
    fetchRooms()
})
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="max-w-[620px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-4">
        <DialogTitle class="text-[15px] font-semibold">
          Phòng đã tham gia
        </DialogTitle>
        <DialogDescription>
          {{ username || 'User' }} đang có trong {{ rooms.length }} phòng
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[62vh] flex-col gap-2 overflow-y-auto px-6 py-4">
        <div v-if="loading" class="flex items-center gap-2 text-sm text-muted-foreground">
          <LoaderIcon class="h-4 w-4 animate-spin" />
          Đang tải...
        </div>

        <p v-else-if="errorMessage" class="text-sm text-destructive">
          {{ errorMessage }}
        </p>

        <template v-else>
          <div
            v-for="room in rooms"
            :key="room.id"
            class="rounded-lg border border-border bg-muted/40 px-3 py-2.5"
          >
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <p class="truncate text-[13px] font-medium">
                    {{ roomDisplayName(room) }}
                  </p>
                  <Badge variant="outline" class="px-2 text-[11px]">
                    {{ room.type }}
                  </Badge>
                  <Badge
                    variant="outline"
                    class="px-2 text-[11px]"
                    :class="room.status === 'OPEN'
                      ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
                      : room.status === 'LOCKED'
                        ? 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-800 dark:bg-rose-950 dark:text-rose-300'
                        : 'border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-300'"
                  >
                    {{ roomStatusLabel(room.status) }}
                  </Badge>
                </div>

                <p class="mt-1 text-[12px] text-muted-foreground">
                  Vai trò: {{ room.memberRole }} · {{ memberStatusLabel(room.memberStatus) }}
                </p>
                <p class="mt-0.5 text-[12px] text-muted-foreground">
                  Thành viên: {{ room.memberCount }} · Cảnh báo: {{ room.warning }}
                </p>
                <p v-if="room.joinedAt" class="mt-0.5 text-[12px] text-muted-foreground">
                  Tham gia lúc: {{ formatTimestamp(room.joinedAt) }}
                </p>
              </div>

              <div class="flex shrink-0 items-center gap-1.5">
                <UiButton size="sm" variant="outline" class="h-8 gap-1 px-2 text-xs" @click="showRoomDetail(room)">
                  <Eye class="h-3.5 w-3.5" />
                  Chi tiết
                </UiButton>
              </div>
            </div>
          </div>

          <p v-if="!rooms.length" class="text-sm text-muted-foreground">
            User này chưa tham gia phòng nào
          </p>
        </template>
      </div>
    </DialogContent>
  </Dialog>
</template>
