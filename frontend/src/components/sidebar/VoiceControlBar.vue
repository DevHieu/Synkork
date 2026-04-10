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

const shareScreen = () => {
  console.log("Sharinggg.....");
};
</script>

<template>
  <div v-if="isInRoom || isJoining" class="border-b">
    <!-- ══ JOINING: Loading state ══ -->
    <template v-if="isJoining && !isInRoom">
      <!-- Collapsed -->
      <div v-if="collapsed" class="w-full flex justify-center py-3">
        <Loader2 class="h-5 w-5 animate-spin text-muted-foreground" />
      </div>

      <!-- Expanded -->
      <div v-else class="px-3 py-2">
        <div class="flex items-center gap-2 mb-2">
          <Loader2 class="h-3.5 w-3.5 animate-spin text-muted-foreground" />
          <span class="text-xs text-muted-foreground">Đang kết nối...</span>
        </div>
        <!-- Disabled controls (skeleton) -->
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
            class="flex-1 flex items-center justify-center py-1.5 rounded-md bg-red-500/15 text-red-400"
          >
            <PhoneOff class="h-4 w-4" />
          </button>
        </div>
      </div>
    </template>

    <!-- ══ IN ROOM: Full controls ══ -->
    <template v-else>
      <!-- Collapsed: 1 nút + popover -->
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
              @click="toggleMic"
              :class="[
                'flex items-center gap-2 px-2 py-1.5 rounded-md text-xs transition-colors w-full',
                micOn
                  ? 'hover:bg-muted'
                  : 'text-red-400 bg-red-500/10 hover:bg-red-500/20',
              ]"
            >
              <Mic v-if="micOn" class="h-4.5 w-4.5" />
              <MicOff v-else class="h-4.5 w-4.5" />
              {{ micOn ? "Tắt mic" : "Bật mic" }}
            </button>
            <button
              @click="toggleAudio"
              :class="[
                'flex items-center gap-2 px-2 py-1.5 rounded-md text-xs transition-colors w-full',
                audioOn
                  ? 'hover:bg-muted'
                  : 'text-red-400 bg-red-500/10 hover:bg-red-500/20',
              ]"
            >
              <Volume2 v-if="audioOn" class="h-4.5 w-4.5" />
              <VolumeX v-else class="h-4.5 w-4.5" />
              {{ audioOn ? "Tắt tiếng" : "Bật tiếng" }}
            </button>
            <button
              @click="toggleShareScreen"
              class="flex items-center gap-2 px-2 py-1.5 rounded-md text-xs hover:bg-muted transition-colors w-full"
            >
              <MonitorUp class="h-4.5 w-4.5" />
              Chia sẻ màn hình
            </button>
            <button
              @click="leaveRoom"
              class="flex items-center gap-2 px-2 py-1.5 rounded-md text-xs text-red-400 bg-red-500/10 hover:bg-red-500/20 transition-colors w-full"
            >
              <PhoneOff class="h-4.5 w-4.5" />
              Rời phòng
            </button>
          </div>
        </PopoverContent>
      </Popover>

      <!-- Expanded: full bar -->
      <div v-else class="px-3 py-2">
        <div
          class="flex items-center justify-between mb-2 cursor-pointer hover:opacity-80 transition"
          @click="shareScreen"
        >
          <div class="flex items-center gap-2">
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
        </div>
        <div class="flex items-center gap-1">
          <button
            @click="toggleMic"
            :class="[
              'flex-1 flex items-center justify-center py-1.5 rounded-md transition-colors',
              micOn
                ? 'hover:bg-muted text-foreground'
                : 'bg-red-500/15 hover:bg-red-500/25 text-red-400',
            ]"
          >
            <Mic v-if="micOn" class="h-4 w-4" /><MicOff
              v-else
              class="h-4 w-4"
            />
          </button>
          <button
            @click="toggleAudio"
            :class="[
              'flex-1 flex items-center justify-center py-1.5 rounded-md transition-colors',
              audioOn
                ? 'hover:bg-muted text-foreground'
                : 'bg-red-500/15 hover:bg-red-500/25 text-red-400',
            ]"
          >
            <Volume2 v-if="audioOn" class="h-4 w-4" /><VolumeX
              v-else
              class="h-4 w-4"
            />
          </button>
          <button
            class="flex-1 flex items-center justify-center py-1.5 rounded-md hover:bg-muted text-foreground transition-colors"
          >
            <MonitorUp class="h-4 w-4" />
          </button>
          <button
            @click="leaveRoom"
            class="flex-1 flex items-center justify-center py-1.5 rounded-md bg-red-500/15 hover:bg-red-500/25 text-red-400 transition-colors"
          >
            <PhoneOff class="h-4 w-4" />
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
