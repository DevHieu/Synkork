import type { Room } from "@/features/rooms/types/Room";
import { socketService } from "../../../services/socketService";

export const roomSocket = {
  async subscribeRoomUpdate(roomId: string, callback: (room: Room) => void) {
    socketService.subscribe(`/topic/rooms/${roomId}/update`, (room) => {
      callback(room);
    });
  },
};
