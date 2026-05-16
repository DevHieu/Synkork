<script setup lang="ts">
import SidebarTrigger from "../ui/sidebar/SidebarTrigger.vue";
import { FileText, Volume2 } from "lucide-vue-next";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { useRoute } from "vue-router";
import { storeToRefs } from "pinia";

const route = useRoute();
const voiceSpaceStore = useVoiceSpaceStore();
const { isRecording } = storeToRefs(voiceSpaceStore);

const handleSummary = () => {
  if (isRecording.value) {
    voiceSpaceStore.stopRecording();
  } else {
    voiceSpaceStore.startRecording();
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
