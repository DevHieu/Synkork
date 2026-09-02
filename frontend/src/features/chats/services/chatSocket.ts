import { socketService } from "@/services/socketService";
import type { Message } from "@/features/chats/types/MessageTypes";
import type { MessageEventSuggestion } from "@/types/SuggestionTypes";
import { chatJoinedSpaceId } from "../utils/chat.utils";

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
    if (chatJoinedSpaceId.value === spaceId) {
      chatJoinedSpaceId.value = null;
    }
  },

  resetJoinedSpace() {
    chatJoinedSpaceId.value = null;
  },

  subscribeMessages(spaceId: string, callback: (msg: Message) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages`,
      callback,
    );
  },

  subscribeDelete(spaceId: string, callback: (id: string) => void) {
    return socketService.subscribe(
      `/topic/space/${spaceId}/messages/delete`,
      callback,
    );
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

  subscribeSuggestions(
    userId: string,
    callback: (suggestion: MessageEventSuggestion) => void,
  ) {
    // Kênh này dùng riêng cho suggestion theo user nên cần giữ persistent.
    if (!socketService.isConnected()) {
      return null;
    }

    return socketService.subscribe(
      `/topic/user/${userId}/suggestions`,
      callback,
      { persistent: true },
    );
  },
};
