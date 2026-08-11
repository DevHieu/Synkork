import { storeToRefs } from "pinia";
import { roomSocket } from "../services/roomSocket";
import { useRoomsStore } from "../stores/roomStore";

export function useRoomSocketComposable() {
  const roomStore = useRoomsStore();
  const { rooms } = storeToRefs(roomStore);

  const subscribeSocket = async (roomId: string) => {
    roomSocket.subscribeRoomUpdate(roomId, (room) => {
      const target = rooms.value.find((item) => item.id === roomId);
      if (target) {
        Object.assign(target, room);
      }
    });
  };

  return { subscribeSocket };
}
