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

// Toolbar chỉ phát sự kiện điều hướng, toàn bộ logic nằm ở layout cha.
</script>

<template>
  <div class="flex flex-col gap-3 bg-transparent px-4 pt-4">
    <!-- Header -->
    <div class="flex items-center justify-between rounded-[1.5rem] border-2 border-border bg-background/95 px-5 py-4 shadow-[0_18px_50px_-32px_var(--color-foreground)]">
      <div class="flex items-center gap-4">
        <SidebarTrigger class="-ml-1 rounded-full border-2 border-primary/40 bg-primary/10 p-2 text-primary transition-colors hover:bg-primary hover:text-primary-foreground" />
        <span class="flex items-center gap-3 font-mono text-lg font-bold uppercase tracking-widest text-foreground">
          <span class="flex size-10 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-[0_10px_24px_-16px_var(--color-primary)]">
            <CalendarDays class="h-5 w-5" />
          </span>
          {{ currentSpaceName }}
        </span>
      </div>

      <!-- View Mode Switcher -->
      <div class="flex rounded-full border-2 border-border bg-muted/40 p-1 shadow-inner">
        <button
          @click="emit('update:viewMode', 'week')"
          :class="['rounded-full px-4 py-2 text-xs font-mono font-bold uppercase tracking-wider transition-all', viewMode === 'week' ? 'bg-primary text-primary-foreground shadow-[0_8px_18px_-12px_var(--color-primary)]' : 'text-muted-foreground hover:bg-background hover:text-foreground']"
        >
          Tuần
        </button>
        <button
          @click="emit('update:viewMode', 'month')"
          :class="['rounded-full px-4 py-2 text-xs font-mono font-bold uppercase tracking-wider transition-all', viewMode === 'month' ? 'bg-primary text-primary-foreground shadow-[0_8px_18px_-12px_var(--color-primary)]' : 'text-muted-foreground hover:bg-background hover:text-foreground']"
        >
          Tháng
        </button>
        <button
          @click="emit('update:viewMode', 'year')"
          :class="['rounded-full px-4 py-2 text-xs font-mono font-bold uppercase tracking-wider transition-all', viewMode === 'year' ? 'bg-primary text-primary-foreground shadow-[0_8px_18px_-12px_var(--color-primary)]' : 'text-muted-foreground hover:bg-background hover:text-foreground']"
        >
          Năm
        </button>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="flex items-center justify-between rounded-[1.5rem] border-2 border-border bg-muted/35 px-5 py-3 shadow-[0_20px_40px_-34px_var(--color-foreground)]">
      <div class="flex items-center gap-2">
        <button @click="emit('goPrev')" class="flex size-10 items-center justify-center rounded-full border-2 border-border bg-background text-muted-foreground transition-all hover:-translate-y-0.5 hover:border-primary hover:bg-primary hover:text-primary-foreground" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} trước`">
          <ChevronLeft :size="16" />
        </button>
        <button @click="emit('goToday')" class="flex h-10 items-center justify-center rounded-full border-2 border-border bg-background px-5 font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground transition-all hover:-translate-y-0.5 hover:border-primary hover:bg-primary hover:text-primary-foreground">
          {{ relativeTimeText }}
        </button>
        <button @click="emit('goNext')" class="flex size-10 items-center justify-center rounded-full border-2 border-border bg-background text-muted-foreground transition-all hover:-translate-y-0.5 hover:border-primary hover:bg-primary hover:text-primary-foreground" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} sau`">
          <ChevronRight :size="16" />
        </button>
      </div>

      <span class="rounded-full border border-primary/20 bg-primary/10 px-4 py-2 font-mono text-sm font-bold uppercase tracking-widest text-primary">{{ headerTitle }}</span>

      <button @click="emit('openCreateDialog')" class="flex h-10 items-center gap-2 rounded-full border-2 border-primary bg-primary px-5 font-mono text-xs font-bold uppercase tracking-wider text-primary-foreground transition-all hover:-translate-y-0.5 hover:bg-background hover:text-primary">
        <Plus :size="14" />
        Thêm sự kiện
      </button>
    </div>
  </div>
</template>

<style scoped>
/* Industrial / Utilitarian styling applied mostly via Tailwind classes. */
</style>
