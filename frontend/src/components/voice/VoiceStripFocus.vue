<script setup lang="ts">
import { MonitorUp } from "lucide-vue-next";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import Avatar from "../ui/avatar/Avatar.vue";
import AvatarFallback from "../ui/avatar/AvatarFallback.vue";

const props = defineProps<{
  otherPeople: VoiceItemType[];
}>();

const emit = defineEmits<{
  focus: [tileId: string];
  "register-ref": [tileId: string, el: HTMLElement];
}>();
</script>

<template>
  <div
    class="shrink-0 flex gap-2 overflow-x-auto no-scrollbar"
    style="height: 96px"
  >
    <div
      v-for="tile in otherPeople"
      :key="tile.id"
      :ref="(el) => el && emit('register-ref', tile.id, el as HTMLElement)"
      class="relative rounded-lg overflow-hidden bg-muted ring-1 ring-border cursor-pointer hover:ring-primary transition-all shrink-0 h-full"
      style="aspect-ratio: 16/9"
      @click="emit('focus', tile.id)"
    >
      <div
        v-if="tile.type === 'participant' && !tile.videoOn"
        class="w-full h-full flex items-center justify-center"
      >
        <Avatar class="h-8 w-8"
          ><AvatarFallback class="text-xs"> </AvatarFallback
        ></Avatar>
      </div>
      <div
        class="absolute bottom-1 left-1 right-1 z-10 text-[10px] px-1.5 py-0.5 rounded text-foreground truncate flex items-center gap-1"
      >
        <MonitorUp v-if="tile.type === 'screen'" class="h-2.5 w-2.5 shrink-0" />
        {{ tile.isLocal ? "Bạn" : tile.userName }}
      </div>
    </div>
  </div>
</template>

<style scoped></style>
