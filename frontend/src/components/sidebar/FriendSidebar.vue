<script setup lang="ts">
import { useRouter } from "vue-router";
import { SidebarHeader, SidebarContent } from "@/components/ui/sidebar";
import { useFriendStore } from "@/stores/friendStore";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import { onMounted } from "vue";
import Avatar from "../ui/avatar/Avatar.vue";
import AvatarImage from "../ui/avatar/AvatarImage.vue";
import AvatarFallback from "../ui/avatar/AvatarFallback.vue";
import { Sparkles } from "lucide-vue-next";
import { useSpaceStore } from "@/stores/spaceStore";

const router = useRouter();
const friendStore = useFriendStore();
const spaceStore = useSpaceStore();
const userStore = useUserStore();

const { friends, loading, friendCount } = storeToRefs(friendStore);
const { userPersonalSpace } = storeToRefs(userStore);


onMounted(() => {
  friendStore.fetchFriends();
});

const jumpToDm = async (conversationId: string) => {
  await spaceStore.joinDMSpace(conversationId);
};

const jumpToPersonalRoom = async (type: "NOTE" | "CALENDAR") => {
  const { noteId, calendarId } = userPersonalSpace.value;
  await spaceStore.joinDMSpace(type === "NOTE" ? noteId : calendarId);
  const path = type === "NOTE" ? `note/${noteId}` : `calendar/${calendarId}`;
  router.push(`/me/${path}`);
};
</script>

<template>
  <SidebarHeader class="gap-3.5 border-b px-4 py-3">
    <div class="flex w-full items-center justify-between">
      <input type="text" placeholder="Tìm bạn..."
        class="w-full rounded-lg border px-2 py-1 pb-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary" />
    </div>
  </SidebarHeader>

  <SidebarContent class="mt-5 bg-[var(--color-sidebar)] text-[var(--color-sidebar-foreground)]">
    <div class="p-2 space-y-1">
      <div @click="router.push('/me/subscriptions')"
        class="flex items-center gap-2 p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        <Sparkles class="w-4 h-4" /> Nâng cấp
      </div>
      <div @click="router.push('/me/friends')"
        class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        Bạn bè
      </div>
      <div @click="jumpToPersonalRoom('NOTE')"
        class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        Ghi chú
      </div>
      <div @click="jumpToPersonalRoom('CALENDAR')"
        class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        Lịch
      </div>
    </div>

    <div class="px-3 mt-3 text-xs text-[var(--color-muted-foreground)] uppercase">
      Bạn bè — {{ friendCount }}
    </div>

    <div v-if="loading" class="px-5 py-3 text-sm text-muted-foreground italic">
      Đang tải danh sách...
    </div>

    <div v-else-if="friends.length === 0" class="px-5 py-3 text-sm text-muted-foreground">
      Chưa có bạn bè nào.
    </div>

    <div v-else class="px-2 mt-2 space-y-1">
      <div v-for="friend in friends" :key="friend.id" @click="jumpToDm(friend.conversationId)"
        class="flex items-center gap-2 p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)] transition">
        <div class="relative">
          <Avatar class="h-8 w-8 text-xs font-bold uppercase">
            <AvatarImage v-if="friend.avatarUrl" :src="friend.avatarUrl" />
            <AvatarFallback class="bg-primary"> </AvatarFallback>
          </Avatar>

          <div class="absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-[var(--color-sidebar)]"
            :class="friend.isOnline ? 'bg-green-500' : 'bg-gray-400'" />
        </div>

        <span class="text-sm truncate">{{ friend.name }}</span>
      </div>
    </div>
  </SidebarContent>
</template>
