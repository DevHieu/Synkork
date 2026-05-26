<script setup lang="ts">
import NavUser from "@/components/sidebar/NavUser.vue";
import VoiceControlBar from "@/components/sidebar/VoiceControlBar.vue";
import {
  SidebarInset,
  SidebarProvider,
  Sidebar,
  SidebarContent,
  SidebarFooter,
} from "@/components/ui/sidebar";
import { getCookie } from "@/lib/cookies";
import { socketService } from "@/services/websocket/socketService";
import { useUserStore } from "@/stores/userStore";
import { useMessageStore } from "@/stores/messageStore";
import { storeToRefs } from "pinia";
import { ref, provide, watch } from "vue";
import { useFriendStore } from "@/stores/friendStore";

const userStore = useUserStore();
const messageStore = useMessageStore();
const friendStore = useFriendStore();
const { user } = storeToRefs(userStore);

const spaceOpen = ref(true);

provide("setSpaceOpen", (val: boolean) => {
  spaceOpen.value = val;
});

watch(
  () => getCookie("accessToken"),
  async (newToken) => {
    if (newToken) {
      await socketService.connect();
      await userStore.getUserInfo();

      friendStore.fetchFriends();
      friendStore.fetchPendingRequests();
      friendStore.fetchSentRequests();

      messageStore.subscribeToSuggestions();
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex h-screen w-full overflow-hidden">
    <!-- RoomSidebar -->
    <SidebarProvider :open="true" style="
        width: auto;
        min-height: 100%;
        flex: none;
        position: static;
        z-index: 2;
      ">
      <Sidebar collapsible="none" class="w-16! border-r h-full">
        <SidebarContent class="overflow-y-auto overflow-x-hidden no-scrollbar">
          <slot name="room-sidebar" />
        </SidebarContent>
        <SidebarFooter v-if="!spaceOpen" class="p-0 border-t gap-0">
          <VoiceControlBar :collapsed="!spaceOpen" />
          <NavUser :user="{
            name: user?.username ?? 'Unknown',
            email: user?.email ?? '',
            avatar: user?.avatarUrl,
          }" :collapsed="true" />
        </SidebarFooter>
      </Sidebar>
    </SidebarProvider>

    <!-- SpaceSidebar + Content -->
    <SidebarProvider v-model:open="spaceOpen" :style="{
      '--sidebar-width': '300px',
      minHeight: '100%',
      flex: 1,
      position: 'static',
    }">
      <div class="flex flex-col border-r h-full bg-sidebar overflow-hidden transition-all duration-300 ease-in-out"
        :style="{
          width: spaceOpen ? '300px' : '0px',
          opacity: spaceOpen ? 1 : 0,
        }">
        <Sidebar collapsible="none" class="h-full w-full">
          <slot name="space-sidebar" />
          <SidebarFooter v-if="spaceOpen" class="p-0 border-t gap-0">
            <VoiceControlBar :collapsed="!spaceOpen" />
            <NavUser :user="{
              name: user?.username ?? 'Unknown',
              email: user?.email ?? '',
              avatar: user?.avatarUrl,
            }" :collapsed="false" />
          </SidebarFooter>
        </Sidebar>
      </div>

      <SidebarInset class="flex-1 min-w-0 background">
        <RouterView />
      </SidebarInset>
    </SidebarProvider>
  </div>
</template>
