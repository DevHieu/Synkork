<script setup lang="ts">
import { useMessageStore } from "@/stores/messageStore";
import { CirclePlus, Smile } from "lucide-vue-next";
import { storeToRefs } from "pinia";
import { watch, nextTick, ref, onMounted, onUnmounted, computed } from "vue";

import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css"; // Nó báo lỗi thì kệ mịa nó đi, sửa lại đúng đường dẫn là ko chạy được đâu á

import ReplyBar from "./sub-components/ReplyBar.vue";
import FilePreview from "./sub-components/FilePreview.vue";

const newMessage = ref("");
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
const hasFiles = computed(() => selectedFiles.value.length > 0);

const addFiles = (newFiles: FileList | File[]) => {
  Array.from(newFiles).forEach((file) => {
    if (
      selectedFiles.value.some(
        (f) => f.name === file.name && f.size === file.size,
      )
    )
      return;
    selectedFiles.value.push(file);
    if (isImage(file)) {
      const reader = new FileReader();
      reader.onload = (e) => {
        filePreviews.value = new Map(filePreviews.value).set(
          file,
          e.target?.result as string,
        );
      };
      reader.readAsDataURL(file);
    }
  });

  console.log(filePreviews);
};

const removeFile = (file: File) => {
  selectedFiles.value = selectedFiles.value.filter((f) => f !== file);
  const map = new Map(filePreviews.value);
  map.delete(file);
  filePreviews.value = map;
};

const clearFiles = () => {
  selectedFiles.value = [];
  filePreviews.value = new Map();
  if (fileInputRef.value) fileInputRef.value.value = "";
};

const handleSubmit = async () => {
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
  messageStore.setReply(null);

  messageStore.sendMessage(props.spaceId, content, formData, files);
};

const handleFileChange = (e: Event) => {
  const files = (e.target as HTMLInputElement).files;
  if (files) addFiles(files);
  if (fileInputRef.value) fileInputRef.value.value = "";

  inputRef.value?.focus();
};

const onSelectEmoji = (emoji: { i: string }) => {
  newMessage.value += emoji.i;
  inputRef.value?.focus();
};

const toggleEmojiPicker = () => {
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
      newMessage.value = "";
      await nextTick();
      inputRef.value?.focus();
    }
  },
  { immediate: true },
);

// Xử lí ném file vào ô input
const isDragging = ref(false);
const handleDragOver = (e: DragEvent) => {
  e.preventDefault();
  isDragging.value = true;
};
const handleDragLeave = () => {
  isDragging.value = false;
};
const handleDrop = (e: DragEvent) => {
  e.preventDefault();
  isDragging.value = false;
  if (e.dataTransfer?.files) addFiles(e.dataTransfer.files);
};

onMounted(() => document.addEventListener("mousedown", handleClickOutside));
onUnmounted(() =>
  document.removeEventListener("mousedown", handleClickOutside),
);
</script>

<template>
  <div
    class="relative border-t border-white/10 bg-muted/60 backdrop-blur-md"
    @dragover="handleDragOver"
    @dragleave="handleDragLeave"
    @drop="handleDrop"
  >
    <!-- Drag overlay -->
    <Transition name="fade">
      <div
        v-if="isDragging"
        class="absolute inset-0 z-50 flex items-center justify-center bg-primary/10 border-2 border-dashed border-primary/50 rounded-lg pointer-events-none"
      >
        <p class="text-primary font-medium text-sm">Thả file vào đây</p>
      </div>
    </Transition>

    <Transition name="reply-slide">
      <ReplyBar
        v-if="replyingTo"
        :replying-to="replyingTo"
        @cancel="messageStore.setReply(null)"
      />
    </Transition>

    <Transition name="reply-slide">
      <FilePreview
        v-if="hasFiles"
        :files="selectedFiles"
        :previews="filePreviews"
        @remove="removeFile"
        @clear="clearFiles"
        @add-more="fileInputRef?.click()"
      />
    </Transition>

    <div class="flex items-center gap-1 px-3 py-3">
      <button
        @click="fileInputRef?.click()"
        title="Đính kèm file"
        class="shrink-0 w-9 h-9 rounded-full flex items-center justify-center text-white/40 hover:text-white/80 hover:bg-white/8 transition-all"
      >
        <CirclePlus />
      </button>
      <input
        ref="fileInputRef"
        type="file"
        multiple
        class="hidden"
        @change="handleFileChange"
      />

      <div
        class="flex-1 flex items-center bg-white/8 rounded-lg px-3 gap-2 border border-white/5 focus-within:border-white/10 transition-colors"
      >
        <input
          ref="inputRef"
          v-model="newMessage"
          :placeholder="
            replyingTo
              ? `Trả lời ${replyingTo.sender?.displayName}...`
              : 'Nhắn tin...'
          "
          class="flex-1 bg-transparent py-2.5 text-white placeholder-white/25 focus:outline-none text-sm"
          @keydown.esc="messageStore.setReply(null)"
          @keydown.enter.exact.prevent="handleSubmit"
        />
        <div class="relative shrink-0">
          <button
            ref="emojiButtonRef"
            @click="toggleEmojiPicker"
            title="Emoji"
            class="w-8 h-8 rounded-full flex items-center justify-center transition-all"
            :class="
              showEmojiPicker
                ? 'text-teal-400'
                : 'text-white/40 hover:text-white/80'
            "
          >
            <Smile />
          </button>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="showEmojiPicker"
        ref="emojiPickerRef"
        class="fixed z-[9999]"
        :style="{
          bottom: emojiPickerPos.bottom + 'px',
          right: emojiPickerPos.right + 'px',
        }"
      >
        <EmojiPicker
          :native="true"
          :disable-skin-tones="true"
          @select="onSelectEmoji"
        />
      </div>
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

:deep(.v3-emoji-picker) {
  --v3-picker-bg: var(--popover);
  --v3-picker-fg: var(--foreground);
  --v3-picker-border: var(--border);
  --v3-picker-input-bg: var(--muted);
  --v3-picker-input-border: var(--border);
  --v3-picker-input-focus-border: var(--primary);
  --v3-picker-emoji-hover: var(--accent);
  --v3-group-image-filter: invert(1);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

:global(.dark) :deep(.v3-emoji-picker) {
  --v3-group-image-filter: invert(1);
}
</style>
