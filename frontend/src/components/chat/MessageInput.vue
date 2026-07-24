<script setup lang="ts">
import { watch, ref, onMounted, onUnmounted, computed } from "vue";
import { Ban, CirclePlus, Smile } from "lucide-vue-next";
import { useUserStore } from "@/stores/userStore.ts";
import { useMessageStore } from "@/stores/messageStore";
import { storeToRefs } from "pinia";
import { PlanLimitUtils } from "@/utils/PlanLimitUtils.ts";
import { chatComposable } from "./composable/chat.composable.ts"

import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css"; // Nó báo lỗi thì kệ mịa nó đi, sửa lại đúng đường dẫn là ko chạy được đâu á

import ReplyBar from "./sub-components/ReplyBar.vue";
import FilePreview from "./sub-components/FilePreview.vue";
import PlanLimitDialog from "../dialog/PlanLimitDialog.vue";
import { useThemeStore } from "@/stores/themeStore.ts";

const userStore = useUserStore();
const { userPlan } = storeToRefs(userStore);
const { isChatDisabled, chatDisabledLabel } = chatComposable();

const newMessage = ref("");
const now = ref(Date.now());
const inputRef = ref<HTMLInputElement | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const showEmojiPicker = ref(false);
const emojiPickerRef = ref<HTMLDivElement | null>(null);
const emojiButtonRef = ref<HTMLButtonElement | null>(null);
const emojiPickerPos = ref({ bottom: 0, right: 0 });

const selectedFiles = ref<File[]>([]);
const filePreviews = ref<Map<File, string>>(new Map());

const props = defineProps<{ spaceId: string }>();

const messageStore = useMessageStore();
const { replyingTo } = storeToRefs(messageStore);

const isImage = (file: File) => file.type.startsWith("image/");
const isVideo = (file: File) => file.type.startsWith("video/");
const isPreviewableMedia = (file: File) => isImage(file) || isVideo(file);
const hasFiles = computed(() => selectedFiles.value.length > 0);

const fileSizeDialogOpen = ref(false);
const rejectedFile = ref<File | null>(null);

const addFiles = (newFiles: FileList | File[]) => {
  if (isChatDisabled.value) return;

  Array.from(newFiles).forEach((file) => {
    if (file.size > PlanLimitUtils.maxFileSizeBytes(userPlan.value)) {
      rejectedFile.value = file;
      fileSizeDialogOpen.value = true;
      return;
    }

    if (
      selectedFiles.value.some(
        (f) => f.name === file.name && f.size === file.size,
      )
    )
      return;
    selectedFiles.value.push(file);
    if (isPreviewableMedia(file)) {
      filePreviews.value = new Map(filePreviews.value).set(
        file,
        URL.createObjectURL(file),
      );
    }
  });
};

const removeFile = (file: File) => {
  const previewUrl = filePreviews.value.get(file);
  if (previewUrl) URL.revokeObjectURL(previewUrl);

  selectedFiles.value = selectedFiles.value.filter((f) => f !== file);
  const map = new Map(filePreviews.value);
  map.delete(file);
  filePreviews.value = map;
};

const clearFiles = () => {
  filePreviews.value.forEach((url) => URL.revokeObjectURL(url));
  selectedFiles.value = [];
  filePreviews.value = new Map();
  if (fileInputRef.value) fileInputRef.value.value = "";
};

const handleSubmit = async () => {
  if (isChatDisabled.value) return;
  if (!newMessage.value.trim() && !hasFiles.value) return;

  const content = newMessage.value.trim();
  let files: File[] | null = null;
  let formData: FormData | null = null;

  if (hasFiles.value) {
    formData = new FormData();
    selectedFiles.value.forEach((file) => formData!.append("fileList", file));

    // Gửi file nhưng ko có text thì cái reply sẽ gắn vào file
    if (replyingTo.value?.id && !content) {
      formData.append("replyToId", replyingTo.value.id);
    }

    files = [...selectedFiles.value];
  }

  // Reset UI trước
  newMessage.value = "";
  clearFiles();

  messageStore.sendMessage(props.spaceId, content, formData, files);
};

const handleFileChange = (e: Event) => {
  if (isChatDisabled.value) return;

  const files = (e.target as HTMLInputElement).files;
  if (files) addFiles(files);
  if (fileInputRef.value) fileInputRef.value.value = "";
  inputRef.value?.focus();
};

const onSelectEmoji = (emoji: { i: string }) => {
  if (isChatDisabled.value) return;

  newMessage.value += emoji.i;
  inputRef.value?.focus();
};

const toggleEmojiPicker = () => {
  if (isChatDisabled.value) return;

  if (!showEmojiPicker.value && emojiButtonRef.value) {
    const rect = emojiButtonRef.value.getBoundingClientRect();
    emojiPickerPos.value = {
      bottom: window.innerHeight - rect.top + 8,
      right: window.innerWidth - rect.right,
    };
  }
  showEmojiPicker.value = !showEmojiPicker.value;
};

const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as Node;
  if (
    emojiPickerRef.value &&
    !emojiPickerRef.value.contains(target) &&
    !emojiButtonRef.value?.contains(target)
  ) {
    showEmojiPicker.value = false;
  }
};

// Watch khi bắt đầu trả lời một tin nhắn, tự động focus vào input và clear nội dung đang nhập
watch(
  replyingTo,
  async (newVal) => {
    if (newVal) {
      inputRef.value?.focus();
    }
  },
  { immediate: true },
);

// Xử lí ném file vào ô input
const isDragging = ref(false);
const handleDragOver = (e: DragEvent) => {
  if (isChatDisabled.value) return;

  e.preventDefault();
  isDragging.value = true;
};
const handleDragLeave = () => {
  isDragging.value = false;
};
const handleDrop = (e: DragEvent) => {
  e.preventDefault();
  isDragging.value = false;
  if (isChatDisabled.value) return;

  if (e.dataTransfer?.files) addFiles(e.dataTransfer.files);
};

let nowTimer: number | null = null;

onMounted(() => {
  document.addEventListener("mousedown", handleClickOutside);
  nowTimer = window.setInterval(() => {
    now.value = Date.now();
  }, 1000);
});
onUnmounted(() => {
  document.removeEventListener("mousedown", handleClickOutside);
  if (nowTimer !== null) {
    window.clearInterval(nowTimer);
  }
});
</script>

<template>
  <div class="relative border-t background" @dragover="handleDragOver" @dragleave="handleDragLeave" @drop="handleDrop">
    <!-- Drag overlay -->
    <Transition name="fade">
      <div v-if="isDragging"
        class="absolute inset-0 z-50 flex items-center justify-center bg-primary/10 border-2 border-dashed border-primary/50 rounded-lg pointer-events-none">
        <p class="text-primary font-medium text-sm">Thả file vào đây</p>
      </div>
    </Transition>

    <Transition name="reply-slide">
      <ReplyBar v-if="replyingTo" :replying-to="replyingTo" @cancel="messageStore.setReply(null)" />
    </Transition>

    <Transition name="reply-slide">
      <FilePreview v-if="hasFiles" :files="selectedFiles" :previews="filePreviews" @remove="removeFile"
        @clear="clearFiles" @add-more="fileInputRef?.click()" />
    </Transition>

    <div v-if="isChatDisabled"
      class="mx-3 mt-3 flex items-center gap-2 rounded-lg border border-destructive/25 bg-destructive/10 px-3 py-2 text-xs text-destructive">
      <Ban class="h-4 w-4 shrink-0" />
      <span>Bạn đang bị chặn nhắn tin đến {{ chatDisabledLabel }}.</span>
    </div>

    <div class="flex items-center gap-1 px-3 py-3">
      <button @click="fileInputRef?.click()" title="Đính kèm file" :disabled="isChatDisabled"
        class="shrink-0 w-9 h-9 rounded-full flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-accent transition-all disabled:cursor-not-allowed disabled:opacity-50">
        <CirclePlus />
      </button>
      <input ref="fileInputRef" type="file" multiple class="hidden" @change="handleFileChange" />

      <div
        class="flex-1 flex items-center bg-muted/50 rounded-lg px-3 gap-2 border border-border focus-within:border-primary/50 transition-colors"
        :class="isChatDisabled ? 'opacity-70' : ''">
        <input ref="inputRef" v-model="newMessage" :placeholder="replyingTo
          ? `Trả lời ${replyingTo.sender?.displayName}...`
          : isChatDisabled
            ? 'Bạn đang bị chặn chat'
            : 'Nhắn tin...'
          "
          class="flex-1 bg-transparent py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none text-sm"
          @keydown.esc="messageStore.setReply(null)" @keydown.enter.exact.prevent="handleSubmit"
          :disabled="isChatDisabled" />
        <div class="relative shrink-0">
          <button ref="emojiButtonRef" @click="toggleEmojiPicker" title="Emoji" :disabled="isChatDisabled"
            class="w-8 h-8 rounded-full flex items-center justify-center transition-all disabled:cursor-not-allowed disabled:opacity-50"
            :class="showEmojiPicker
              ? 'text-primary'
              : 'text-muted-foreground hover:text-foreground'
              ">
            <Smile />
          </button>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showEmojiPicker" ref="emojiPickerRef" class="fixed z-20" :style="{
        bottom: emojiPickerPos.bottom + 'px',
        right: emojiPickerPos.right + 'px',
      }">
        <EmojiPicker :native="true" :disable-skin-tones="true" @select="onSelectEmoji"
          :theme="useThemeStore().isDark ? 'dark' : 'light'" />
      </div>

      <PlanLimitDialog v-model:open="fileSizeDialogOpen" :limit-type="'file'" :file-name="rejectedFile?.name ?? ''"
        :file-size="rejectedFile?.size ?? 0" :current-plan="userPlan" @dismiss="rejectedFile = null" />
    </Teleport>
  </div>
</template>

<style scoped>
.reply-slide-enter-active,
.reply-slide-leave-active {
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.reply-slide-enter-from,
.reply-slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.reply-slide-enter-to,
.reply-slide-leave-from {
  opacity: 1;
  max-height: 160px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
