import { defineStore } from "pinia";
import { toast } from "vue-sonner";
import { notificationSocket } from "../services/notificationSocket";

import type { NotificationDTO } from "@/features/notifications/types/Notification";
import {
  getNotifications,
  markNotificationAsRead,
  deleteNotification,
  markAllAsRead
} from "@/features/notifications/services/notificationService";

import globalAudio from "@/utils/appAudioManager";
import { navigateFromNotification } from "../services/notificationNavigation";
import { getNotificationPath, notificationMessage } from "../composables/notification-helper";

export const useNotificationStore = defineStore("notification", {
  state: () => ({
    notifications: [] as NotificationDTO[],
  }),

  actions: {
    async fetchNotifications() {
      try {
        this.notifications = await getNotifications();
      } catch (error) {
        console.error("Lỗi tải thông báo:", error);
      }
    },

    connect() {
      notificationSocket.subscribeNotifications(
        (notification: NotificationDTO) => {
          globalAudio.playSystemSound("/assets/sounds/notiSound.wav");
          this.addNotification(notification);
        },
      );
    },

    disconnect() {
      notificationSocket.unsubscribeNotifications();
    },

    addNotification(notification: NotificationDTO) {
      console.log("[notification]", notification);
      this.notifications.unshift(notification);

      const path = getNotificationPath(notification);

      toast(notificationMessage(notification, 'toast'), {
        description: notification.actorName
          ? `từ ${notification.actorName}`
          : "Thông báo hệ thống",
        action: path
          ? {
              label: "Xem",
              onClick: async () => {
                await this.markAsRead(notification.id);
                navigateFromNotification(notification, path)
              },
            }
          : undefined,
        duration: 5000,
      });
    },

    async markAsRead(id: string) {
      try {
        await markNotificationAsRead(id);
        const n = this.notifications.find((n) => n.id === id);

        if (n) n.read = true;
      } catch (error) {
        console.error("Lỗi đánh dấu đã đọc:", error);
      }
    },

    markAllNotiAsRead() {
      try {
        markAllAsRead();
        this.notifications.forEach((n) => (n.read = true));
      } catch (error) {
        console.error("Lỗi đánh dấu đã đọc:", error);
      }
    },

    async removeNotification(id: string) {
      try {
        await deleteNotification(id);
        this.notifications = this.notifications.filter((n) => n.id !== id);
      } catch (error) {
        console.error("Lỗi xóa thông báo:", error);
      }
    },

    clearNotifications() {
      try {
        this.notifications.map((n) => deleteNotification(n.id));
        this.notifications = [];
      } catch (error) {
        console.error("Lỗi xóa thông báo:", error);
      }
    },
  },

  getters: {
    unreadCount: (state): number =>
      state.notifications.filter((n) => !n.read).length,
  },
});

