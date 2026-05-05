<script setup lang="ts">
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";
import {
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  MonitorUp,
  PhoneOff,
  Radio,
  Loader2,
} from "lucide-vue-next";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

defineProps<{ collapsed?: boolean }>();

const voiceSpaceStore = useVoiceSpaceStore();
const { micOn, audioOn, isInRoom, isJoining, screenOn } =
  storeToRefs(voiceSpaceStore);
const { toggleMic, toggleAudio, toggleShareScreen } = voiceSpaceStore;

const leaveRoom = async () => {
  await voiceSpaceStore.leaveRoom();
};
</script>

<template>
  <div v-if="isInRoom || isJoining" class="border-b">
    <!-- ══ JOINING: Loading state ══ -->
    <template v-if="isJoining && !isInRoom">
      <div v-if="collapsed" class="w-full flex justify-center py-3">
        <Loader2 class="h-5 w-5 animate-spin text-muted-foreground" />
      </div>

      <div v-else class="px-3 py-2">
        <div class="flex items-center gap-2 mb-2">
          <Loader2 class="h-3.5 w-3.5 animate-spin text-muted-foreground" />
          <span class="text-xs text-muted-foreground">Đang kết nối...</span>
        </div>
        <div class="flex items-center gap-1 opacity-40 pointer-events-none">
          <button
            class="flex-1 flex items-center justify-center py-1.5 rounded-md hover:bg-muted"
          >
            <Mic class="h-4 w-4" />
          </button>
          <button
            class="flex-1 flex items-center justify-center py-1.5 rounded-md hover:bg-muted"
          >
            <Volume2 class="h-4 w-4" />
          </button>
          <button
            class="flex-1 flex items-center justify-center py-1.5 rounded-md hover:bg-muted"
          >
            <MonitorUp class="h-4 w-4" />
          </button>
          <button
            class="flex-1 flex items-center justify-center py-1.5 rounded-md bg-destructive/15 text-destructive"
          >
            <PhoneOff class="h-4 w-4" />
          </button>
        </div>
      </div>
    </template>

    <!-- ══ IN ROOM: Full controls ══ -->
    <template v-else>
      <!-- Collapsed -->
      <Popover v-if="collapsed">
        <PopoverTrigger as-child>
          <button
            class="w-full flex justify-center py-3 hover:bg-muted transition-colors"
          >
            <span class="relative flex h-2 w-2 top-2 right-2">
              <span
                class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"
              />
              <span
                class="relative inline-flex rounded-full h-2 w-2 bg-green-500"
              />
            </span>
            <Radio class="h-5 w-5 text-green-500" />
          </button>
        </PopoverTrigger>
        <PopoverContent side="right" align="end" class="w-44 p-2">
          <p class="text-xs text-muted-foreground mb-2 px-1">
            Đang trong phòng voice
          </p>
          <div class="flex flex-col gap-1">
            <button
              @click="() => toggleMic()"
              :class="[
                'flex items-center gap-2 px-2 py-1.5 rounded-md text-xs transition-colors w-full',
                micOn
                  ? 'hover:bg-muted'
                  : 'text-destructive bg-destructive/10 hover:bg-destructive/20',
              ]"
            >
              <Mic v-if="micOn" class="h-4.5 w-4.5" />
              <MicOff v-else class="h-4.5 w-4.5" />
              {{ micOn ? "Tắt mic" : "Bật mic" }}
            </button>
            <button
              @click="() => toggleAudio()"
              :class="[
                'flex items-center gap-2 px-2 py-1.5 rounded-md text-xs transition-colors w-full',
                audioOn
                  ? 'hover:bg-muted'
                  : 'text-destructive bg-destructive/10 hover:bg-destructive/20',
              ]"
            >
              <Volume2 v-if="audioOn" class="h-4.5 w-4.5" />
              <VolumeX v-else class="h-4.5 w-4.5" />
              {{ audioOn ? "Tắt tiếng" : "Bật tiếng" }}
            </button>
            <button
              @click="() => toggleShareScreen()"
              :class="[
                'flex items-center gap-2 px-2 py-1.5 rounded-md text-xs transition-colors w-full',
                screenOn
                  ? 'bg-primary/15 hover:bg-primary/25 text-primary'
                  : 'bg-muted hover:bg-accent text-foreground',
              ]"
            >
              <MonitorUp class="h-4.5 w-4.5" />
              {{ screenOn ? "Dừng chia sẻ" : "Chia sẻ màn hình" }}
            </button>
            <button
              @click="leaveRoom"
              class="flex items-center gap-2 px-2 py-1.5 rounded-md text-xs text-destructive bg-destructive/10 hover:bg-destructive/20 transition-colors w-full"
            >
              <PhoneOff class="h-4.5 w-4.5" />
              Rời phòng
            </button>
          </div>
        </PopoverContent>
      </Popover>

      <!-- Expanded -->
      <div v-else class="px-3 py-2">
        <div class="flex items-center gap-2 mb-2">
          <span class="relative flex h-2 w-2">
            <span
              class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"
            />
            <span
              class="relative inline-flex rounded-full h-2 w-2 bg-green-500"
            />
          </span>
          <span class="text-xs text-green-500 font-medium">Đang kết nối</span>
        </div>
        <div class="flex items-center gap-1">
          <button
            @click="() => toggleMic()"
            :class="[
              'flex-1 flex items-center justify-center py-1.5 rounded-md transition-colors',
              micOn
                ? 'hover:bg-muted text-foreground'
                : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
            ]"
          >
            <Mic v-if="micOn" class="h-4 w-4" />
            <MicOff v-else class="h-4 w-4" />
          </button>
          <button
            @click="() => toggleAudio()"
            :class="[
              'flex-1 flex items-center justify-center py-1.5 rounded-md transition-colors',
              audioOn
                ? 'hover:bg-muted text-foreground'
                : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
            ]"
          >
            <Volume2 v-if="audioOn" class="h-4 w-4" />
            <VolumeX v-else class="h-4 w-4" />
          </button>
          <button
            @click="() => toggleShareScreen()"
            :class="[
              'flex-1 flex items-center justify-center py-1.5 rounded-md transition-colors',
              screenOn
                ? 'bg-primary/15 hover:bg-primary/25 text-primary'
                : 'bg-muted hover:bg-accent text-foreground',
            ]"
          >
            <MonitorUp class="h-4 w-4" />
          </button>
          <button
            @click="leaveRoom"
            class="flex-1 flex items-center justify-center py-1.5 rounded-md bg-destructive/15 hover:bg-destructive/25 text-destructive transition-colors"
          >
            <PhoneOff class="h-4 w-4" />
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
