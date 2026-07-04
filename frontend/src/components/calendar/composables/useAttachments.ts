import { ref } from "vue";
import type { CalendarEventAttachment } from "@/types/CalendarEvent";

export interface Attachment extends CalendarEventAttachment {
  file?: File;
}

// Quản lý file đính kèm
export function useAttachments(initialList: Attachment[] = []) {
  const attachments = ref<Attachment[]>([...initialList]);

  // Thêm file
  const addFromFileInput = (event: Event): void => {
    const target = event.target as HTMLInputElement;
    if (!target.files) return;

    for (const file of Array.from(target.files)) {
      attachments.value.push({ name: file.name, size: file.size, file });
    }

    target.value = "";
  };

  // Xóa file theo vị trí
  const removeAttachment = (index: number): void => {
    attachments.value.splice(index, 1);
  };

  // Làm mới danh sách file
  const resetAttachments = (list: Attachment[] = []): void => {
    attachments.value = [...list];
  };

  return { attachments, addFromFileInput, removeAttachment, resetAttachments };
}
