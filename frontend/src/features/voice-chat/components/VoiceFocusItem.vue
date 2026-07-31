<script setup lang="ts">
import { MonitorUp, MoreHorizontal } from "lucide-vue-next";
import type { User } from "@/types/User";
import type { VoiceItemType } from "@/features/voice-chat/types/VoiceTypes.ts";
import DropdownMenu from "@/components/ui/dropdown-menu/DropdownMenu.vue";
import DropdownMenuTrigger from "@/components/ui/dropdown-menu/DropdownMenuTrigger.vue";
import VoiceDropdownMenu from "./VoiceDropdownMenu.vue";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { storeToRefs } from "pinia";
import Avatar from "@/components/ui/avatar/Avatar.vue";
import AvatarImage from "@/components/ui/avatar/AvatarImage.vue";
import AvatarFallback from "@/components/ui/avatar/AvatarFallback.vue";

const props = defineProps<{
  focusedTile: VoiceItemType;
  user: User | null;
}>();

const emit = defineEmits<{
  minimize: [];
  "register-ref": [tileId: string, el: HTMLElement];
}>();

const { canManage } = storeToRefs(useRoomMemberStore());
</script>

<template>
  <div :ref="(el) => el && emit('register-ref', focusedTile.id, el as HTMLElement)"
    class="flex-1 min-h-0 relative rounded-xl overflow-hidden bg-muted ring-1 ring-border">
    <div v-if="focusedTile.type === 'participant' && !focusedTile.videoOn"
      class="absolute inset-0 flex flex-col items-center justify-center gap-3 z-1">
      <Avatar class="h-16 w-16 shrink-0">
        <AvatarImage v-if="focusedTile.isLocal && user?.avatarUrl" :src="user.avatarUrl" />
        <AvatarFallback class="text-xs"> </AvatarFallback>
      </Avatar>

      <span class="text-sm text-foreground/70">
        {{ focusedTile.userName }}
        <span v-if="focusedTile.isLocal" class="opacity-60">(Bạn)</span>
      </span>
    </div>

    <div
      class="absolute bottom-2 left-2 z-10 bg-black/50 backdrop-blur-sm text-xs px-2 py-1 rounded-md ttext-foreground flex items-center gap-1">
      <MonitorUp v-if="focusedTile.type === 'screen'" class="h-3 w-3" />
      {{ focusedTile.isLocal ? "Bạn" : focusedTile.userName }}
      <span v-if="focusedTile.type === 'screen'" class="opacity-70">đang chia sẻ</span>
    </div>

    <div class="absolute top-2 right-2 z-10 flex items-center gap-1">
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <button class="backdrop-blur-sm rounded-md p-1 text-foreground transition-colors">
            <MoreHorizontal class="h-5 w-5" />
          </button>
        </DropdownMenuTrigger>
        <VoiceDropdownMenu :item="focusedTile" :is-admin="canManage" @focus="emit('minimize')" />
      </DropdownMenu>

      <button @click="emit('minimize')"
        class="text-[10px] bg-muted/50 hover:bg-muted/70 text-white px-2 py-1 rounded-md transition-colors">
        Thu nhỏ
      </button>
    </div>
  </div>
</template>
