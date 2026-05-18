<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from "vue";
import { chatSocket } from "@/services/websocket/chatSocket";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { useMessageStore } from "@/stores/messageStore";
import { useFriendStore } from "@/stores/friendStore";
import { useRoomsStore } from "@/stores/roomStore";
import { useCalendarSuggestionStore } from "@/stores/calendarSuggestionStore";
import { useUserStore } from "@/stores/userStore";
import { buildSuggestedEventDraft } from "@/utils/calendarSuggestion";
import { storeToRefs } from "pinia";

import ChatHeader from "@/components/chat/ChatHeader.vue";
import MessageList from "@/components/chat/MessageList.vue";
import MessageInput from "@/components/chat/MessageInput.vue";
import MemberPanel from "@/components/chat/MemberPanel.vue";
import PinPanel from "@/components/chat/PinPanel.vue";
import CalendarSuggestionChannelDialog from "@/components/chat/sub-components/CalendarSuggestionChannelDialog.vue";
import type { CalendarChannelOption } from "@/types/CalendarSuggestion";

const route = useRoute();
const spaceId = ref(route.params.spaceId as string);

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
const roomStore = useRoomsStore();
const calendarSuggestionStore = useCalendarSuggestionStore();
const { rooms } = storeToRefs(roomStore);
const userStore = useUserStore();
const { user } = storeToRefs(userStore);

const messageStore = useMessageStore();
const { messages, beforeHasMore, afterHasMore, replyingTo } =
  storeToRefs(messageStore);

const friendStore = useFriendStore();
const dmFriend = computed(() => {
  if (!isDM.value) return null;
  return (
    friendStore.friends.find(
      (f) => f.conversationId === currentSpace.value?.id,
    ) ?? null
  );
});

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
  console.log("[ChatWindow] Bat dau vao space:", {
    nextSpaceId: id,
    currentSpaceId: currentSpace.value?.id ?? null,
  });
  if (currentSpace.value?.id && currentSpace.value.id !== id) {
    chatSocket.leaveSpace(currentSpace.value.id);
  }
  messageStore.clearAll();
  await messageStore.fetchMessages(id, null);
  messageStore.scrollToBottom(spaceId.value);
  messageStore.subscribeToChat(id);
  messageStore.fetchPinnedList(id, null);
  console.log("[ChatWindow] Da vao xong space:", {
    activeSpaceId: id,
    loadedMessages: messageStore.messages.length,
  });
};

const handleOpenSuggestion = (messageId: string) => {
  const suggestion = messageStore.suggestionsByMessageId[messageId];
  if (!suggestion) {
    console.warn("[Goi y] Da bam nut tao lich nhung khong tim thay cache cho message:", messageId);
    return;
  }

  calendarSuggestionStore.openChannelDialog(suggestion);
};

const handleSelectCalendarChannel = async (option: CalendarChannelOption) => {
  const suggestion = calendarSuggestionStore.selectedSuggestion;
  if (!suggestion) return;

  const room = rooms.value.find((item) => item.id === option.roomId);
  if (!room) return;

  // Lưu sẵn draft để màn hình calendar mở ra là nhận đúng dữ liệu từ gợi ý.
  calendarSuggestionStore.setPendingDraft(
    option.spaceId,
    buildSuggestedEventDraft(suggestion),
  );
  calendarSuggestionStore.closeChannelDialog();

  await roomStore.changeRoom(room, option.spaceId, "CALENDAR");
};

watch(currentSpace, (space, prevSpace) => {
  console.log("[ChatWindow] currentSpace da thay doi:", {
    prevSpaceId: prevSpace?.id ?? null,
    nextSpaceId: space?.id ?? null,
  });
  if (!space?.id) return;
  if (space.id === prevSpace?.id) return; // không re-join nếu cùng space
  joinSpace(space.id);
});

watch(
  () => user.value?.id,
  async (userId) => {
    console.log("[ChatWindow] watcher user da chay:", {
      userId: userId ?? null,
      suggestionSubscriptionReady: messageStore.suggestionSubscriptionReady,
    });
    if (!userId) return;

    await messageStore.subscribeToSuggestions();
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <ChatHeader
      :space-name="currentSpace?.name ?? ''"
      :space-id="spaceId"
      :member-open="memberOpen"
      :pin-open="pinOpen"
      :dm-friend="dmFriend"
      :is-dm="isDM"
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
          :is-dm="isDM"
          :friendName="dmFriend?.name"
          @open-suggestion="handleOpenSuggestion"
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
        v-if="!isDM"
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

    <CalendarSuggestionChannelDialog
      v-model:open="calendarSuggestionStore.isChannelDialogOpen"
      @select="handleSelectCalendarChannel"
    />
  </div>
</template>
