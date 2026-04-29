<script setup lang="ts">
import type { User } from "@/types/User";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import { MicOff, VolumeX, MonitorUp } from "lucide-vue-next";

const props = defineProps<{
  item: VoiceItemType;
  user: User | null;
}>();

const emit = defineEmits<{
  focus: [tileId: string];
}>();

const getInitials = (name: string) =>
  name
    ? name
        .split(/[\s_-]/)
        .map((w) => w[0])
        .join("")
        .toUpperCase()
        .slice(0, 2)
    : "?";
</script>

<template>
  <div
    class="relative rounded-xl overflow-hidden bg-muted ring-1 ring-border cursor-pointer aspect-video hover:ring-primary/50 transition-all group/tile"
    @click="emit('focus', item.id)"
  >
    <!-- Avatar khi tắt cam -->
    <div
      v-if="item.type === 'participant' && !item.videoOn"
      class="absolute inset-0 flex flex-col items-center justify-center gap-3"
    >
      <img
        v-if="item.isLocal && user?.avatarUrl"
        :src="user.avatarUrl"
        class="w-16 h-16 rounded-full object-cover ring-2 ring-primary/30"
      />
      <div
        v-else
        class="w-16 h-16 rounded-full bg-primary flex items-center justify-center text-xl font-bold text-primary-foreground"
      >
        {{ getInitials(item.userName) }}
      </div>
      <span class="text-sm text-muted-foreground font-medium">
        {{ item.userName }}
        <span v-if="item.isLocal" class="opacity-60">(Bạn)</span>
      </span>
    </div>

    <!-- Hover overlay -->
    <div
      class="absolute inset-0 bg-black/0 group-hover/tile:bg-black/10 transition-colors"
    />

    <!-- Label bottom-left -->
    <div
      v-if="item.type === 'screen'"
      class="absolute bottom-2 left-2 z-10 bg-black/50 backdrop-blur-sm text-xs px-2 py-1 rounded-md text-white flex items-center gap-1"
    >
      <MonitorUp class="h-3 w-3" />
      {{ item.isLocal ? "Bạn" : item.userName }}
    </div>
    <div
      v-else-if="item.videoOn"
      class="absolute bottom-2 left-2 z-10 bg-background/70 backdrop-blur-sm text-xs px-2 py-1 rounded-md font-medium"
    >
      {{ item.userName }}
      <span v-if="item.isLocal" class="text-muted-foreground">(Bạn)</span>
    </div>

    <!-- Mic / Audio indicators -->
    <div class="flex gap-1 absolute top-2 right-2 z-10">
      <div v-if="!item.micOn" class="bg-destructive/80 rounded-full p-1">
        <MicOff class="h-3 w-3 text-destructive-foreground" />
      </div>
      <div v-if="!item.audioOn" class="bg-destructive/80 rounded-full p-1">
        <VolumeX class="h-3 w-3 text-destructive-foreground" />
      </div>
    </div>
  </div>
</template>
