<script setup lang="ts">
import NavUser from "@/features/friends/components/NavUser.vue";
import VoiceControlBar from "@/components/VoiceControlBar.vue";
import {
  SidebarInset,
  SidebarProvider,
  Sidebar,
  SidebarContent,
  SidebarFooter,
} from "@/components/ui/sidebar";
import { getCookie } from "@/lib/cookies";
import { socketService } from "@/services/socketService";
import { useUserStore } from "@/features/users/stores/userStore";
import { storeToRefs } from "pinia";
import { ref, provide, watch, onMounted, onUnmounted } from "vue";
import { useNotificationStore } from '@/features/notifications/stores/notificationStore'
import { useFriendActions } from "@/features/friends/composables/useFriendActions";
import globalAudio from "@/utils/appAudioManager"
import { useChatSocketComposable } from "@/features/chats/composable/chat-socket.compsable";
import { WifiOff } from "lucide-vue-next";

const notificationStore = useNotificationStore()
const userStore = useUserStore();
const { fetchFriends, fetchPendingRequests, fetchSentRequests } =
  useFriendActions();
const { user } = storeToRefs(userStore);

const isOnline = ref(navigator.onLine);
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

      fetchFriends();
      fetchPendingRequests();
      fetchSentRequests();

      useChatSocketComposable().subscribeToSuggestions();
    }
  },
  { immediate: true },
);

function handleOffline() {
  isOnline.value = false;
}

function handleOnline() {
  isOnline.value = true;
}

onMounted(async () => {
  await notificationStore.fetchNotifications()
  await notificationStore.connect()
  await globalAudio.init()

  window.addEventListener("offline", handleOffline);
  window.addEventListener("online", handleOnline);
})

onUnmounted(() => {
  window.removeEventListener("offline", handleOffline);
  window.removeEventListener("online", handleOnline);
})

</script>

<template>
  <Transition enter-active-class="transition-transform duration-500 ease-out" enter-from-class="-translate-y-full"
    enter-to-class="translate-y-0" leave-active-class="transition-transform duration-300 ease-in"
    leave-from-class="translate-y-0" leave-to-class="-translate-y-full">
    <div v-if="!isOnline"
      class="fixed left-0 top-0 z-50 flex h-8 w-full items-center justify-center gap-2 border-b border-destructive/25 bg-destructive/95 px-4 text-xs font-medium text-white shadow-sm backdrop-blur"
      role="status" aria-live="polite">
      <WifiOff class="h-3.5 w-3.5 shrink-0" />
      <span class="truncate">Mất kết nối mạng. Đang chờ kết nối lại...</span>
    </div>
  </Transition>

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

  <!-- Uhhh vấn đề là lúc đầu cái này trong VoiceWindow để nó là chỗ cho thanwgf zego nhét cái audio vào.
   Nhưng nếu để đấy thì phải vào trong window thì mới có tiếng. Nhưng tao lại làm có thể thu nhỏ bên sidebar nên vậy là không ổn
   Nên cái này tao sẽ đưa ra ngoài đây. Để nó luôn có chỗ để zego nó nhét âm thanh. DONE -->
  <div id="audio-players" hidden />
</template>
