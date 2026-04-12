import { useRoomsStore } from "@/stores/roomStore";
import { socketService } from "./socketService";
import { toast } from "vue-sonner";

export const userSocket = {
  subscribeKicked() {
    const roomStore = useRoomsStore();

    console.log(
      "[userSocket] subscribeKicked called, connected:",
      socketService.isConnected(),
    );

    return socketService.subscribe("/user/queue/kick", (roomId: string) => {
      console.log("[userSocket] kicked from room:", roomId);
      toast.error("Bạn đã bị xóa khỏi phòng");
      roomStore.leaveRoom(roomId);
    });
  },
};
