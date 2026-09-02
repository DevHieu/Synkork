<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { BellOff, Trash2, Loader2 } from 'lucide-vue-next'
import { useNotificationStore } from '@/features/notifications/stores/notificationStore'
import type { NotificationDTO } from '@/features/notifications/types/Notification'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'
import DeleteConfirmDialog from '@/components/dialog/DeleteConfirmDialog.vue'
import { getNotificationPath, timeAgo, notificationMessage } from '../composables/notification-helper'
import { navigateFromNotification } from '../services/notificationNavigation'

const emit = defineEmits<{
  (e: 'close'): void
}>()

const notiStore = useNotificationStore()
const { notifications, unreadCount } = storeToRefs(notiStore)

const isLoading = ref(false)
const notiDeleteId = ref<string | null>(null)
const isDeleteOpen = ref(false)
const isDeleteAllOpen = ref(false)

onMounted(async () => {
  isLoading.value = true
  try {
    notiStore.connect()
    await notiStore.fetchNotifications()
  } finally {
    isLoading.value = false
  }
})

onUnmounted(() => {
  notiStore.disconnect()
})

function confirmDeleteAll() {
  isDeleteAllOpen.value = true
}

function confirmDelete(notiId : string) {
  notiDeleteId.value = notiId
  isDeleteOpen.value = true
}

function clearAll() {
  notiStore.clearNotifications()
  isDeleteOpen.value = false
}

function deleteNoti() {
  if(notiDeleteId.value) {
    notiStore.removeNotification(notiDeleteId.value)
  }
  notiDeleteId.value = null
  isDeleteOpen.value = false
}

async function handleClick(notification: NotificationDTO) {
  console.log('[raw notification]', JSON.stringify(notification, null, 2))
  await notiStore.markAsRead(notification.id)

  const path = getNotificationPath(notification)
  console.log('[handleClick]', { type: notification.type, roomId: notification.roomId, spaceId: notification.spaceId, path })
  if (!path) return

  navigateFromNotification(notification, path)
}
</script>
<template>
  <div class="w-[380px] bg-background rounded-xl border border-border shadow-lg overflow-hidden">
    <!-- Header -->
    <div class="flex items-center justify-between px-5 py-4 border-b border-border">
      <div class="flex items-center gap-2">
        <h3 class="font-semibold text-sm text-foreground">Thông báo</h3>
        <Badge v-if="unreadCount > 0" variant="secondary" class="text-xs h-5 px-1.5">
          {{ unreadCount }}
        </Badge>
      </div>

      <div class="flex items-center gap-1">
        <Button
          v-if="unreadCount > 0"
          variant="ghost"
          size="sm"
          class="text-xs text-muted-foreground hover:text-foreground h-7 px-2 cursor-pointer"
          @click="notiStore.markAllNotiAsRead()"
        >
          Đánh dấu đã đọc
        </Button>

        <Button
          v-if="notifications.length > 0"
          variant="ghost"
          size="icon"
          class="h-7 w-7 text-muted-foreground hover:text-destructive cursor-pointer"
          @click="confirmDeleteAll"
        >
          <Trash2 class="w-4 h-4" />
        </Button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12 text-muted-foreground gap-2">
      <Loader2 class="w-5 h-5 animate-spin opacity-60" />
      <p class="text-sm">Đang tải thông báo...</p>
    </div>

    <!-- List -->
    <ScrollArea v-else class="h-[420px]">
      <div class="py-1">
        <button
          v-for="n in notifications"
          :key="n.id"
          @click="handleClick(n)"
          class="group w-full flex items-start gap-3 px-4 py-3 text-left transition-colors"
          :class="
            n.read
              ? 'hover:bg-muted/50'
              : 'bg-blue-50/60 dark:bg-blue-950/20 hover:bg-blue-100/60 dark:hover:bg-blue-900/20'
          "
        >
          <!-- Avatar -->
          <Avatar class="w-9 h-9 flex-shrink-0">
            <AvatarImage :src="n.actorAvatar ?? ''" />
            <AvatarFallback class="text-xs bg-muted">
              {{ n.actorName?.charAt(0)?.toUpperCase() }}
            </AvatarFallback>
          </Avatar>

          <!-- Content -->
          <div class="flex-1 min-w-0 space-y-0.5 cursor-pointer">
            <p class="text-sm text-foreground leading-snug break-words">
              <span class="font-semibold">{{ n.actorName }}</span>
              {{ ' ' }}{{ notificationMessage(n, 'dropdown') }}
            </p>
            <p class="text-xs text-muted-foreground">{{ timeAgo(n.createdAt) }}</p>
          </div>

          <!-- Unread dot + delete -->
          <div class="flex items-start gap-2">
            <span v-if="!n.read" class="w-2 h-2 rounded-full bg-blue-500 mt-2 flex-shrink-0" />

            <Button
              variant="ghost"
              size="icon"
              class="h-7 w-7 opacity-0 group-hover:opacity-100 hover:text-destructive transition-opacity cursor-pointer"
              @click.stop="confirmDelete(n.id)"
            >
              <Trash2 class="w-4 h-4" />
            </Button>
          </div>
        </button>

        <div
          v-if="notifications.length === 0"
          class="flex flex-col items-center justify-center py-12 text-muted-foreground gap-2"
        >
          <BellOff class="w-8 h-8 opacity-40" />
          <p class="text-sm">Không có thông báo nào</p>
        </div>
      </div>
    </ScrollArea>
  </div>

  <DeleteConfirmDialog
    v-model:open="isDeleteAllOpen"
    :title="'Xóa tất cả thông báo?'"
    description="Bạn không thể khôi phục lại các thông báo đã xóa."
    @confirm="clearAll"
  />

  <DeleteConfirmDialog
    v-model:open="isDeleteOpen"
    :title="'Xóa thông báo này?'"
    description="Bạn không thể khôi phục lại thông báo đã xóa."
    @confirm="deleteNoti()"
  />
</template>