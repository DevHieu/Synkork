import { useRoomsStore } from "@/stores/roomStore";
import { socketService } from "./socketService";
import { toast } from "vue-sonner";

export const userSocket = {
  subscribeKicked() {
    const roomStore = useRoomsStore();

    return socketService.subscribe(
      "/user/queue/kick",
      (roomId: string) => {
        toast.error("Bạn đã bị xóa khỏi phòng");
        roomStore.leaveRoom(roomId);
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
};
