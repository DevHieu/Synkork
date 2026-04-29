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
  <div class="toolbar-root">
    <!-- Header -->
    <div class="toolbar-header">
      <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1" />
        <span class="toolbar-title">
          <CalendarDays class="toolbar-title-icon" />
          {{ currentSpaceName }}
        </span>
      </div>

      <!-- View Mode Switcher -->
      <div class="view-switcher">
        <button
          @click="emit('update:viewMode', 'week')"
          :class="['view-btn', viewMode === 'week' ? 'view-btn--active' : '']"
        >
          Tuần
        </button>
        <button
          @click="emit('update:viewMode', 'month')"
          :class="['view-btn', viewMode === 'month' ? 'view-btn--active' : '']"
        >
          Tháng
        </button>
        <button
          @click="emit('update:viewMode', 'year')"
          :class="['view-btn', viewMode === 'year' ? 'view-btn--active' : '']"
        >
          Năm
        </button>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="nav-bar">
      <div class="flex items-center gap-2">
        <button @click="emit('goPrev')" class="nav-arrow-btn" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} trước`">
          <ChevronLeft :size="16" />
        </button>
        <button @click="emit('goToday')" class="today-btn">
          {{ relativeTimeText }}
        </button>
        <button @click="emit('goNext')" class="nav-arrow-btn" :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} sau`">
          <ChevronRight :size="16" />
        </button>
      </div>

      <span class="nav-title">{{ headerTitle }}</span>

      <button @click="emit('openCreateDialog')" class="create-btn">
        <Plus :size="14" />
        Thêm sự kiện
      </button>
    </div>


  </div>
</template>

<style scoped>
/* ── Root ──────────────────────────────────────────────── */
.toolbar-root {
  display: flex;
  flex-direction: column;
  background: transparent;
}

/* ── Header ────────────────────────────────────────────── */
.toolbar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border);
}

.toolbar-title {
  font-weight: 600;
  font-size: 1.125rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--foreground);
}

.toolbar-title-icon {
  display: block;
  flex-shrink: 0;
  color: var(--primary);
  width: 18px;
  height: 18px;
}

/* ── View Switcher ─────────────────────────────────────── */
.view-switcher {
  display: flex;
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid var(--border);
}

.view-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
  color: var(--muted-foreground);
  background: transparent;
  cursor: pointer;
}

.view-btn:hover {
  background: color-mix(in oklch, var(--foreground) 8%, transparent);
}

.view-btn--active {
  background: var(--primary);
  color: var(--primary-foreground);
}

.view-btn--active:hover {
  background: var(--primary);
}

/* ── Navigation Bar ────────────────────────────────────── */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  border-bottom: 1px solid var(--border);
}

.nav-arrow-btn {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  color: var(--muted-foreground);
  background: transparent;
  transition: background 0.15s, color 0.15s;
  cursor: pointer;
}

.nav-arrow-btn:hover {
  background: color-mix(in oklch, var(--foreground) 8%, transparent);
  color: var(--foreground);
}

.today-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 0.5rem;
  min-width: 90px;
  background: color-mix(in oklch, var(--secondary) 20%, transparent);
  color: var(--secondary);
  transition: background 0.15s;
  cursor: pointer;
}

.today-btn:hover {
  background: color-mix(in oklch, var(--secondary) 30%, transparent);
}

.nav-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--foreground);
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 1rem;
  background: var(--primary);
  color: var(--primary-foreground);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: opacity 0.15s;
  cursor: pointer;
}

.create-btn:hover {
  opacity: 0.88;
}


</style>
