import { getUserInfo } from "@/services/userService";
import type { User } from "@/types/User";
import { defineStore } from "pinia";

export const useUserStore = defineStore("users", {
  state: () => ({
    user: null as User | null,
    loading: false,
  }),

  actions: {
    async getUserInfo() {
      console.trace("[getUserInfo] Trace");

      this.loading = true;
      try {
        const response = await getUserInfo();

        this.user = response.data;
        console.log("user: " + JSON.stringify(this.user));
      } catch (error) {
        console.error("Error fetching user info:", error);
      } finally {
        this.loading = false;
      }
    },
  },
});
