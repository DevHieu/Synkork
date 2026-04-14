<!-- components/sidebar/MemberSection.vue -->
<script setup lang="ts">
defineProps<{ label: string; count: number }>();
</script>
<template>
  <div>
    <div
      class="flex items-center justify-between px-3 py-1.5 text-[11px] font-medium uppercase tracking-widest text-muted-foreground"
    >
      <span>{{ label }}</span>
      <span class="text-[10px] text-muted-foreground/60">{{ count }}</span>
    </div>
    <slot />
  </div>
</template>
