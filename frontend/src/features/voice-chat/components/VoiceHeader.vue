<script setup lang="ts">
import { ref, computed } from "vue";
import SidebarTrigger from "@/components/ui/sidebar/SidebarTrigger.vue";
import { FileText, Volume2, UploadCloud } from "lucide-vue-next";
import { useUserStore } from "@/stores/userStore";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore.ts";
import { useRoute } from "vue-router";
import PremiumFeatureDialog from "@/components/dialog/PremiumFeatureDialog.vue";
import VoiceSummaryModal from "./VoiceSummaryModal.vue";
import axiosClient from "@/lib/axiosClient";
import { toast } from "vue-sonner";

const isRecording = ref(false);
let recorder: MediaRecorder | null = null;
let chunks: Blob[] = [];

const route = useRoute();
const voiceSpaceStore = useVoiceSpaceStore();
const userStore = useUserStore();

const isBusinessPlan = computed(() => userStore.userPlan === "BUSINESS");

const showPremiumDialog = ref(false);
const showSummaryModal = ref(false);
const isSummaryLoading = ref(false);
const meetingTranscript = ref("");
const meetingSummaryJson = ref("{}");
const testFileInput = ref<HTMLInputElement | null>(null);

const triggerTestUpload = () => {
  testFileInput.value?.click();
};

const handleTestFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;
  showSummaryModal.value = true;
  isSummaryLoading.value = true;
  meetingTranscript.value = "";
  meetingSummaryJson.value = "{}";
  const formData = new FormData();
  formData.append("file", file);
  formData.append("roomId", (route.params.roomId as string) || "00000000-0000-0000-0000-000000000000");
  try {
    const response = await axiosClient.post("/api/public/voice-summary/test-upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 180000, // 3 phút 
    });
    const data = response.data;
    meetingTranscript.value = data.transcript;
    meetingSummaryJson.value = data.summaryJson;
    toast.success("Xử lý file test thành công!");
  } catch (error) {
    console.error("Lỗi khi test tóm tắt cuộc họp:", error);
    toast.error("Gửi file test thất bại hoặc lỗi xử lý AI.");
    showSummaryModal.value = false;
  } finally {
    isSummaryLoading.value = false;
    target.value = ""; // Reset input
  }
};

const handleSummary = () => {
  // Đang record → dừng lại
  if (isRecording.value) {
    recorder?.stop();
    isRecording.value = false;
    return;
  }

  if (userStore.userPlan !== "BUSINESS") {
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
  recorder = new MediaRecorder(audioStream, { mimeType: "audio/webm" });
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
        timeout: 60000,
        // t set timeout 60s, vì file ghi âm có thể khá dài, nên cần thời gian xử lý lâu hơn - AI TỰ GỢI Ý =))))
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
    <div class="flex items-center gap-2.5">
      <SidebarTrigger class="-ml-1" />
      <div class="h-4 w-px bg-border/60" />
      <span class="font-semibold text-base flex gap-2 items-center">
        <div class="w-7 h-7 rounded-lg bg-primary/15 flex items-center justify-center">
          <Volume2 class="h-4.5 w-4.5 text-primary" />
        </div>

        {{ route.params.spaceName ?? "Voice" }}
      </span>
    </div>

    <!-- <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1 shrink-0 text-muted-foreground hover:text-foreground" />
        
        <span class="flex items-center gap-2 font-sans text-sm font-semibold text-foreground">
          <div class="w-7 h-7 rounded-lg bg-primary/15 flex items-center justify-center">
            <CalendarDays class="h-4.5 w-4.5 text-primary" />
          </div>
          {{ currentSpaceName }}
        </span>
      </div> -->

    <div class="flex items-center gap-2">
      <!-- Test upload button -->
      <input v-if="isBusinessPlan" type="file" ref="testFileInput" class="hidden" accept="audio/*"
        @change="handleTestFileUpload" />
      <button v-if="isBusinessPlan" @click="triggerTestUpload"
        class="flex items-center gap-2 px-4 py-1 rounded-lg text-sm font-normal text-amber-500 hover:text-amber-600 hover:bg-amber-500/10 transition-colors border border-amber-500/30 h-8">
        <UploadCloud class="h-4 w-4" />
        Test Tóm Tắt
      </button>

      <button v-if="isBusinessPlan" @click="handleSummary"
        class="flex items-center gap-2 px-4 py-1 rounded-lg text-sm font-normal text-muted-foreground hover:text-foreground hover:bg-muted transition-colors border border-border h-8"
        :class="{ 'text-red-500 border-red-500': isRecording }">
        <FileText class="h-5 w-5" />
        {{ isRecording ? "Đang ghi... (bấm để dừng)" : "Tóm tắt cuộc họp" }}
      </button>
    </div>
  </div>
  <PremiumFeatureDialog v-model:open="showPremiumDialog" feature-name="Tóm tắt cuộc họp" :business-only="true" />
  <VoiceSummaryModal v-model:open="showSummaryModal" :is-loading="isSummaryLoading" :transcript="meetingTranscript"
    :summary-json="meetingSummaryJson" />
</template>

<style scoped></style>
