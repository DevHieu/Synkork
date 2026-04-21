import { ref } from "vue";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

// Quản lý người tham gia
export function useAttendees(initialList: string[] = []) {
  const attendees = ref<string[]>([...initialList]);
  const attendeeInput = ref("");

  // Kiểm tra email hợp lệ
  const isValidEmail = (email: string): boolean => EMAIL_REGEX.test(email);

  // Kiểm tra trùng lặp
  const isDuplicate = (email: string): boolean =>
    attendees.value.includes(email);

  // Thêm người tham gia
  const addAttendee = (): void => {
    const email = attendeeInput.value.trim();
    if (!isValidEmail(email) || isDuplicate(email)) return;
    attendees.value.push(email);
    attendeeInput.value = "";
  };

  // Xóa người tham gia
  const removeAttendee = (index: number): void => {
    attendees.value.splice(index, 1);
  };

  // Làm mới danh sách
  const resetAttendees = (list: string[] = []): void => {
    attendees.value = [...list];
    attendeeInput.value = "";
  };

  return { attendees, attendeeInput, addAttendee, removeAttendee, resetAttendees };
}
