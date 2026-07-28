<script setup lang="ts">
import type { Message } from "@/types/Message";

defineProps<{
  replyingTo: Message;
}>();

const emit = defineEmits<{
  cancel: [];
}>();
</script>

<template>
  <div class="flex items-center gap-3 px-4 pt-2.5 pb-1">
    <div class="flex items-center gap-2 shrink-0">
      <div class="w-0.5 h-8 rounded-full bg-primary/80"></div>
      <svg
        class="w-3.5 h-3.5 text-primary shrink-0"
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
      <p class="text-[11px] font-semibold text-primary mb-0.5">
        Đang trả lời
        <span class="text-foreground">{{
          replyingTo.sender?.displayName
        }}</span>
      </p>
      <p class="text-xs text-muted-foreground truncate leading-tight">
        {{ replyingTo.content }}
      </p>
    </div>
    <button
      @click="emit('cancel')"
      class="shrink-0 w-5 h-5 rounded-full bg-muted hover:bg-accent flex items-center justify-center transition-colors group"
    >
      <svg
        class="w-2.5 h-2.5 text-muted-foreground group-hover:text-foreground transition-colors"
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
</template>
