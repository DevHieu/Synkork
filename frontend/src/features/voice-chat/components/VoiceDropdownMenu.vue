<script setup lang="ts">
import type { VoiceItemType } from "@/features/voice-chat/types/VoiceTypes";
import {
  MicOff,
  VolumeX,
  MonitorUp,
  Mic,
  Volume2,
  VideoOff,
  PhoneOff,
  ShieldOff,
  MonitorOff,
} from "lucide-vue-next";
import {
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";

const props = defineProps<{
  item: VoiceItemType;
  isAdmin?: boolean;
}>();

const emit = defineEmits<{
  focus: [tileId: string];
}>();

const { toggleAudio, toggleMic, toggleShareScreen } =
  useVoiceSpaceStore();
const { mutedList } = storeToRefs(useVoiceSpaceStore());

const handleMuteUser = (userId: string, type: "ROOM_MUTE" | "ROOM_DEAFEN") => {
  const currentMuted = type === "ROOM_MUTE" ? props.item.muted : null;
  const currentDeafen = type === "ROOM_DEAFEN" ? props.item.deafen : null;
  useVoiceSpaceStore().toggleMuteUser(userId, type, {
    muted: currentMuted !== null ? !currentMuted : null,
    deafen: currentDeafen !== null ? !currentDeafen : null,
  });
};

const handleKickUser = (userId: string) =>
  useVoiceSpaceStore().kickMember(userId);

const handleMutePerson = (audioId: string | undefined) => {
  if (!audioId) return;
  useVoiceSpaceStore().toggleAudioUser(audioId);
};
</script>

<template>
  <DropdownMenuContent class="w-44" align="end">
    <!-- Header -->
    <div class="px-2 py-1.5 text-xs text-muted-foreground font-medium">
      {{ item.userName }}<span v-if="item.isLocal"> (Bạn)</span>
    </div>
    <DropdownMenuSeparator />

    <!-- Local + Voice -->
    <template v-if="item.isLocal && item.type === 'participant'">
      <DropdownMenuItem class="gap-2" @click="toggleMic()">
        <Mic v-if="!item.micOn" class="h-4 w-4" />
        <MicOff v-else class="h-4 w-4" />
        {{ item.micOn ? "Tắt mic" : "Bật mic" }}
      </DropdownMenuItem>
      <DropdownMenuItem class="gap-2" @click="toggleAudio()">
        <Volume2 v-if="!item.audioOn" class="h-4 w-4" />
        <VolumeX v-else class="h-4 w-4" />
        {{ item.audioOn ? "Tắt âm thanh" : "Bật âm thanh" }}
      </DropdownMenuItem>
      <template v-if="isAdmin">
        <DropdownMenuSeparator />
        <DropdownMenuItem class="gap-2" :class="item.muted
          ? 'text-amber-500 focus:text-amber-500'
          : 'text-destructive focus:text-destructive'
          " @click="handleMuteUser(item.userID, 'ROOM_MUTE')">
          <ShieldOff v-if="item.muted" class="h-4 w-4" />
          <MicOff v-else class="h-4 w-4" />
          {{ item.muted ? "Gỡ tắt mic" : "Tắt mic người này" }}
        </DropdownMenuItem>
        <DropdownMenuItem class="gap-2" :class="item.deafen
          ? 'text-amber-500 focus:text-amber-500'
          : 'text-destructive focus:text-destructive'
          " @click="handleMuteUser(item.userID, 'ROOM_DEAFEN')">
          <ShieldOff v-if="item.deafen" class="h-4 w-4" />
          <VolumeX v-else class="h-4 w-4" />
          {{ item.deafen ? "Gỡ tắt âm thanh" : "Tắt âm thanh người này" }}
        </DropdownMenuItem>
        <DropdownMenuItem class="gap-2 text-destructive focus:text-destructive" @click="handleKickUser(item.userID)">
          <PhoneOff class="h-4 w-4" />
          Kick khỏi phòng
        </DropdownMenuItem>
      </template>
    </template>

    <!-- Local + Screen -->
    <template v-else-if="item.isLocal && item.type === 'screen'">
      <DropdownMenuItem class="gap-2 text-destructive focus:text-destructive" @click="toggleShareScreen()">
        <MonitorOff class="h-4 w-4" />
        Dừng chia sẻ màn hình
      </DropdownMenuItem>
    </template>

    <!-- Remote + Voice -->
    <template v-else-if="!item.isLocal && item.type === 'participant'">
      <DropdownMenuItem class="gap-2" @click="handleMutePerson(item.audioId)">
        <Volume2 v-if="mutedList.has(item.audioId ?? '')" class="h-4 w-4" />
        <VolumeX v-else class="h-4 w-4" />
        {{
          mutedList.has(item.audioId ?? "") ? "Bật âm thanh" : "Tắt âm thanh"
        }}
      </DropdownMenuItem>
      <template v-if="isAdmin">
        <DropdownMenuSeparator />
        <DropdownMenuItem class="gap-2" :class="item.muted
          ? 'text-amber-500 focus:text-amber-500'
          : 'text-destructive focus:text-destructive'
          " @click="handleMuteUser(item.userID, 'ROOM_MUTE')">
          <ShieldOff v-if="item.muted" class="h-4 w-4" />
          <MicOff v-else class="h-4 w-4" />
          {{ item.muted ? "Gỡ tắt mic" : "Tắt mic người này" }}
        </DropdownMenuItem>
        <DropdownMenuItem class="gap-2" :class="item.deafen
          ? 'text-amber-500 focus:text-amber-500'
          : 'text-destructive focus:text-destructive'
          " @click="handleMuteUser(item.userID, 'ROOM_DEAFEN')">
          <ShieldOff v-if="item.deafen" class="h-4 w-4" />
          <VolumeX v-else class="h-4 w-4" />
          {{ item.deafen ? "Gỡ tắt âm thanh" : "Tắt âm thanh người này" }}
        </DropdownMenuItem>
        <DropdownMenuItem v-if="item.videoOn" class="gap-2 text-destructive focus:text-destructive"
          @click="useVoiceSpaceStore().stopUserVideo(item.userID)">
          <VideoOff class="h-4 w-4" />
          Tắt camera người này
        </DropdownMenuItem>
        <DropdownMenuItem class="gap-2 text-destructive focus:text-destructive" @click="handleKickUser(item.userID)">
          <PhoneOff class="h-4 w-4" />
          Kick khỏi phòng
        </DropdownMenuItem>
      </template>
    </template>

    <!-- Remote + Screen -->
    <template v-else-if="!item.isLocal && item.type === 'screen'">
      <DropdownMenuItem class="gap-2" @click="emit('focus', item.id)">
        <MonitorUp class="h-4 w-4" />
        Xem màn hình
      </DropdownMenuItem>
      <template v-if="isAdmin">
        <DropdownMenuSeparator />
        <DropdownMenuItem class="gap-2 text-destructive focus:text-destructive"
          @click="useVoiceSpaceStore().stopUserScreen(item.userID)">
          <MonitorOff class="h-4 w-4" />
          Dừng chia sẻ màn hình
        </DropdownMenuItem>
        <DropdownMenuItem class="gap-2 text-destructive focus:text-destructive" @click="handleKickUser(item.userID)">
          <PhoneOff class="h-4 w-4" />
          Kick khỏi phòng
        </DropdownMenuItem>
      </template>
    </template>
  </DropdownMenuContent>
</template>
