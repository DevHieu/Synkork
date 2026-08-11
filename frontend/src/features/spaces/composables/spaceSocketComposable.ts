import { socketService } from "@/services/socketService";
import { spaceSocket } from "../services/spaceSocket";
import { useSpaceStore } from "../stores/spaceStore";
import { useSpaceComposable } from "./spaceComposable";

export function useSpaceSocketComposable() {
  const subscribeSocket = (roomId: string) => {
    socketService.connect();

    const roomStore = useSpaceStore();
    const { handleSpaceDeleted } = useSpaceComposable();

    spaceSocket.subscribeSpaceCreated(roomId, (space) => {
      roomStore.addSpaceToArray(space);
    });

    spaceSocket.subscribeSpaceUpdated(roomId, (updatedSpace) => {
      roomStore.updateSpaceToArray(updatedSpace);
    });

    spaceSocket.subscribeSpaceDeleted(roomId, (spaceId) => {
      handleSpaceDeleted(spaceId);
    });
  };

  return { subscribeSocket };
}
