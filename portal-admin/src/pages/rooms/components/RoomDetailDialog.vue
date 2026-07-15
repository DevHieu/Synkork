<script lang="ts" setup>
import {
  Calendar,
  Crown,
  KeyRound,
  Layers,
  Users,
} from '@lucide/vue'
import { computed, ref, watch } from 'vue'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { formatTimestamp } from '@/utils/date.utils'

import type { RoomDetail } from '../types/RoomTypes'

import { roomService } from '../service/roomService'

const props = defineProps<{
  roomId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'edit': [room: RoomDetail]
}>()

const isOpen = computed({
  get: () => props.open,
  set: val => emit('update:open', val),
})

const room = ref<RoomDetail | null>(null)
const isLoading = ref(false)
const loadError = ref('')

const isMembersOpen = ref(false)
const isSpacesOpen = ref(false)

const spaceGroups = computed(() => {
  const spaces = room.value?.spaces ?? []
  const groups = [
    {
      key: 'CHAT',
      label: 'Chat',
      items: spaces.filter(space => normalizeSpaceType(space.type) === 'CHAT'),
    },
    {
      key: 'VOICE',
      label: 'Voice',
      items: spaces.filter(space => normalizeSpaceType(space.type) === 'VOICE'),
    },
    {
      key: 'CALENDAR',
      label: 'Calendar',
      items: spaces.filter(space => normalizeSpaceType(space.type) === 'CALENDAR'),
    },
    {
      key: 'NOTE',
      label: 'Note',
      items: spaces.filter(space => normalizeSpaceType(space.type) === 'NOTE'),
    },
    {
      key: 'TASK',
      label: 'Task',
      items: spaces.filter(space => normalizeSpaceType(space.type) === 'TASK'),
    },
  ]

  const groupedIds = new Set(groups.flatMap(group => group.items.map(space => space.id)))
  const ungroupedSpaces = spaces.filter(space => !groupedIds.has(space.id))

  return groups.map(group => ({
    ...group,
    items: group.key === 'CHAT'
      ? [...group.items, ...ungroupedSpaces]
      : group.items,
  }))
})

watch(() => props.open, async (opened) => {
  if (!opened || !props.roomId)
    return

  isLoading.value = true
  loadError.value = ''
  room.value = null

  try {
    room.value = await roomService.getRoomDetail(props.roomId)
  }
  catch (error) {
    console.error('Lỗi khi tải chi tiết room:', error)
    loadError.value = 'Không thể tải chi tiết room này'
  }
  finally {
    isLoading.value = false
  }
}, { immediate: true })

function handleEdit() {
  if (room.value) {
    emit('edit', room.value)
    isOpen.value = false
  }
}

function normalizeSpaceType(type: string) {
  const normalized = type?.toUpperCase().replace(/[\s_-]/g, '') ?? ''

  if (normalized.includes('VOICE') || normalized.includes('CALL'))
    return 'VOICE'
  if (normalized.includes('CALENDAR') || normalized.includes('EVENT'))
    return 'CALENDAR'
  if (normalized.includes('NOTE') || normalized.includes('DOC'))
    return 'NOTE'
  if (normalized.includes('TASK') || normalized.includes('TODO'))
    return 'TASK'

  return 'CHAT'
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
                ? 'border-emerald-300 bg-emerald-100/50 text-emerald-700'
                : 'border-neutral-300 bg-neutral-200/50 text-neutral-700'"
            >
              {{ room.status }}
            </span>
          </div>

          <!-- Room info -->
          <div>
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Thông tin Room
            </p>

            <div class="grid grid-cols-2 gap-2.5">
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Tên Room
                </p>
                <p class="text-[13px] font-medium">
                  {{ room.type === 'DM' ? 'Tin nhắn trực tiếp' : room.name }}
                </p>
              </div>

              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Thành viên
                </p>
                <p class="text-[13px] font-medium">
                  {{ room.memberCount }}
                </p>
              </div>

              <div class="col-span-2 rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Mô tả
                </p>
                <p class="text-[13px]">
                  {{ room.description || '—' }}
                </p>
              </div>

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

          <!-- Owner -->
          <div v-if="room.owner">
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Chủ phòng
            </p>

            <div class="rounded-lg border border-border bg-muted/40 p-3">
              <div class="flex items-center gap-3">
                <Crown class="h-4 w-4 text-amber-500" />
                <div>
                  <p class="text-[13px] font-medium">
                    {{ room.owner.username }}
                  </p>
                  <p class="text-[12px] text-muted-foreground">
                    {{ room.owner.email }}
                  </p>
                </div>
              </div>
            </div>
          </div>

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
        </div>

        <!-- Footer -->
        <div class="flex justify-end gap-2 border-t border-border px-6 py-4">
          <UiButton
            variant="outline"
            size="sm"
            @click="isOpen = false"
          >
            Đóng
          </UiButton>

          <UiButton
            v-if="room.type === 'GROUP'"
            size="sm"
            @click="handleEdit"
          >
            Sửa Room
          </UiButton>
        </div>
      </template>
    </DialogContent>
  </Dialog>

  <!-- Members sub-dialog -->
  <Dialog v-model:open="isMembersOpen">
    <DialogContent class="max-w-[480px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-4">
        <DialogTitle class="text-[15px] font-semibold">
          Danh sách thành viên ({{ room?.members?.length ?? 0 }})
        </DialogTitle>
        <DialogDescription class="sr-only">
          Danh sách thành viên trong room
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[60vh] flex-col gap-2 overflow-y-auto px-6 py-4">
        <div
          v-for="member in room?.members"
          :key="member.id"
          class="rounded-lg border border-border bg-muted/40 px-3 py-2.5"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-[13px] font-medium">
                {{ member.username }}
              </p>
              <p class="text-[12px] text-muted-foreground">
                {{ member.email }}
              </p>
            </div>
            <span class="text-[11px] font-medium text-primary">
              {{ member.role }}
            </span>
          </div>
        </div>
      </div>

      <div class="flex justify-end border-t border-border px-6 py-3">
        <UiButton
          variant="outline"
          size="sm"
          @click="isMembersOpen = false"
        >
          Đóng
        </UiButton>
      </div>
    </DialogContent>
  </Dialog>

  <!-- Spaces sub-dialog -->
  <Dialog v-model:open="isSpacesOpen">
    <DialogContent class="max-w-[760px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-4">
        <DialogTitle class="text-[15px] font-semibold">
          Danh sách Spaces ({{ room?.spaces?.length ?? 0 }})
        </DialogTitle>
        <DialogDescription class="sr-only">
          Danh sách spaces trong room
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[60vh] flex-col gap-4 overflow-y-auto px-6 py-4">
        <div
          v-for="group in spaceGroups"
          :key="group.key"
          class="space-y-2"
        >
          <p class="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
            {{ group.label }} ({{ group.items.length }})
          </p>

          <div class="flex flex-wrap gap-2">
            <div
              v-for="space in group.items"
              :key="space.id"
              class="rounded-lg border border-border bg-muted/40 px-3 py-2"
            >
              <p class="text-[13px] font-medium">
                {{ space.name }}
              </p>
            </div>

            <p
              v-if="!group.items.length"
              class="text-sm text-muted-foreground"
            >
              Chưa có space {{ group.label.toLowerCase() }}
            </p>
          </div>
        </div>
      </div>

      <div class="flex justify-end border-t border-border px-6 py-3">
        <UiButton
          variant="outline"
          size="sm"
          @click="isSpacesOpen = false"
        >
          Đóng
        </UiButton>
      </div>
    </DialogContent>
  </Dialog>
</template>
