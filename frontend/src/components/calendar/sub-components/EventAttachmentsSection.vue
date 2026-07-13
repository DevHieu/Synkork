<script setup lang="ts">
import { watch } from "vue";
import { useAttachments, type Attachment } from "../composables/useAttachments";

const props = defineProps<{
  initialAttachments?: Attachment[];
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", attachments: Attachment[]): void;
}>();

const { attachments, addFromFileInput, removeAttachment, resetAttachments } =
  useAttachments(props.initialAttachments || []);

// Section này chỉ giữ state tạm rồi đẩy ngược lên form cha.
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
    <div class="flex flex-col gap-2">
      <label
        class="flex h-20 w-full cursor-pointer items-center justify-center rounded-md border border-dashed border-border/60 bg-muted/15 px-4 transition hover:border-primary hover:bg-muted/30 focus:outline-none">
        <span class="flex items-center gap-2">
          <i class="pi pi-upload text-muted-foreground" />
          <span class="font-sans font-semibold text-muted-foreground text-xs uppercase tracking-wider">Nhấn để chọn tệp...</span>
        </span>
        <input type="file" multiple class="hidden" @change="addFromFileInput" />
      </label>
      <div v-if="attachments.length > 0" class="flex flex-col gap-1.5 mt-2">
        <div v-for="(file, idx) in attachments" :key="idx"
          class="flex items-center justify-between rounded-md border border-border/60 bg-background p-2.5 text-xs shadow-sm">
          <div class="flex items-center gap-2 truncate">
            <i class="pi pi-file text-primary" />
            <span class="font-sans font-medium text-foreground truncate text-[11px]">{{ file.name }}</span>
          </div>
          <button type="button" @click="removeAttachment(idx)" class="text-muted-foreground hover:text-destructive px-2 shrink-0 transition-colors">
            <i class="pi pi-trash" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
