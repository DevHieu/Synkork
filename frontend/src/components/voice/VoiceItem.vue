<script setup lang="ts">
import type { User } from "@/types/User";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import {
  MicOff,
  VolumeX,
  MonitorUp,
  MoreHorizontal,
  Mic,
  Volume2,
  VideoOff,
  PhoneOff,
  ShieldOff,
} from "lucide-vue-next";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";

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

const { toggleVideo, toggleAudio, toggleMic } = useVoiceSpaceStore();
const { mutedList } = storeToRefs(useVoiceSpaceStore());

const handleMuteUser = (userId: string, type: "ROOM_MUTE" | "ROOM_DEAFEN") => {
  // Lấy trạng thái hiện tại rồi đảo ngược
  const currentMuted = type === "ROOM_MUTE" ? props.item.muted : null;
  const currentDeafen = type === "ROOM_DEAFEN" ? props.item.deafen : null;

  const payload = {
    muted: currentMuted !== null ? !currentMuted : null,
    deafen: currentDeafen !== null ? !currentDeafen : null,
  };

  useVoiceSpaceStore().toggleMuteUser(userId, type, payload);
};

const handleKickUser = (userId: string) => {
  useVoiceSpaceStore().kickMember(userId);
};

const handleMutePerson = (audioId: string | undefined) => {
  if (!audioId) return;

  useVoiceSpaceStore().toggleAudioUser(audioId);
};
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
      <div
        v-if="!item.micOn"
        class="bg-destructive/80 rounded-full p-2"
        :class="!item.muted ? 'bg-ring/80' : 'bg-destructive/80'"
      >
        <MicOff class="h-4 w-4 text-destructive-foreground" />
      </div>
      <div
        v-if="!item.audioOn"
        class="bg-destructive/80 rounded-full p-2"
        :class="!item.deafen ? 'bg-ring/80' : 'bg-destructive/80'"
      >
        <VolumeX class="h-4 w-4 text-destructive-foreground" />
      </div>
      <div
        v-if="mutedList.has(item.audioId ?? '')"
        class="rounded-full p-2 bg-secondary/80"
      >
        <MicOff class="h-4 w-4 text-destructive-foreground" />
      </div>
    </div>

    <!-- 3 chấm -->
    <div
      class="absolute bottom-2 right-2 z-10 opacity-0 group-hover/tile:opacity-100 transition-opacity"
      @click.stop
    >
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <button
            class="bg-black/50 hover:bg-black/70 backdrop-blur-sm rounded-md p-1 text-white transition-colors"
          >
            <MoreHorizontal class="h-5 w-5" />
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent class="w-44" align="end">
          <div class="px-2 py-1.5 text-xs text-muted-foreground font-medium">
            {{ item.userName }}<span v-if="item.isLocal"> (Bạn)</span>
          </div>
          <DropdownMenuSeparator />
          <template v-if="item.isLocal">
            <DropdownMenuItem class="gap-2" @click="() => toggleMic()">
              <Mic v-if="!item.micOn" class="h-4 w-4" />
              <MicOff v-else class="h-4 w-4" />
              {{ item.micOn ? "Tắt mic" : "Bật mic" }}
            </DropdownMenuItem>
            <DropdownMenuItem class="gap-2" @click="() => toggleAudio()">
              <Volume2 v-if="!item.audioOn" class="h-4 w-4" />
              <VolumeX v-else class="h-4 w-4" />
              {{ item.audioOn ? "Tắt âm thanh" : "Bật âm thanh" }}
            </DropdownMenuItem>
          </template>
          <template v-else>
            <DropdownMenuItem
              class="gap-2"
              @click="() => handleMutePerson(item.audioId)"
            >
              <Volume2
                v-if="mutedList.has(item.audioId ?? '')"
                class="h-4 w-4"
              />
              <VolumeX v-else class="h-4 w-4" />
              {{
                mutedList.has(item.audioId ?? "")
                  ? "Bật âm thanh"
                  : "Tắt âm thanh"
              }}
            </DropdownMenuItem>
            <DropdownMenuItem
              v-if="item.videoOn"
              class="gap-2"
              @click="() => toggleVideo()"
            >
              <VideoOff class="h-4 w-4" />
              Tắt camera
            </DropdownMenuItem>
          </template>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            class="gap-2"
            :class="
              item.muted
                ? 'text-amber-500 focus:text-amber-500'
                : 'text-destructive focus:text-destructive'
            "
            @click="handleMuteUser(item.userID, 'ROOM_MUTE')"
          >
            <ShieldOff v-if="item.muted" class="h-4 w-4" />
            <MicOff v-else class="h-4 w-4" />
            {{ item.muted ? "Gỡ tắt mic" : "Tắt mic người này" }}
          </DropdownMenuItem>
          <DropdownMenuItem
            class="gap-2"
            :class="
              item.deafen
                ? 'text-amber-500 focus:text-amber-500'
                : 'text-destructive focus:text-destructive'
            "
            @click="handleMuteUser(item.userID, 'ROOM_DEAFEN')"
          >
            <ShieldOff v-if="item.deafen" class="h-4 w-4" />
            <VolumeX v-else class="h-4 w-4" />
            {{ item.deafen ? "Gỡ tắt âm thanh" : "Tắt âm thanh người này" }}
          </DropdownMenuItem>
          <DropdownMenuItem
            class="gap-2 text-destructive focus:text-destructive"
            @click="() => handleKickUser(item.userID)"
          >
            <PhoneOff class="h-4 w-4" />
            Kick khỏi phòng
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  </div>
</template>
