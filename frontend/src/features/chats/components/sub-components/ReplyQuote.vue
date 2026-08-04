<script setup lang="ts">
import type { ReplyPreview } from "@/features/chats/types/MessageTypes";

defineProps<{
  replyTo: ReplyPreview;
}>();

const emit = defineEmits<{
  jump: [];
}>();
</script>

<template>
  <div class="flex items-stretch mb-2 cursor-pointer max-w-xs rounded-r-md overflow-hidden group/quote"
    @click="emit('jump')">
    <div class="w-0.5 shrink-0 bg-primary/60" />
    <div class="flex-1 min-w-0 bg-muted group-hover/quote:bg-accent transition-colors px-2.5 py-1.5">
      <p class="text-[11px] font-semibold text-primary mb-0.5 leading-none truncate">
        {{ replyTo.senderDisplayName }}
      </p>
      <p class="text-[11px] text-muted-foreground truncate leading-snug" :class="{ italic: replyTo.deleted }">
        {{ replyTo.deleted ? "Tin nhắn đã bị xóa" : replyTo.content }}
      </p>
    </div>
  </div>
</template>
