import { useRoomsStore } from "@/stores/roomStore";
import { socketService } from "./socketService";
import { toast } from "vue-sonner";
import type { Room } from "@/types/Room";

export const userSocket = {
  subscribeKicked() {
    const roomStore = useRoomsStore();

    return socketService.subscribe(
      "/user/queue/kick",
      (roomId: string) => {
        toast.error("Bạn đã bị đuổi khỏi phòng");
        roomStore.removeRoomFromArray(roomId);
      },
      { persistent: true },
    );
  },

  subscribeFriendOnlineStatus(
    callback: (payload: { userId: string; isOnline: boolean }) => void,
  ) {
    return socketService.subscribe(
      "/user/queue/friends/online-status",
      (payload: { userId: string; isOnline: boolean }) => {
        callback(payload);
      },
      { persistent: true },
    );
  },

  subscribeRoomDeleted() {
    const roomStore = useRoomsStore();
    return socketService.subscribe(
      "/user/queue/rooms/deleted",
      (roomId: string) => {
        roomStore.removeRoomFromArray(roomId);
      },
      { persistent: true },
    );
  },

  subscribeRoomInvited() {
    const roomStore = useRoomsStore();
    return socketService.subscribe(
      "/user/queue/room/members/invited",
      async () => {
        await roomStore.fetchRooms();
        toast.info("Bạn vừa được gia nhập vào 1 room mới");
      },
      { persistent: true },
    );
  },
};
