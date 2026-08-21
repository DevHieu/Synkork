<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

import type { Space } from '../types/RoomTypes'

import { roomService } from '../service/roomService'

const props = defineProps<{
  open: boolean
  roomId: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const spaces = ref<Space[]>([])
const loading = ref(false)

async function fetchSpaces() {
  if (!props.roomId)
    return
  loading.value = true
  try {
    spaces.value = await roomService.getRoomSpaces(props.roomId)
  }
  finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen)
      fetchSpaces()
  },
)

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

const spaceGroups = computed(() => {
  const list = spaces.value ?? []
  const groups = [
    { key: 'CHAT', label: 'Chat', items: list.filter(s => normalizeSpaceType(s.type) === 'CHAT') },
    { key: 'VOICE', label: 'Voice', items: list.filter(s => normalizeSpaceType(s.type) === 'VOICE') },
    { key: 'CALENDAR', label: 'Calendar', items: list.filter(s => normalizeSpaceType(s.type) === 'CALENDAR') },
    { key: 'NOTE', label: 'Note', items: list.filter(s => normalizeSpaceType(s.type) === 'NOTE') },
    { key: 'TASK', label: 'Task', items: list.filter(s => normalizeSpaceType(s.type) === 'TASK') },
  ]

  const groupedIds = new Set(groups.flatMap(g => g.items.map(s => s.id)))
  const ungrouped = list.filter(s => !groupedIds.has(s.id))

  return groups.map(g => ({
    ...g,
    items: g.key === 'CHAT' ? [...g.items, ...ungrouped] : g.items,
  }))
})

function close() {
  emit('update:open', false)
}
</script>

<template>
  <Dialog :open="props.open" @update:open="val => emit('update:open', val)">
    <DialogContent class="max-w-[760px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-4">
        <DialogTitle class="text-[15px] font-semibold">
          Danh sách Spaces ({{ spaces.length }})
        </DialogTitle>
        <DialogDescription class="sr-only">
          Danh sách spaces trong room
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[60vh] flex-col gap-4 overflow-y-auto px-6 py-4">
        <p v-if="loading" class="text-sm text-muted-foreground">
          Đang tải...
        </p>

        <template v-else>
          <div v-for="group in spaceGroups" :key="group.key" class="space-y-2">
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

              <p v-if="!group.items.length" class="text-sm text-muted-foreground">
                Chưa có space {{ group.label.toLowerCase() }}
              </p>
            </div>
          </div>
        </template>
      </div>

      <div class="flex justify-end border-t border-border px-6 py-3">
        <UiButton variant="outline" size="sm" @click="close">
          Đóng
        </UiButton>
      </div>
    </DialogContent>
  </Dialog>
</template>
