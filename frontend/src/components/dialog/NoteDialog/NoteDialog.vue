<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="handleClose" />
        <div class="relative z-10 w-full max-w-lg mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden">
          <div v-if="form.color" class="h-1.5 w-full" :style="{ backgroundColor: form.color }" />
          <div class="p-6">
            <!-- Header -->
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-lg font-semibold">{{ isEdit ? 'Chỉnh sửa ghi chú' : 'Ghi chú mới' }}</h2>
              <button @click="handleClose" class="p-1 rounded hover:bg-muted transition-colors">
                <X :size="18" />
              </button>
            </div>

            <div class="space-y-3">
              <!-- Title -->
              <input v-model="form.title" placeholder="Tiêu đề..."
                class="w-full bg-transparent border-0 border-b border-border pb-2 text-base font-medium placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors" />

              <!-- Content -->
              <textarea v-model="form.note" placeholder="Nội dung ghi chú..." rows="6"
                class="w-full bg-transparent border rounded-lg p-3 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring resize-none transition-colors" />

              <!-- Toolbar -->
              <div class="flex items-center justify-between border rounded-lg px-2 py-1.5 bg-muted/30">
                <div class="flex items-center gap-0.5">
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Định dạng văn bản">
                    <ALargeSmall :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Màu sắc">
                    <Palette :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Nhắc nhở">
                    <BellPlus :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Cộng tác">
                    <UserPlus :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Thêm ảnh">
                    <ImagePlus :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Lưu trữ">
                    <Archive :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Thêm tùy chọn">
                    <MoreVertical :size="16" />
                  </button>
                </div>
                <div class="flex items-center gap-0.5">
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Hoàn tác">
                    <Undo2 :size="16" />
                  </button>
                  <button type="button"
                    class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                    title="Làm lại">
                    <Redo2 :size="16" />
                  </button>
                </div>
              </div>

              <!-- Color picker -->
              <div class="flex items-center gap-2">
                <span class="text-xs text-muted-foreground">Màu:</span>
                <div class="flex gap-1.5">
                  <button v-for="color in COLORS" :key="color"
                    class="w-5 h-5 rounded-full border-2 transition-transform hover:scale-110"
                    :style="{ backgroundColor: color, borderColor: form.color === color ? color : 'transparent' }"
                    @click="form.color = form.color === color ? '' : color" />
                  <button
                    class="w-5 h-5 rounded-full border-2 border-dashed border-border hover:border-muted-foreground transition-colors flex items-center justify-center"
                    @click="form.color = ''">
                    <X :size="10" class="text-muted-foreground" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center justify-between mt-5">
              <label class="flex items-center gap-2 text-sm cursor-pointer select-none">
                <input type="checkbox" v-model="form.pinned" class="rounded" />
                <Pin :size="14" /> Ghim
              </label>
              <div class="flex gap-2">
                <button @click="handleClose"
                  class="px-4 py-2 text-sm rounded-lg border hover:bg-muted transition-colors">
                  Hủy
                </button>
                <button @click="handleSubmit" :disabled="!form.title.trim() || submitting"
                  class="px-4 py-2 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {{ submitting ? 'Đang lưu...' : isEdit ? 'Cập nhật' : 'Tạo mới' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  X, Pin,
  ALargeSmall, Palette, BellPlus, UserPlus,
  ImagePlus, Archive, MoreVertical, Undo2, Redo2
} from 'lucide-vue-next'
import type { Note, NoteRequest } from '@/types/NoteType'
import type { SuggestedNoteDraft } from '@/types/CalendarSuggestion'
import { useNoteStore } from '@/stores/noteStore'

const COLORS = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#3b82f6', '#8b5cf6', '#ec4899']

const props = defineProps<{ spaceId: string; open: boolean; note?: Note | null; draft?: SuggestedNoteDraft | null }>()
const emit = defineEmits<{
  close: []
  // submit: [data: NoteRequest, id?: string]
}>()

const store = useNoteStore()

const isEdit = ref(false)
const submitting = ref(false)
const form = ref<NoteRequest>({ title: '', note: '', color: '', pinned: false })

function syncFormFromProps() {
  if (!props.open) return;

  if (props.note) {
    isEdit.value = true;
    form.value = {
      title: props.note.title,
      note: props.note.note || '',
      color: props.note.color || '',
      pinned: props.note.pinned,
    };
    return;
  }

  if (props.draft) {
    isEdit.value = false;
    form.value = {
      title: props.draft.title,
      note: props.draft.note || '',
      color: props.draft.color || '',
      pinned: props.draft.pinned || false,
    };
    return;
  }

  isEdit.value = false;
  form.value = { title: '', note: '', color: '', pinned: false };
}

watch(
  () => [props.open, props.note, props.draft],
  syncFormFromProps,
  { immediate: true },
);

function handleClose() { emit('close') }

async function handleSubmit() {
  if (!form.value.title.trim()) return
  submitting.value = true
  try {
    const data: NoteRequest = {
      title: form.value.title.trim(),
      note: form.value.note?.trim() ?? '',
      color: form.value.color || '',
      pinned: form.value.pinned ?? false,
    };

    if (props.note?.id) {
      await store.updateNote(props.spaceId, props.note.id, data);
    } else {
      await store.createNote(props.spaceId, data);
    }

    emit('close');
  } finally {
    submitting.value = false
  }



}
</script>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}
</style>