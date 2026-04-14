import { socketService } from "./socketService";
import type { Message } from "@/types/Message";

export const chatSocket = {
  // hủy subscription khi rời khỏi space để tránh nhận tin nhắn mấy phòng trước đó vào
  leaveSpace(spaceId: string) {
    socketService.unsubscribeByDestination(`/topic/space/${spaceId}/messages`);
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/messages/delete`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/messages/update`,
    );
    socketService.unsubscribeByDestination(
      `/topic/space/${spaceId}/messages/pin`,
    );
  },

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

  subscribePinStatus(spaceId: string, callback: (msg: Message) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages/pin`,
      callback,
    );
  },
};
