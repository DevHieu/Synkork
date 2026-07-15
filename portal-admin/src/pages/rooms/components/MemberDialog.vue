<script lang="ts" setup>
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

const props = defineProps<{
  open: boolean
  members: Member[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const router = useRouter()

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
          Danh sách thành viên ({{ props.members.length }})
        </DialogTitle>
        <DialogDescription class="sr-only">
          Danh sách thành viên trong room
        </DialogDescription>
      </DialogHeader>

      <div class="flex max-h-[60vh] flex-col gap-2 overflow-y-auto px-6 py-4">
        <div
          v-for="member in props.members"
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
              <!-- @click="emit('view-detail', member)" -->
              <UiButton size="sm" variant="outline" @click="showUserDetail(member.email)">
                Chi tiết
              </UiButton>
            </div>
          </div>
        </div>

        <p v-if="!props.members.length" class="text-sm text-muted-foreground">
          Không có thành viên
        </p>
      </div>

      <div class="flex justify-end border-t border-border px-6 py-3">
        <UiButton variant="outline" size="sm" @click="close">
          Đóng
        </UiButton>
      </div>
    </DialogContent>
  </Dialog>
</template>
