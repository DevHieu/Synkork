<script setup lang="ts">
import { onMounted, onUnmounted } from "vue";

const props = defineProps<{
  open: boolean;
  src: string;
  name?: string;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
}>();

const close = () => emit("update:open", false);

const handleDownload = async () => {
  if (!props.src) return;
  const res = await fetch(props.src);
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = props.name ?? "image";
  a.click();
  URL.revokeObjectURL(url);
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Escape") close();
};

onMounted(() => window.addEventListener("keydown", handleKeydown));
onUnmounted(() => window.removeEventListener("keydown", handleKeydown));
</script>

<template>
  <Teleport to="body">
    <Transition name="viewer-fade">
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm"
        @click.self="close"
      >
        <div
          class="relative w-full max-w-4xl mx-4 rounded-xl overflow-hidden bg-black/60"
        >
          <!-- Header -->
          <div
            class="absolute top-0 left-0 right-0 z-10 flex items-center justify-between px-4 py-3"
            style="
              background: linear-gradient(
                to bottom,
                rgba(0, 0, 0, 0.65),
                transparent
              );
            "
          >
            <span class="text-white/80 text-sm font-medium truncate max-w-xs">
              {{ name ?? "image" }}
            </span>
            <div class="flex items-center gap-2">
              <button
                @click="handleDownload"
                class="flex items-center justify-center w-8 h-8 rounded-md bg-white/10 hover:bg-white/20 transition-colors text-white"
                title="Tải xuống"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
              </button>
              <button
                @click="close"
                class="flex items-center justify-center w-8 h-8 rounded-md bg-white/10 hover:bg-white/20 transition-colors text-white"
                title="Đóng"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Image -->
          <div class="flex items-center justify-center p-2 pt-14 pb-4">
            <img
              :src="src"
              :alt="name"
              class="max-h-[80vh] max-w-full rounded-lg object-contain select-none"
              draggable="false"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.viewer-fade-enter-active,
.viewer-fade-leave-active {
  transition: opacity 0.2s ease;
}
.viewer-fade-enter-from,
.viewer-fade-leave-to {
  opacity: 0;
}
</style>
