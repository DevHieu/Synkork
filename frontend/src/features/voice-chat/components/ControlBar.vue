<script setup lang="ts">
import {
  Mic,
  MicOff,
  MonitorUp,
  PhoneOff,
  Video,
  VideoOff,
  Volume2,
  VolumeX,
} from "lucide-vue-next";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";

const voiceSpaceStore = useVoiceSpaceStore();
const { videoOn, micOn, audioOn, screenOn } = storeToRefs(voiceSpaceStore);
const { toggleVideo, toggleAudio, toggleMic, toggleShareScreen } =
  voiceSpaceStore;
</script>

<template>
  <div class="shrink-0 flex items-center justify-center gap-2 px-4 py-3 border-t border-border bg-card">
    <button @click="() => toggleMic()" :class="[
      'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
      micOn
        ? 'bg-muted hover:bg-accent text-foreground'
        : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
    ]">
      <Mic v-if="micOn" class="h-5 w-5" />
      <MicOff v-else class="h-5 w-5" />
      {{ micOn ? "Mic" : "Mic off" }}
    </button>
    <button @click="() => toggleVideo()" :class="[
      'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
      videoOn
        ? 'bg-muted hover:bg-accent text-foreground'
        : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
    ]">
      <Video v-if="videoOn" class="h-5 w-5" />
      <VideoOff v-else class="h-5 w-5" />
      {{ videoOn ? "Cam" : "Cam off" }}
    </button>
    <button @click="() => toggleAudio()" :class="[
      'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
      audioOn
        ? 'bg-muted hover:bg-accent text-foreground'
        : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
    ]">
      <Volume2 v-if="audioOn" class="h-5 w-5" />
      <VolumeX v-else class="h-5 w-5" />
      {{ audioOn ? "Âm thanh" : "Tắt tiếng" }}
    </button>
    <button @click="() => toggleShareScreen()" :class="[
      'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
      screenOn
        ? 'bg-primary/15 hover:bg-primary/25 text-primary'
        : 'bg-muted hover:bg-accent text-foreground',
    ]">
      <MonitorUp class="h-5 w-5" />
      {{ screenOn ? "Dừng chia sẻ" : "Chia sẻ" }}
    </button>
    <div class="w-px h-10 bg-border mx-1" />
    <button @click="voiceSpaceStore.leaveRoom()"
      class="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all bg-destructive/15 hover:bg-destructive/30 text-destructive">
      <PhoneOff class="h-5 w-5" />
      Rời phòng
    </button>
  </div>
</template>

<style scoped></style>
