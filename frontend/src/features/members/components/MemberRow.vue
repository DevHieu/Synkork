<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { ref } from "vue";
import UserInfoPopover from "@/components/dialog/UserInfoPopover.vue";
import { useRoomsStore } from "@/features/rooms/stores/roomStore";
import { storeToRefs } from "pinia";

const props = defineProps<{
  member: {
    memberId: string;
    username: string;
    displayName: string;
    avatarUrl?: string;
    role: "OWNER" | "ADMIN" | "MEMBER";
  };
  badge?: "ADMIN" | "OWNER";
}>();

const isDialogOpen = ref(false);
const roomStore = useRoomsStore();
const { currentRoom } = storeToRefs(roomStore);
</script>

<template>
  <UserInfoPopover :username="props.member.username" :room-id="currentRoom?.id" :member-id="props.member.memberId"
    :member-role="props.member.role">
    <div class="member-row flex items-center gap-2.5 px-2 py-1.5 mx-1 rounded cursor-pointer transition-colors"
      @click="isDialogOpen = true">
      <Avatar class="h-8 w-8 shrink-0">
        <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl" />
        <AvatarFallback class="text-xs"> </AvatarFallback>
      </Avatar>

      <div class="flex-1 min-w-0">
        <p class="text-[13px] font-medium truncate text-foreground">
          {{ member.displayName || member.username }}
        </p>
        <Badge v-if="badge" variant="outline" class="text-[10px] px-1.5 py-0 h-4 mt-0.5" :class="badge === 'OWNER'
          ? 'border-amber-300 bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-700'
          : 'border-blue-300 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-700'
          ">
          {{ badge === "OWNER" ? "Owner" : "Admin" }}
        </Badge>
      </div>
    </div>
  </UserInfoPopover>
</template>

<style scoped>
.member-row:hover {
  background: color-mix(in oklch, var(--sidebar-accent) 20%, transparent);
}
</style>
