<script setup lang="ts">
import { ref } from "vue";
import SidebarTrigger from "../ui/sidebar/SidebarTrigger.vue";
import { FileText, Volume2 } from "lucide-vue-next";
import { useUserStore } from "@/stores/userStore";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { useRoute } from "vue-router";
import PremiumFeatureDialog from "../dialog/PremiumFeatureDialog.vue";
import VoiceSummaryModal from "./VoiceSummaryModal.vue";
import axiosClient from "@/lib/axiosClient";
import { toast } from "vue-sonner";

const isRecording = ref(false);
let recorder: MediaRecorder | null = null;
let chunks: Blob[] = [];

const route = useRoute();
const voiceSpaceStore = useVoiceSpaceStore();
const userStore = useUserStore();

const showPremiumDialog = ref(false);
const showSummaryModal = ref(false);
const isSummaryLoading = ref(false);
const meetingTranscript = ref("");
const meetingSummaryJson = ref("{}");




const handleSummary = () => {
  // Đang record → dừng lại
  if (isRecording.value) {
    recorder?.stop();
    isRecording.value = false;
    return;
  }

  if (userStore.userPlan === "FREE") {
    showPremiumDialog.value = true;
    return;
  }

  const tracks = voiceSpaceStore.getAudioTracks();
  if (tracks.length === 0) {
    alert("Chưa có audio nào trong phòng!");
    return;
  }

  // Truyền tracks vào đây chứ không để rỗng
  const audioStream = new MediaStream(tracks);
  recorder = new MediaRecorder(audioStream, { mimeType: "audio/mp4" });
  chunks = [];

  recorder.ondataavailable = (e) => {
    if (e.data.size > 0) chunks.push(e.data);
  };

  // Gửi file ghi âm lên backend và hiển thị modal tóm tắt cuộc họp
  recorder.onstop = async () => {
    const blob = new Blob(chunks, { type: "audio/webm" });
    const file = new File([blob], "meeting.webm", { type: "audio/webm" });

    showSummaryModal.value = true;
    isSummaryLoading.value = true;
    meetingTranscript.value = "";
    meetingSummaryJson.value = "{}";

    const formData = new FormData();
    formData.append("file", file);
    formData.append("roomId", route.params.roomId as string);

    try {
      const response = await axiosClient.post("/api/collaboration/voice-summary/upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      // Nhận dữ liệu phản hồi từ backend
      const data = response.data;
      meetingTranscript.value = data.transcript;
      meetingSummaryJson.value = data.summaryJson;
    } catch (error) {
      console.error("Lỗi khi xử lý tóm tắt cuộc họp:", error);
      toast.error("Gửi file ghi âm cuộc họp thất bại hoặc bị lỗi AI.");
      showSummaryModal.value = false;
    } finally {
      isSummaryLoading.value = false;
    }
  };

  recorder.start(5000);
  isRecording.value = true;
};
</script>

<template>
  <div class="flex items-center justify-between px-4 py-3.5 border-b border-border shrink-0">
    <div class="flex items-center gap-2">
      <SidebarTrigger class="-ml-1" />
      <span class="font-semibold text-base flex gap-2 items-center">
        <Volume2 class="h-5 w-5" />
        {{ route.params.spaceName ?? "Voice" }}
      </span>
    </div>
    <div class="flex items-center gap-2">

      <button @click="handleSummary"
        class="flex items-center gap-2 px-4 py-1 rounded-lg text-sm font-normal text-muted-foreground hover:text-foreground hover:bg-muted transition-colors border border-border h-8"
        :class="{ 'text-red-500 border-red-500': isRecording }">
        <FileText class="h-5 w-5" />
        {{ isRecording ? "Đang ghi... (bấm để dừng)" : "Tóm tắt cuộc họp" }}
      </button>
    </div>
  </div>
  <PremiumFeatureDialog v-model:open="showPremiumDialog" feature-name="Tóm tắt cuộc họp" :business-only="true" />
  <VoiceSummaryModal 
    v-model:open="showSummaryModal" 
    :is-loading="isSummaryLoading" 
    :transcript="meetingTranscript" 
    :summary-json="meetingSummaryJson" 
  />
</template>

<style scoped></style>

