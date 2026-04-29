<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from "vue";
import { chatSocket } from "@/services/websocket/chatSocket";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { useMessageStore } from "@/stores/messageStore";
import { storeToRefs } from "pinia";

import ChatHeader from "@/components/chat/ChatHeader.vue";
import MessageList from "@/components/chat/MessageList.vue";
import MessageInput from "@/components/chat/MessageInput.vue";
import MemberPanel from "@/components/chat/MemberPanel.vue";
import PinPanel from "@/components/chat/PinPanel.vue";

const route = useRoute();
const spaceId = ref(route.params.spaceId as string);

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const messageStore = useMessageStore();
const { messages, beforeHasMore, afterHasMore, replyingTo } =
  storeToRefs(messageStore);

const memberOpen = ref(true);
const toggleMembers = () => {
  memberOpen.value = !memberOpen.value;
  pinOpen.value = false;
};

const pinOpen = ref(false);
const togglePins = () => {
  pinOpen.value = !pinOpen.value;
  memberOpen.value = false;
};

onMounted(() => {
  if (currentSpace.value?.id) {
    joinSpace(currentSpace.value.id);
  }
});

onUnmounted(() => {
  chatSocket.leaveSpace(spaceId.value);
});

const joinSpace = async (id: string) => {
  if (!id) return;
  if (currentSpace.value?.id && currentSpace.value.id !== id) {
    chatSocket.leaveSpace(currentSpace.value.id);
  }
  messageStore.clearAll();
  await messageStore.fetchMessages(id, null);
  messageStore.scrollToBottom(spaceId.value);
  messageStore.subscribeToChat(id);
  messageStore.fetchPinnedList(id, null);
};

watch(currentSpace, (space, prevSpace) => {
  if (!space?.id) return;
  if (space.id === prevSpace?.id) return; // không re-join nếu cùng space
  joinSpace(space.id);
});
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <ChatHeader
      :space-name="currentSpace?.name ?? ''"
      :member-open="memberOpen"
      :pin-open="pinOpen"
      @toggle-members="toggleMembers"
      @toggle-pins="togglePins"
      @search="(q) => console.log('search:', q)"
    />

    <div class="flex flex-1 min-w-0 overflow-hidden">
      <div class="flex flex-col flex-1 min-w-0 overflow-hidden">
        <MessageList
          :key="currentSpace?.id"
          :messages="messages"
          :beforeHasMore="beforeHasMore"
          :afterHasMore="afterHasMore"
          :spaceId="currentSpace?.id ?? ''"
          :space-name="currentSpace?.name ?? ''"
        />
        <MessageInput
          :spaceId="currentSpace?.id ?? ''"
          :replying-to="replyingTo"
        />
      </div>

      <div
        class="flex-none border-l h-full overflow-hidden transition-all duration-300 ease-in-out"
        :style="{
          width: pinOpen ? '260px' : '0px',
          opacity: pinOpen ? 1 : 0,
          borderColor: 'var(--border)',
        }"
      >
        <PinPanel :space-id="currentSpace?.id ?? ''" />
      </div>

      <!-- Member Sidebar -->
      <div
        class="flex-none border-l h-full overflow-hidden transition-all duration-300 ease-in-out"
        :style="{
          width: memberOpen ? '250px' : '0px',
          opacity: memberOpen ? 1 : 0,
          borderColor: 'var(--border)',
        }"
      >
        <MemberPanel />
      </div>
    </div>
  </div>
</template>
