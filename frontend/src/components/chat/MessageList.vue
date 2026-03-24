<script setup lang="ts">
import { computed } from "vue";
import MessageItem from "./MessageItem.vue";
import dayjs from "dayjs";

interface Sender {
  avatarUrl: string;
  displayName: string;
}

interface Message {
  id: string;
  content: string;
  createdAt: string;
  sender: Sender;
}

const props = defineProps<{
  messages: Message[];
  containerRef: (el: HTMLElement | null) => void;
}>();

const processedMessages = computed(() => {
  return props.messages.map((msg, index) => {
    const prevMsg = props.messages[index - 1];

    const isDifferentDay =
      !prevMsg || !dayjs(msg.createdAt).isSame(dayjs(prevMsg.createdAt), "day");

    const isGrouped =
      prevMsg &&
      !isDifferentDay &&
      prevMsg.sender.username === msg.sender.username &&
      dayjs(msg.createdAt).diff(dayjs(prevMsg.createdAt), "minute") < 5;
    return { ...msg, isGrouped: !!isGrouped, isDifferentDay: !!isDifferentDay };
  });
});
</script>

<template>
  <div :ref="containerRef" class="flex-1 overflow-y-auto px-4 py-3">
    <div class="min-h-full flex flex-col justify-end space-y-4">
      <MessageItem
        v-for="msg in processedMessages"
        :key="msg.id"
        :message="msg"
        :isGrouped="msg.isGrouped"
        :isDifferentDay="msg.isDifferentDay"
      />
    </div>
  </div>
</template>
