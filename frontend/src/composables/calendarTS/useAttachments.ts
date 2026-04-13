import { ref } from "vue";

export interface Attachment {
  name: string;
  size: number;
  file?: File;
}

// Chức năng này để quản lý danh sách file đính kèm
export function useAttachments(initialList: Attachment[] = []) {
  const attachments = ref<Attachment[]>([...initialList]);

  // Thêm file khi người dùng chọn từ máy tính
  const addFromFileInput = (event: Event): void => {
    const target = event.target as HTMLInputElement;
    if (!target.files) return;

    for (const file of Array.from(target.files)) {
      attachments.value.push({ name: file.name, size: file.size, file });
    }

    target.value = "";
  };

  // Xóa file khỏi danh sách theo vị trí
  const removeAttachment = (index: number): void => {
    attachments.value.splice(index, 1);
  };

  // Làm mới danh sách file về trạng thái ban đầu
  const resetAttachments = (list: Attachment[] = []): void => {
    attachments.value = [...list];
  };

  return { attachments, addFromFileInput, removeAttachment, resetAttachments };
}
