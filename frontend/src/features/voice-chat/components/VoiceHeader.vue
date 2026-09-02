<script setup lang="ts">
import { ref, computed } from "vue";
import SidebarTrigger from "@/components/ui/sidebar/SidebarTrigger.vue";
import { FileText, Volume2, UploadCloud } from "lucide-vue-next";
import { useUserStore } from "@/features/users/stores/userStore";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore.ts";
import { useRoute } from "vue-router";
import PremiumFeatureDialog from "@/components/dialog/PremiumFeatureDialog.vue";
import VoiceSummaryModal from "./VoiceSummaryModal.vue";
import axiosClient from "@/lib/axiosClient";
import { toast } from "vue-sonner";

const isRecording = ref(false);
let recorder: MediaRecorder | null = null;
let chunks: Blob[] = [];

const EMPTY_SUMMARY = JSON.stringify({
  summary: "Nội dung không đủ để tóm tắt.",
  keyPoints: [],
  actionItems: [],
});

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
  try {
    const response = await axiosClient.post("/api/public/voice-summary/test-upload", formData, {
      timeout: 180000,
    });
    const data = response.data;
    meetingTranscript.value = data.transcript ?? "";
    meetingSummaryJson.value = data.summaryJson ?? EMPTY_SUMMARY;
    toast.success("Đã tạo tóm tắt từ file test.");
  } catch (error: any) {
    console.error("Lỗi khi test tóm tắt cuộc họp:", error);
    toast.error(error.response?.data || "Gửi file test thất bại hoặc lỗi xử lý AI.");
    showSummaryModal.value = false;
  } finally {
    isSummaryLoading.value = false;
    target.value = "";
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

  const tracks = voiceSpaceStore.getAudioTracks().filter((track) => track.readyState === "live");
  if (tracks.length === 0) {
    toast.error("Không có audio đang hoạt động trong phòng.");
    return;
  }

  // Truyền tracks vào đây chứ không để rỗng
  const audioStream = new MediaStream(tracks);
  const mimeType = ["audio/webm;codecs=opus", "audio/webm", "audio/mp4"]
    .find((type) => MediaRecorder.isTypeSupported(type));
  if (!mimeType) {
    toast.error("Trình duyệt không hỗ trợ định dạng ghi âm.");
    return;
  }
  const audioType = mimeType.split(";", 1)[0];
  const extension = audioType === "audio/mp4" ? "m4a" : "webm";

  recorder = new MediaRecorder(audioStream, { mimeType });
  chunks = [];

  const audioContext = new AudioContext();
  void audioContext.resume();
  const analyser = audioContext.createAnalyser();
  analyser.fftSize = 2048;
  const source = audioContext.createMediaStreamSource(audioStream);
  source.connect(analyser);
  const samples = new Uint8Array(analyser.fftSize);
  const speechSamples: number[] = [];
  const detectSpeech = () => {
    analyser.getByteTimeDomainData(samples);
    let sum = 0;
    for (const sample of samples) {
      const normalized = (sample - 128) / 128;
      sum += normalized * normalized;
    }
    speechSamples.push(Math.sqrt(sum / samples.length));
  };
  const speechTimer = window.setInterval(detectSpeech, 250);
  const hasSpeech = () => speechSamples.some((rms) => rms > 0.03);
  const stopAnalyser = async () => {
    window.clearInterval(speechTimer);
    source.disconnect();
    await audioContext.close();
  };

  recorder.ondataavailable = (e) => {
    if (e.data.size > 0) chunks.push(e.data);
  };

  // Gửi file ghi âm lên backend và hiển thị modal tóm tắt cuộc họp
  recorder.onstop = async () => {
    await stopAnalyser();

    if (!hasSpeech() || chunks.length === 0) {
      meetingTranscript.value = "";
      meetingSummaryJson.value = EMPTY_SUMMARY;
      showSummaryModal.value = true;
      isSummaryLoading.value = false;
      toast.info("Không phát hiện lời nói trong bản ghi.");
      audioStream.getTracks().forEach((track) => track.stop());
      return;
    }

    const blob = new Blob(chunks, { type: audioType });
    const file = new File([blob], `meeting.${extension}`, { type: audioType });

    showSummaryModal.value = true;
    isSummaryLoading.value = true;
    meetingTranscript.value = "";
    meetingSummaryJson.value = "{}";

    const roomId = route.params.roomId;
    if (typeof roomId !== "string" || !roomId) {
      toast.error("Không xác định được phòng họp.");
      showSummaryModal.value = false;
      isSummaryLoading.value = false;
      audioStream.getTracks().forEach((track) => track.stop());
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("roomId", roomId);

    try {
      const response = await axiosClient.post("/api/collaboration/voice-summary/upload", formData, {
        timeout: 180000,
      });

      // Nhận dữ liệu phản hồi từ backend
      const data = response.data;
      meetingTranscript.value = data.transcript ?? "";
      meetingSummaryJson.value = data.summaryJson ?? EMPTY_SUMMARY;
      toast.success("Đã tạo tóm tắt cuộc họp.");
    } catch (error: any) {
      console.error("Lỗi khi xử lý tóm tắt cuộc họp:", {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
      });
      toast.error(error.response?.data || "Gửi file ghi âm cuộc họp thất bại hoặc bị lỗi AI.");
      showSummaryModal.value = false;
    } finally {
      isSummaryLoading.value = false;
      audioStream.getTracks().forEach((track) => track.stop());
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
