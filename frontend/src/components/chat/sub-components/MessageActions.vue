<script setup lang="ts">
import { Pin, Trash2, Reply, Pen } from "lucide-vue-next";

const emits = defineEmits(["reply", "edit", "delete", "pin"]);

const props = defineProps<{
  isSender: boolean;
  isPinned: boolean;
}>();
</script>

<template>
  <div
    class="absolute right-2 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity"
  >
    <!-- Action buttons -->
    <div
      class="flex items-center gap-1 bg-secondary border border-border rounded-lg px-1 py-1 shadow-md"
    >
      <button
        class="p-1.5 rounded hover:bg-primary/20 text-foreground/70 hover:text-foreground transition-colors"
        title="Reply"
        @click="$emit('reply')"
      >
        <Reply class="w-4 h-4" />
      </button>
      <button
        class="p-1.5 rounded transition-colors"
        :class="
          isPinned
            ? 'text-yellow-400 hover:bg-yellow-400/10'
            : 'text-foreground/70 hover:bg-foreground/10 hover:text-foreground'
        "
        :title="isPinned ? 'Bỏ ghim' : 'Ghim'"
        @click="$emit('pin')"
      >
        <Pin class="w-4 h-4" :class="isPinned ? 'fill-yellow-400' : ''" />
      </button>
      <button
        v-if="props.isSender"
        class="p-1.5 rounded hover:bg-foreground/10 text-foreground/70 hover:text-foreground transition-colors"
        title="Sửa"
        @click="$emit('edit')"
      >
        <Pen class="w-4 h-4" />
      </button>
      <button
        v-if="props.isSender"
        class="p-1.5 rounded hover:bg-red-500/20 text-foreground/70 hover:text-red-400 transition-colors"
        title="Xóa"
        @click="$emit('delete')"
      >
        <Trash2 class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>
