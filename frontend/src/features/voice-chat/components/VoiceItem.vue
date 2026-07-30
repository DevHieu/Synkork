<script setup lang="ts">
import type { User } from "@/types/User";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import { MicOff, VolumeX, MonitorUp, MoreHorizontal } from "lucide-vue-next";
import {
  DropdownMenu,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore.ts";
import { storeToRefs } from "pinia";
import VoiceDropdownMenu from "./VoiceDropdownMenu.vue";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import Avatar from "@/components/ui/avatar/Avatar.vue";
import AvatarImage from "@/components/ui/avatar/AvatarImage.vue";
import AvatarFallback from "@/components/ui/avatar/AvatarFallback.vue";

const props = defineProps<{
  item: VoiceItemType;
  user: User | null;
}>();

const emit = defineEmits<{
  focus: [tileId: string];
}>();

const { canManage } = storeToRefs(useRoomMemberStore());
const { mutedList } = storeToRefs(useVoiceSpaceStore());
</script>

<template>
  <div
    class="relative rounded-xl overflow-hidden bg-muted ring-1 ring-border cursor-pointer aspect-video hover:ring-primary/50 transition-all group/tile"
    @click="emit('focus', item.id)">
    <!-- Avatar khi tắt cam -->
    <div v-if="item.type === 'participant' && !item.videoOn"
      class="absolute inset-0 flex flex-col items-center justify-center gap-3">
      <Avatar class="h-8 w-8 shrink-0">
        <AvatarImage v-if="item.isLocal && user?.avatarUrl" :src="user?.avatarUrl" />
        <AvatarFallback class="text-xs"> </AvatarFallback>
      </Avatar>

      <span class="text-sm text-muted-foreground font-medium">
        {{ item.userName }}
        <span v-if="item.isLocal" class="opacity-60">(Bạn)</span>
      </span>
    </div>

    <!-- Hover overlay -->
    <div class="absolute inset-0 bg-black/0 group-hover/tile:bg-black/10 transition-colors" />

    <!-- Label bottom-left -->
    <div v-if="item.type === 'screen'"
      class="absolute bottom-2 left-2 z-10 bg-black/50 backdrop-blur-sm text-xs px-2 py-1 rounded-md text-white flex items-center gap-1">
      <MonitorUp class="h-3 w-3" />
      {{ item.isLocal ? "Bạn" : item.userName }}
    </div>
    <div v-else-if="item.videoOn"
      class="absolute bottom-2 left-2 z-10 bg-background/70 backdrop-blur-sm text-xs px-2 py-1 rounded-md font-medium">
      {{ item.userName }}
      <span v-if="item.isLocal" class="text-muted-foreground">(Bạn)</span>
    </div>

    <!-- Mic / Audio indicators -->
    <div class="flex gap-1 absolute top-2 right-2 z-10">
      <div v-if="!item.micOn" class="bg-destructive/80 rounded-full p-2"
        :class="!item.muted ? 'bg-ring/80' : 'bg-destructive/80'">
        <MicOff class="h-4 w-4 text-destructive-foreground" />
      </div>
      <div v-if="!item.audioOn" class="bg-destructive/80 rounded-full p-2"
        :class="!item.deafen ? 'bg-ring/80' : 'bg-destructive/80'">
        <VolumeX class="h-4 w-4 text-destructive-foreground" />
      </div>
      <div v-if="mutedList.has(item.audioId ?? '')" class="rounded-full p-2 bg-secondary/80">
        <MicOff class="h-4 w-4 text-destructive-foreground" />
      </div>
    </div>

    <!-- 3 chấm -->
    <div class="absolute bottom-2 right-2 z-10 opacity-0 group-hover/tile:opacity-100 transition-opacity" @click.stop>
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <button class="bg-black/50 hover:bg-black/70 backdrop-blur-sm rounded-md p-1 text-white transition-colors">
            <MoreHorizontal class="h-5 w-5" />
          </button>
        </DropdownMenuTrigger>

        <VoiceDropdownMenu :item="item" :is-admin="canManage" @focus="emit('focus', $event)" />
      </DropdownMenu>
    </div>
  </div>
</template>
