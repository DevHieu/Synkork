<template>
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="conflict" class="fixed inset-0 z-[60] flex items-center justify-center">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" />
          <div class="relative z-10 w-full max-w-md mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden">
            <div class="p-6">
              <div class="flex items-center gap-3 mb-3">
                <div class="p-2 rounded-full bg-amber-500/10">
                  <AlertTriangle :size="20" class="text-amber-500" />
                </div>
                <h2 class="text-base font-semibold">Đã có người chỉnh sửa ghi chú này</h2>
              </div>
  
              <p class="text-sm text-muted-foreground mb-4">
                Nội dung ghi chú đã thay đổi so với lúc bạn mở nó.
                {{ conflict?.type === 'delete'
                  ? 'Bạn không thể xóa phiên bản cũ này.'
                  : 'Bạn có muốn tạo một ghi chú mới để giữ lại nội dung bạn vừa nhập không?' }}
              </p>
  
              <div class="border rounded-lg p-3 bg-muted/30 mb-4">
                <p class="text-xs text-muted-foreground mb-1">Nội dung mới nhất hiện tại:</p>
                <p class="text-sm font-medium truncate">{{ conflict?.currentNote.title }}</p>
                <p v-if="conflict?.currentNote.note" class="text-xs text-muted-foreground line-clamp-2 mt-1">
                  {{ conflict?.currentNote.note }}
                </p>
              </div>
  
              <div class="flex justify-end gap-2">
                <button
                  @click="handleDiscard"
                  class="px-3 py-1.5 text-sm rounded-lg border hover:bg-muted transition-colors"
                >
                  Đóng, bỏ thay đổi
                </button>
  
                <button
                  v-if="conflict?.type === 'update' && conflict?.pendingData"
                  @click="handleCreateNew"
                  :disabled="creating"
                  class="px-3 py-1.5 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 disabled:opacity-50 transition-colors"
                >
                  {{ creating ? 'Đang tạo...' : 'Tạo ghi chú mới với nội dung của tôi' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import { AlertTriangle } from 'lucide-vue-next'
  import { useNoteStore } from '@/features/note/stores/noteStore'
  import { storeToRefs } from 'pinia'
  
  const props = defineProps<{
    spaceId: string
  }>()
  
  const emit = defineEmits<{
    created: [note: any]
  }>()
  
  const store = useNoteStore()
  const { conflict } = storeToRefs(store)
  
  const creating = ref(false)
  
  function handleDiscard() {
    store.clearConflict()
  }
  
  async function handleCreateNew() {
    if (!conflict.value?.pendingData) return
    creating.value = true
    try {
      const newNote = await store.createNote(props.spaceId, conflict.value.pendingData)
      if (newNote) {
        emit('created', newNote)
      }
      store.clearConflict()
    } finally {
      creating.value = false
    }
  }
  </script>
  
  <style scoped>
  .dialog-enter-active, .dialog-leave-active { transition: opacity 0.2s ease; }
  .dialog-enter-from, .dialog-leave-to { opacity: 0; }
  </style>