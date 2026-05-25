<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import dayjs from "dayjs";
import MessageActions from "./sub-components/MessageActions.vue";
import ReplyQuote from "./sub-components/ReplyQuote.vue";
import FileAttachment from "./sub-components/FileAttachment.vue";
import type { Message } from "@/types/Message";

import { chatSocket } from "@/services/websocket/chatSocket";
import { computed, ref, watch } from "vue";
import { useMessageStore } from "@/stores/messageStore";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import DeleteConfirmDialog from "@/components/dialog/DeleteConfirmDialog.vue";
import UserInfoPopover from "../dialog/UserInfoPopover.vue";
import type { MessageEventSuggestion } from "@/types/CalendarSuggestion";

const props = defineProps<{
  message: Message;
  isGrouped: boolean;
  isDifferentDay: boolean;
}>();

const userStore = useUserStore();
const { user } = storeToRefs(userStore);

const messageStore = useMessageStore();

const senderNameColor = computed(() => {
  switch (props.message.sender?.role) {
    case "OWNER":
      return "text-yellow-400";
    case "ADMIN":
      return "text-red-400";
    default:
      return "text-foreground";
  }
});

const isFullAction = computed(
  () => props.message.sender?.username === user.value?.username,
);
const isEditing = ref(false);
const editContent = ref("");
const isDeleteOpen = ref(false);

const handleEdit = () => {
  isEditing.value = true;
  editContent.value = props.message.content ?? "";
};

const handleSaveEdit = () => {
  const trimmed = editContent.value.trim();
  if (!trimmed || trimmed === props.message.content) {
    handleCancelEdit();
    return;
  }
  chatSocket.updateMessage({ ...props.message, content: trimmed });
  isEditing.value = false;
};

const handleCancelEdit = () => {
  isEditing.value = false;
};

const handleDelete = () => {
  chatSocket.deleteMessage(props.message);
};

const handleReply = () => messageStore.setReply(props.message);

const handlePin = () =>
  messageStore.changePinStatus(props.message.spaceId, props.message.id);
const handleSuggestion = () => emit("openSuggestion", props.message.id);

const jumpToReply = () => {
  if (!props.message.replyTo?.id) return;
  messageStore.jumpToMessage(props.message.spaceId, props.message.replyTo.id);
};

const deleteFailedMessage = () => {
  messageStore.dismissFailedMessage([props.message.id]);
};

const emit = defineEmits<{
  (e: "openSuggestion", messageId: string): void;
}>();

const messageSuggestion = computed<MessageEventSuggestion | null>(() => {
  return messageStore.suggestionsByMessageId[props.message.id] ?? null;
});

// Nếu message có suggestion hợp lệ thì coi như đang ở trạng thái hover.
const shouldHighlightSuggestion = computed(
  () => !!messageSuggestion.value && messageSuggestion.value.suggestionType !== "NONE",
);

// Đổi nhãn nút sang "Tạo nhanh" chung cho các loại nội dung.
const suggestionLabel = computed(() => {
  return "Tạo nhanh";
});

const isSuggestionForceVisible = ref(false);

watch(
  shouldHighlightSuggestion,
  (isHighlighted) => {
    isSuggestionForceVisible.value = isHighlighted;

    if (!isHighlighted) return;

    console.log("[Goi y UI] Tin nhan da duoc bat trang thai goi y:", {
      messageId: props.message.id,
      suggestionType: messageSuggestion.value?.suggestionType,
      title: messageSuggestion.value?.title ?? null,
    });
  },
  { immediate: true },
);

const disableSuggestionForceVisible = () => {
  if (isSuggestionForceVisible.value) {
    isSuggestionForceVisible.value = false;
  }
};

// Tách nội dung tin nhắn thành các phần text và link để hiển thị đúng
const parsedContent = computed(() => {
  if (!props.message.content) return [];
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  const parts = props.message.content.split(urlRegex);
  return parts.map((part) => ({
    text: part,
    isLink: /^https?:\/\/[^\s]+$/.test(part),
  }));
});
</script>

<template>
  <div v-if="isDifferentDay" class="flex items-center gap-3 my-6 px-4">
    <div class="flex-1 h-px bg-border/50"></div>
    <span class="text-[10px] uppercase font-bold text-muted-foreground tracking-wider">
      {{ dayjs(props.message.createdAt).format("DD MMMM, YYYY") }}
    </span>
    <div class="flex-1 h-px bg-border/50"></div>
  </div>

  <div :id="`message-${props.message.id}`"
    class="relative group flex gap-3 p-2 mx-2 rounded-lg transition-colors hover:bg-secondary/20 mb-2"
    :class="{ 'bg-secondary/50 ring-1 ring-primary/20': shouldHighlightSuggestion }"
    @mouseenter="disableSuggestionForceVisible">
    <!-- Avatar -->
    <div class="w-10 shrink-0">
      <UserInfoPopover :username="props.message.sender?.username" v-if="!isGrouped || props.message.replyTo">
        <Avatar class="h-10 w-10 cursor-pointer">
          <AvatarImage v-if="props.message.sender?.avatarUrl" :src="props.message.sender.avatarUrl" />
          <AvatarFallback class="bg-primary"> </AvatarFallback>
        </Avatar>
      </UserInfoPopover>

      <span v-else
        class="text-[10px] text-muted-foreground opacity-0 group-hover:opacity-100 flex justify-center items-center h-full">
        {{ dayjs(props.message.createdAt).format("HH:mm") }}
      </span>
    </div>

    <!-- Content -->
    <div class="flex-1 min-w-0">
      <ReplyQuote v-if="props.message.replyTo" :reply-to="props.message.replyTo" @jump="jumpToReply" />

      <div v-if="!isGrouped || props.message.replyTo" class="flex items-center gap-2 mb-1">
        <span class="font-bold text-sm" :class="senderNameColor">
          {{ props.message.sender?.displayName }}
        </span>
        <span class="text-[10px] text-muted-foreground">
          {{ dayjs(props.message.createdAt).format("HH:mm") }}
        </span>
      </div>

      <!-- Edit mode -->
      <div v-if="isEditing" class="mt-1">
        <textarea v-model="editContent"
          class="w-full bg-background border border-primary/30 rounded-md p-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          rows="2" @keydown.enter.exact.prevent="handleSaveEdit" @keydown.esc="handleCancelEdit" />
        <div class="flex gap-2 mt-1 text-[11px]">
          <button @click="handleSaveEdit" class="text-primary hover:underline font-medium">
            Lưu thay đổi
          </button>
          <button @click="handleCancelEdit" class="text-muted-foreground hover:underline">
            Hủy
          </button>
        </div>
      </div>

      <div v-else class="text-sm leading-relaxed wrap-break-word">
        <template v-if="props.message.deleted">
          <span class="text-muted-foreground italic text-xs">{{
            props.message.type === "TEXT" ? "Tin nhắn" : "Tệp đính kèm"
          }}
            đã bị xóa</span>
        </template>

        <template v-else-if="props.message.sending || props.message.failed">
          <div class="flex items-center gap-2">
            <FileAttachment v-if="props.message.type !== 'TEXT'" :type="props.message.type"
              :attachment-url="props.message.attachmentUrl ?? ''" :attachment-name="props.message.attachmentName"
              :sending="true" />
          </div>

          <div v-if="props.message.sending" class="flex items-center gap-1 mt-1">
            <span class="w-2 h-2 rounded-full bg-muted-foreground/50 animate-pulse" />
            <span class="text-[10px] text-muted-foreground">Đang gửi...</span>
          </div>

          <div v-else-if="props.message.failed" class="flex items-center gap-2 mt-1">
            <span class="text-[10px] text-red-400 flex items-center gap-1">
              <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                  clip-rule="evenodd" />
              </svg>
              Gửi thất bại
            </span>
            <button @click="deleteFailedMessage()" class="text-[10px] text-muted-foreground hover:underline">
              Xóa
            </button>
          </div>
        </template>

        <template v-else>
          <div v-if="props.message.content">
            <!-- Check xem tin nhắn có chứa link trong đấy ko -->
            <template v-for="(part, i) in parsedContent" :key="i">
              <a v-if="part.isLink" :href="part.text" target="_blank" rel="noopener noreferrer"
                class="text-primary underline underline-offset-2 hover:text-primary/80 break-all">{{ part.text }}
              </a>
              <span v-else class="text-foreground/90">{{ part.text }}</span>
            </template>

            <span v-if="props.message.edited" class="text-[10px] text-muted-foreground ml-1">(đã chỉnh sửa)</span>
          </div>

          <FileAttachment v-if="props.message.type !== 'TEXT' && props.message.attachmentUrl" :type="props.message.type"
            :attachment-url="props.message.attachmentUrl" :attachment-name="props.message.attachmentName" />
        </template>
      </div>
    </div>

    <!-- Actions -->
    <div class="absolute right-4 -top-4 transition-opacity z-10" :class="isSuggestionForceVisible
      ? 'opacity-100'
      : 'opacity-0 group-hover:opacity-100'
      ">
      <MessageActions v-if="!isEditing && !props.message.deleted" :isSender="isFullAction"
        :isPinned="props.message.pinned" :showSuggestion="shouldHighlightSuggestion" :suggestionLabel="suggestionLabel"
        @reply="handleReply" @edit="handleEdit" @delete="isDeleteOpen = true" @pin="handlePin"
        @suggest="handleSuggestion" />
    </div>
  </div>

  <DeleteConfirmDialog v-model:open="isDeleteOpen" title="Xóa tin nhắn này?"
    description="Bạn không thể khôi phục tin nhắn này sau khi xóa." @confirm="handleDelete" />
</template>

<style scoped>
.message-highlight {
  animation: highlightFade 1s ease;
}

@keyframes highlightFade {
  0% {
    background-color: var(--primary);
  }

  100% {
    background-color: transparent;
  }
}
</style>
