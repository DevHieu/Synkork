<script setup lang="ts">
import { FileIcon, Plus, X } from "lucide-vue-next";

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
</script>

<template>
  <div class="px-4 pt-2.5 pb-1">
    <div class="flex items-center justify-between mb-2">
      <span class="text-[11px] font-semibold text-white/50">
        {{ files.length }} file{{ files.length > 1 ? "s" : "" }}
      </span>
      <button
        @click="emit('clear')"
        class="text-[11px] text-white/40 hover:text-white/70 transition-colors"
      >
        Xoá tất cả
      </button>
    </div>

    <div class="flex gap-2 flex-wrap">
      <div
        v-for="file in files"
        :key="file.name + file.size"
        class="relative group/thumb w-16 h-16 rounded-lg overflow-hidden border border-white/10 bg-white/5 shrink-0"
      >
        <img
          v-if="isImage(file) && previews.get(file)"
          :src="previews.get(file)"
          class="w-full h-full object-cover"
          :alt="file.name"
        />
        <div
          v-else
          class="w-full h-full flex flex-col items-center justify-center gap-1 px-1"
        >
          <FileIcon class="w-6 h-6 text-primary/80" />
          <span
            class="text-[9px] text-white/50 truncate w-full text-center leading-tight"
          >
            {{ file.name }}
          </span>
        </div>
        <button
          @click="emit('remove', file)"
          class="absolute top-0.5 right-0.5 w-4 h-4 rounded-full bg-black/60 flex items-center justify-center opacity-0 group-hover/thumb:opacity-100 transition-opacity"
        >
          <X class="w-2.5 h-2.5 text-white" />
        </button>
      </div>

      <button
        @click="emit('addMore')"
        class="w-16 h-16 rounded-lg border border-dashed border-white/20 flex items-center justify-center text-white/30 hover:text-white/60 hover:border-white/40 transition-all shrink-0"
      >
        <Plus class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>
