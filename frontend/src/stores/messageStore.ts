import { defineStore } from "pinia";
import {
  getChatFromSpaceId,
  changePinStatus,
  getPinnedChatList,
  getAroundMessage,
} from "@/services/chatService";
import { chatSocket } from "@/services/websocket/chatSocket";
import type { Message } from "@/types/Message";

const MESSAGE_SIZE = 20;
const PINNED_SIZE = 10;

export const useMessageStore = defineStore("message", {
  state: () => ({
    messages: [] as Message[],
    beforeHasMore: false, // còn tin nhắn cũ hơn không
    afterHasMore: false, // còn tin nhắn mới hơn không (sau khi jump)
    beforeCursor: null as string | null,
    afterCursor: null as string | null,

    isScrollTop: false,

    isJumpMode: false, // đang ở chế độ jump hay scroll bình thường
    jumpTargetId: null as string | null,

    pinnedMessages: [] as Message[],
    pinnedHasMore: false,
    pinnedCursor: null as string | null,
    pinLoading: false,

    replyingTo: null as Message | null,
  }),

  actions: {
    clearAll() {
      this.messages = [];
      this.beforeHasMore = false;
      this.afterHasMore = false;
      this.beforeCursor = null;
      this.afterCursor = null;
      this.isJumpMode = false;
      this.jumpTargetId = null;

      this.pinnedMessages = [];
      this.pinnedHasMore = false;
      this.pinnedCursor = null;
      this.replyingTo = null;
    },

    // Load lần đầu hoặc scroll lên
    async fetchMessages(spaceId: string, cursor: string | null) {
      const res = await getChatFromSpaceId(spaceId, cursor, true, MESSAGE_SIZE);
      console.log(res.data);

      const { messages, beforeHasMore, beforeCursor } = res.data;

      this.messages = [...this.messages, ...messages];
      this.beforeHasMore = beforeHasMore;
      this.beforeCursor = beforeCursor ?? null;
    },

    // Scroll xuống (sau khi jump)
    async fetchNewerMessages(spaceId: string) {
      if (!this.afterCursor || !this.afterHasMore) return;
      const res = await getChatFromSpaceId(
        spaceId,
        this.afterCursor,
        false,
        MESSAGE_SIZE,
      );
      console.log(res.data);

      const { messages, afterHasMore, afterCursor } = res.data;

      this.messages = [...messages, ...this.messages];
      this.afterHasMore = afterHasMore;
      this.afterCursor = afterCursor ?? null;

      // Hết tin nhắn mới → thoát jump mode, về normal
      if (!afterHasMore) this.isJumpMode = false;
    },

    async loadMore(spaceId: string) {
      await this.fetchMessages(spaceId, this.beforeCursor);
    },

    async jumpToMessage(spaceId: string, messageId: string) {
      // Kiểm tra có trong list hiện tại không
      const exists = this.messages.find((m) => m.id === messageId);
      if (exists) {
        this.jumpTargetId = messageId;
        return;
      }

      // Fetch around
      const res = await getAroundMessage(spaceId, messageId);
      console.log(res);

      const {
        messages,
        beforeCursor,
        afterCursor,
        beforeHasMore,
        afterHasMore,
      } = res.data;

      this.messages = messages;
      this.beforeCursor = beforeCursor ?? null;
      this.afterCursor = afterCursor ?? null;
      this.beforeHasMore = beforeHasMore;
      this.afterHasMore = afterHasMore;
      this.isJumpMode = true;
      this.jumpTargetId = messageId;
    },

    async fetchPinnedList(spaceId: string, cursor: string | null) {
      this.pinLoading = true;
      try {
        const res = await getPinnedChatList(spaceId, cursor, PINNED_SIZE);
        this.pinnedMessages = [...this.pinnedMessages, ...res.data.messages];
        this.pinnedHasMore = res.data.beforeHasMore;
        this.pinnedCursor = res.data.beforeCursor ?? null;
      } finally {
        this.pinLoading = false;
      }
    },

    subscribeToChat(spaceId: string) {
      chatSocket.subscribeMessages(spaceId, (msg: Message) => {
        // Nếu đang jump mode thì không push tin mới vào (tránh lộn xộn)
        if (!this.isJumpMode) this.messages.unshift(msg);
      });

      chatSocket.subscribeDelete(spaceId, (messageId: string) => {
        const msg = this.messages.find((m) => m.id === messageId);
        if (msg) msg.deleted = true;

        this.pinnedMessages = this.pinnedMessages.filter(
          (m) => m.id !== messageId,
        );
      });

      chatSocket.subscribeUpdate(spaceId, (updatedMsg: Message) => {
        const index = this.messages.findIndex((m) => m.id === updatedMsg.id);
        if (index !== -1) this.messages[index] = updatedMsg;

        const pinnedIndex = this.pinnedMessages.findIndex(
          (m) => m.id === updatedMsg.id,
        );
        if (pinnedIndex !== -1) this.pinnedMessages[pinnedIndex] = updatedMsg;
      });

      chatSocket.subscribePinStatus(spaceId, (updatedMsg: Message) => {
        const index = this.messages.findIndex((m) => m.id === updatedMsg.id);
        if (index !== -1) this.messages[index] = updatedMsg;

        if (updatedMsg.pinned) {
          const alreadyPinned = this.pinnedMessages.some(
            (m) => m.id === updatedMsg.id,
          );
          if (!alreadyPinned) this.pinnedMessages.unshift(updatedMsg);
        } else {
          this.pinnedMessages = this.pinnedMessages.filter(
            (m) => m.id !== updatedMsg.id,
          );
        }
      });
    },

    sendMessage(spaceId: string, content: string) {
      if (!content.trim()) return;
      chatSocket.sendMessage({ content, spaceId });
      this.replyingTo = null;
    },

    setReply(msg: Message | null) {
      this.replyingTo = msg;
    },

    setScrollTop(val: boolean) {
      this.isScrollTop = val;
    },

    async changePinStatus(spaceId: string, messageId: string) {
      this.pinLoading = true;
      try {
        await changePinStatus(spaceId, messageId);
      } finally {
        this.pinLoading = false;
      }
    },

    async exitJumpMode(spaceId: string) {
      this.isJumpMode = false;
      this.messages = [];
      this.beforeCursor = null;
      this.afterCursor = null;
      this.afterHasMore = false;
      await this.fetchMessages(spaceId, null);
    },
  },
});
