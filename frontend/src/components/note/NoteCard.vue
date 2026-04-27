<template>
  <div
    class="group relative rounded-lg border shadow-sm transition-all hover:shadow-md flex flex-col h-full"
    :style="note.color ? { backgroundColor: note.color + '22', borderColor: note.color + '55' } : {}"
  >
    <!-- Drag handle - chỉ vùng này kéo được -->
    <div class="drag-handle absolute top-0 left-0 right-0 h-6 rounded-t-lg cursor-grab active:cursor-grabbing flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-10 bg-black/5">
      <GripHorizontal :size="13" class="text-muted-foreground" />
    </div>

    <!-- Main content - click để view, có padding-top để tránh drag handle -->
    <div class="p-4 pt-7 flex-1 cursor-pointer no-drag" @click="$emit('view', note)">
      <!-- Pin button -->
      <button
        class="absolute top-7 right-3 opacity-0 group-hover:opacity-100 transition-opacity p-1 rounded hover:bg-black/10"
        @click.stop="$emit('pin', note.id)"
        :title="note.pinned ? 'Bỏ ghim' : 'Ghim'"
      >
        <Pin :size="14" :class="note.pinned ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground'" />
      </button>

      <!-- Color dot -->
      <div v-if="note.color" class="absolute top-8 left-3 w-2 h-2 rounded-full" :style="{ backgroundColor: note.color }" />

      <!-- Title -->
      <h3 class="font-semibold text-sm leading-tight mb-2 pr-6" :class="note.color ? 'pl-4' : ''">
        {{ note.title }}
      </h3>

      <!-- Content -->
      <p v-if="note.note" class="text-xs text-muted-foreground line-clamp-4 whitespace-pre-wrap">
        {{ note.note }}
      </p>
    </div>

    <!-- Toolbar - hiện khi hover -->
    <div class="no-drag opacity-0 group-hover:opacity-100 transition-opacity border-t border-border/50 px-2 py-1.5 flex items-center justify-between">
      <div class="flex items-center gap-0.5">
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Định dạng văn bản" @click.stop>
          <ALargeSmall :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Màu sắc" @click.stop>
          <Palette :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Nhắc nhở" @click.stop>
          <BellPlus :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Cộng tác" @click.stop>
          <UserPlus :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Thêm ảnh" @click.stop>
          <ImagePlus :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Lưu trữ" @click.stop>
          <Archive :size="13" />
        </button>
        <button type="button" class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground" title="Thêm tùy chọn" @click.stop>
          <MoreVertical :size="13" />
        </button>
      </div>

      <div class="flex items-center gap-0.5">
        <button
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          @click.stop="$emit('edit', note)"
          title="Chỉnh sửa"
        >
          <Pencil :size="13" />
        </button>
        <button
          class="p-1.5 rounded hover:bg-destructive/20 transition-colors text-muted-foreground hover:text-destructive"
          @click.stop="$emit('delete', note.id)"
          title="Xóa"
        >
          <Trash2 :size="13" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Pin, Pencil, Trash2, GripHorizontal,
  ALargeSmall, Palette, BellPlus, UserPlus,
  ImagePlus, Archive, MoreVertical
} from 'lucide-vue-next'
import type { Note } from '@/types/NoteType'

defineProps<{ note: Note }>()
defineEmits<{
  view: [note: Note]
  edit: [note: Note]
  delete: [id: string]
  pin: [id: string]
}>()
</script>