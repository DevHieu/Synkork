import axios from "axios";
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
        const res = await axios.get(`/rooms/${userId}`);

        this.rooms = res.data;
      } finally {
        this.loading = false;
      }
    },

    async changeRoom(room: any) {
      this.currentRoom = room;
    },
  },
});
