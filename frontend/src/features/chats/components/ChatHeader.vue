<script setup lang="ts">
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Pin, Users } from "lucide-vue-next";
import type { Friend } from "@/types/Friends";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import UserInfoPopover from "@/components/dialog/UserInfoPopover.vue";
import SearchBar from "./sub-components/SearchBar.vue";


const props = defineProps<{
  spaceName: string;
  spaceId: string;
  memberOpen: boolean;
  pinOpen: boolean;
  dmFriend: Friend | null;
  isDm: boolean;
}>();

const emit = defineEmits<{
  search: [query: string];
  "toggle-members": [];
  "toggle-pins": [];
}>();
</script>

<template>
  <div class="flex items-center justify-between px-4 py-3 border-b transition-all">
    <!-- Left -->
    <div class="flex items-center gap-2 min-w-0">
      <SidebarTrigger class="-ml-1 shrink-0" />
      <template v-if="isDm && dmFriend">
        <UserInfoPopover :username="dmFriend.username">
          <Avatar class="w-7 h-7 text-xs font-bold uppercase">
            <AvatarImage v-if="dmFriend.avatarUrl" :src="dmFriend.avatarUrl" />
            <AvatarFallback class="bg-primary"> </AvatarFallback>
          </Avatar>
        </UserInfoPopover>
        <span class="font-semibold text-[15px] truncate">{{
          dmFriend.name
        }}</span>
      </template>

      <!-- Group: hiện dấu # -->
      <template v-else>
        <span class="text-muted-foreground text-xl font-light shrink-0">#</span>

        <span class="font-semibold text-[15px] truncate">{{ spaceName }}</span>
      </template>
    </div>

    <!-- Right -->
    <div class="flex items-center gap-1.5 shrink-0 ml-4">
      <SearchBar :space-id="spaceId" />

      <!-- Pin -->
      <button class="w-8 h-8 rounded-md flex items-center justify-center transition-colors" :class="pinOpen
        ? 'bg-accent text-foreground'
        : 'text-foreground/70 hover:bg-accent hover:text-foreground'
        " title="Tin nhắn được ghim" @click="$emit('toggle-pins')">
        <Pin class="w-4.5 h-4.5" />
      </button>

      <template v-if="!isDm">
        <div class="w-px h-5 bg-border" />

        <!-- Toggle members -->
        <button class="w-8 h-8 rounded-md flex items-center justify-center transition-colors" :class="memberOpen
          ? 'bg-accent text-foreground'
          : 'text-foreground/70 hover:bg-accent hover:text-foreground'
          " title="Danh sách thành viên" @click="$emit('toggle-members')">
          <Users class="w-4.5 h-4.5" />
        </button>
      </template>
    </div>
  </div>
</template>
