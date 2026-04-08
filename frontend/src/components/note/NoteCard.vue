<template>
  <div
    class="group relative rounded-lg border shadow-sm transition-all hover:shadow-md cursor-pointer flex flex-col"
    :style="note.color ? { backgroundColor: note.color + '22', borderColor: note.color + '55' } : {}"
    @click="$emit('view', note)"
  >
    <!-- Main content -->
    <div class="p-4 flex-1">
      <!-- Pin button -->
      <button
        class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity p-1 rounded hover:bg-black/10"
        @click.stop="$emit('pin', note.id)"
        :title="note.pinned ? 'Bỏ ghim' : 'Ghim'"
      >
        <Pin :size="14" :class="note.pinned ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground'" />
      </button>

      <!-- Color dot -->
      <div v-if="note.color" class="absolute top-3 left-3 w-2 h-2 rounded-full" :style="{ backgroundColor: note.color }" />

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
    <div class="opacity-0 group-hover:opacity-100 transition-opacity border-t border-border/50 px-2 py-1.5 flex items-center justify-between">
      <div class="flex items-center gap-0.5">
        <!-- Text format -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Định dạng văn bản"
          @click.stop
        >
          <ALargeSmall :size="13" />
        </button>

        <!-- Color palette -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Màu sắc"
          @click.stop
        >
          <Palette :size="13" />
        </button>

        <!-- Reminder -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Nhắc nhở"
          @click.stop
        >
          <BellPlus :size="13" />
        </button>

        <!-- Collaborator -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Cộng tác"
          @click.stop
        >
          <UserPlus :size="13" />
        </button>

        <!-- Image -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Thêm ảnh"
          @click.stop
        >
          <ImagePlus :size="13" />
        </button>

        <!-- Archive -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Lưu trữ"
          @click.stop
        >
          <Archive :size="13" />
        </button>

        <!-- More -->
        <button
          type="button"
          class="p-1.5 rounded hover:bg-black/10 transition-colors text-muted-foreground hover:text-foreground"
          title="Thêm tùy chọn"
          @click.stop
        >
          <MoreVertical :size="13" />
        </button>
      </div>

      <!-- Edit & Delete -->
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
  Pin, Pencil, Trash2,
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