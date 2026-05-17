<template>
  <div
  class="w-[360px] max-h-[500px]
         bg-white rounded-2xl
         shadow-2xl border
         overflow-hidden z-[9999]"
>
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b">
      <span class="font-semibold">Thông báo</span>

      <button
        v-if="unreadCount > 0"
        @click="store.markAllAsRead()"
        class="text-sm text-blue-500 hover:underline"
      >
        Đánh dấu tất cả đã đọc
      </button>
    </div>

    <!-- List -->
    <ul class="max-h-[420px] overflow-y-auto">
      <li
        v-for="n in notifications"
        :key="n.id"
        @click="handleClick(n)"
        :class="[
          'flex items-start gap-3 px-4 py-3 cursor-pointer transition',
          n.read
            ? 'hover:bg-gray-50'
            : 'bg-blue-50 hover:bg-blue-100'
        ]"
      >
        <!-- avatar -->
        <img
          :src="n.actorAvatar ?? '/default-avatar.png'"
          class="w-10 h-10 rounded-full object-cover flex-shrink-0"
        />

        <!-- content -->
        <div class="flex-1 min-w-0">
          <p class="text-sm text-gray-700 leading-snug break-words">
            <span class="font-semibold">
              {{ n.actorName }}
            </span>

            {{ notificationMessage(n) }}
          </p>

          <p class="text-xs text-gray-400 mt-1">
            {{ timeAgo(n.createdAt) }}
          </p>
        </div>

        <!-- dot -->
        <span
          v-if="!n.read"
          class="w-2 h-2 rounded-full bg-blue-500 mt-2 flex-shrink-0"
        />
      </li>

      <li
        v-if="notifications.length === 0"
        class="px-4 py-8 text-center text-gray-400"
      >
        Không có thông báo nào
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import type { NotificationDTO } from '@/types/Notification'

const emit = defineEmits<{ (e: 'close'): void }>()

const store = useNotificationStore()
const { notifications, unreadCount } = storeToRefs(store)
const router = useRouter()

function notificationMessage(n: NotificationDTO): string {
  switch (n.refType) {
    case 'CARD_ASSIGNED':    return 'đã assign bạn vào một task'
    case 'CARD_DUE_SOON':    return 'task của bạn sắp đến hạn'
    case 'CARD_OVER_DUE':    return 'task của bạn đã quá hạn'
    case 'FRIEND_REQUEST':   return 'đã gửi cho bạn lời mời kết bạn'
    case 'EVENT_REMINDER':   return 'nhắc nhở: sự kiện sắp diễn ra'
    default:                 return 'có thông báo mới'
  }
}

function handleClick(n: NotificationDTO): void {
  store.markAsRead(n.id)
  if (n.spaceId) router.push(`/space/${n.spaceId}`)
  emit('close')
}

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return 'Vừa xong'
  if (minutes < 60) return `${minutes} phút trước`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} giờ trước`
  return `${Math.floor(hours / 24)} ngày trước`
}
</script>