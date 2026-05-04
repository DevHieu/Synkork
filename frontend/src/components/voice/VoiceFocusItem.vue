<script setup lang="ts">
import { MonitorUp } from "lucide-vue-next";
import type { User } from "@/types/User";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";

const props = defineProps<{
  focusedTile: VoiceItemType;
  user: User | null;
}>();

const emit = defineEmits<{
  minimize: [];
  "register-ref": [tileId: string, el: HTMLElement];
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
    :ref="(el) => el && emit('register-ref', focusedTile.id, el as HTMLElement)"
    class="flex-1 min-h-0 relative rounded-xl overflow-hidden bg-muted ring-1 ring-border"
  >
    <div
      v-if="focusedTile.type === 'participant' && !focusedTile.videoOn"
      class="absolute inset-0 flex flex-col items-center justify-center gap-3 z-[1]"
    >
      <img
        v-if="focusedTile.isLocal && user?.avatarUrl"
        :src="user.avatarUrl"
        class="w-20 h-20 rounded-full object-cover ring-2 ring-primary/30"
      />
      <div
        v-else
        class="w-20 h-20 rounded-full bg-primary flex items-center justify-center text-2xl font-bold text-primary-foreground"
      >
        {{ getInitials(focusedTile.userName) }}
      </div>
      <span class="text-sm text-foreground/70">
        {{ focusedTile.userName }}
        <span v-if="focusedTile.isLocal" class="opacity-60">(Bạn)</span>
      </span>
    </div>

    <div
      class="absolute bottom-2 left-2 z-10 bg-black/50 backdrop-blur-sm text-xs px-2 py-1 rounded-md text-white flex items-center gap-1"
    >
      <MonitorUp v-if="focusedTile.type === 'screen'" class="h-3 w-3" />
      {{ focusedTile.isLocal ? "Bạn" : focusedTile.userName }}
      <span v-if="focusedTile.type === 'screen'" class="opacity-70"
        >đang chia sẻ</span
      >
    </div>

    <button
      @click="emit('minimize')"
      class="absolute top-2 right-2 z-10 text-[10px] bg-black/50 hover:bg-black/70 text-white px-2 py-1 rounded-md transition-colors"
    >
      Thu nhỏ
    </button>
  </div>
</template>
