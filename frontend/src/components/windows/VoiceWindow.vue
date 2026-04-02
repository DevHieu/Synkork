<script setup lang="ts">
import { computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import {
  MicOff,
  VolumeX,
  Mic,
  Volume2,
  Video,
  VideoOff,
  MonitorUp,
  PhoneOff,
  FileText,
} from "lucide-vue-next";
import { SidebarTrigger } from "@/components/ui/sidebar";

const route = useRoute();
const spaceId = route.params.spaceId as string;

const voiceSpaceStore = useVoiceSpaceStore();
const { participantList, videoOn, micOn, audioOn } =
  storeToRefs(voiceSpaceStore);
const { toggleVideo, toggleAudio, toggleMic } = voiceSpaceStore;
const { user } = storeToRefs(useUserStore());

onMounted(async () => {
  if (voiceSpaceStore.isInRoom && voiceSpaceStore.currentSpaceId === spaceId)
    return;
  await voiceSpaceStore.joinRoom(spaceId);
});

watch(
  user,
  async (newUser) => {
    if (newUser && !voiceSpaceStore.isInRoom) {
      await voiceSpaceStore.joinRoom(spaceId);
    }
  },
  { immediate: false },
);

const getInitials = (name: string) => {
  if (!name) return "?";
  return name
    .split(/[\s_-]/)
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
};

const gridCols = computed(() => {
  const n = participantList.value.length;
  if (n <= 1) return 1;
  if (n === 2) return 2;
  if (n === 3) return 3; // Cho 3 người dàn hàng ngang sẽ đẹp hơn trên desktop
  if (n === 4) return 2; // 2x2
  return 3; // 5-9 người thì 3 cột
});

const handleLeave = () => {
  voiceSpaceStore.leaveRoom();
};

const handleSummary = () => {
  // TODO: meeting summary
};

const handleShareScreen = () => {
  // TODO: share screen
};
</script>

<template>
  <div class="flex flex-col h-full bg-background text-foreground select-none">
    <!-- ── Top Bar ── -->
    <div
      class="flex items-center justify-between px-4 py-3 border-b border-border shrink-0"
    >
      <div class="flex items-center gap-2">
        <SidebarTrigger class="-ml-1" />
        <span class="font-semibold text-base"
          >🔊 {{ route.params.spaceName ?? "Voice" }}</span
        >
      </div>
      <button
        @click="handleSummary"
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors border border-border"
      >
        <FileText class="h-3.5 w-3.5" />
        Tóm tắt cuộc họp
      </button>
    </div>

    <!-- ── Video Grid ── -->
    <div
      class="flex-1 overflow-hidden p-3 min-h-0 flex items-center justify-center"
    >
      <div
        class="grid gap-3 w-full max-h-full mx-auto justify-center"
        :style="{
          gridTemplateColumns: `repeat(${gridCols}, minmax(0, 1fr))`,
          maxWidth: participantList.length <= 1 ? '800px' : '100%',
        }"
      >
        <div
          v-for="p in participantList"
          :key="p.userID"
          class="relative rounded-xl overflow-hidden bg-muted flex items-center justify-center ring-1 ring-border aspect-video w-full"
        >
          <!-- Local video -->
          <div
            v-if="p.isLocal"
            v-show="p.videoOn"
            id="local-video-container"
            class="absolute inset-0 [&>video]:w-full [&>video]:h-full [&>video]:object-cover"
          />

          <!-- Remote video -->
          <div
            v-else
            v-show="p.videoOn"
            :id="`remote-video-${p.userID}`"
            class="absolute inset-0 [&>video]:w-full [&>video]:h-full [&>video]:object-cover"
          />

          <div v-if="!p.videoOn" class="flex flex-col items-center gap-3">
            <img
              v-if="p.isLocal && user?.avatarUrl"
              :src="user.avatarUrl"
              alt="Avatar"
              class="w-16 h-16 rounded-full object-cover ring-2 ring-primary/30"
            />
            <div
              v-else
              class="w-16 h-16 rounded-full bg-primary flex items-center justify-center text-xl font-bold text-primary-foreground"
            >
              {{ getInitials(p.userName) }}
            </div>
            <span class="text-sm text-muted-foreground font-medium">
              {{ p.userName
              }}<span v-if="p.isLocal" class="text-muted-foreground/60">
                (Bạn)</span
              >
            </span>
          </div>

          <div
            v-if="p.videoOn"
            class="absolute bottom-2 left-2 bg-background/70 backdrop-blur-sm text-xs px-2 py-1 rounded-md text-foreground font-medium"
          >
            {{ p.userName
            }}<span v-if="p.isLocal" class="text-muted-foreground"> (Bạn)</span>
          </div>

          <div class="flex gap-1 absolute top-2 right-2">
            <div v-if="!p.micOn" class="bg-destructive/80 rounded-full p-1">
              <MicOff class="h-3 w-3 text-destructive-foreground" />
            </div>
            <div v-if="!p.audioOn" class="bg-destructive/80 rounded-full p-1">
              <VolumeX class="h-3 w-3 text-destructive-foreground" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Control Bar ── -->
    <div
      class="shrink-0 flex items-center justify-center gap-2 px-4 py-3 border-t border-border bg-card"
    >
      <button
        @click="toggleMic"
        :class="[
          'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
          micOn
            ? 'bg-muted hover:bg-accent text-foreground'
            : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
        ]"
      >
        <Mic v-if="micOn" class="h-5 w-5" />
        <MicOff v-else class="h-5 w-5" />
        {{ micOn ? "Mic" : "Mic off" }}
      </button>

      <button
        @click="toggleVideo"
        :class="[
          'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
          videoOn
            ? 'bg-muted hover:bg-accent text-foreground'
            : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
        ]"
      >
        <Video v-if="videoOn" class="h-5 w-5" />
        <VideoOff v-else class="h-5 w-5" />
        {{ videoOn ? "Cam" : "Cam off" }}
      </button>

      <button
        @click="toggleAudio"
        :class="[
          'flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all',
          audioOn
            ? 'bg-muted hover:bg-accent text-foreground'
            : 'bg-destructive/15 hover:bg-destructive/25 text-destructive',
        ]"
      >
        <Volume2 v-if="audioOn" class="h-5 w-5" />
        <VolumeX v-else class="h-5 w-5" />
        {{ audioOn ? "Âm thanh" : "Tắt tiếng" }}
      </button>

      <button
        @click="handleShareScreen"
        class="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all bg-muted hover:bg-accent text-foreground"
      >
        <MonitorUp class="h-5 w-5" />
        Chia sẻ
      </button>

      <div class="w-px h-10 bg-border mx-1" />

      <button
        @click="handleLeave"
        class="flex flex-col items-center gap-1 px-4 py-2.5 rounded-xl text-xs font-medium transition-all bg-destructive/15 hover:bg-destructive/30 text-destructive"
      >
        <PhoneOff class="h-5 w-5" />
        Rời phòng
      </button>
    </div>
  </div>
</template>
