<script setup lang="ts">
import { ref } from "vue";
import SidebarTrigger from "../ui/sidebar/SidebarTrigger.vue";
import { FileText, Volume2 } from "lucide-vue-next";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { useRoute } from "vue-router";
import axiosClient from "@/lib/axiosClient";



const isRecording = ref(false);
let recorder: MediaRecorder | null = null;

const activeRecorders: { userId: string; recorder: MediaRecorder; chunks: Blob[] }[] = [];

const route = useRoute();
const voiceSpaceStore = useVoiceSpaceStore();

const handleSummary = () => {
  // Đang record → dừng lại
  if (isRecording.value) {
    activeRecorders.forEach(item => item.recorder.stop());
    activeRecorders.length = 0
    isRecording.value = false;
    return;
  }

  const streams = voiceSpaceStore.getAudioStreamsByParticipant();
   streams.forEach(({ userId, userName, stream }) => {
    const chunks: Blob[] = [];
    const recorder = new MediaRecorder(stream, { mimeType: "audio/webm" });
    recorder.ondataavailable = (e) => {
      if (e.data.size > 0) chunks.push(e.data);
    };
    recorder.onstop = async () => {
      const blob = new Blob(chunks, { type: "audio/webm" });
      await uploadVoiceToBackend(blob, userId, userName);
    };


  recorder.start(5000);
  activeRecorders.push({
      userId,
      recorder,
      chunks
    });
  });
  isRecording.value = true;
};


const uploadVoiceToBackend = async (blob: Blob, userId: string, userName: string) => {
  const formData = new FormData();
  formData.append("file", blob, `${userId}.webm`);
  formData.append("userId", userId);
  formData.append("userName", userName);
  formData.append("roomId", route.params.roomId as string);

  try {
    await axiosClient.post("/api/collaboration/voice-summary/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
  } catch (error) {
    console.error(`Lỗi gửi voice của ${userName}:`, error);
  }
};
</script>

<template>
  <div
    class="flex items-center justify-between px-4 py-3.5 border-b border-border shrink-0"
  >
    <div class="flex items-center gap-2">
      <SidebarTrigger class="-ml-1" />
      <span class="font-semibold text-base flex gap-2 items-center"
        ><Volume2 class="h-5 w-5" />
        {{ route.params.spaceName ?? "Voice" }}</span
      >
    </div>
    <button
      @click="handleSummary"
      class="flex items-center gap-2 px-4 py-1 rounded-lg text-sm font-normal text-muted-foreground hover:text-foreground hover:bg-muted transition-colors border border-border h-8"
      :class="{ 'text-red-500 border-red-500': isRecording }"
    >
      <FileText class="h-5 w-5" />
      {{ isRecording ? "Đang ghi... (bấm để dừng)" : "Tóm tắt cuộc họp" }}
    </button>
  </div>
</template>

<style scoped></style>
