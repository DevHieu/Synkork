<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import dayjs from "dayjs";
import MessageActions from "./MessageActions.vue";
import type { Message } from "@/types/Message";
import { deleteMessage, updateMessage } from "@/services/websocket/chatSocket";
import { computed, ref } from "vue";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";

const props = defineProps<{
  message: Message;
  isGrouped: boolean;
  isDifferentDay: boolean;
}>();

// CÁI MÀU NÀY ĐỂ TẠM -> SAU NÀY LÀM TIẾP
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

const userStore = useUserStore();
const { user } = storeToRefs(userStore);

const isFullAction = computed(
  () => props.message.sender?.username === user.value?.username,
);
const isEditing = ref(false);
const editContent = ref(null as Message | null);

const handleReply = () => console.log("Reply to message:", props.message.id);

const handleEdit = () => {
  isEditing.value = true;
  editContent.value = props.message;
};

const handleSaveEdit = () => {
  if (!editContent.value?.content.trim()) return;
  updateMessage(editContent.value);
  isEditing.value = false;
};

const handleCancelEdit = () => {
  isEditing.value = false;
  editContent.value = null;
};

const handleDelete = () => deleteMessage(props.message);
const handlePin = () => console.log("Pin message:", props.message.id);
</script>

<template>
  <!-- Divider ngày mới -->
  <div v-if="isDifferentDay" class="flex items-center gap-3 my-4">
    <div class="flex-1 h-px bg-gray-600"></div>
    <span class="text-xs text-gray-400 shrink-0">
      {{ dayjs(props.message.createdAt).format("DD/MM/YYYY") }}
    </span>
    <div class="flex-1 h-px bg-gray-600"></div>
  </div>

  <!-- Gộp -->
  <div
    v-if="isGrouped"
    class="relative flex items-center gap-2 pl-6 mb-0 p-2 rounded-lg transition-all hover:bg-secondary/30 message-hover group"
  >
    <span class="text-xs text-foreground hide-item">{{
      dayjs(props.message.createdAt).format("HH:mm")
    }}</span>

    <!-- Edit mode -->
    <div v-if="isEditing" class="flex-1 flex flex-col gap-1">
      <textarea
        v-model="editContent.content"
        class="w-full bg-secondary/50 border border-border rounded-md px-3 py-2 text-sm resize-none focus:outline-none focus:ring-1 focus:ring-primary"
        rows="2"
        @keydown.enter.exact.prevent="handleSaveEdit"
        @keydown.esc="handleCancelEdit"
      />
      <div class="flex gap-2 text-xs">
        <button @click="handleSaveEdit" class="text-primary hover:underline">
          Lưu
        </button>
        <span class="text-muted-foreground">·</span>
        <button
          @click="handleCancelEdit"
          class="text-muted-foreground hover:underline"
        >
          Hủy
        </button>
      </div>
    </div>

    <div v-else class="text-white-800 whitespace-pre-wrap">
      <span
        v-if="props.message.deleted"
        class="text-muted-foreground italic text-sm"
      >
        Tin nhắn đã bị xóa
      </span>
      <template v-else>
        {{ props.message.content }}
        <span
          v-if="props.message.updatedAt !== props.message.createdAt"
          class="text-xs text-muted-foreground"
        >
          (đã chỉnh sửa)
        </span>
      </template>
    </div>

    <MessageActions
      v-if="!isEditing && !props.message.deleted"
      :isSender="isFullAction"
      @reply="handleReply"
      @edit="handleEdit"
      @delete="handleDelete"
      @pin="handlePin"
    />
  </div>

  <!-- Không gộp -->
  <div
    v-else
    class="relative flex gap-3 mb-0 mt-5 p-2 rounded-lg transition-all hover:bg-secondary/30 message-hover group"
  >
    <Avatar class="h-10 w-10 rounded-full shrink-0">
      <AvatarImage :src="props.message.sender?.avatarUrl" />
      <AvatarFallback
        class="rounded-full bg-primary text-primary-foreground"
        :class="senderNameColor"
      >
        {{ props.message.sender?.displayName?.charAt(0).toUpperCase() ?? "CN" }}
      </AvatarFallback>
    </Avatar>

    <div class="flex-1">
      <div class="flex items-center gap-2">
        <span class="font-semibold" :class="senderNameColor">{{
          props.message.sender?.displayName
        }}</span>
        <span class="text-xs text-gray-400">{{
          dayjs(props.message.createdAt).format("HH:mm DD/MM/YYYY")
        }}</span>
      </div>

      <!-- Edit mode -->
      <div v-if="isEditing" class="flex flex-col gap-1 mt-1">
        <textarea
          v-model="editContent.content"
          class="w-full bg-secondary/50 border border-border rounded-md px-3 py-2 text-sm resize-none focus:outline-none focus:ring-1 focus:ring-primary"
          rows="2"
          @keydown.enter.exact.prevent="handleSaveEdit"
          @keydown.esc="handleCancelEdit"
        />
        <div class="flex gap-2 text-xs">
          <button @click="handleSaveEdit" class="text-primary hover:underline">
            Lưu
          </button>
          <span class="text-muted-foreground">·</span>
          <button
            @click="handleCancelEdit"
            class="text-muted-foreground hover:underline"
          >
            Hủy
          </button>
        </div>
      </div>

      <div v-else class="text-white-800 whitespace-pre-wrap">
        <span
          v-if="props.message.deleted"
          class="text-muted-foreground italic text-sm"
        >
          Tin nhắn đã bị xóa
        </span>
        <template v-else>
          {{ props.message.content
          }}<span
            v-if="props.message.updatedAt !== props.message.createdAt"
            class="text-xs text-muted-foreground"
          >
            (đã chỉnh sửa)
          </span>
        </template>
      </div>
    </div>

    <MessageActions
      v-if="!isEditing && !props.message.deleted"
      :isSender="isFullAction"
      @reply="handleReply"
      @pin="handlePin"
      @edit="handleEdit"
      @delete="handleDelete"
    />
  </div>
</template>

<style scoped>
.hide-item {
  visibility: hidden;
}
.message-hover:hover .hide-item {
  visibility: visible;
}
</style>
