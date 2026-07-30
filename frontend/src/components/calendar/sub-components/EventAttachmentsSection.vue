<script setup lang="ts">
import { ref, watch } from "vue";
import { X } from "lucide-vue-next";
import type { CalendarEventAttachment } from "@/types/CalendarEvent";

export interface Attachment extends CalendarEventAttachment {
  file?: File;
}

const props = defineProps<{
  initialAttachments?: Attachment[];
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", attachments: Attachment[]): void;
}>();

const attachments = ref<Attachment[]>([...(props.initialAttachments || [])]);

const addFromFileInput = (event: Event): void => {
  const target = event.target as HTMLInputElement;
  if (!target.files) return;
  for (const file of Array.from(target.files)) {
    attachments.value.push({ name: file.name, size: file.size, file });
  }
  target.value = "";
  emit("change", attachments.value);
};

const removeAttachment = (index: number): void => {
  attachments.value.splice(index, 1);
  emit("change", attachments.value);
};

watch(() => props.show, (isOpen) => {
  if (isOpen) attachments.value = [...(props.initialAttachments || [])];
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
            <X class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
