// stores/notificationStore.ts
import { defineStore } from 'pinia'
import { toast } from 'vue-sonner'
import { notificationSocket } from '@/services/websocket/notificationSocket'
import router from '@/routers'
import type { NotificationDTO } from '@/types/Notification'
import { getNotifications, markNotificationAsRead } from '@/services/notificationService'

interface NotificationState {
  notifications: NotificationDTO[]
}

export const useNotificationStore = defineStore('notification', {
  state: (): NotificationState => ({
    notifications: [],
  }),

  getters: {
    unreadCount: (state): number => state.notifications.filter(n => !n.read).length,
  },

  actions: {
    async fetchNotifications(): Promise<void> {
      this.notifications = await getNotifications()
    },

    async connect(userId: string): Promise<void> {
  console.log('CONNECT NOTI:', userId)

  await notificationSocket.subscribeNotifications(
    userId,
    (notification: NotificationDTO) => {
      console.log('SOCKET RECEIVED:', notification)
      this.addNotification(notification)
    }
  )
},

    disconnect(userId: string): void {
      notificationSocket.unsubscribeNotifications(userId)
    },

    addNotification(notification: NotificationDTO): void {
      console.log('>>> addNotification called:', notification)
      this.notifications = [notification, ...this.notifications]

      toast(notificationMessage(notification), {
        description: `từ ${notification.actorName}`,
        action: notification.spaceId ? {
          label: 'Xem ngay',
          onClick: () => {
            this.markAsRead(notification.id)
            router.push(
              `/rooms/task/${notification.roomId}/${notification.spaceId}`
            )
          }
        } : undefined,
        duration: 5000,
      })
    },

    async markAsRead(id: string): Promise<void> {
      await markNotificationAsRead(id)
      const n = this.notifications.find(n => n.id === id)
      if (n) n.read = true
    },

    markAllAsRead(): void {
      this.notifications.forEach(n => { n.read = true })
    }
  }
})

function notificationMessage(n: NotificationDTO): string {
  switch (n.refType) {
    case 'CARD_ASSIGNED': return 'Bạn vừa được assign vào một task'
    case 'CARD_DUE_SOON': return 'Task của bạn sắp đến hạn'
    case 'CARD_OVER_DUE': return 'Task của bạn đã quá hạn'
    case 'FRIEND_REQUEST': return 'Bạn có lời mời kết bạn mới'
    case 'EVENT_REMINDER': return 'Nhắc nhở: sự kiện sắp diễn ra'
    default: return 'Bạn có thông báo mới'
  }
}