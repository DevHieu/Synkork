import { chatSocket } from "../services/chatSocket";
import type { Message } from "@/features/chats/types/MessageTypes";
import { useUserStore } from "@/features/users/stores/userStore";
import { useMessageStore } from "@/features/chats/stores/messageStore";
import { storeToRefs } from "pinia";
import { nextTick } from "vue";
import { socketService } from "@/services/socketService";
import { useChatUtilsComposable } from "./chat-utils.composable";

export function useChatSocketComposable() {
  const userStore = useUserStore();
  const messageStore = useMessageStore();
  const { isJumpMode, messages, pinnedMessages, isScrollTop } =
    storeToRefs(messageStore);
  const chatUtils = useChatUtilsComposable();

  const subscribeToChat = (spaceId: string) => {
    chatSocket.subscribeMessages(spaceId, (msg: Message) => {
      // Nếu đang jump mode thì không push tin mới vào (tránh lộn xộn)
      if (!isJumpMode.value) {
        messages.value = messages.value.filter(
          (m) => m.id !== msg.id && !chatUtils.isSameOptimisticMessage(m, msg),
        );
        messages.value.unshift(msg);

        if (!isScrollTop.value) {
          // Tự nhảy xuống
          nextTick(() => chatUtils.scrollToBottom(spaceId));
        }
      }
    });

    chatSocket.subscribeDelete(spaceId, (messageId: string) => {
      // Xóa mềm trong list hiện tại để UI cập nhật ngay khi backend broadcast.
      const msg = messages.value.find((m) => m.id === messageId);
      if (msg) msg.deleted = true;

      pinnedMessages.value = pinnedMessages.value.filter(
        (m) => m.id !== messageId,
      );
    });

    chatSocket.subscribeUpdate(spaceId, (updatedMsg: Message) => {
      const index = messages.value.findIndex((m) => m.id === updatedMsg.id);
      if (index !== -1) messages.value[index] = updatedMsg;

      const pinnedIndex = pinnedMessages.value.findIndex(
        (m) => m.id === updatedMsg.id,
      );
      if (pinnedIndex !== -1) pinnedMessages.value[pinnedIndex] = updatedMsg;
    });

    chatSocket.subscribePinStatus(spaceId, (updatedMsg: Message) => {
      const index = messages.value.findIndex((m) => m.id === updatedMsg.id);
      if (index !== -1) messages.value[index] = updatedMsg;

      if (updatedMsg.pinned) {
        const alreadyPinned = pinnedMessages.value.some(
          (m) => m.id === updatedMsg.id,
        );
        if (!alreadyPinned) pinnedMessages.value.unshift(updatedMsg);
      } else {
        pinnedMessages.value = pinnedMessages.value.filter(
          (m) => m.id !== updatedMsg.id,
        );
      }
    });

    subscribeToSuggestions();
  };

  const subscribeToSuggestions = async () => {
    const currentUserId = userStore.user?.id;
    if (!currentUserId) {
      console.warn("[Goi y] Bo qua dang ky vi chua co userId hien tai");
      return;
    }

    if (messageStore.suggestionSubscriptionReady) {
      console.log("[Goi y] Bo qua dang ky vi kenh goi y da san sang truoc do");
      return;
    }

    await socketService.connect();

    const subscription = chatSocket.subscribeSuggestions(
      currentUserId,
      (suggestion) => {
        messageStore.suggestionsByMessageId = {
          ...messageStore.suggestionsByMessageId,
          [suggestion.messageId]: suggestion,
        };
      },
    );

    if (!subscription) {
      console.warn("[Goi y] Dang ky that bai vi socket chua san sang");
      return;
    }

    messageStore.suggestionSubscriptionReady = true;
    console.log("[Goi y] Dang ky thanh cong cho user:", currentUserId);
  };

  return { subscribeToChat, subscribeToSuggestions };
}
