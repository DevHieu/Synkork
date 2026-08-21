<script setup lang="ts">
import { computed } from "vue";
import { Download, X } from "lucide-vue-next";

import { Dialog, DialogContent } from "@/components/ui/dialog";

const props = defineProps<{
  open: boolean;
  src: string;
  name?: string;
  resourceType?: "image" | "video";
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
}>();

const VIDEO_EXTENSIONS = ["mp4", "webm", "ogg", "mov", "avi", "mkv", "3gp"];

function getExtension(value?: string) {
  if (!value) return "";

  const clean = value.split("?")[0].split("#")[0];
  const parts = clean.split(".");

  return parts.length > 1 ? parts.pop()?.toLowerCase() ?? "" : "";
}

const isVideo = computed(() => {
  if (props.resourceType) return props.resourceType === "video";

  const ext = getExtension(props.name) || getExtension(props.src);
  return VIDEO_EXTENSIONS.includes(ext);
});

const mediaName = computed(() => props.name ?? (isVideo.value ? "video" : "image"));

const close = () => emit("update:open", false);

const handleDownload = async () => {
  if (!props.src) return;

  const res = await fetch(props.src);
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");

  a.href = url;
  a.download = mediaName.value;
  a.click();

  URL.revokeObjectURL(url);
};
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent :show-close-button="false"
      class="!w-[min(92vw,960px)] !max-w-[min(92vw,960px)] border-0 bg-transparent p-0 shadow-none">
      <div class="relative w-full overflow-hidden rounded-xl bg-black/80">
        <div class="absolute left-0 right-0 top-0 z-10 flex items-center justify-between px-4 py-3"
          style="background: linear-gradient(to bottom, rgba(0, 0, 0, 0.68), transparent)">
          <span class="max-w-[70%] truncate text-sm font-medium text-white/85">
            {{ mediaName }}
          </span>

          <div class="flex items-center gap-2">
            <button type="button"
              class="flex h-8 w-8 items-center justify-center rounded-md bg-white/10 text-white transition-colors hover:bg-white/20"
              title="Tai xuong" @click="handleDownload">
              <Download class="h-4 w-4" />
            </button>

            <button type="button"
              class="flex h-8 w-8 items-center justify-center rounded-md bg-white/10 text-white transition-colors hover:bg-white/20"
              title="Dong" @click="close">
              <X class="h-4 w-4" />
            </button>
          </div>
        </div>

        <div class="flex max-h-[86vh] min-h-48 items-center justify-center p-2 pt-14">
          <video v-if="isVideo" :src="src" controls autoplay playsinline
            class="max-h-[78vh] max-w-full select-none rounded-lg" />

          <img v-else :src="src" :alt="mediaName" class="max-h-[78vh] max-w-full select-none rounded-lg object-contain"
            draggable="false">
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
