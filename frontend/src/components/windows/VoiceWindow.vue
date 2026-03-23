<script setup lang="ts">
import { computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { zegoFunctions } from "@/lib/zegoFunctions";

const route = useRoute();
const spaceId = route.params.spaceId as string;

const {
  user,
  participantList,
  videoOn,
  micOn,
  audioOn,
  setup,
  toggleVideo,
  toggleMic,
  toggleAudio,
} = zegoFunctions(spaceId);

onMounted(setup);

const getInitials = (name: string) =>
  name
    .split(/[\s_-]/)
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);

const gridCols = computed(() => {
  const n = participantList.value.length;
  if (n <= 1) return 1;
  if (n <= 4) return 2;
  if (n <= 9) return 3;
  return 4;
});
</script>

<template>
  <div class="flex flex-col h-full bg-[#1a1b1e] text-white select-none">
    <!-- ── Video Grid ──────────────────────────────────────────── -->
    <div class="flex-1 flex items-center justify-center p-4 overflow-hidden">
      <div
        class="grid gap-2 w-full h-full"
        :style="{ gridTemplateColumns: `repeat(${gridCols}, minmax(0, 1fr))` }"
      >
        <div
          v-for="p in participantList"
          :key="p.userID"
          class="relative rounded-xl overflow-hidden bg-[#2b2d31] aspect-video flex items-center justify-center"
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

          <!-- Avatar (cam off) -->
          <div v-if="!p.videoOn" class="flex flex-col items-center gap-3">
            <!-- Có avatar -->
            <img
              v-if="p.isLocal && user?.avatarUrl"
              :src="user.avatarUrl"
              alt="Avatar"
              class="w-16 h-16 rounded-full object-cover shadow-lg"
            />
            <!-- Không có avatar → initials -->
            <div
              v-else
              class="w-16 h-16 rounded-full bg-primary flex items-center justify-center text-xl font-bold shadow-lg"
            >
              {{ getInitials(p.userName) }}
            </div>

            <span class="text-sm text-zinc-300 font-medium tracking-wide">
              {{ p.userName }}
            </span>
          </div>

          <!-- Name tag (cam on) -->
          <div
            v-if="p.videoOn"
            class="absolute bottom-2 left-2 bg-black/50 backdrop-blur-sm text-xs px-2 py-1 rounded-md text-zinc-200 font-medium"
          >
            {{ p.userName }}
            <span v-if="p.isLocal" class="text-zinc-400"> (Bạn)</span>
          </div>

          <!-- Mic muted badge -->
          <div
            v-if="p.isLocal && !micOn"
            class="absolute top-2 right-2 bg-red-500/80 rounded-full p-1"
          >
            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
              <path
                d="M19 11a7 7 0 0 1-14 0H3a9 9 0 0 0 8 8.94V22h2v-2.06A9 9 0 0 0 21 11h-2zm-7 6a5 5 0 0 1-5-5V6a5 5 0 0 1 10 0v6a5 5 0 0 1-5 5z"
              />
              <line
                x1="2"
                y1="2"
                x2="22"
                y2="22"
                stroke="white"
                stroke-width="2"
              />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Control Bar ─────────────────────────────────────────── -->
    <div
      class="flex items-center justify-center gap-3 py-4 border-t border-white/5 bg-[#111214]"
    >
      <!-- Mic -->
      <button
        @click="toggleMic"
        :class="[
          'flex flex-col items-center gap-1 px-5 py-2.5 rounded-xl text-xs font-medium transition-all duration-150',
          micOn
            ? 'bg-[#2b2d31] hover:bg-[#35373c] text-white'
            : 'bg-red-500/20 hover:bg-red-500/30 text-red-400',
        ]"
      >
        <svg
          class="w-5 h-5"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          viewBox="0 0 24 24"
        >
          <template v-if="micOn">
            <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
            <path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v4M8 23h8" />
          </template>
          <template v-else>
            <line x1="1" y1="1" x2="23" y2="23" />
            <path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V4a3 3 0 0 0-5.94-.6" />
            <path
              d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23M12 19v4M8 23h8"
            />
          </template>
        </svg>
        {{ micOn ? "Mic" : "Mic off" }}
      </button>

      <!-- Cam -->
      <button
        @click="toggleVideo"
        :class="[
          'flex flex-col items-center gap-1 px-5 py-2.5 rounded-xl text-xs font-medium transition-all duration-150',
          videoOn
            ? 'bg-[#2b2d31] hover:bg-[#35373c] text-white'
            : 'bg-red-500/20 hover:bg-red-500/30 text-red-400',
        ]"
      >
        <svg
          class="w-5 h-5"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          viewBox="0 0 24 24"
        >
          <template v-if="videoOn">
            <polygon points="23 7 16 12 23 17 23 7" />
            <rect x="1" y="5" width="15" height="14" rx="2" ry="2" />
          </template>
          <template v-else>
            <path
              d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"
            />
            <line x1="1" y1="1" x2="23" y2="23" />
          </template>
        </svg>
        {{ videoOn ? "Cam" : "Cam off" }}
      </button>

      <!-- Audio -->
      <button
        @click="toggleAudio"
        :class="[
          'flex flex-col items-center gap-1 px-5 py-2.5 rounded-xl text-xs font-medium transition-all duration-150',
          audioOn
            ? 'bg-[#2b2d31] hover:bg-[#35373c] text-white'
            : 'bg-red-500/20 hover:bg-red-500/30 text-red-400',
        ]"
      >
        <svg
          class="w-5 h-5"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          viewBox="0 0 24 24"
        >
          <template v-if="audioOn">
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
            <path
              d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"
            />
          </template>
          <template v-else>
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5" />
            <line x1="23" y1="9" x2="17" y2="15" />
            <line x1="17" y1="9" x2="23" y2="15" />
          </template>
        </svg>
        {{ audioOn ? "Âm thanh" : "Tắt tiếng" }}
      </button>
    </div>

    <!-- Hidden audio output container -->
    <div id="audio-players" class="hidden" />
  </div>
</template>
