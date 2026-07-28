<script setup lang="ts">
import { FileIcon, PlayCircle } from "lucide-vue-next";
import { computed, ref } from "vue";

import MediaPreviewDialog from "./MediaPreviewDialog.vue";

const props = defineProps<{
  type: "IMAGE" | "VIDEO" | "FILE";
  attachmentUrl: string;
  attachmentName: string | null;
  sending?: boolean;
}>();

const viewerOpen = ref(false);

const isPreviewableMedia = computed(
  () => props.type === "IMAGE" || props.type === "VIDEO",
);

const handleClick = async () => {
  if (isPreviewableMedia.value) {
    viewerOpen.value = true;
  } else {
    const res = await fetch(props.attachmentUrl);
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = props.attachmentName ?? "file";
    a.click();
    URL.revokeObjectURL(url);
  }
};
</script>

<template>
  <!-- Skeleton -->
  <template v-if="sending">
    <div
      v-if="isPreviewableMedia"
      class="h-36 w-48 animate-pulse rounded-lg bg-muted"
    />
    <div
      v-else
      class="flex items-center gap-3 bg-muted border border-border rounded-lg px-3 py-2.5 max-w-xs"
    >
      <div class="w-8 h-8 rounded-md bg-accent animate-pulse shrink-0" />
      <div class="flex-1 space-y-1.5">
        <div class="h-3 w-32 bg-accent rounded animate-pulse" />
        <div class="h-2.5 w-20 bg-accent rounded animate-pulse" />
      </div>
    </div>
  </template>

  <!-- Image / Video -->
  <div v-else-if="isPreviewableMedia" class="mt-1">
    <button
      type="button"
      class="group relative block max-w-xs overflow-hidden rounded-lg bg-muted focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      @click="handleClick"
    >
      <img
        v-if="type === 'IMAGE'"
        :src="attachmentUrl"
        :alt="attachmentName ?? 'image'"
        class="max-h-72 max-w-xs cursor-zoom-in object-cover transition-opacity group-hover:opacity-90"
      >

      <video
        v-else
        :src="attachmentUrl"
        muted
        playsinline
        preload="metadata"
        class="max-h-72 max-w-xs cursor-pointer object-cover transition-opacity group-hover:opacity-90"
      />

      <div
        v-if="type === 'VIDEO'"
        class="absolute inset-0 flex items-center justify-center bg-black/10 transition-colors group-hover:bg-black/25"
      >
        <div class="rounded-full bg-black/65 p-2 text-white shadow-sm">
          <PlayCircle class="h-7 w-7" />
        </div>
      </div>
    </button>

    <MediaPreviewDialog
      v-model:open="viewerOpen"
      :src="attachmentUrl"
      :name="attachmentName ?? (type === 'VIDEO' ? 'video' : 'image')"
      :resource-type="type === 'VIDEO' ? 'video' : 'image'"
    />
  </div>

  <!-- File -->
  <div
    v-else
    class="mt-1 flex items-center gap-3 bg-muted border border-border rounded-lg px-3 py-2.5 max-w-xs hover:bg-accent transition-colors cursor-pointer"
    @click="handleClick"
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
