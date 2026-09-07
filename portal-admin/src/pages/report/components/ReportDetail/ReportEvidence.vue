<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { ZoomIn, PlayCircle } from '@lucide/vue'

import {
  Dialog,
  DialogContent,
} from '@/components/ui/dialog'

const props = defineProps<{
  src: string
  open: boolean
  name?: string
  resourceType?: string // 'image' | 'video'
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

// Nhận diện video dựa vào đuôi file trong src (hoặc name nếu có)
const VIDEO_EXTENSIONS = ['mp4', 'webm', 'ogg', 'mov', 'avi', 'mkv', '3gp']

function getExtension(value?: string) {
  if (!value) return ''
  const clean = value.split('?')[0].split('#')[0]
  const parts = clean.split('.')
  return parts.length > 1 ? parts.pop()!.toLowerCase() : ''
}

const isVideo = computed(() => {
  if (props.resourceType) return props.resourceType === 'video'
  const ext = getExtension(props.name) || getExtension(props.src)
  return VIDEO_EXTENSIONS.includes(ext)
})

const close = () => emit('update:open', false)

const handleDownload = async () => {
  if (!props.src) return

  const res = await fetch(props.src)
  const blob = await res.blob()

  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = props.name ?? (isVideo.value ? 'video' : 'image')
  a.click()

  URL.revokeObjectURL(url)
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape')
    close()
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <div class="space-y-1.5">
    <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
      {{ isVideo ? 'Video minh chứng' : 'Hình ảnh minh chứng' }}
    </p>

    <button
      type="button"
      class="group relative block w-full overflow-hidden rounded-lg border bg-muted/30 cursor-zoom-in transition-shadow hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      @click="emit('update:open', true)"
    >
      <video
        v-if="isVideo"
        :src="src"
        muted
        class="max-h-64 w-full object-contain transition-transform duration-300 ease-out group-hover:scale-[1.03]"
      />
      <img
        v-else
        :src="src"
        alt="Hình ảnh vi phạm"
        class="max-h-64 w-full object-contain transition-transform duration-300 ease-out group-hover:scale-[1.03]"
      >

      <div class="absolute inset-0 flex items-center justify-center bg-black/0 transition-colors duration-200 group-hover:bg-black/40">
        <div class="flex items-center gap-1.5 rounded-full bg-black/70 px-3 py-1.5 text-xs font-medium text-white opacity-0 scale-95 transition-all duration-200 group-hover:opacity-100 group-hover:scale-100">
          <PlayCircle v-if="isVideo" class="h-3.5 w-3.5" />
          <ZoomIn v-else class="h-3.5 w-3.5" />
          {{ isVideo ? 'Xem video gốc' : 'Xem ảnh gốc' }}
        </div>
      </div>
    </button>

    <Dialog
      :open="open"
      @update:open="emit('update:open', $event)"
    >
      <DialogContent
        :show-close-button="false"
        class="!w-[65vw] !max-w-[65vw] p-0 border-0 bg-transparent shadow-none"
      >
        <div class="relative w-full rounded-xl overflow-hidden bg-black/60">

          <div
            class="absolute top-0 left-0 right-0 z-10 flex items-center justify-between px-4 py-3"
            style="background: linear-gradient(to bottom, rgba(0,0,0,.65), transparent);"
          >
            <span class="text-white/80 text-sm font-medium truncate max-w-xs">
              {{ name ?? (isVideo ? 'video' : 'image') }}
            </span>

            <div class="flex items-center gap-2">
              <button
                class="flex items-center justify-center w-8 h-8 rounded-md bg-white/10 hover:bg-white/20 transition-colors text-white"
                @click="handleDownload"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
              </button>

              <button
                class="flex items-center justify-center w-8 h-8 rounded-md bg-white/10 hover:bg-white/20 transition-colors text-white"
                @click="close"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>

          <div class="flex items-center justify-center p-2 pt-14 pb-4">
            <video
              v-if="isVideo"
              :src="src"
              controls
              autoplay
              class="max-h-[80vh] max-w-full rounded-lg select-none"
            />
            <img
              v-else
              :src="src"
              :alt="name"
              class="max-h-[80vh] max-w-full rounded-lg object-contain select-none"
              draggable="false"
            >
          </div>

        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>