<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  connectWebSocket,
  addUserToSocketRoom,
  subscribeSpace,
  sendMessage,
} from "@/services/websocket/chatSocket";
import { getChatFromSpaceId } from "@/services/chatService";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";

const route = useRoute();
const roomId = route.params.roomId;
const spaceId = route.params.spaceId;

const spaceStore = useSpaceStore();

const { currentSpace } = storeToRefs(spaceStore);

const isSocketConnected = ref(false); // Check xem webSocket đã sẵn sàng chưa

const messages = ref([]);
const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null); // Ref đến container chứa tin nhắn (nhảy xuống đáy khi có tin nhắn mới)

// Số lượng tin nhắn mỗi lần lấy
const size = 50;
const page = 0;

onMounted(() => {
  if (roomId && spaceId) {
    connectWebSocket(async () => {
      await addUserToSocketRoom(sessionStorage.getItem("userId")!);
      isSocketConnected.value = true;
    });
  }
});

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const formatTime = (time: string) => {
  return dayjs(time).format("HH:mm DD/MM/YYYY");
};

const fetchMessages = async (id: string) => {
  const chatResponse = await getChatFromSpaceId(id as string, page, size);
  messages.value = chatResponse.data.content.reverse();
  scrollToBottom();
};

// Mỗi lần chuyển space, watch sẽ bắt và gọi hàm joinSpace để vào space và lấy tin nhắn
watch(
  [currentSpace, isSocketConnected],
  ([space, connected]) => {
    if (!space?.id || !connected) return;
    joinSpace(space.id);
  },
  { immediate: true }
);

// Hàm tham gia space và lắng nghe tin nhắn mới
const joinSpace = (spaceId: string) => {
  if (!spaceId) return;

  messages.value = [];
  console.log("➡️ Joining space:", spaceId);

  fetchMessages(spaceId); // Lấy tin nhắn cũ

  // subscribeSpace: Dùng để lắng nghe tin nhắn mới từ server
  subscribeSpace(spaceId, (msg) => {
    messages.value.push(msg);
    scrollToBottom();
  });
};

const handleSendMessage = async () => {
  if (!newMessage.value.trim()) return;

  const message = {
    content: newMessage.value,
    spaceId: currentSpace.value.id,
  };

  sendMessage(message); // Gửi tin nhắn lên server qua WebSocket

  newMessage.value = "";
};
</script>

<template>
  <div class="flex flex-col h-screen bg-transparent overflow-hidden">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b">
      <div class="flex items-center gap-2">
        <SidebarTrigger class="-ml-1" />
        <span class="font-semibold text-lg"># {{ currentSpace?.name }}</span>
      </div>

      <div class="flex gap-3 text-gray-600">
        <button class="hover:text-black">📞</button>
        <button class="hover:text-black">🎥</button>
      </div>
    </div>

    <!-- Message list -->
    <div ref="messageContainer" class="flex-1 overflow-y-auto px-4 py-3">
      <div class="min-h-full flex flex-col justify-end space-y-4">
        <div v-for="msg in messages" :key="msg.id" class="flex gap-3">
          <!-- Avatar -->
          <img :src="msg?.sender?.avatarUrl" class="w-10 h-10 rounded-full" />

          <!-- Content -->
          <div>
            <div class="flex items-center gap-2">
              <span class="font-semibold">{{ msg?.sender?.displayName }}</span>
              <span class="text-xs text-gray-400">{{
                formatTime(msg?.createdAt)
              }}</span>
            </div>
            <div class="text-white-800 whitespace-pre-wrap">
              {{ msg.content }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Input -->
    <div class="border-t px-4 py-3">
      <form @submit.prevent="handleSendMessage" class="flex gap-2">
        <input
          v-model="newMessage"
          placeholder="Nhắn tin..."
          class="flex-1 border px-3 py-2 rounded focus:outline-none focus:ring"
        />
        <button
          class="px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-700"
        >
          Gửi
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped></style>
