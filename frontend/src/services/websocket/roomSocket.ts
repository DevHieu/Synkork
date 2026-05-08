import type { Room } from "@/types/Room";
import { socketService } from "./socketService";

export const roomSocket = {
  async subscribeRoomUpdate(roomId: string, callback: (room: Room) => void) {
    console.log(`/topic/rooms/${roomId}/update`);

    socketService.subscribe(`/topic/rooms/${roomId}/update`, (room) => {
      callback(room);
    });
  },
};
