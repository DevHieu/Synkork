import { socketService } from "./socketService";
import type { Message } from "@/types/Message";

export const chatSocket = {
  sendMessage(msg: { content: string; spaceId: string }) {
    socketService.publish("/app/chat.sendMessage", msg);
  },

  subscribeMessages(spaceId: string, callback: (msg: Message) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages`,
      callback,
    );
  },

  deleteMessage(message: Message) {
    socketService.publish("/app/chat.deleteMessage", message);
  },

  subscribeDelete(spaceId: string, callback: (id: string) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages/delete`,
      callback,
    );
  },

  updateMessage(message: Message) {
    socketService.publish("/app/chat.updateMessage", message);
  },

  subscribeUpdate(spaceId: string, callback: (msg: Message) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages/update`,
      callback,
    );
  },
};
