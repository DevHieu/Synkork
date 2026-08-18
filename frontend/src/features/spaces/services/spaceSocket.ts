import { socketService } from "../../../services/socketService";

export const spaceSocket = {
  subscribeSpaceCreated(roomId: string, callback: (space: any) => void) {
    return socketService.subscribe(
      `/topic/rooms/${roomId}/spaces/create`,
      (space) => {
        callback(space);
      },
    );
  },

  subscribeSpaceDeleted(roomId: string, callback: (spaceId: string) => void) {
    return socketService.subscribe(
      `/topic/rooms/${roomId}/spaces/delete`,
      (spaceId: string) => {
        callback(spaceId);
      },
    );
  },
  subscribeSpaceUpdated(roomId: string, callback: (space: any) => void) {
    return socketService.subscribe(
      `/topic/rooms/${roomId}/spaces/update`,
      (space) => {
        callback(space);
      },
    );
  },
};
