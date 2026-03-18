<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  connectWebSocket,
  subscribeSpace,
  sendMessage,
} from "@/services/websocket/chatSocket";
import { getChatFromSpaceId } from "@/services/chatService";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";

// Tạo interface để thanwgf Typescript bớt kêu lỗi. Nhờ AI làm cho lẹ
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

// lấy route và lấy mấy cái trường cần thiết từ params
const route = useRoute();
const roomId = route.params.roomId;
const spaceId = route.params.spaceId;

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const messages = ref<Message[]>([]);
const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null); // Ref đến container chứa tin nhắn (nhảy xuống đáy khi có tin nhắn mới)
// Số lượng tin nhắn mỗi lần lấy
const size = 50;
const page = 0;

const isSocketConnected = ref(false); // Check xem webSocket đã sẵn sàng chưa

onMounted(() => {
  if (roomId && spaceId) {
    connectWebSocket(async () => {
      isSocketConnected.value = true; // Sẵn sàng gòi
    });
  }
});

// Mỗi lần currentSpace thay đổi hoặc isSocketConnected thay đổi, watch sẽ bắt và gọi hàm joinSpace để vào space và lấy tin nhắn
watch(
  [currentSpace, isSocketConnected],
  ([space, connected]) => {
    if (!space?.id || !connected) return; // Check xem cái socket sẵn sàng chưa thì mới joinSpace
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

const fetchMessages = async (id: string) => {
  const chatResponse = await getChatFromSpaceId(id as string, page, size);
  messages.value = chatResponse.data.content.reverse();
  scrollToBottom();
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

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const formatTime = (time: string) => {
  return dayjs(time).format("HH:mm DD/MM/YYYY");
};
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden background">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3.5 border-b">
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
    <div
      class="border-t border-white/10 px-4 py-3 bg-zinc-950/70 backdrop-blur-md"
    >
      <form @submit.prevent="handleSendMessage" class="flex gap-2">
        <input
          v-model="newMessage"
          placeholder="Nhắn tin..."
          class="flex-1 bg-white/5 border border-white/10 px-3 py-2 rounded text-white placeholder-gray-400 focus:outline-none focus:ring-1 focus:ring-teal-500"
        />
        <button
          class="px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-500 transition-colors font-medium"
        >
          Gửi
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped></style>
