<script setup lang="ts">
import { ref, watch, onUnmounted, computed, nextTick } from "vue";
import { chatSocket } from "./services/chatSocket";
import { useSpaceStore } from "@/features/spaces/stores/spaceStore.ts";
import { useMessageStore } from "@/features/chats/stores/messageStore";
import { useFriendStore } from "@/features/friends/stores/friendStore";
import { storeToRefs } from "pinia";

import type { MessageEventSuggestion } from "@/types/SuggestionTypes";
import { useChatComposable } from "@/features/chats/composable/chat.composable.ts";
import { useChatUtilsComposable } from "@/features/chats/composable/chat-utils.composable.ts";
import { useChatSocketComposable } from "@/features/chats/composable/chat-socket.compsable.ts";
import ChatHeader from "@/features/chats/components/ChatHeader.vue";
import MessageList from "@/features/chats/components/MessageList.vue";
import MessageInput from "@/features/chats/components/MessageInput.vue";
import PinPanel from "@/features/chats/components/PinPanel.vue";
import MemberPanel from "@/features/chats/components/MemberPanel.vue";
import SuggestionDialog from "@/features/chats/components/SuggestionDialog.vue";
import { chatJoinedSpaceId } from "./utils/chat.utils";

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
const spaceId = currentSpace.value?.id;

const messageStore = useMessageStore();
const { messages, beforeHasMore, afterHasMore, replyingTo } =
  storeToRefs(messageStore);
const chat = useChatComposable();
const chatUtils = useChatUtilsComposable();
const chatRealtime = useChatSocketComposable();

const friendStore = useFriendStore();
const dmFriend = computed(() => {
  if (!isDM.value) return null;
  return (
    friendStore.friends.find(
      (f) => f.conversationId === spaceId,
    ) ?? null
  );
});

const suggestionDialogOpen = ref(false);
const suggestionData = ref<MessageEventSuggestion | null>(null);

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

const isDM = computed(() => currentSpace.value?.roomType === "DM");
const sidePanelOpen = computed(() => pinOpen.value || (!isDM.value && memberOpen.value));

const closeSidePanel = () => {
  pinOpen.value = false;
  memberOpen.value = false;
};

onUnmounted(() => {
  if (chatJoinedSpaceId.value) {
    chatSocket.leaveSpace(chatJoinedSpaceId.value);
  }
  chatJoinedSpaceId.value = null;
});

const joinSpace = async (id: string) => {
  if (!id) return;
  if (chatJoinedSpaceId.value === id) return; // đã join rồi

  console.trace("JOIN RUNNING");
  if (chatJoinedSpaceId.value && chatJoinedSpaceId.value !== id) {
    chatSocket.leaveSpace(chatJoinedSpaceId.value);
  }
  chatJoinedSpaceId.value = id;

  await messageStore.clearAll();
  await chat.fetchMessages(id, null);
  chatUtils.scrollToBottom(id);
  chatRealtime.subscribeToChat(id);
  chat.fetchPinnedList(id, null);
};

watch(currentSpace, (space) => {
  if (!space?.id) return;
  joinSpace(space.id);
}, { immediate: true });

const handleOpenSuggestion = async (messageId: string) => {
  const suggestion = messageStore.suggestionsByMessageId[messageId];
  if (!suggestion) {
    return;
  }
  if (suggestion.suggestionType === "NONE") return;


  // Vô hiệu hóa riêng gợi ý đã chọn; gợi ý mới nhất của message khác vẫn còn dùng được.
  delete messageStore.suggestionsByMessageId[messageId];

  suggestionData.value = suggestion;
  await nextTick();
  suggestionDialogOpen.value = true;
};
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <ChatHeader :space-name="currentSpace?.name ?? ''" :space-id="spaceId" :member-open="memberOpen" :pin-open="pinOpen"
      :dm-friend="dmFriend" :is-dm="isDM" @toggle-members="toggleMembers" @toggle-pins="togglePins"
      @search="(q) => console.log('search:', q)" />

    <div class="relative flex flex-1 min-w-0 overflow-hidden">
      <div class="flex flex-col flex-1 min-w-0 overflow-hidden">
        <MessageList :key="currentSpace?.id" :messages="messages" :beforeHasMore="beforeHasMore"
          :afterHasMore="afterHasMore" :spaceId="currentSpace?.id ?? ''" :space-name="currentSpace?.name ?? ''"
          :is-dm="isDM" :friendName="dmFriend?.name" @open-suggestion="handleOpenSuggestion" />
        <MessageInput :spaceId="currentSpace?.id ?? ''" :replying-to="replyingTo" />
      </div>

      <button v-if="sidePanelOpen" type="button" aria-label="Dong panel ben phai"
        class="absolute inset-0 z-20 bg-background/50 backdrop-blur-[1px] lg:hidden" @click="closeSidePanel" />

      <div v-if="pinOpen"
        class="absolute inset-y-0 right-0 z-30 h-full w-[min(86vw,320px)] overflow-hidden border-l shadow-xl transition-transform duration-300 ease-in-out lg:relative lg:z-auto lg:w-65 lg:flex-none lg:shadow-none"
        :style="{
          borderColor: 'var(--border)',
        }">
        <PinPanel :space-id="currentSpace?.id ?? ''" />
      </div>

      <!-- Member Sidebar -->
      <div v-if="!isDM && memberOpen"
        class="absolute inset-y-0 right-0 z-30 h-full w-[min(86vw,320px)] overflow-hidden border-l shadow-xl transition-transform duration-300 ease-in-out lg:relative lg:z-auto lg:w-62.5 lg:flex-none lg:shadow-none"
        :style="{
          borderColor: 'var(--border)',
        }">
        <MemberPanel />
      </div>
    </div>

    <SuggestionDialog v-model:open="suggestionDialogOpen" :room-id="currentSpace?.id ?? ''"
      :message-info="suggestionData" @close="suggestionDialogOpen = false" />
  </div>
</template>
