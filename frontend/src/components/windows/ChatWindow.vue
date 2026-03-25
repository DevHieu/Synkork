<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from "vue";
import { socketService } from "@/services/websocket/socketService";
import { chatSocket } from "@/services/websocket/chatSocket";

import { getChatFromSpaceId } from "@/services/chatService";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

import ChatHeader from "@/components/chat/ChatHeader.vue";
import MessageList from "@/components/chat/MessageList.vue";
import MessageInput from "@/components/chat/MessageInput.vue";
import type { Message } from "@/types/Message";

const route = useRoute();
const spaceId = route.params.spaceId;

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const messages = ref<Message[]>([]);
const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null);

const size = 50;
const page = 0;

const isSocketConnected = ref(false);

const setContainerRef = (el: HTMLElement | null) => {
  messageContainer.value = el;
};

onMounted(() => {
  if (spaceId) {
    socketService.connect(() => {
      isSocketConnected.value = true;
    });
  }
});

watch(
  [currentSpace, isSocketConnected],
  ([space, connected]) => {
    if (!space?.id || !connected) return;
    joinSpace(space.id);
  },
  { immediate: true },
);

const joinSpace = (spaceId: string) => {
  if (!spaceId) return;
  messages.value = [];
  fetchMessages(spaceId);
  subscribeToChat(spaceId);
};

const subscribeToChat = (spaceId: string) => {
  chatSocket.subscribeMessages(spaceId, (msg: Message) => {
    messages.value.push(msg);
    scrollToBottom();
  });

  chatSocket.subscribeDelete(spaceId, (messageId: string) => {
    const index = messages.value.findIndex((m) => m.id === messageId);
    if (index !== -1)
      messages.value[index] = { ...messages.value[index], deleted: true };
  });

  chatSocket.subscribeUpdate(spaceId, (updatedMsg: Message) => {
    const index = messages.value.findIndex((m) => m.id === updatedMsg.id);
    if (index !== -1) messages.value[index] = updatedMsg;
  });
};

const fetchMessages = async (id: string) => {
  const chatResponse = await getChatFromSpaceId(id, page, size);
  messages.value = chatResponse.data.content.reverse();
  scrollToBottom();
};

const handleSendMessage = () => {
  if (!newMessage.value.trim()) return;

  chatSocket.sendMessage({
    content: newMessage.value,
    spaceId: currentSpace.value.id,
  });

  newMessage.value = "";
};

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden background">
    <ChatHeader :space-name="currentSpace?.name ?? ''" />

    <MessageList :messages="messages" :container-ref="setContainerRef" />

    <MessageInput v-model="newMessage" @send="handleSendMessage" />
  </div>
</template>
