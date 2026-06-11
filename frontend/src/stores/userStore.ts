import { getUserInfo } from "@/services/userService";
import type { User } from "@/types/User";
import { defineStore } from "pinia";
import { useNotificationStore } from "./notificationStore";

export const useUserStore = defineStore("users", {
  state: () => ({
    user: null as User | null,
    loading: false,
  }),

  actions: {
    async getUserInfo() {
      this.loading = true;
      try {
        const response = await getUserInfo();
        this.user = response.data;
        if (this.user?.id) {
          const notificationStore = useNotificationStore()
          try {
            await notificationStore.fetchNotifications()
          } catch (e) {
            console.warn("Không load được notifications:", e)
          }
          await notificationStore.connect()
        }
      } catch (error) {
        console.error("Error fetching user info:", error);
      } finally {
        this.loading = false;
      }
    }
  },

  getters: {
    isLoggedIn: (state) => !!state.user,
    userName: (state) => state.user?.displayName || "",
    userEmail: (state) => state.user?.email || "",
    userPlan: (state) => state.user?.currentPlan || "FREE",
    planExpiresAt: (state) => state.user?.planExpiresAt || null,
    userPersonalSpace: (state) => ({
      calendarId: state.user?.personalCalendarId ?? "",
      noteId: state.user?.personalNoteId ?? "",
    }),
  }
});
