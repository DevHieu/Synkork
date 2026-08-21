<script setup lang="ts">
import { Trash2 } from 'lucide-vue-next'
defineProps<{ open: boolean }>()
defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="$emit('cancel')" />
          <div class="relative z-10 w-full max-w-sm mx-4 bg-background rounded-xl shadow-2xl border p-6">
            <div class="flex items-start gap-3 mb-4">
              <div class="p-2 rounded-full bg-destructive/10">
                <Trash2 :size="18" class="text-destructive" />
              </div>
              <div>
                <h3 class="font-semibold">Xóa ghi chú?</h3>
                <p class="text-sm text-muted-foreground mt-1">Hành động này không thể hoàn tác.</p>
              </div>
            </div>
            <div class="flex justify-end gap-2">
              <button @click="$emit('cancel')" class="px-4 py-2 text-sm rounded-lg border hover:bg-muted transition-colors">
                Hủy
              </button>
              <button @click="$emit('confirm')" class="px-4 py-2 text-sm rounded-lg bg-destructive text-destructive-foreground hover:bg-destructive/90 transition-colors">
                Xóa
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </template>
  
  <style scoped>
  .dialog-enter-active, .dialog-leave-active { transition: opacity 0.15s ease; }
  .dialog-enter-from, .dialog-leave-to { opacity: 0; }
  </style>