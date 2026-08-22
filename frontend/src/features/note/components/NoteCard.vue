<script setup lang="ts">
import { ref } from 'vue'

import {
  Pin,
  Pencil,
  Trash2,
  GripHorizontal,
  Palette,
  BellPlus,
  Bell,
  X,
  Archive
} from 'lucide-vue-next'

const COLORS = [
  '#ef4444',
  '#f97316',
  '#eab308',
  '#22c55e',
  '#3b82f6',
  '#8b5cf6',
  '#ec4899'
]

const props = defineProps<{
  note: any
}>()

const emit = defineEmits([
  'view',
  'edit',
  'delete',
  'pin',
  'reminder',
  'color',
  'archive'
])

const showColorPicker = ref(false)

function pickColor(color: string) {
  emit('color', props.note.id, color)
  showColorPicker.value = false
}

function formatReminder(dateStr: string) {
  const d = new Date(dateStr)

  return `${String(d.getHours()).padStart(2, '0')}:${String(
    d.getMinutes()
  ).padStart(2, '0')} • ${d.getDate()}/${d.getMonth() + 1}`
}
</script>

<template>
  <div class="group relative rounded-lg border shadow-sm transition-all hover:shadow-md flex flex-col h-full"
    :style="note.color ? { backgroundColor: note.color + '22', borderColor: note.color + '55' } : {}">
    <!-- Drag handle -->
    <div
      class="drag-handle absolute top-0 left-0 right-0 h-6 rounded-t-lg cursor-grab active:cursor-grabbing flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10 bg-black/5">
      <GripHorizontal :size="13" />
    </div>

    <!-- Content -->
    <div class="p-4 pt-7 flex-1 cursor-pointer no-drag" @click="$emit('view', note)">
      <!-- Pin -->
      <button class="absolute top-7 right-3 opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-black/10"
        @click.stop="$emit('pin', note.id)">
        <Pin :size="14" :class="note.pinned ? 'text-yellow-500 fill-yellow-500' : ''" />
      </button>

      <!-- Title -->
      <h3 class="font-semibold text-sm mb-2 pr-6">
        {{ note.title }}
      </h3>

      <!-- Content -->
      <p v-if="note.note" class="text-xs text-muted-foreground line-clamp-4 whitespace-pre-wrap">
        {{ note.note }}
      </p>
    </div>

    <!-- Toolbar -->
    <div class="no-drag opacity-0 group-hover:opacity-100 border-t px-2 py-1.5 flex justify-between">
      <div class="flex gap-1">

        <!-- Color picker -->
        <div class="relative">
          <button title="Đổi màu" @click.stop="showColorPicker = !showColorPicker"
            class="p-1 rounded hover:bg-black/10 transition-colors">
            <Palette :size="13" />
          </button>

          <!-- Color popover -->
          <div v-if="showColorPicker"
            class="absolute bottom-8 left-0 z-20 flex items-center gap-1.5 rounded-lg border bg-background shadow-lg px-2.5 py-2"
            @click.stop>
            <button v-for="color in COLORS" :key="color"
              class="w-5 h-5 rounded-full border-2 transition-transform hover:scale-110 shrink-0" :style="{
                backgroundColor: color,
                borderColor: note.color === color ? color : 'transparent'
              }" @click.stop="pickColor(color)" />

            <!-- Clear color -->
            <button
              class="w-5 h-5 rounded-full border-2 border-dashed border-border hover:border-muted-foreground flex items-center justify-center shrink-0"
              @click.stop="pickColor('')">
              <X :size="10" class="text-muted-foreground" />
            </button>
          </div>
        </div>

        <!-- Reminder -->
        <button title="Nhắc nhở" @click.stop="$emit('reminder', note)"
          class="p-1 rounded hover:bg-black/10 transition-colors">
          <BellPlus :size="13" :class="note.reminderAt && !note.reminderSent
            ? 'text-blue-500 fill-blue-500'
            : ''" />
        </button>
      </div>

      <!-- RIGHT ACTIONS -->
      <div class="flex gap-1">

        <!-- Archive -->
        <button title="Lưu trữ" @click.stop="$emit('archive', note.id)"
          class="p-1 rounded hover:bg-black/10 transition-colors">
          <Archive :size="13" />
        </button>

        <!-- Edit -->
        <button @click.stop="$emit('edit', note)" class="p-1 rounded hover:bg-black/10 transition-colors">
          <Pencil :size="13" />
        </button>
      </div>
    </div>

    <!-- Reminder badge -->
    <div v-if="note.reminderAt && !note.reminderSent" class="px-3 pb-3">
      <div
        class="inline-flex items-center gap-1 rounded-full bg-blue-500/10 border border-blue-500/20 px-2 py-1 text-[11px] text-blue-600">
        <Bell :size="10" />
        <span>
          {{ formatReminder(note.reminderAt) }}
        </span>
      </div>
    </div>
  </div>
</template>
