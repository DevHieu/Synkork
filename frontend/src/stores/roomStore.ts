import { getUserRooms } from "@/services/roomService";
import { defineStore } from "pinia";

export const useRoomsStore = defineStore("rooms", {
  state: () => ({
    rooms: [] as any[],
    currentRoom: null as {} | null,
    loading: false,
  }),

  actions: {
    async fetchRooms(userId: string) {
      this.loading = true;
      try {
        console.log(`Fetching rooms for userId: ${userId}`);

        this.rooms = await getUserRooms(userId);
      } finally {
        this.loading = false;
      }
    },

    async changeRoom(room: any) {
      this.currentRoom = room;
    },
  },
});
