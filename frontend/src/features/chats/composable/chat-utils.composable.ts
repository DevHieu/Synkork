import { useUserStore } from "@/stores/userStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { useMessageStore } from "@/features/chats/stores/messageStore";
import { storeToRefs } from "pinia";
import { computed, nextTick, onMounted, ref } from "vue";
import { parseValidDate, formatVNDateTime } from "@/utils/date";
import type { Message } from "@/types/Message";

export function useChatUtilsComposable() {
  const userStore = useUserStore();

  const messageStore = useMessageStore();
  const { messages, isJumpMode, _container } = storeToRefs(messageStore);

  const roomMemberStore = useRoomMemberStore();
  const { chatDisabledTime } = storeToRefs(roomMemberStore);

  const now = ref(Date.now());
  let timer: ReturnType<typeof setInterval> | undefined;

  onMounted(() => {
    timer = setInterval(() => {
      now.value = Date.now();
    }, 1000); // tick mỗi giây, tùy nhu cầu chỉnh lại
  });

  const chatDisabledUntilDate = computed(() =>
    parseValidDate(chatDisabledTime.value),
  );

  const isChatDisabled = computed(
    () =>
      !!chatDisabledUntilDate.value &&
      chatDisabledUntilDate.value.getTime() > now.value,
  );

  const chatDisabledLabel = computed(() =>
    chatDisabledUntilDate.value
      ? formatVNDateTime(chatDisabledUntilDate.value)
      : "",
  );

  const scrollToBottom = async (spaceId: string) => {
    if (isJumpMode.value) {
      await messageStore.exitJumpMode(spaceId);
    }

    await nextTick();
    const el = _container.value;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  };

  const dismissFailedMessage = (tempIds: string[]) => {
    messages.value = messages.value.filter((m) => !tempIds.includes(m.id));
  };

  const isSameOptimisticMessage = (temp: Message, incoming: Message) => {
    if (!temp.sending && !temp.failed) return false;
    if (temp.spaceId !== incoming.spaceId) return false;
    if (temp.type !== incoming.type) return false;
    if (temp.sender?.username !== incoming.sender?.username) return false;

    if (temp.attachmentName || incoming.attachmentName) {
      return temp.attachmentName === incoming.attachmentName;
    }

    return temp.content === incoming.content;
  };

  const getOptimisticRole = (role: string | null) => {
    return role === "OWNER" || role === "ADMIN" ? role : "MEMBER";
  };

  const createTempMessage = (
    file: File | null,
    spaceId: string,
    content: string | null,
    replyTo: Message | null,
  ): Message => {
    const isImage = file?.type.startsWith("image/") ?? false;
    const isVideo = file?.type.startsWith("video/") ?? false;
    const user = userStore.user;
    const currentMember = roomMemberStore.members.find(
      (member) => member.username === user?.username,
    );

    return {
      id: crypto.randomUUID(),
      content,
      spaceId,
      type: file ? (isImage ? "IMAGE" : isVideo ? "VIDEO" : "FILE") : "TEXT",
      attachmentName: file ? file.name : null,
      attachmentUrl:
        file && (isImage || isVideo) ? URL.createObjectURL(file) : null,
      sending: true,
      failed: false,
      sender: currentMember ?? {
        memberId: user?.id ?? "",
        username: user?.username ?? "",
        displayName: user?.displayName ?? "",
        avatarUrl: user?.avatarUrl,
        role: getOptimisticRole(roomMemberStore.currentAuthority),
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
  };

  return {
    chatDisabledUntilDate,
    isChatDisabled,
    chatDisabledLabel,
    scrollToBottom,
    dismissFailedMessage,
    isSameOptimisticMessage,
    getOptimisticRole,
    createTempMessage,
  };
}
