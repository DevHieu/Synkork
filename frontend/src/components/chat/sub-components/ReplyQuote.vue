<script setup lang="ts">
import type { ReplyPreview } from "@/types/Message";

defineProps<{
  replyTo: ReplyPreview;
}>();

const emit = defineEmits<{
  jump: [];
}>();
</script>

<template>
  <div
    class="flex items-stretch mb-2 cursor-pointer max-w-xs rounded-r-md overflow-hidden group/quote"
    @click="emit('jump')"
  >
    <div class="w-0.5 shrink-0 bg-teal-500/60" />
    <div
      class="flex-1 min-w-0 bg-white/4 group-hover/quote:bg-white/[0.07] transition-colors px-2.5 py-1.5"
    >
      <p
        class="text-[11px] font-semibold text-teal-400 mb-0.5 leading-none truncate"
      >
        {{ replyTo.senderDisplayName }}
      </p>
      <p
        class="text-[11px] text-white/40 truncate leading-snug"
        :class="{ italic: replyTo.deleted }"
      >
        {{ replyTo.deleted ? "Tin nhắn đã bị xóa" : replyTo.content }}
      </p>
    </div>
  </div>
</template>
