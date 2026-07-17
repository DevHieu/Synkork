<script setup lang="ts">
import { ref } from "vue";
import { Volume2, MicOff, VolumeX, Settings, UploadCloud } from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { SidebarMenuButton, SidebarMenuItem } from "@/components/ui/sidebar";
import SpaceSettingDialog from "./SpaceSettingDialog.vue";
import Avatar from "../ui/avatar/Avatar.vue";
import AvatarImage from "../ui/avatar/AvatarImage.vue";
import AvatarFallback from "../ui/avatar/AvatarFallback.vue";
import VoiceSummaryModal from "../voice/VoiceSummaryModal.vue";
import axiosClient from "@/lib/axiosClient";
import { toast } from "vue-sonner";
import { useRoute } from "vue-router";

const props = defineProps<{
  spaceId: string;
  spaceName: string;
  isActive: boolean;
  canManage: boolean;
  restricted: boolean;
  participants: {
    userID: string;
    userName: string;
    avatarUrl?: string;
    micOn: boolean;
    audioOn: boolean;
    isLocal: boolean;
  }[];
}>();

const emit = defineEmits<{
  join: [];
  save: [data: { name: string; restricted: boolean }];
  delete: [];
}>();

const settingOpen = ref(false);
const route = useRoute();

// ------------------------------------------------------------------------------------------------
// tính năng này là để test vì t quá lười
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
}; // -------------------------------------------------------------------------------------------------- END
</script>

<template>
  <SidebarMenuItem>
    <SidebarMenuButton
      @click="emit('join')"
      :isActive="isActive"
      class="group/item pr-1"
    >
      <Volume2 class="mr-2 h-4 w-4 shrink-0" />
      <span class="flex-1 truncate">{{ spaceName }}</span>

      <button
        v-if="canManage"
        @click.stop="settingOpen = true"
        class="opacity-0 group-hover/item:opacity-100 transition-opacity ml-1 p-0.5 rounded hover:bg-muted"
      >
        <Settings class="h-4 w-4 text-foreground" />
      </button>
      <!-- Test upload button -->
      <input
        type="file"
        ref="testFileInput"
        class="hidden"
        accept="audio/*"
        @change="handleTestFileUpload"
      />
      <Button variant="ghost" size="sm" @click.stop="triggerTestUpload" class="text-amber-500 hover:text-amber-600 hover:bg-amber-500/10 h-7 text-xs px-2">
        <UploadCloud class="h-4 w-4 mr-1.5" />
        Test Tóm Tắt
      </Button>
    </SidebarMenuButton>

    <!-- Participants -->
    <div v-if="participants.length" class="ml-4 mt-0.5 mb-1 space-y-0.5">
      <div
        v-for="p in participants"
        :key="p.userID"
        class="flex items-center gap-2 px-2 py-0.5 rounded text-xs text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors cursor-default"
      >
        <div class="relative shrink-0">
          <Avatar class="h-6 w-6 shrink-0">
            <AvatarImage v-if="p.avatarUrl" :src="p.avatarUrl" />
            <AvatarFallback class="text-xs"> </AvatarFallback>
          </Avatar>
        </div>
        <span class="truncate flex-1">
          {{ p.userName }}
          <span v-if="p.isLocal" class="opacity-50">(Bạn)</span>
        </span>
        <div class="flex items-center gap-1 shrink-0">
          <MicOff v-if="!p.micOn" class="h-3.5 w-3.5 text-red-500" />
          <VolumeX v-if="!p.audioOn" class="h-3.5 w-3.5 text-red-500" />
        </div>
      </div>
    </div>

    <SpaceSettingDialog
      v-model:open="settingOpen"
      :space-id="spaceId"
      :space-name="spaceName"
      :restricted="restricted"
      @save="emit('save', $event)"
      @delete="emit('delete')"
    />
  </SidebarMenuItem>
  <VoiceSummaryModal
    v-model:open="showSummaryModal"
    :is-loading="isSummaryLoading"
    :transcript="meetingTranscript"
    :summary-json="meetingSummaryJson"
  />
</template>
