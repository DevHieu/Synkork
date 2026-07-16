<script setup lang="ts">
defineProps<{
  reasonLabel: string
  description?: string
}>()
</script>

<template>
  <div class="space-y-3">
    <div class="space-y-1">
      <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
        Lý do
      </p>
      <p class="text-sm font-semibold">
        {{ reasonLabel }}
      </p>
    </div>

    <div v-if="description" class="space-y-1">
      <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
        Mô tả
      </p>
      <textarea
        :value="description"
        disabled
        rows="3"
        class="w-full text-sm text-foreground leading-relaxed rounded-md bg-muted px-3 py-2 resize-none disabled:opacity-100 disabled:cursor-default overflow-y-auto"
      />
    </div>
  </div>
</template>
