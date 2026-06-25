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
</script>

<template>
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
        <div
          class="flex max-h-[70vh] flex-col gap-5 overflow-y-auto px-6 py-5"
        >
          <!-- badge -->
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

          <!-- room info -->
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
                  {{ room.type === 'DM' ? 'Direct Message' : room.name }}
                </p>
              </div>

              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Members
                </p>

                <p class="text-[13px] font-medium">
                  {{ room.memberCount }}
                </p>
              </div>

              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5 col-span-2">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Description
                </p>

                <p class="text-[13px]">
                  {{ room.description || '—' }}
                </p>
              </div>

              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5 col-span-2">
                <div class="flex items-center gap-2">
                  <KeyRound class="h-4 w-4 text-muted-foreground" />

                  <p class="text-[13px] font-medium">
                    Invite Code:
                    <span class="font-mono">
                      {{ room.inviteCode || '—' }}
                    </span>
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div class="border-t border-border" />

          <!-- owner -->
          <div v-if="room.owner">
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Owner
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

          <!-- members -->
          <div>
            <p class="mb-2.5 flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              <Users class="h-3.5 w-3.5" />
              Members ({{ room.members?.length ?? 0 }})
            </p>

            <div
              v-if="room.members?.length"
              class="space-y-2"
            >
              <div
                v-for="member in room.members"
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

            <p
              v-else
              class="text-sm text-muted-foreground"
            >
              Không có thành viên
            </p>
          </div>

          <!-- spaces -->
          <div>
            <p class="mb-2.5 flex items-center gap-2 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              <Layers class="h-3.5 w-3.5" />
              Spaces ({{ room.spaces?.length ?? 0 }})
            </p>

            <div
              v-if="room.spaces?.length"
              class="flex flex-wrap gap-2"
            >
              <div
                v-for="space in room.spaces"
                :key="space.id"
                class="rounded-lg border border-border bg-muted/40 px-3 py-2"
              >
                <p class="text-[13px] font-medium">
                  {{ space.name }}
                </p>

                <p class="text-[11px] text-muted-foreground">
                  {{ space.type }}
                </p>
              </div>
            </div>

            <p
              v-else
              class="text-sm text-muted-foreground"
            >
              Không có spaces
            </p>
          </div>

          <!-- timestamps -->
          <div class="border-t border-border pt-4">
            <div class="flex items-center gap-5 text-[12px] text-muted-foreground">
              <div class="flex items-center gap-1">
                <Calendar class="h-3.5 w-3.5" />
                Created:
                {{ formatTimestamp(room.createdAt) }}
              </div>

              <div class="flex items-center gap-1">
                <Calendar class="h-3.5 w-3.5" />
                Updated:
                {{ formatTimestamp(room.updatedAt) }}
              </div>
            </div>
          </div>
        </div>

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
</template>