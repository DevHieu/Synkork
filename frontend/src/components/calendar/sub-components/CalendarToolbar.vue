<script setup lang="ts">
import { computed } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";

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
  (e: "jumpDate", amount: number, unit: "week" | "month" | "year"): void;
  (e: "openCreateDialog"): void;
}>();

const quickJumpBtnClass = "px-2.5 py-1 text-xs font-medium rounded-md bg-white/5 text-gray-400 hover:bg-teal-600/20 hover:text-teal-400 transition-all border border-transparent hover:border-teal-500/30 whitespace-nowrap cursor-pointer";
</script>

<template>
  <div class="flex flex-col bg-transparent">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-white/10">
      <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1" />
        <span class="font-semibold text-lg flex items-center gap-2">
          <i class="pi pi-calendar text-teal-400"></i>
          {{ currentSpaceName }}
        </span>
      </div>

      <div class="flex items-center gap-2">
        <!-- View Mode Buttons -->
        <div class="flex rounded-lg overflow-hidden border border-white/20">
          <button @click="emit('update:viewMode', 'week')" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'week' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Tuần
          </button>
          <button @click="emit('update:viewMode', 'month')" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'month' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Tháng
          </button>
          <button @click="emit('update:viewMode', 'year')" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'year' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Năm
          </button>
        </div>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="flex items-center justify-between px-4 py-2 border-b border-white/10">
      <div class="flex items-center gap-2">
        <button @click="emit('goPrev')"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300">
          <i class="pi pi-chevron-left"></i>
        </button>
        <button @click="emit('goToday')"
          class="px-3 py-1.5 text-sm rounded-lg bg-teal-600/20 text-teal-400 hover:bg-teal-600/30 transition-colors font-medium min-w-[90px]">
          {{ relativeTimeText }}
        </button>
        <button @click="emit('goNext')"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300">
          <i class="pi pi-chevron-right"></i>
        </button>
      </div>
      <span class="text-lg font-semibold text-white">{{ headerTitle }}</span>
      <button @click="emit('openCreateDialog')"
        class="px-4 py-1.5 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors text-sm font-medium flex items-center gap-1.5">
        <i class="pi pi-plus"></i>
        Thêm sự kiện
      </button>
    </div>

    <!-- Quick Navigation Bar -->
    <div class="flex items-center gap-4 px-4 py-2 border-b border-white/10 bg-black/10 overflow-x-auto no-scrollbar scroll-smooth">
      <!-- Week Group -->
      <div class="flex items-center gap-1.5 shrink-0 px-2 py-1 rounded-lg bg-teal-500/5 border border-teal-500/10">
        <span class="text-[10px] font-bold text-teal-500/70 uppercase tracking-widest mr-1">Tuần</span>
        <div class="flex items-center gap-1">
          <button @click="emit('jumpDate', -1, 'week')" :class="quickJumpBtnClass">Tuần trước</button>
          <button @click="emit('jumpDate', 1, 'week')" :class="quickJumpBtnClass">Tuần sau</button>
          <button @click="emit('jumpDate', 2, 'week')" :class="quickJumpBtnClass">2 tuần sau</button>
        </div>
      </div>

      <!-- Month Group -->
      <div class="flex items-center gap-1.5 shrink-0 px-2 py-1 rounded-lg bg-teal-500/5 border border-teal-500/10">
        <span class="text-[10px] font-bold text-teal-500/70 uppercase tracking-widest mr-1">Tháng</span>
        <div class="flex items-center gap-1">
          <button @click="emit('jumpDate', -1, 'month')" :class="quickJumpBtnClass">Tháng trước</button>
          <button @click="emit('jumpDate', 1, 'month')" :class="quickJumpBtnClass">Tháng sau</button>
        </div>
      </div>

      <!-- Year Group -->
      <div class="flex items-center gap-1.5 shrink-0 px-2 py-1 rounded-lg bg-teal-500/5 border border-teal-500/10">
        <span class="text-[10px] font-bold text-teal-500/70 uppercase tracking-widest mr-1">Năm</span>
        <div class="flex items-center gap-1">
          <button @click="emit('jumpDate', -1, 'year')" :class="quickJumpBtnClass">Năm trước</button>
          <button @click="emit('jumpDate', 1, 'year')" :class="quickJumpBtnClass">Năm sau</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
</style>
