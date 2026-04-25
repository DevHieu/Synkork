<script setup lang="ts">
import { useMessageStore } from "@/stores/messageStore";
import { CirclePlus, Smile } from "lucide-vue-next";
import { storeToRefs } from "pinia";
import { watch, nextTick, ref, onMounted, onUnmounted } from "vue";

import EmojiPicker from "vue3-emoji-picker";
import "vue3-emoji-picker/css";

const newMessage = ref("");

const inputRef = ref<HTMLInputElement | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const showEmojiPicker = ref(false);
const emojiPickerRef = ref<HTMLDivElement | null>(null);
const emojiButtonRef = ref<HTMLButtonElement | null>(null);
const emojiPickerPos = ref({ bottom: 0, right: 0 });

const props = defineProps<{
  spaceId: string;
  replyingTo?: any;
}>();

const messageStore = useMessageStore();
const { replyingTo } = storeToRefs(messageStore);

const handleSubmit = async () => {
  messageStore.sendMessage(props.spaceId, newMessage.value);
  newMessage.value = "";
  await nextTick();
  messageStore.scrollToBottom(props.spaceId);
};

const cancelReply = () => {
  messageStore.setReply(null);
};

const handleFileClick = () => {
  fileInputRef.value?.click();
};

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  console.log("[log] File selected:", file);
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

onMounted(() => document.addEventListener("mousedown", handleClickOutside));
onUnmounted(() =>
  document.removeEventListener("mousedown", handleClickOutside),
);

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
</script>

<template>
  <div class="border-t border-white/10 bg-muted/60 backdrop-blur-md">
    <!-- Reply preview bar -->
    <Transition name="reply-slide">
      <div v-if="replyingTo" class="flex items-center gap-3 px-4 pt-2.5 pb-1">
        <div class="flex items-center gap-2 shrink-0">
          <div class="w-0.5 h-8 rounded-full bg-teal-500/80"></div>
          <svg
            class="w-3.5 h-3.5 text-teal-400 shrink-0"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="9 14 4 9 9 4" />
            <path d="M20 20v-7a4 4 0 0 0-4-4H4" />
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-[11px] font-semibold text-teal-400 mb-0.5">
            Đang trả lời
            <span class="text-white/80">{{
              replyingTo.sender?.displayName
            }}</span>
          </p>
          <p class="text-xs text-white/40 truncate leading-tight">
            {{ replyingTo.content }}
          </p>
        </div>
        <button
          @click="cancelReply"
          class="shrink-0 w-5 h-5 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors group"
          title="Hủy reply"
        >
          <svg
            class="w-2.5 h-2.5 text-white/50 group-hover:text-white/90 transition-colors"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="3"
            stroke-linecap="round"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
    </Transition>

    <!-- Input row -->
    <div class="flex items-center gap-1 px-3 py-3">
      <!-- Nút gửi file -->
      <button
        @click="handleFileClick"
        title="Đính kèm file"
        class="shrink-0 w-9 h-9 rounded-full flex items-center justify-center text-white/40 hover:text-white/80 hover:bg-white/8 transition-all"
      >
        <CirclePlus />
      </button>
      <input
        ref="fileInputRef"
        type="file"
        class="hidden"
        @change="handleFileChange"
      />

      <!-- Input box -->
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
          @keydown.esc="cancelReply"
          @keydown.enter.exact.prevent="handleSubmit"
        />

        <!-- Nút emoji -->
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

    <!-- Emoji picker teleported to body -->
    <Teleport to="body">
      <div
        v-if="showEmojiPicker"
        ref="emojiPickerRef"
        class="fixed z-50"
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
  max-height: 60px;
}

:deep(.v3-emoji-picker) {
  --v3-picker-bg: var(--popover);
  --v3-picker-fg: var(--foreground);
  --v3-picker-border: var(--border);
  --v3-picker-input-bg: var(--muted);
  --v3-picker-input-border: var(--border);
  --v3-picker-input-focus-border: var(--primary);
  --v3-picker-emoji-hover: var(--accent);
  /* Để invert màu tạm lại vì chưa biết fix theme trắng đen */
  --v3-group-image-filter: invert(1);

  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

:global(.dark) :deep(.v3-emoji-picker) {
  --v3-group-image-filter: invert(1);
}
</style>
Z
