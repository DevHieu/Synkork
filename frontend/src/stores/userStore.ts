import { getUserInfo } from "@/services/userService";
import { defineStore } from "pinia";

export const useUserStore = defineStore("users", {
    state: () => ({
        user: null as {} | null,
        loading: false,
    }),

    actions: {
        async getUserInfo() {
            this.loading = true;
            try {
                const response = await getUserInfo();
                console.log("user: " + response.data);
                
                this.user = response.data;
            } catch (error) {
                console.error("Error fetching user info:", error);
            } finally {
                this.loading = false;
            }
        }
    }
});