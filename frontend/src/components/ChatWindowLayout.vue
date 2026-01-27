<template>
  <div class="flex flex-col h-screen bg-transparent">
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
    <div
      ref="messageContainer"
      class="flex-1 overflow-y-auto px-4 py-3 space-y-4"
    >
      <div v-for="msg in messages" :key="msg.id" class="flex gap-3">
        <!-- Avatar -->
        <img :src="msg.sender.avatarUrl" class="w-10 h-10 rounded-full" />

        <!-- Content -->
        <div>
          <div class="flex items-center gap-2">
            <span class="font-semibold">{{ msg.sender.displayName }}</span>
            <span class="text-xs text-gray-400">{{ msg.createdAt }}</span>
          </div>
          <div class="text-gray-800 whitespace-pre-wrap">
            {{ msg.content }}
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

<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  connectWebSocket,
  addUserToSocketRoom,
  subscribeSpace,
  sendMessage,
} from "../services/websocket/chatSocket";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

const route = useRoute();
const spaceStore = useSpaceStore();

const { currentSpace } = storeToRefs(spaceStore);
const roomId = route.params.roomId;
const spaceId = route.params.spaceId;

onMounted(() => {
  if (roomId && spaceId) {
    connectWebSocket(async () => {
      await addUserToSocketRoom(sessionStorage.getItem("userId")!);
      subscribeSpace(currentSpace.value.id, (msg) => {
        console.log("📨", msg);
        console.log("📨", messages.value);
        messages.value.push(msg);
      });
    });
  }
});

watch(currentSpace, (newSpace) => {
  if (!newSpace?.id) return;

  console.log("🔁 switch space:", newSpace.id);
  messages.value = [];

  joinSpace(newSpace.id);
});

const joinSpace = (spaceId: String) => {
  if (!spaceId) return;

  subscribeSpace(currentSpace.value.id, (msg) => {
    console.log("📨", msg);
    console.log("📨", messages.value);
    messages.value.push(msg);
  });
};

const newMessage = ref("");
const messageContainer = ref<HTMLElement | null>(null);

const messages = ref([]);

const handleSendMessage = async () => {
  if (!newMessage.value.trim()) return;

  const message = {
    content: newMessage.value,
    spaceId: currentSpace.value.id,
  };

  sendMessage(message);

  newMessage.value = "";

  nextTick(() => {
    messageContainer.value?.scrollTo({
      top: messageContainer.value.scrollHeight,
      behavior: "smooth",
    });
  });
  newMessage.value = "";
};
</script>

<style scoped>
/* không cần css thêm */
</style>
