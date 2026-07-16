<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

import type { Member } from '../types/RoomTypes'

import { roomService } from '../service/roomService'

const props = defineProps<{
  open: boolean
  roomId: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const router = useRouter()

const members = ref<Member[]>([])
const loading = ref(false)

async function fetchMembers() {
  if (!props.roomId)
    return
  loading.value = true
  try {
    members.value = await roomService.getRoomMembers(props.roomId)
  }
  finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen)
      fetchMembers()
  },
)

function close() {
  emit('update:open', false)
}

function showUserDetail(userEmail: string) {
  router.push(`/users?keyword=${userEmail}`)
}
</script>

<template>
  <Dialog :open="props.open" @update:open="val => emit('update:open', val)">
    <DialogContent class="max-w-[480px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-4">
        <DialogTitle class="text-[15px] font-semibold">
          Danh sách thành viên ({{ members.length }})
        </DialogTitle>
        <DialogDescription class="sr-only">
          Danh sách thành viên trong room
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[60vh] flex-col gap-2 overflow-y-auto px-6 py-4">
        <p v-if="loading" class="text-sm text-muted-foreground">
          Đang tải...
        </p>

        <template v-else>
          <div
            v-for="member in members"
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
              <div class="flex gap-3 items-center">
                <span class="text-[11px] font-medium text-primary">
                  {{ member.role }}
                </span>
                <UiButton size="sm" variant="outline" @click="showUserDetail(member.email)">
                  Chi tiết
                </UiButton>
              </div>
            </div>
          </div>

          <p v-if="!members.length" class="text-sm text-muted-foreground">
            Không có thành viên
          </p>
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
