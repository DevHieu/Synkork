import { socketService } from "./socketService";
import type { Message } from "@/types/Message";

export const chatSocket = {
  // Gửi tin nhắn
  sendMessage: (msg: { content: string; spaceId: string }) =>
    socketService.send("/app/chat.sendMessage", msg),

  // Lắng nghe tin nhắn mới
  subscribeMessages: (spaceId: string, callback: (msg: Message) => void) =>
    socketService.subscribe(`/topic/space/${spaceId}/messages`, callback),

  // Xóa tin nhắn
  deleteMessage: (message: Message) =>
    socketService.send("/app/chat.deleteMessage", message),

  subscribeDelete: (spaceId: string, callback: (id: string) => void) =>
    socketService.subscribe(`/topic/space/${spaceId}/messages/delete`, callback),
};