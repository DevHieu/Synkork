<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from "vue";
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
const spaceId = route.params.spaceId as string;

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

const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null);

const setContainerRef = (el: HTMLElement | null) => {
  messageContainer.value = el;
};

const isSocketConnected = ref(false);

onMounted(() => {
  if (spaceId) isSocketConnected.value = true;
});

onUnmounted(() => {
  chatSocket.leaveSpace(spaceId);
});

watch(
  [currentSpace, isSocketConnected],
  ([space, connected]) => {
    if (!space?.id || !connected) return;
    joinSpace(space.id);
    console.log(space);
  },
  { immediate: true },
);

const joinSpace = async (id: string) => {
  if (!id) return;
  if (currentSpace.value?.id && currentSpace.value.id !== id) {
    chatSocket.leaveSpace(currentSpace.value.id);
  }
  messageStore.clearAll();
  await messageStore.fetchMessages(id, null);
  messageStore.subscribeToChat(id);
  messageStore.fetchPinnedList(id, null);
  await scrollToBottom();
};

const handleSendMessage = () => {
  messageStore.sendMessage(currentSpace.value.id, newMessage.value);
  newMessage.value = "";
};

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const jumpToMessage = async (id: string) => {
  await messageStore.jumpToMessage(spaceId, id);

  await nextTick();

  const el = document.getElementById(`message-${id}`);
  const container = messageContainer.value;

  if (!el || !container) return;

  const offset =
    el.offsetTop - container.clientHeight / 2 + el.clientHeight / 2;

  container.scrollTop = offset;

  el.classList.add("message-highlight");
  setTimeout(() => el.classList.remove("message-highlight"), 2000);
};
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
          :container-ref="setContainerRef"
          :space-name="currentSpace?.name ?? ''"
          @loadBeforeMore="() => messageStore.loadMore(currentSpace.id)"
          @loadAfterMore="
            () => messageStore.fetchNewerMessages(currentSpace.id)
          "
        />
        <MessageInput
          v-model="newMessage"
          :replying-to="replyingTo"
          @send="handleSendMessage"
          @cancel-reply="messageStore.setReply(null)"
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
        <PinPanel @jump-to="jumpToMessage" />
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
