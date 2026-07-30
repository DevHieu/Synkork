<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="$emit('close')" />
        <div class="relative z-10 w-full max-w-lg mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden">
          <div v-if="note?.color" class="h-1.5 w-full" :style="{ backgroundColor: note.color }" />

          <div class="p-6">
            <!-- Header -->
            <div class="flex items-start justify-between mb-4 gap-3">
              <div class="flex items-center gap-2 flex-1 min-w-0">
                <div v-if="note?.color" class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: note.color }" />
                <h2 class="text-lg font-bold leading-tight truncate">{{ note?.title }}</h2>
                <Pin v-if="note?.pinned" :size="14" class="text-yellow-500 fill-yellow-500 shrink-0" />
              </div>
              <button @click="$emit('close')" class="p-1 rounded hover:bg-muted transition-colors shrink-0">
                <X :size="18" />
              </button>
            </div>

            <!-- Content -->
            <div class="min-h-[100px] max-h-[300px] overflow-y-auto">
              <p v-if="note?.note" class="text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                {{ note.note }}
              </p>
              <p v-else class="text-sm text-muted-foreground italic">Không có nội dung</p>
            </div>

            <!-- Footer -->
<div class="mt-5 pt-4 border-t border-border/50 space-y-3">
  <div class="text-xs text-muted-foreground space-y-0.5">
    <p>Tạo lúc: {{ formatDate(note?.createdAt) }}</p>
    <p>Cập nhật: {{ formatDate(note?.updatedAt) }}</p>
  </div>

            <div class="flex items-center gap-2">
              <!-- Save to personal -->
              <button
                v-if="showSavePersonal"
                @click="handleSavePersonal"
                :disabled="savingPersonal"
                title="Lưu vào ghi chú cá nhân"
                class="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 text-sm whitespace-nowrap rounded-lg border border-teal-500/50 text-teal-600 hover:bg-teal-500/10 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <BookmarkPlus :size="14" class="shrink-0" />
                {{ savingPersonal ? 'Đang lưu...' : 'Lưu cá nhân' }}
              </button>

              <button
                @click="$emit('delete', note!.id)"
                class="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 text-sm whitespace-nowrap rounded-lg border border-destructive/50 text-destructive hover:bg-destructive/10 transition-colors"
              >
                <Trash2 :size="14" class="shrink-0" />
                Xóa
              </button>
              <button
                @click="$emit('edit', note!)"
                class="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 text-sm whitespace-nowrap rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
              >
                <Pencil :size="14" class="shrink-0" />
                Chỉnh sửa
              </button>
            </div>
          </div>

            <!-- Feedback -->
            <Transition name="fade">
              <p
                v-if="feedback"
                :class="[
                  'mt-2 text-xs text-right',
                  feedback.type === 'success' ? 'text-teal-600' : 'text-destructive'
                ]"
              >
                {{ feedback.message }}
              </p>
            </Transition>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { X, Pin, Pencil, Trash2, BookmarkPlus } from 'lucide-vue-next'
import type { Note } from '@/types/NoteType'
import { useNoteStore } from '@/stores/noteStore'

// spaceId: id của space đang xem note này (nhóm)
// personalSpaceId: id không gian cá nhân của user hiện tại (nếu có, để so sánh ẩn nút khi trùng)
const props = defineProps<{
  open: boolean
  note?: Note | null
  spaceId: string
  personalSpaceId?: string | null
}>()

const emit = defineEmits<{
  close: []
  edit: [note: Note]
  delete: [id: string]
}>()

const store = useNoteStore()

const savingPersonal = ref(false)
const feedback = ref<{ type: 'success' | 'error'; message: string } | null>(null)

// Ẩn nút nếu đang đứng ngay trong không gian cá nhân của chính mình
const showSavePersonal = computed(() => {
  if (!props.personalSpaceId) return true
  return props.spaceId !== props.personalSpaceId
})

watch(() => props.note?.id, () => {
  feedback.value = null
  savingPersonal.value = false
})

async function handleSavePersonal() {
  if (!props.note?.id || savingPersonal.value) return
  savingPersonal.value = true
  feedback.value = null
  try {
    await store.copyNoteToPersonal(props.spaceId, props.note.id)
    feedback.value = { type: 'success', message: 'Đã lưu vào ghi chú cá nhân' }
  } catch (e) {
    feedback.value = { type: 'error', message: 'Không thể lưu ghi chú, thử lại sau' }
  } finally {
    savingPersonal.value = false
    setTimeout(() => { feedback.value = null }, 2500)
  }
}

function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '---'
  return new Date(dateStr).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
} 
</script>

<style scoped>
.dialog-enter-active, .dialog-leave-active { transition: opacity 0.2s ease; }
.dialog-enter-from, .dialog-leave-to { opacity: 0; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>