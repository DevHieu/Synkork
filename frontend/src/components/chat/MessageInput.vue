<script setup lang="ts">
import { useMessageStore } from "@/stores/messageStore";
import { storeToRefs } from "pinia";
import { watch, nextTick, ref } from "vue";

const newMessage = ref("");

const props = defineProps<{
  spaceId: string;
}>();

const inputRef = ref<HTMLInputElement | null>(null);
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
        <!-- Accent line + icon -->
        <div class="flex items-center gap-2 shrink-0">
          <div class="w-0.5 h-8 rounded-full bg-teal-500/80"></div>
          <!-- Reply icon -->
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

        <!-- Reply content -->
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

        <!-- Cancel button -->
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
    <div class="flex gap-2 px-4 py-3.5">
      <input
        ref="inputRef"
        v-model="newMessage"
        :placeholder="
          replyingTo
            ? `Trả lời ${replyingTo.sender?.displayName}...`
            : 'Nhắn tin...'
        "
        class="flex-1 bg-white/5 border px-3 py-2 rounded text-white placeholder-gray-500 focus:outline-none focus:ring-1 transition-colors text-sm"
        :class="
          replyingTo
            ? 'border-teal-500/40 focus:ring-teal-500/50 focus:border-teal-500/60'
            : 'border-white/10 focus:ring-teal-500 focus:border-white/20'
        "
        @keydown.esc="cancelReply"
        @keydown.enter.exact.prevent="handleSubmit"
      />
      <button
        @click="handleSubmit"
        class="px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-500 active:scale-95 transition-all font-medium text-sm"
      >
        Gửi
      </button>
    </div>
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
</style>
