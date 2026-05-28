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
        <Button v-if="unreadCount > 0" variant="ghost" size="sm"
          class="text-xs text-muted-foreground hover:text-foreground h-7 px-2" @click="notiStore.markAllAsRead()">
          Đánh dấu đã đọc
        </Button>

        <Button v-if="notifications.length > 0" variant="ghost" size="icon"
          class="h-7 w-7 text-muted-foreground hover:text-destructive" @click="confirmDelete">
          <Trash2 class="w-4 h-4" />
        </Button>
      </div>
    </div>

    <!-- List -->
    <ScrollArea class="h-[420px]">
      <div class="py-1">
        <button v-for="n in notifications" :key="n.id" @click="handleClick(n)"
          class="group w-full flex items-start gap-3 px-4 py-3 text-left transition-colors" :class="n.read
            ? 'hover:bg-muted/50'
            : 'bg-blue-50/60 dark:bg-blue-950/20 hover:bg-blue-100/60 dark:hover:bg-blue-900/20'">
          <!-- Avatar -->
          <Avatar class="w-9 h-9 flex-shrink-0">
            <AvatarImage :src="n.actorAvatar ?? ''" />
            <AvatarFallback class="text-xs bg-muted">
              {{ n.actorName?.charAt(0)?.toUpperCase() }}
            </AvatarFallback>
          </Avatar>

          <!-- Content -->
          <div class="flex-1 min-w-0 space-y-0.5">
            <p class="text-sm text-foreground leading-snug break-words">
              <span class="font-semibold">{{ n.actorName }}</span>
              {{ ' ' }}{{ notificationMessage(n) }}
            </p>
            <p class="text-xs text-muted-foreground">{{ timeAgo(n.createdAt) }}</p>
          </div>

          <!-- Unread dot -->
          <div class="flex items-start gap-2">
            <!-- unread dot -->
            <span v-if="!n.read" class="w-2 h-2 rounded-full bg-blue-500 mt-2 flex-shrink-0" />

            <!-- delete button -->
            <Button variant="ghost" size="icon"
              class="h-7 w-7 opacity-0 group-hover:opacity-100 hover:text-destructive transition-opacity"
              @click.stop="notiStore.removeNotification(n.id)">
              <Trash2 class="w-4 h-4" />
            </Button>
          </div>
        </button>


        <!-- Empty state -->
        <div v-if="notifications.length === 0"
          class="flex flex-col items-center justify-center py-12 text-muted-foreground gap-2">
          <BellOff class="w-8 h-8 opacity-40" />
          <p class="text-sm">Không có thông báo nào</p>
        </div>
      </div>
    </ScrollArea>
  </div>
  <DeleteConfirmDialog v-model:open="isDeleteOpen" :title="'Xóa tất cả thông báo?'"
    description="Bạn không thể khôi phục lại các thông báo đã xóa." @confirm="clearAll" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { BellOff, Trash2 } from 'lucide-vue-next'

import { useNotificationStore } from '@/stores/notificationStore'
import { useSpaceStore } from '@/stores/spaceStore'

import type { NotificationDTO } from '@/types/Notification'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'

import DeleteConfirmDialog from '@/components/dialog/DeleteConfirmDialog.vue'

const emit = defineEmits<{
  (e: 'close'): void
}>()

const router = useRouter()

const notiStore = useNotificationStore()

const { notifications, unreadCount } = storeToRefs(notiStore)

const isDeleteOpen = ref(false)

function confirmDelete() {
  isDeleteOpen.value = true
}

function clearAll() {
  notiStore.clearNotifications()
  isDeleteOpen.value = false
}

async function handleClick(notification: NotificationDTO) {
  await notiStore.markAsRead(notification.id)
  emit('close')
  const path = getNotificationPath(notification)
  if (!path) return

  if (notification.spaceId) {
    const spaceStore = useSpaceStore()
    spaceStore.changeSpaceById(
      notification.spaceId,
      notification.type
    )
  }

  router.push({
    path,
    query: {
      refId: notification.refId
    }
  })
}

function notificationMessage(notification: NotificationDTO) {
  switch (notification.refType) {
    case 'CARD_ASSIGNED':
      return 'đã assign bạn vào một task'

    case 'CARD_DUE_SOON':
      return 'task của bạn sắp đến hạn'

    case 'CARD_OVER_DUE':
      return 'task của bạn đã quá hạn'

    case 'FRIEND_REQUEST':
      return 'đã gửi cho bạn lời mời kết bạn'

    case 'FRIEND_REJECT':
      return 'đã từ chối lời mời kết bạn của bạn'

    case 'FRIEND_ACCEPT':
      return 'đã chấp nhận lời mời kết bạn của bạn'

    case 'NOTE_REMINDER':
      return 'Nhắc nhở: Ghi chú sắp đến hạn'

    case 'EVENT_REMINDER':
      return 'Nhắc nhở: Sự kiện sắp diễn ra'

    default:
      return 'có thông báo mới'
  }
}

function getNotificationPath(notification: NotificationDTO) {
  if (notification.type === 'FRIEND') return '/me/friends'
  
  if (!notification.roomId || !notification.spaceId) return null

  switch (notification.type) {
    case 'TASK':
      return `/rooms/task/${notification.roomId}/${notification.spaceId}`

    case 'NOTE':
      return `/rooms/note/${notification.roomId}/${notification.spaceId}`

    case 'CALENDAR':
      return `/rooms/calendar/${notification.roomId}/${notification.spaceId}`

    case 'CHAT':
      return `/rooms/chat/${notification.roomId}/${notification.spaceId}`

    default:
      return null
  }
}

function timeAgo(dateStr: string) {
  const diff = Date.now() - new Date(dateStr).getTime()
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1) {
    return 'Vừa xong'
  }

  if (minutes < 60) {
    return `${minutes} phút trước`
  }

  const hours = Math.floor(minutes / 60)

  if (hours < 24) {
    return `${hours} giờ trước`
  }

  return `${Math.floor(hours / 24)} ngày trước`
}
</script>