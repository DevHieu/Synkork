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
        class="flex h-24 w-full cursor-pointer items-center justify-center rounded-xl border-2 border-dashed border-border bg-muted/20 px-4 transition hover:border-primary hover:bg-muted/50 focus:outline-none">
        <span class="flex items-center gap-2">
          <i class="pi pi-upload text-muted-foreground" />
          <span class="font-mono font-bold text-muted-foreground text-xs uppercase tracking-wider">Nhấn để chọn tệp...</span>
        </span>
        <input type="file" multiple class="hidden" @change="addFromFileInput" />
      </label>
      <div v-if="attachments.length > 0" class="flex flex-col gap-1.5 mt-2">
        <div v-for="(file, idx) in attachments" :key="idx"
          class="flex items-center justify-between rounded-xl border-2 border-border bg-background p-3 text-xs shadow-[0_16px_34px_-30px_var(--color-foreground)]">
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
