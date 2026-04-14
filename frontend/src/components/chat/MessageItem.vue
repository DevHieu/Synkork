<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import dayjs from "dayjs";
import MessageActions from "./MessageActions.vue";
import type { Message } from "@/types/Message";

import { chatSocket } from "@/services/websocket/chatSocket";
import { computed, ref } from "vue";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";

const props = defineProps<{
  message: Message;
  isGrouped: boolean;
  isDifferentDay: boolean;
}>();

const userStore = useUserStore();
const { user } = storeToRefs(userStore);

// hard code tạm màu của owner, admin với member thường khi nhắn tin
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
const editContent = ref(""); // Chỉ cần lưu nội dung text để edit

const handleEdit = () => {
  isEditing.value = true;
  // Clone content để tránh sửa trực tiếp vào props khi đang gõ
  editContent.value = props.message.content;
};

const handleSaveEdit = () => {
  const trimmed = editContent.value.trim();
  if (!trimmed || trimmed === props.message.content) {
    handleCancelEdit();
    return;
  }

  // Tạo object message mới dựa trên dữ liệu cũ nhưng thay content
  const updatedMessage: Message = {
    ...props.message,
    content: trimmed,
  };

  chatSocket.updateMessage(updatedMessage);
  isEditing.value = false;
};

const handleCancelEdit = () => {
  isEditing.value = false;
};

const handleDelete = () => {
  if (confirm("Bạn có chắc chắn muốn xóa tin nhắn này?")) {
    chatSocket.deleteMessage(props.message);
  }
};

const handleReply = () => console.log("Reply to:", props.message.id);
const handlePin = () => console.log("Pin message:", props.message.id);
</script>

<template>
  <div v-if="isDifferentDay" class="flex items-center gap-3 my-4 px-4">
    <div class="flex-1 h-px bg-border/50"></div>
    <span
      class="text-[10px] uppercase font-bold text-muted-foreground tracking-wider"
    >
      {{ dayjs(props.message.createdAt).format("DD MMMM, YYYY") }}
    </span>
    <div class="flex-1 h-px bg-border/50"></div>
  </div>

  <div
    class="relative group flex gap-3 p-2 mx-2 rounded-lg transition-colors hover:bg-secondary/20"
    :class="[isGrouped ? 'mt-0' : 'mt-4']"
  >
    <div class="w-10 shrink-0">
      <Avatar v-if="!isGrouped" class="h-10 w-10">
        <AvatarImage
          v-if="props.message.sender?.avatarUrl"
          :src="props.message.sender.avatarUrl"
        />
        <AvatarFallback class="bg-primary">
          {{ props.message.sender?.displayName?.charAt(0).toUpperCase() }}
        </AvatarFallback>
      </Avatar>
      <span
        v-else
        class="text-[10px] text-muted-foreground opacity-0 group-hover:opacity-100 flex justify-center items-center h-full"
      >
        {{ dayjs(props.message.createdAt).format("HH:mm") }}
      </span>
    </div>

    <div class="flex-1 min-w-0">
      <div v-if="!isGrouped" class="flex items-center gap-2 mb-1">
        <span class="font-bold text-sm" :class="senderNameColor">
          {{ props.message.sender?.displayName }}
        </span>
        <span class="text-[10px] text-muted-foreground">
          {{ dayjs(props.message.createdAt).format("HH:mm") }}
        </span>
      </div>

      <div v-if="isEditing" class="mt-1">
        <textarea
          v-model="editContent"
          class="w-full bg-background border border-primary/30 rounded-md p-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          rows="2"
          @keydown.enter.exact.prevent="handleSaveEdit"
          @keydown.esc="handleCancelEdit"
        />
        <div class="flex gap-2 mt-1 text-[11px]">
          <button
            @click="handleSaveEdit"
            class="text-primary hover:underline font-medium"
          >
            Lưu thay đổi
          </button>
          <button
            @click="handleCancelEdit"
            class="text-muted-foreground hover:underline"
          >
            Hủy
          </button>
        </div>
      </div>

      <div v-else class="text-sm leading-relaxed break-words">
        <template v-if="props.message.deleted">
          <span class="text-muted-foreground italic text-xs"
            >Tin nhắn đã bị xóa</span
          >
        </template>
        <template v-else>
          <span class="text-foreground/90">{{ props.message.content }}</span>
          <span
            v-if="props.message.updatedAt !== props.message.createdAt"
            class="text-[10px] text-muted-foreground ml-1"
          >
            (đã chỉnh sửa)
          </span>
        </template>
      </div>
    </div>

    <div
      class="absolute right-4 -top-4 opacity-0 group-hover:opacity-100 transition-opacity z-10"
    >
      <MessageActions
        v-if="!isEditing && !props.message.deleted"
        :isSender="isFullAction"
        @reply="handleReply"
        @edit="handleEdit"
        @delete="handleDelete"
        @pin="handlePin"
      />
    </div>
  </div>
</template>
