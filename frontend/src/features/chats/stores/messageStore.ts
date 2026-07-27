import { defineStore } from "pinia";
import { chatService } from "@/features/chats/services/chatService";
import type { Message } from "@/types/Message";
import type { MessageEventSuggestion } from "@/types/CalendarSuggestion";
import { useChatComposable } from "@/features/chats/composable/chat.composable";

export const useMessageStore = defineStore("message", {
  state: () => ({
    _container: null as HTMLElement | null,
    isScrollTop: false,
    isJumpMode: false, // đang ở chế độ jump hay scroll bình thường

    messages: [] as Message[],

    beforeHasMore: false, // còn tin nhắn cũ hơn không
    afterHasMore: false, // còn tin nhắn mới hơn không (sau khi jump)
    beforeCursor: null as string | null,
    afterCursor: null as string | null,

    pinnedMessages: [] as Message[],
    pinnedHasMore: false,
    pinnedCursor: null as string | null,
    pinLoading: false,

    replyingTo: null as Message | null,
    suggestionsByMessageId: {} as Record<string, MessageEventSuggestion>,
    suggestionSubscriptionReady: false,
  }),

  actions: {
    clearAll() {
      this.messages = [];
      this.beforeHasMore = false;
      this.afterHasMore = false;
      this.beforeCursor = null;
      this.afterCursor = null;
      this.isJumpMode = false;

      this.pinnedMessages = [];
      this.pinnedHasMore = false;
      this.pinnedCursor = null;
      this.replyingTo = null;
      this.suggestionsByMessageId = {};
    },

    async changePinStatus(spaceId: string, messageId: string) {
      const chatApi = chatService();
      this.pinLoading = true;
      try {
        await chatApi.changePinStatus(spaceId, messageId);
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

      const chat = useChatComposable();
      await chat.fetchMessages(spaceId, null);
    },

    setReply(msg: Message | null) {
      this.replyingTo = msg;
    },

    setScrollTop(val: boolean) {
      this.isScrollTop = val;
    },

    setScrollContainer(el: HTMLElement | null) {
      this._container = el;
    },
  },
});
