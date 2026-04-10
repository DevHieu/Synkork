<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from "vue";
import { chatSocket } from "@/services/websocket/chatSocket";

import { getChatFromSpaceId } from "@/services/chatService";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

import ChatHeader from "@/components/chat/ChatHeader.vue";
import MessageList from "@/components/chat/MessageList.vue";
import MessageInput from "@/components/chat/MessageInput.vue";
import type { Message } from "@/types/Message";
import MemberSidebar from "../sidebar/MemberSidebar.vue";

const route = useRoute();
const spaceId = route.params.spaceId as string;

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const memberOpen = ref(true);
const toggleMembers = () => (memberOpen.value = !memberOpen.value);

const messages = ref<Message[]>([]);
const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null);

const size = 20;
const hasMore = ref(false);
const lastCursor = ref<string | null>("");

const isSocketConnected = ref(false);

const setContainerRef = (el: HTMLElement | null) => {
  messageContainer.value = el;
};

onMounted(() => {
  if (spaceId) {
    isSocketConnected.value = true;
  }
});

// Xóa subscription khi rời khỏi space
onUnmounted(() => {
  chatSocket.leaveSpace(spaceId);
});

watch(
  [currentSpace, isSocketConnected],
  ([space, connected]) => {
    if (!space?.id || !connected) return;
    joinSpace(space.id);
  },
  { immediate: true },
);

const joinSpace = async (spaceId: string) => {
  if (!spaceId) return;

  if (currentSpace.value?.id && currentSpace.value.id !== spaceId) {
    chatSocket.leaveSpace(currentSpace.value.id);
  }

  await clearAll();
  await fetchMessages(spaceId, null);
  await subscribeToChat(spaceId);
  await scrollToBottom();
};

const clearAll = async () => {
  messages.value = [];
  newMessage.value = "";
  messageContainer.value = null;
  lastCursor.value = null;
  hasMore.value = false;

  console.log(messages.value);
};

const subscribeToChat = (spaceId: string) => {
  chatSocket.subscribeMessages(spaceId, (msg: Message) => {
    messages.value.push(msg);
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

const fetchMessages = async (id: string, cursor: string | null) => {
  const chatResponse = await getChatFromSpaceId(id, size, cursor);
  messages.value = [...chatResponse.data.messages.reverse(), ...messages.value];

  console.log(chatResponse.data);
  console.log(id, cursor);

  hasMore.value = chatResponse.data.hasMore;
  if (hasMore.value) lastCursor.value = chatResponse.data.nextCursor;
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

const loadMore = async () => {
  await fetchMessages(currentSpace.value.id, lastCursor.value);
};
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <ChatHeader
      :space-name="currentSpace?.name ?? ''"
      :member-open="memberOpen"
      @toggle-members="toggleMembers"
      @search="(q) => console.log('search:', q)"
    />

    <div class="flex flex-1 min-w-0 overflow-hidden">
      <div class="flex flex-col flex-1 min-w-0 overflow-hidden">
        <MessageList
          :key="currentSpace?.id"
          :messages="messages"
          :hasMore="hasMore"
          :container-ref="setContainerRef"
          :space-name="currentSpace?.name ?? ''"
          @loadMore="loadMore"
        />
        <MessageInput v-model="newMessage" @send="handleSendMessage" />
      </div>

      <!-- Member Sidebar -->
      <div
        class="flex-none border-l h-full overflow-hidden transition-all duration-300 ease-in-out"
        :style="{
          width: memberOpen ? '250px' : '0px',
          opacity: memberOpen ? 1 : 0,
          borderColor: 'var(--border)',
          background: 'transparent',
        }"
      >
        <div
          class="flex-none border-l h-full overflow-hidden transition-all duration-300 ease-in-out"
          :style="{
            width: memberOpen ? '250px' : '0px',
            opacity: memberOpen ? 1 : 0,
            borderColor: 'var(--border)',
            background: 'transparent',
          }"
        >
          <MemberSidebar />
        </div>
      </div>
    </div>
  </div>
</template>
