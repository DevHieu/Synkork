<template>
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="$emit('close')" />
          <div class="relative z-10 w-full max-w-lg mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden max-h-[80vh] flex flex-col">
            <div class="p-4 border-b flex items-center justify-between shrink-0">
              <h2 class="text-lg font-semibold flex items-center gap-2">
                <Archive :size="18" />
                Ghi chú đã lưu trữ
              </h2>
              <button @click="$emit('close')" class="p-1 rounded hover:bg-muted transition-colors">
                <X :size="18" />
              </button>
            </div>
  
            <div class="overflow-y-auto p-4 space-y-2 flex-1">
              <div v-if="loading" class="text-center py-10 text-muted-foreground">
                <Loader2 class="animate-spin mx-auto mb-2" :size="20" />
                Đang tải...
              </div>
  
              <div v-else-if="notes.length === 0" class="text-center py-10 text-sm text-muted-foreground">
                Không có ghi chú nào đã lưu trữ
              </div>
  
              <div
                v-for="note in notes"
                :key="note.id"
                class="flex items-start justify-between gap-3 p-3 rounded-lg border hover:bg-muted/40 transition-colors"
              >
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-2">
                    <div v-if="note.color" class="w-2 h-2 rounded-full shrink-0" :style="{ backgroundColor: note.color }" />
                    <h3 class="text-sm font-medium truncate">{{ note.title }}</h3>
                  </div>
                  <p v-if="note.note" class="text-xs text-muted-foreground line-clamp-2 mt-1">
                    {{ note.note }}
                  </p>
                </div>
  
                <button
                  @click="handleRestore(note.id)"
                  :disabled="restoringId === note.id"
                  title="Khôi phục"
                  class="shrink-0 flex items-center gap-1 px-2.5 py-1.5 text-xs rounded-md border border-teal-500/50 text-teal-600 hover:bg-teal-500/10 disabled:opacity-50 transition-colors"
                >
                  <Undo2 :size="12" />
                  {{ restoringId === note.id ? 'Đang khôi phục...' : 'Khôi phục' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </template>
  
  <script setup lang="ts">
  import { ref, watch } from 'vue'
  import { X, Archive, Loader2, Undo2 } from 'lucide-vue-next'
  import { useNoteStore } from '@/stores/noteStore'
  import { storeToRefs } from 'pinia'
  
  const props = defineProps<{
    open: boolean
    spaceId: string
  }>()
  
  defineEmits<{ close: [] }>()
  
  const store = useNoteStore()
  const { archivedNotes: notes, loadingArchived: loading } = storeToRefs(store)
  
  const restoringId = ref<string | null>(null)
  
  watch(
    () => props.open,
    (isOpen) => {
      if (isOpen) {
        store.fetchArchivedNotes(props.spaceId)
      }
    }
  )
  
  async function handleRestore(id: string) {
    restoringId.value = id
    try {
      await store.restoreNote(props.spaceId, id)
    } finally {
      restoringId.value = null
    }
  }
  </script>
  
  <style scoped>
  .dialog-enter-active, .dialog-leave-active { transition: opacity 0.2s ease; }
  .dialog-enter-from, .dialog-leave-to { opacity: 0; }
  </style>