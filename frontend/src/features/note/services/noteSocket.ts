import { socketService } from "../../../services/socketService";

const subscriptions: Record<string, any> = {};

export const noteSocket = {
  subscribeCreateNote(spaceId: string, callback: (payload: any) => void) {
    subscriptions[`create_${spaceId}`] = socketService.subscribe(
      `/topic/space/${spaceId}/notes/create`,
      callback,
    );
  },

  subscribeUpdateNote(spaceId: string, callback: (payload: any) => void) {
    subscriptions[`update_${spaceId}`] = socketService.subscribe(
      `/topic/space/${spaceId}/notes/update`,
      callback,
    );
  },

  subscribeDeleteNote(spaceId: string, callback: (payload: any) => void) {
    subscriptions[`delete_${spaceId}`] = socketService.subscribe(
      `/topic/space/${spaceId}/notes/delete`,
      callback,
    );
  },

  subscribetogglePin(spaceId: string, callback: (payload: any) => void) {
    subscriptions[`pin_${spaceId}`] = socketService.subscribe(
      `/topic/space/${spaceId}/notes/pin`,
      callback,
    );
  },

  unsubscribeAll(spaceId: string) {
    [`create`, `update`, `delete`, `pin`, `reminder`].forEach((type) => {
      const key = `${type}_${spaceId}`;
      if (subscriptions[key]) {
        subscriptions[key].unsubscribe();
        delete subscriptions[key];
      }
    });
  },
};
 