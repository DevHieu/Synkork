import { createRoom, getUserRooms, joinRoom } from "@/services/roomService";
import { defineStore } from "pinia";
import { useRoomMemberStore } from "./roomMemberStore";
import { useSpaceStore } from "./spaceStore";
import { useUserStore } from "./userStore";
import { storeToRefs } from "pinia";
import router from "@/routers";
import type { Room } from "@/types/Room";
import { socketService } from "@/services/websocket/socketService";
import { roomSocket } from "@/services/websocket/roomSocket";
import { toast } from "vue-sonner";

export const useRoomsStore = defineStore("rooms", {
  state: () => ({
    rooms: [] as Room[],
    currentRoom: null as Room | null,
    loading: false,
  }),

  actions: {
    async fetchRooms() {
      this.loading = true;
      try {
        this.rooms = await getUserRooms();
        console.trace(this.rooms);
      } finally {
        this.loading = false;
      }
    },

    connectRoomSocket(roomId: string) {
      roomSocket.subscribeRoomUpdate(roomId, (room) => {
        const target = this.rooms.find((item) => item.id === roomId);
        if (target) {
          Object.assign(target, room);
        }
      });
    },

    // Nhận spaceId để check xem khi đổi room có cần redirect đến space nào không
    async changeRoom(room: Room, spaceId?: string, spaceType?: string) {
      this.currentRoom = room;
      socketService.unsubscribeAll(); // Hủy tất cả subscription cũ khi đổi room để tránh nhận dữ liệu của phòng trước đó vào

      // Cần nối lại socket của room mới trước khi điều hướng sang space bên trong.
      this.connectRoomSocket(room.id);

      const spaceStore = useSpaceStore();
      await spaceStore.fetchSpacesByRoomId(room.id);

      const { user } = storeToRefs(useUserStore());

      useRoomMemberStore().fetchMembers(
        room.id,
        user.value?.username as string,
      );

      // Nếu không có spaceId, mặc định redirect đến CHAT space đầu tiên của room mới
      if (spaceId === undefined) {
        await spaceStore.changeSpace(0, "CHAT");
        router.push(`/rooms/chat/${room.id}/${spaceStore.currentSpace?.id}`);
      } else {
        // Cho phép caller chỉ định rõ loại space để điều hướng sang đúng màn hình.
        const targetSpaceType =
          spaceType ?? (router.currentRoute.value.meta.spaceType as string);

        await spaceStore.changeSpaceById(spaceId, targetSpaceType);
      }
    },

    async createRoom(roomData: {
      name: string;
      ownerId: string;
      imageFile?: File;
    }) {
      const userStore = useUserStore();
      const { user } = storeToRefs(userStore);

      if (!user.value) return;

      roomData.ownerId = (user.value as any).id;
      const newRoom = await createRoom(roomData);

      this.rooms.unshift(newRoom);
      this.changeRoom(newRoom, undefined);
    },

    async joinRoom(inviteCode: string) {
      const roomInvited = await joinRoom(inviteCode);

      await this.fetchRooms();
      await this.changeRoom(roomInvited);
    },

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
    }
  },
});
