<script setup lang="ts">
import FriendListTab from "./components/FriendListTab.vue";
import FriendPendingTab from "./components/FriendPendingTab.vue";
import FriendAddTab from "./components/FriendAddTab.vue";
import { useFriendPage } from "./composables/useFriendPage";
import { Users } from "lucide-vue-next";

const { activeTab, totalPending, handleSwitchTab } = useFriendPage();
</script>

<template>
  <div class="h-full flex flex-col background text-foreground">
    <!-- TOP NAV -->
    <div class="h-12 border-b border-border flex items-center px-4 gap-4 flex-shrink-0">
      <div class="flex items-center gap-2">
        <span class="text-xl">
          <Users class="w-4 h-4" />
        </span>
        <span class="font-semibold">Bạn bè</span>
      </div>

      <div class="flex gap-1 bg-muted rounded-md p-0.5">
        <button @click="activeTab = 'all'" :class="activeTab === 'all'
          ? 'bg-card text-foreground'
          : 'text-muted-foreground hover:text-foreground'
          " class="px-5 py-1.5 text-sm font-medium rounded transition">
          Tất cả
        </button>

        <button v-if="totalPending > 0" @click="activeTab = 'pending'" :class="activeTab === 'pending'
          ? 'bg-card text-foreground'
          : 'text-muted-foreground hover:text-foreground'
          " class="px-5 py-1.5 text-sm font-medium rounded transition flex items-center gap-1.5">
          Đang chờ xử lý
          <span
            class="bg-red-500 text-white text-xs font-bold rounded-full w-4 h-4 flex items-center justify-center leading-none">
            {{ totalPending }}
          </span>
        </button>

        <button @click="activeTab = 'add'" :class="activeTab === 'add'
          ? 'bg-primary text-primary-foreground'
          : 'text-muted-foreground hover:text-foreground'
          " class="px-5 py-1.5 text-sm font-medium rounded transition">
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
