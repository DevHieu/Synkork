import { MESSAGE_SIZE, PINNED_SIZE } from "../utils/chat.utils";
import { chatService } from "../services/chatService";
import { useMessageStore } from "@/features/chats/stores/messageStore";
import { storeToRefs } from "pinia";
import type { Message } from "@/types/Message";
import { useChatUtilsComposable } from "./chat-utils.composable";
import { nextTick } from "vue";

export function useChatComposable() {
  const chatApi = chatService();
  const messageStore = useMessageStore();
  const {
    messages,
    beforeHasMore,
    beforeCursor,
    afterHasMore,
    afterCursor,
    isJumpMode,
    pinLoading,
    pinnedMessages,
    pinnedHasMore,
    pinnedCursor,
    replyingTo,
  } = storeToRefs(messageStore);

  // Load lần đầu hoặc scroll lên
  const fetchMessages = async (spaceId: string, cursor: string | null) => {
    const res = await chatApi.getChatFromSpaceId(
      spaceId,
      cursor,
      true,
      MESSAGE_SIZE,
    );
    const {
      messages: newMessages,
      beforeHasMore: newBeforeHasMore,
      beforeCursor: newBeforeCursor,
    } = res.data;

    messages.value = [...messages.value, ...newMessages];
    beforeHasMore.value = newBeforeHasMore;
    beforeCursor.value = newBeforeCursor ?? null;
  };

  // Scroll xuống (sau khi jump)
  const fetchNewerMessages = async (spaceId: string) => {
    if (!afterCursor.value || !afterHasMore.value) return;
    const res = await chatApi.getChatFromSpaceId(
      spaceId,
      afterCursor.value,
      false,
      MESSAGE_SIZE,
    );

    const {
      messages: newMessages,
      afterHasMore: newAfterHasMore,
      afterCursor: newAfterCursor,
    } = res.data;

    messages.value = [...newMessages, ...messages.value];
    afterHasMore.value = newAfterHasMore;
    afterCursor.value = newAfterCursor ?? null;

    // Hết tin nhắn mới → thoát jump mode, về normal
    if (!afterHasMore) isJumpMode.value = false;
  };

  const loadMore = async (spaceId: string, direction: "later" | "newer") => {
    if (direction === "later") {
      await fetchMessages(spaceId, beforeCursor.value);
    } else {
      await fetchNewerMessages(spaceId);
    }
  };

  const fetchPinnedList = async (spaceId: string, cursor: string | null) => {
    pinLoading.value = true;
    try {
      const res = await chatApi.getPinnedChatList(
        spaceId,
        cursor,
        PINNED_SIZE,
      );
      pinnedMessages.value = [...pinnedMessages.value, ...res.data.messages];
      pinnedHasMore.value = res.data.beforeHasMore;
      pinnedCursor.value = res.data.beforeCursor ?? null;
    } finally {
      pinLoading.value = false;
    }
  };

  const sendMessage = async (
    spaceId: string,
    content: string,
    formData: FormData | null,
    files: File[] | null,
  ) => {
    const chatUtils = useChatUtilsComposable();
    // Cần các mảng này để lưu lại, khi lỗi thì báo lỗi tin nhắn dựa vào id này
    const tempMsgs: Message[] = [];
    const textTempIds: string[] = [];
    const fileTempIds: string[] = [];

    if (files) {
      files.forEach((file) => {
        const msg = chatUtils.createTempMessage(
          file,
          spaceId,
          null,
          replyingTo.value,
        );
        tempMsgs.push(msg);
        fileTempIds.push(msg.id);
      });

      console.log(files);
    }

    if (content.trim()) {
      const msg = chatUtils.createTempMessage(
        null,
        spaceId,
        content,
        replyingTo.value,
      );
      tempMsgs.push(msg);
      textTempIds.push(msg.id);
    }

    const allTempIds = [...fileTempIds, ...textTempIds];

    messages.value = [...tempMsgs, ...messages.value];
    await nextTick();
    await chatUtils.scrollToBottom(spaceId);

    // Phải như này để t clear cái UI
    const replyId = replyingTo.value?.id ?? null;
    replyingTo.value = null;

    try {
      if (content.trim()) {
        // Xóa cái temp message đã add vào trước đấy, tại vì socket trả về message khá nhanh. Xóa tránh hiện 2 tin nhắn
        messages.value = messages.value.filter(
          (m) => !textTempIds.includes(m.id),
        );
        await chatApi.sendMessage(spaceId, {
          content,
          replyToId: replyId,
        });
      }

      if (files && formData) {
        await chatApi.sendFileMessage(spaceId, formData);
        messages.value = messages.value.filter(
          (m) => !fileTempIds.includes(m.id),
        );
      }

      replyingTo.value = null;
    } catch (err) {
      messages.value = messages.value.map((m) =>
        allTempIds.includes(m.id) ? { ...m, sending: false, failed: true } : m,
      );
    } finally {
      replyingTo.value = null;
    }
  };

  const jumpToMessage = async (spaceId: string, messageId: string) => {
    // Kiểm tra có trong list hiện tại không
    const exists = messages.value.find((m) => m.id === messageId);
    if (exists) {
      highlightMessage(messageId);
      return;
    }

    // Fetch around
    const res = await chatApi.getAroundMessage(spaceId, messageId);

    const {
      messages: newMessages,
      beforeCursor,
      afterCursor,
      beforeHasMore,
      afterHasMore,
    } = res.data;

    messages.value = newMessages;
    beforeCursor.value = beforeCursor ?? null;
    afterCursor.value = afterCursor ?? null;
    beforeHasMore.value = beforeHasMore;
    afterHasMore.value = afterHasMore;
    isJumpMode.value = true;

    await nextTick();
    highlightMessage(messageId);
  };

  return {
    fetchMessages,
    loadMore,
    fetchPinnedList,
    sendMessage,
    jumpToMessage,
  };
}

function highlightMessage(messageId: string) {
  const el = document.getElementById(`message-${messageId}`);
  if (!el) return;
  el.scrollIntoView({ block: "center" });
  el.classList.add("message-highlight");
  setTimeout(() => el.classList.remove("message-highlight"), 2000);
}
