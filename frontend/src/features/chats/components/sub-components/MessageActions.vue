<script setup lang="ts">
import { Pin, Trash2, Reply, Pen, Sparkles } from "lucide-vue-next";

const emits = defineEmits(["reply", "edit", "delete", "pin", "suggest"]);

const props = defineProps<{
  isOwnMessage: boolean;
  fullAction: boolean;
  isPinned: boolean;
  showSuggestion: boolean;
  suggestionLabel?: string;
}>();
</script>

<template>
  <div>
    <!-- Action buttons -->
    <div class="flex items-center gap-1 bg-secondary border border-border rounded-lg px-1 py-1 shadow-md">
      <button v-if="props.showSuggestion"
        class="rounded bg-primary px-2.5 py-1.5 text-primary-foreground shadow-sm transition-colors hover:bg-primary/90"
        :title="'Tạo nhanh từ tin nhắn'" @click="$emit('suggest')">
        <span class="flex items-center gap-1.5 text-xs font-semibold">
          <Sparkles class="h-4 w-4" />
          {{ props.suggestionLabel ?? 'Tạo nhanh' }}
        </span>
      </button>
      <button class="p-1.5 rounded hover:bg-primary/20 text-white/70 hover:text-foreground transition-colors"
        title="Reply" @click="$emit('reply')">
        <Reply class="w-4 h-4" />
      </button>
      <button class="p-1.5 rounded transition-colors" :class="isPinned
        ? 'text-yellow-400 hover:bg-yellow-400/10'
        : 'text-white/70 hover:bg-foreground/10 hover:text-foreground'
        " :title="isPinned ? 'Bỏ ghim' : 'Ghim'" @click="$emit('pin')">
        <Pin class="w-4 h-4" :class="isPinned ? 'fill-yellow-400' : ''" />
      </button>
      <button v-if="props.isOwnMessage"
        class="p-1.5 rounded hover:bg-foreground/10 text-white/70 hover:text-foreground transition-colors" title="Sửa"
        @click="$emit('edit')">
        <Pen class="w-4 h-4" />
      </button>
      <button v-if="props.fullAction"
        class="p-1.5 rounded hover:bg-red-500/20 text-white/70 hover:text-red-400 transition-colors" title="Xóa"
        @click="$emit('delete')">
        <Trash2 class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>
