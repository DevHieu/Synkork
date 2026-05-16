<script setup lang="ts">

import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Plus,
} from "lucide-vue-next";

const props = defineProps<{
  currentSpaceName?: string;
  viewMode: string;
  headerTitle: string;
  relativeTimeText: string;
}>();

const emit = defineEmits<{
  (e: "update:viewMode", mode: string): void;
  (e: "goPrev"): void;
  (e: "goNext"): void;
  (e: "goToday"): void;

  (e: "openCreateDialog"): void;
}>();
</script>

<template>
  <div class="flex flex-col border-b-2 border-border bg-background">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b-2 border-border">
      <div class="flex items-center gap-4">
        <SidebarTrigger class="-ml-1 rounded-none border-2 border-primary text-primary hover:bg-primary hover:text-primary-foreground" />
        <span class="font-mono text-lg font-bold uppercase tracking-widest flex items-center gap-2 text-foreground">
          <CalendarDays class="w-5 h-5 text-primary" />
          {{ currentSpaceName }}
        </span>
      </div>

      <!-- View Mode Switcher -->
      <div class="flex border-2 border-border bg-background">
        <button
          @click="emit('update:viewMode', 'week')"
          :class="['px-4 py-1 text-xs font-mono uppercase tracking-wider font-bold transition-colors border-r-2 border-border', viewMode === 'week' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-muted-foreground hover:text-foreground']"
        >
          Tuần
        </button>
        <button
          @click="emit('update:viewMode', 'month')"
          :class="['px-4 py-1 text-xs font-mono uppercase tracking-wider font-bold transition-colors border-r-2 border-border', viewMode === 'month' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-muted-foreground hover:text-foreground']"
        >
          Tháng
        </button>
        <button
          @click="emit('update:viewMode', 'year')"
          :class="['px-4 py-1 text-xs font-mono uppercase tracking-wider font-bold transition-colors', viewMode === 'year' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-muted-foreground hover:text-foreground']"
        >
          Năm
        </button>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="flex items-center justify-between px-4 py-2 bg-muted/30">
      <div class="flex items-center gap-1">
        <button @click="emit('goPrev')" class="w-8 h-8 flex items-center justify-center border-2 border-border text-muted-foreground hover:border-primary hover:bg-primary hover:text-primary-foreground transition-colors" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} trước`">
          <ChevronLeft :size="16" />
        </button>
        <button @click="emit('goToday')" class="px-4 py-1 h-8 flex items-center justify-center border-2 border-border font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground hover:border-primary hover:bg-primary hover:text-primary-foreground transition-colors">
          {{ relativeTimeText }}
        </button>
        <button @click="emit('goNext')" class="w-8 h-8 flex items-center justify-center border-2 border-border text-muted-foreground hover:border-primary hover:bg-primary hover:text-primary-foreground transition-colors" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} sau`">
          <ChevronRight :size="16" />
        </button>
      </div>

      <span class="font-mono text-sm font-bold uppercase tracking-widest text-primary">{{ headerTitle }}</span>

      <button @click="emit('openCreateDialog')" class="flex items-center gap-2 px-4 py-1 h-8 bg-primary text-primary-foreground border-2 border-primary font-mono text-xs font-bold uppercase tracking-wider hover:bg-background hover:text-primary transition-colors">
        <Plus :size="14" />
        Thêm sự kiện
      </button>
    </div>
  </div>
</template>

<style scoped>
/* Industrial / Utilitarian styling applied mostly via Tailwind classes. */
</style>
