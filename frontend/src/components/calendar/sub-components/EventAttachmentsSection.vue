<script setup lang="ts">
import { watch } from "vue";
import { useAttachments } from "../composables/useAttachments";

const props = defineProps<{
  initialAttachments?: { name: string; size: number; file?: File }[];
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", attachments: { name: string; size: number; file?: File }[]): void;
}>();

const { attachments, addFromFileInput, removeAttachment, resetAttachments } =
  useAttachments(props.initialAttachments || []);

// Đồng bộ với component cha khi danh sách tệp đính kèm thay đổi
watch(attachments, (newList) => {
  emit("change", newList);
}, { deep: true });

// Làm mới khi Dialog mở ra
watch(() => props.show, (isOpen) => {
  if (isOpen) resetAttachments(props.initialAttachments || []);
});
</script>

<template>
  <div>
    <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">Tệp đính kèm</label>
    <div class="flex flex-col gap-2">
      <label
        class="flex justify-center items-center w-full h-20 px-4 transition bg-muted/20 border-2 border-border border-dashed appearance-none cursor-pointer hover:border-primary hover:bg-muted/50 focus:outline-none">
        <span class="flex items-center space-x-2">
          <i class="pi pi-upload text-muted-foreground" />
          <span class="font-mono font-bold text-muted-foreground text-xs uppercase tracking-wider">Nhấn để chọn tệp...</span>
        </span>
        <input type="file" multiple class="hidden" @change="addFromFileInput" />
      </label>
      <div v-if="attachments.length > 0" class="flex flex-col gap-1.5 mt-2">
        <div v-for="(file, idx) in attachments" :key="idx"
          class="flex items-center justify-between bg-background p-2 border-2 border-border text-xs">
          <div class="flex items-center gap-2 truncate">
            <i class="pi pi-file text-primary" />
            <span class="font-mono font-bold text-foreground truncate">{{ file.name }}</span>
          </div>
          <button type="button" @click="removeAttachment(idx)" class="text-muted-foreground hover:text-destructive px-2 shrink-0 transition-colors">
            <i class="pi pi-trash" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
