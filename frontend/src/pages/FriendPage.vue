<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useFriendStore } from "@/stores/friendStore";
import { friendSocket } from "@/services/websocket/friendSocket";
import FriendListTab from "@/components/sidebar/friend/FriendListTab.vue";
import FriendPendingTab from "@/components/sidebar/friend/FriendPendingTab.vue";
import FriendAddTab from "@/components/sidebar/friend/FriendAddTab.vue";

const store = useFriendStore();

const activeTab = ref<"all" | "pending" | "add">("all");

onMounted(async () => {
  // Fetch data và subscribe socket song song
  await Promise.all([
    store.fetchFriends(),
    store.fetchPendingRequests(),
    store.fetchSentRequests(),
  ]);

  // Subscribe riêng — không await để không block
  subscribeFriendSocket();
  subscribeFriendAccept();
  subscribeFriendReject();
  subscribeFriendCancel();
  subscribeFriendRemove();
});

// Có lời mời mới đến → fetch pending
const subscribeFriendSocket = () => {
  friendSocket.subscribeFriendRequest(async () => {
    await Promise.all([
      store.fetchPendingRequests(),
      store.fetchSentRequests(),
    ]);
  });
};

// Lời mời được chấp nhận → fetch friends + clear pending/sent
const subscribeFriendAccept = () => {
  friendSocket.subscribeFriendAccept(async () => {
    await Promise.all([
      store.fetchFriends(),
      store.fetchPendingRequests(),
      store.fetchSentRequests(),
    ]);
  });
};

// Lời mời bị từ chối → clear sent
const subscribeFriendReject = () => {
  friendSocket.subscribeFriendReject(async () => {
    await store.fetchSentRequests();
  });
};

// Lời mời bị hủy → clear pending
const subscribeFriendCancel = () => {
  friendSocket.subscribeFriendCancel(async () => {
    await store.fetchPendingRequests();
  });
};

// Bị xóa khỏi danh sách bạn → refresh friends
const subscribeFriendRemove = () => {
  friendSocket.subscribeFriendRemove(async () => {
    await store.fetchFriends();
  });
};

const totalPending = () =>
  store.pendingRequests.length + store.sentRequests.length;

const handleSwitchTab = async (tab: "pending") => {
  activeTab.value = tab;
  if (tab === "pending") {
    await Promise.all([
      store.fetchPendingRequests(),
      store.fetchSentRequests(),
    ]);
  }
};

// Tự chuyển về "all" khi hết cả 2 danh sách
watch(
  () => store.pendingRequests.length + store.sentRequests.length,
  (total) => {
    if (total === 0 && activeTab.value === "pending") {
      activeTab.value = "all";
    }
  },
);
</script>

<template>
  <div class="h-full flex flex-col background text-foreground">
    <!-- TOP NAV -->
    <div
      class="h-12 border-b border-border flex items-center px-4 gap-4 flex-shrink-0"
    >
      <div class="flex items-center gap-2">
        <span class="text-xl">👥</span>
        <span class="font-semibold">Bạn bè</span>
      </div>

      <div class="flex gap-1 bg-muted rounded-md p-0.5">
        <button
          @click="activeTab = 'all'"
          :class="
            activeTab === 'all'
              ? 'bg-card text-foreground'
              : 'text-muted-foreground hover:text-foreground'
          "
          class="px-5 py-1.5 text-sm font-medium rounded transition"
        >
          Tất cả
        </button>

        <button
          v-if="totalPending() > 0"
          @click="activeTab = 'pending'"
          :class="
            activeTab === 'pending'
              ? 'bg-card text-foreground'
              : 'text-muted-foreground hover:text-foreground'
          "
          class="px-5 py-1.5 text-sm font-medium rounded transition flex items-center gap-1.5"
        >
          Đang chờ xử lý
          <span
            class="bg-red-500 text-white text-xs font-bold rounded-full w-4 h-4 flex items-center justify-center leading-none"
          >
            {{ totalPending() }}
          </span>
        </button>

        <button
          @click="activeTab = 'add'"
          :class="
            activeTab === 'add'
              ? 'bg-primary text-primary-foreground'
              : 'text-muted-foreground hover:text-foreground'
          "
          class="px-5 py-1.5 text-sm font-medium rounded transition"
        >
          Thêm Bạn
        </button>
      </div>
    </div>

    <!-- NỘI DUNG TAB -->
    <FriendListTab v-if="activeTab === 'all'" />
    <FriendPendingTab v-else-if="activeTab === 'pending'" />
    <FriendAddTab v-else @switchTab="handleSwitchTab" />
  </div>
</template>