<script setup lang="ts">
import { SidebarTrigger } from "@/components/ui/sidebar";

const props = defineProps<{
  spaceName: string;
}>();
</script>

<template>
  <div class="flex items-center justify-between px-4 py-3.5 border-b">
    <div class="flex items-center gap-2">
      <SidebarTrigger class="-ml-1" />
      <span class="font-semibold text-lg"># {{ props.spaceName }}</span>
    </div>

    <div class="flex gap-3 text-gray-600">
      <button class="hover:text-black">📞</button>
      <button class="hover:text-black">🎥</button>
    </div>
  </div>
</template>
