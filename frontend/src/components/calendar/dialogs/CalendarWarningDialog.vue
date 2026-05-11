<script setup lang="ts">
const props = defineProps<{
  show: boolean;
  message: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
}>();
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-[60] flex items-center justify-center">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="emit('update:show', false)"></div>

      <!-- Content -->
      <div class="relative bg-card rounded-xl border border-accent/20 w-full max-w-sm mx-4 p-5 shadow-2xl">
        <h3 class="text-lg font-semibold text-foreground mb-2 flex items-center gap-2">
          <span class="text-accent"><i class="pi pi-exclamation-triangle"></i></span>
          Cảnh Báo
        </h3>

        <p class="text-sm text-foreground mb-6 leading-relaxed">
          {{ message }}
        </p>

        <div class="flex justify-end">
          <button @click="emit('update:show', false)"
            class="px-4 py-2 rounded-lg text-sm font-bold bg-accent text-accent-foreground hover:bg-accent/80 transition-colors flex items-center gap-2 active:scale-95 shadow-lg shadow-accent/20">
            Đã hiểu
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
