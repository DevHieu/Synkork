import { defineStore } from "pinia";
import type { Room } from "@/features/rooms/types/Room";
import router from "@/routers";
import { toast } from "vue-sonner";

export const useRoomsStore = defineStore("rooms", {
  state: () => ({
    rooms: [] as Room[],
    currentRoom: null as Room | null,
    loading: false,
  }),

  actions: {
    async removeRoomFromArray(roomId: string) {
      if (this.currentRoom?.id === roomId) {
        this.currentRoom = null;
        router.push("/me");
        toast.error("Phòng đã bị xóa");
      }

      this.rooms = this.rooms.filter((room) => room.id !== roomId);
    },
  },

  getters: {
    isInRoom: (state) => !!state.currentRoom,
    roomPlan: (state) => {
      if (!state.currentRoom) return "FREE";
      return state.currentRoom.currentPlan;
    },
    roomId: (state) => {
      return state.currentRoom?.id || null;
    },
  },
});
