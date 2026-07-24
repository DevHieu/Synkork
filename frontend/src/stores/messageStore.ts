import { defineStore } from "pinia";
import {
  getChatFromSpaceId,
  changePinStatus,
  getPinnedChatList,
  getAroundMessage,
  sendFileMessage as sendFileMessageApi,
  chatService,
} from "@/services/chatService";
import { chatSocket } from "@/services/websocket/chatSocket";
import { socketService } from "@/services/websocket/socketService";
import type { Message } from "@/types/Message";
import type { MessageEventSuggestion } from "@/types/CalendarSuggestion";
import { nextTick } from "vue";
import { useUserStore } from "./userStore";
import { useRoomMemberStore } from "./roomMemberStore";

let _container: HTMLElement | null = null;
const MESSAGE_SIZE = 20;
const PINNED_SIZE = 10;

export const useMessageStore = defineStore("message", {
  state: () => ({
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

    subscribeToChat(spaceId: string) {
      chatSocket.subscribeMessages(spaceId, (msg: Message) => {
        // Nếu đang jump mode thì không push tin mới vào (tránh lộn xộn)
        if (!this.isJumpMode) {
          this.messages = this.messages.filter(
            (m) => m.id !== msg.id && !isSameOptimisticMessage(m, msg),
          );
          this.messages.unshift(msg);

          if (!this.isScrollTop) {
            // Tự nhảy xuống
            nextTick(() => this.scrollToBottom(spaceId));
          }
        }
      });

      chatSocket.subscribeDelete(spaceId, (messageId: string) => {
        // Xóa mềm trong list hiện tại để UI cập nhật ngay khi backend broadcast.
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

      this.subscribeToSuggestions();
    },

    async subscribeToSuggestions() {
      const currentUserId = useUserStore().user?.id;
      if (!currentUserId) {
        console.warn("[Goi y] Bo qua dang ky vi chua co userId hien tai");
        return;
      }

      if (this.suggestionSubscriptionReady) {
        console.log("[Goi y] Bo qua dang ky vi kenh goi y da san sang truoc do");
        return;
      }

      await socketService.connect();

      const subscription = chatSocket.subscribeSuggestions(currentUserId, (suggestion) => {
        this.suggestionsByMessageId = {
          ...this.suggestionsByMessageId,
          [suggestion.messageId]: suggestion,
        };
      });

      if (!subscription) {
        console.warn("[Goi y] Dang ky that bai vi socket chua san sang");
        return;
      }

      this.suggestionSubscriptionReady = true;
      console.log("[Goi y] Dang ky thanh cong cho user:", currentUserId);
    },

    // Load lần đầu hoặc scroll lên
    async fetchMessages(spaceId: string, cursor: string | null) {
      const res = await getChatFromSpaceId(spaceId, cursor, true, MESSAGE_SIZE);
      const { messages, beforeHasMore, beforeCursor } = res.data;

      console.trace(messages);

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

    async loadMore(spaceId: string, direction: "later" | "newer") {
      if (direction === "later") {
        await this.fetchMessages(spaceId, this.beforeCursor);
      } else {
        await this.fetchNewerMessages(spaceId);
      }
    },

    async sendMessage(
      spaceId: string,
      content: string,
      formData: FormData | null,
      files: File[] | null,
    ) {
      // Cần các mảng này để lưu lại, khi lỗi thì báo lỗi tin nhắn dựa vào id này
      const tempMsgs: Message[] = [];
      const textTempIds: string[] = [];
      const fileTempIds: string[] = [];

      if (files) {
        files.forEach((file) => {
          const msg = createTempMessage(file, spaceId, null, this.replyingTo);
          tempMsgs.push(msg);
          fileTempIds.push(msg.id);
        });

        console.log(files);
      }

      if (content.trim()) {
        const msg = createTempMessage(null, spaceId, content, this.replyingTo);
        tempMsgs.push(msg);
        textTempIds.push(msg.id);
      }

      const allTempIds = [...fileTempIds, ...textTempIds];

      this.messages = [...tempMsgs, ...this.messages];
      await nextTick();
      await this.scrollToBottom(spaceId);

      // Phải như này để t clear cái UI
      const replyId = this.replyingTo?.id ?? null;
      this.replyingTo = null;

      try {
        if (content.trim()) {
          // Xóa cái temp message đã add vào trước đấy, tại vì socket trả về message khá nhanh. Xóa tránh hiện 2 tin nhắn
          this.messages = this.messages.filter(
            (m) => !textTempIds.includes(m.id),
          );
          await chatService.sendMessage(spaceId, {
            content,
            replyToId: replyId,
          })
        }

        if (files && formData) {
          await sendFileMessageApi(spaceId, formData);
          this.messages = this.messages.filter(
            (m) => !fileTempIds.includes(m.id),
          );
        }

        this.replyingTo = null;
      } catch (err) {
        this.messages = this.messages.map((m) =>
          allTempIds.includes(m.id)
            ? { ...m, sending: false, failed: true }
            : m,
        );
      } finally {
        this.replyingTo = null;
      }
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

    async changePinStatus(spaceId: string, messageId: string) {
      this.pinLoading = true;
      try {
        await changePinStatus(spaceId, messageId);
      } finally {
        this.pinLoading = false;
      }
    },

    async jumpToMessage(spaceId: string, messageId: string) {
      // Kiểm tra có trong list hiện tại không
      const exists = this.messages.find((m) => m.id === messageId);
      if (exists) {
        highlightMessage(messageId);
        return;
      }

      // Fetch around
      const res = await getAroundMessage(spaceId, messageId);

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

      await nextTick();
      highlightMessage(messageId);
    },

    async exitJumpMode(spaceId: string) {
      this.isJumpMode = false;
      this.messages = [];
      this.beforeCursor = null;
      this.afterCursor = null;
      this.afterHasMore = false;
      await this.fetchMessages(spaceId, null);
    },

    setReply(msg: Message | null) {
      this.replyingTo = msg;
    },

    setScrollTop(val: boolean) {
      this.isScrollTop = val;
    },

    setScrollContainer(el: HTMLElement | null) {
      _container = el;
    },

    async scrollToBottom(spaceId: string) {
      if (this.isJumpMode) {
        await this.exitJumpMode(spaceId);
      }

      await nextTick();
      const el = _container;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    },

    dismissFailedMessage(tempIds: string[]) {
      this.messages = this.messages.filter((m) => !tempIds.includes(m.id));
    },
  },
});

function highlightMessage(messageId: string) {
  const el = document.getElementById(`message-${messageId}`);
  if (!el) return;
  el.scrollIntoView({ block: "center" });
  el.classList.add("message-highlight");
  setTimeout(() => el.classList.remove("message-highlight"), 2000);
}

function isSameOptimisticMessage(temp: Message, incoming: Message) {
  if (!temp.sending && !temp.failed) return false;
  if (temp.spaceId !== incoming.spaceId) return false;
  if (temp.type !== incoming.type) return false;
  if (temp.sender?.username !== incoming.sender?.username) return false;

  if (temp.attachmentName || incoming.attachmentName) {
    return temp.attachmentName === incoming.attachmentName;
  }

  return temp.content === incoming.content;
}

function getOptimisticRole(role: string | null) {
  return role === "OWNER" || role === "ADMIN" ? role : "MEMBER";
}

function createTempMessage(
  file: File | null,
  spaceId: string,
  content: string | null,
  replyTo: Message | null,
): Message {
  const isImage = file?.type.startsWith("image/") ?? false;
  const isVideo = file?.type.startsWith("video/") ?? false;
  const user = useUserStore().user;
  const memberStore = useRoomMemberStore();
  const currentMember = memberStore.members.find(
    (member) => member.username === user?.username,
  );

  return {
    id: crypto.randomUUID(),
    content,
    spaceId,
    type: file ? (isImage ? "IMAGE" : isVideo ? "VIDEO" : "FILE") : "TEXT",
    attachmentName: file ? file.name : null,
    attachmentUrl: file && (isImage || isVideo) ? URL.createObjectURL(file) : null,
    sending: true,
    failed: false,
    sender: currentMember ?? {
      memberId: user?.id ?? "",
      username: user?.username ?? "",
      displayName: user?.displayName ?? "",
      avatarUrl: user?.avatarUrl,
      role: getOptimisticRole(memberStore.currentAuthority),
      muted: false,
      deafen: false,
      chatDisableUntil: null,
    },
    replyTo,
    deleted: false,
    pinned: false,
    edited: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  } as Message;
}
