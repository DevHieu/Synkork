// stores/notificationStore.ts
import { defineStore } from 'pinia'
import { toast } from 'vue-sonner'
import { notificationSocket } from '@/services/websocket/notificationSocket'
import router from '@/routers'
import type { NotificationDTO } from '@/types/Notification'
import { getNotifications, markNotificationAsRead, deleteNotification } from '@/services/notificationService'
import { useSpaceStore } from '@/stores/spaceStore'
import { useRoomsStore } from '@/stores/roomStore'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [] as NotificationDTO[]
  }),

  actions: {
    async fetchNotifications() {
      try {
        this.notifications = await getNotifications()
      } catch (error) {
        console.error('Lỗi tải thông báo:', error)
      }
    },

    async connect() {
      await notificationSocket.subscribeNotifications(
        (notification: NotificationDTO) => {
          this.addNotification(notification)
        }
      )
    },

    disconnect() {
      notificationSocket.unsubscribeNotifications()
    },

    addNotification(notification: NotificationDTO) {
      console.log('[notification]', notification)
      this.notifications.unshift(notification)

      const path = getNotificationPath(notification)

      toast(notificationMessage(notification), {
        description: notification.actorName 
                    ? `từ ${notification.actorName}` 
                    : 'Thông báo hệ thống',
        action: path ? {
          label: 'Xem',
          onClick: async () => {
            await this.markAsRead(notification.id)

            if(notification.roomId && notification.spaceId) {
              const roomStore = useRoomsStore()
              const currentRoomId = roomStore.currentRoom?.id

              if(currentRoomId != notification.roomId) {
                const targetRoom = roomStore.rooms.find(r => r.id === notification.roomId)

                if(targetRoom){
                  await roomStore.changeRoom(targetRoom, notification.spaceId, notification.type)
                }
              } else {
                const spaceStore = useSpaceStore()

                await spaceStore.changeSpaceById(
                  notification.spaceId,
                  notification.type
                ) 
              }
            } 
            router.push({
              path,
              query: {
                refId: notification.refId
              }
            })
          }
        } : undefined,
        duration: 5000,
      })
    },

    async markAsRead(id: string) {
      try {
        await markNotificationAsRead(id)
        const n = this.notifications.find(n => n.id === id)

        if (n) n.read = true
      } catch (error) {
        console.error('Lỗi đánh dấu đã đọc:', error)
      }
    },

    markAllAsRead() {
      this.notifications.forEach(n => { n.read = true })
    },

    async removeNotification(id: string) {
      try {
        await deleteNotification(id)
        this.notifications = this.notifications.filter(n => n.id !== id)
      } catch (error) {
        console.error('Lỗi xóa thông báo:', error)
      }
    },

    clearNotifications() {
      try {
        this.notifications.map(n => deleteNotification(n.id))
      this.notifications = []
      } catch (error) {
        console.error('Lỗi xóa thông báo:', error)
      }  
    },

  },

  getters: {
    unreadCount: (state): number => state.notifications.filter(n => !n.read).length,
  }
})

function notificationMessage(n: NotificationDTO) {
  switch (n.refType) {

    case 'CARD_ASSIGNED': return 'Bạn vừa được assign vào một task'
    case 'CARD_DUE_SOON': return 'Nhắc nhở: Task của bạn sắp đến hạn'
    case 'CARD_OVER_DUE': return 'Nhắc nhở: Task của bạn đã quá hạn'

    case 'FRIEND_REQUEST': return 'Bạn có lời mời kết bạn mới'
    case 'FRIEND_REJECT': return 'Lời mời kết bạn của bạn đã bị từ chối'
    case 'FRIEND_ACCEPT': return 'Lời mời kết bạn của bạn đã được chấp nhận'

    case 'NOTE_REMINDER': return 'Nhắc nhở: Ghi chú sắp đến hạn'

    case 'EVENT_REMINDER': return 'Nhắc nhở: Sự kiện sắp diễn ra'

    default: return 'Bạn có thông báo mới'
  }
}

function getNotificationPath(n: NotificationDTO) {
  if (n.type === 'FRIEND') return '/me/friends'

  // if (!n.roomId || !n.spaceId) return null

  switch (n.type) {
    case 'TASK':
      return `/rooms/task/${n.roomId}/${n.spaceId}`

    case 'NOTE':
      return `/rooms/note/${n.roomId}/${n.spaceId}`

    case 'CALENDAR':
      return `/rooms/calendar/${n.roomId}/${n.spaceId}`

    case 'CHAT':
      return `/rooms/chat/${n.roomId}/${n.spaceId}`
  
    default:
      return null
  }
}