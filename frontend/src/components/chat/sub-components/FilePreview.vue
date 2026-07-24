<script setup lang="ts">
import { FileIcon, PlayCircle, Plus, X } from "lucide-vue-next";

const props = defineProps<{
  files: File[];
  previews: Map<File, string>;
}>();

const emit = defineEmits<{
  remove: [file: File];
  clear: [];
  addMore: [];
}>();

const isImage = (file: File) => file.type.startsWith("image/");
const isVideo = (file: File) => file.type.startsWith("video/");
const isPreviewableMedia = (file: File) => isImage(file) || isVideo(file);
</script>

<template>
  <div class="px-4 pt-2.5 pb-1">
    <div class="flex items-center justify-between mb-2">
      <span class="text-[11px] font-semibold text-muted-foreground">
        {{ files.length }} file{{ files.length > 1 ? "s" : "" }}
      </span>
      <button
        @click="emit('clear')"
        class="text-[11px] text-muted-foreground hover:text-foreground transition-colors"
      >
        Xoá tất cả
      </button>
    </div>
    <div class="flex gap-2 flex-wrap">
      <div
        v-for="file in files"
        :key="file.name + file.size"
        class="relative group/thumb w-16 h-16 rounded-lg overflow-hidden border border-border bg-muted shrink-0"
      >
        <template v-if="isPreviewableMedia(file) && previews.get(file)">
          <img
            v-if="isImage(file)"
            :src="previews.get(file)"
            class="h-full w-full object-cover"
            :alt="file.name"
          >

          <video
            v-else
            :src="previews.get(file)"
            muted
            playsinline
            preload="metadata"
            class="h-full w-full object-cover"
          />

          <div
            v-if="isVideo(file)"
            class="absolute inset-0 flex items-center justify-center bg-black/20 text-white"
          >
            <PlayCircle class="h-6 w-6" />
          </div>
        </template>

        <div
          v-else
          class="w-full h-full flex flex-col items-center justify-center gap-1 px-1"
        >
          <FileIcon class="w-6 h-6 text-primary/80" />
          <span
            class="text-[9px] text-muted-foreground truncate w-full text-center leading-tight"
            >{{ file.name }}</span
          >
        </div>
        <button
          @click="emit('remove', file)"
          class="absolute top-0.5 right-0.5 w-4 h-4 rounded-full bg-background/80 flex items-center justify-center opacity-0 group-hover/thumb:opacity-100 transition-opacity"
        >
          <X class="w-2.5 h-2.5 text-foreground" />
        </button>
      </div>
      <button
        @click="emit('addMore')"
        class="w-16 h-16 rounded-lg border border-dashed border-border flex items-center justify-center text-muted-foreground hover:text-foreground hover:border-foreground/40 transition-all shrink-0"
      >
        <Plus class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>
