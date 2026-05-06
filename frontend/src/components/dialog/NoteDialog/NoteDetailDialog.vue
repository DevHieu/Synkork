<template>
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="$emit('close')" />
          <div class="relative z-10 w-full max-w-lg mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden">
            <!-- Color bar -->
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
              <div class="flex items-center justify-between mt-5 pt-4 border-t border-border/50">
                <div class="text-xs text-muted-foreground space-y-0.5">
                  <p>Tạo lúc: {{ formatDate(note?.createdAt) }}</p>
                  <p>Cập nhật: {{ formatDate(note?.updatedAt) }}</p>
                </div>
                <div class="flex gap-2">
                  <button
                    @click="$emit('delete', note!.id)"
                    class="flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-lg border border-destructive/50 text-destructive hover:bg-destructive/10 transition-colors"
                  >
                    <Trash2 :size="14" />
                    Xóa
                  </button>
                  <button
                    @click="$emit('edit', note!)"
                    class="flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
                  >
                    <Pencil :size="14" />
                    Chỉnh sửa
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
  import { X, Pin, Pencil, Trash2 } from 'lucide-vue-next'
  import type { Note } from '@/types/NoteType'
  
  defineProps<{ open: boolean; note?: Note | null }>()
  defineEmits<{
    close: []
    edit: [note: Note]
    delete: [id: string]
  }>()
  
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
  </style>