<script setup lang="ts">
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  show: boolean;
  eventToDelete: CalendarEvent | null;
  isDeletingEvent: boolean;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "executeDelete"): void;
}>();
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="emit('update:show', false)"></div>

      <!-- Content -->
      <div class="relative bg-card rounded-xl border border-destructive/20 w-full max-w-sm mx-4 p-5 shadow-2xl">
        <h3 class="text-lg font-semibold text-foreground mb-2 flex items-center gap-2">
          <span class="text-destructive"><i class="pi pi-trash"></i></span>
          Xóa Sự Kiện
        </h3>

        <p class="text-sm text-muted-foreground mb-6">
          Bạn có chắc chắn muốn xóa sự kiện "<span class="text-foreground font-medium">{{ eventToDelete?.title }}</span>" không?
          Hành động này không thể hoàn tác.
        </p>

        <div class="flex justify-end gap-2">
          <button type="button" @click="emit('update:show', false)"
            class="px-4 py-2 rounded-lg text-sm text-muted-foreground hover:bg-muted transition-colors"
            :disabled="isDeletingEvent">
            Hủy
          </button>
          <button @click="emit('executeDelete')"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-destructive text-destructive-foreground hover:bg-destructive/80 transition-colors disabled:opacity-50 flex items-center gap-2"
            :disabled="isDeletingEvent">
            <span v-if="isDeletingEvent"
              class="w-4 h-4 rounded-full border-2 border-destructive-foreground/30 border-t-destructive-foreground animate-spin"></span>
            Xoá sự kiện
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
