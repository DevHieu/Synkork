<script setup lang="ts">
import { FileIcon } from "lucide-vue-next";

const props = defineProps<{
  type: "IMAGE" | "FILE";
  attachmentUrl: string;
  attachmentName: string | null;
  sending?: boolean;
}>();

const emit = defineEmits<{
  open: [];
}>();
</script>

<template>
  <!-- Skeleton -->
  <template v-if="sending">
    <div
      v-if="type === 'IMAGE'"
      class="w-48 h-36 bg-white/10 rounded-lg animate-pulse"
    />
    <div
      v-else
      class="flex items-center gap-3 bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 max-w-xs"
    >
      <div class="w-8 h-8 rounded-md bg-white/10 animate-pulse shrink-0" />
      <div class="flex-1 space-y-1.5">
        <div class="h-3 w-32 bg-white/10 rounded animate-pulse" />
        <div class="h-2.5 w-20 bg-white/10 rounded animate-pulse" />
      </div>
    </div>
  </template>

  <!-- Image -->
  <div v-else-if="type === 'IMAGE'" class="mt-1">
    <img
      :src="attachmentUrl"
      :alt="attachmentName ?? 'image'"
      class="max-w-xs max-h-72 rounded-lg object-cover cursor-pointer hover:opacity-90 transition-opacity"
      @click="emit('open')"
    />
  </div>

  <!-- File -->
  <div
    v-else
    class="mt-1 flex items-center gap-3 bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 max-w-xs hover:bg-white/8 transition-colors cursor-pointer"
    @click="emit('open')"
  >
    <div
      class="w-8 h-8 rounded-md bg-primary/20 flex items-center justify-center shrink-0"
    >
      <FileIcon class="w-4 h-4 text-primary" />
    </div>
    <div class="flex-1 min-w-0">
      <p class="text-sm text-foreground truncate">
        {{ attachmentName ?? "File" }}
      </p>
      <p class="text-[11px] text-muted-foreground">Nhấn để tải xuống</p>
    </div>
  </div>
</template>
