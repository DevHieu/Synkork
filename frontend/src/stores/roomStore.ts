import { getUserRooms } from "@/services/roomService";
import { defineStore } from "pinia";
import { useSpaceStore } from "./spaceStore";
import router from "@/routers";

export const useRoomsStore = defineStore("rooms", {
  state: () => ({
    rooms: [] as any[],
    currentRoom: null as any | null,
    loading: false,
  }),

  actions: {
    async fetchRooms(userId: string) {
      this.loading = true;
      try {
        this.rooms = await getUserRooms(userId);
      } finally {
        this.loading = false;
      }
    },

    // Nhận spaceId để check xem khi đổi room có cần redirect đến space nào không
    async changeRoom(room: any, spaceId: string) {
      this.currentRoom = room;

      const spaceStore = useSpaceStore();
      await spaceStore.fetchSpacesByRoomId(room.id);

      // Nếu không có spaceId, mặc định redirect đến CHAT space đầu tiên của room mới
      if (spaceId === undefined) {
        await spaceStore.changeSpace(0, "CHAT");
        router.push(`/rooms/chat/${room.id}/${spaceStore.currentSpace?.id}`);
      } else {
        const spaceType = router.currentRoute.value.meta.spaceType as string; // Leeys type của space trên URL

        await spaceStore.changeSpaceById(spaceId, spaceType);
      }
    },
  },
});
