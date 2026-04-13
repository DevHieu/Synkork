import { ref } from "vue";

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

// Chức năng này để quản lý danh sách người tham gia
export function useAttendees(initialList: string[] = []) {
  const attendees = ref<string[]>([...initialList]);
  const attendeeInput = ref("");

  // Kiểm tra email có đúng định dạng không
  const isValidEmail = (email: string): boolean => EMAIL_REGEX.test(email);

  // Kiểm tra email đã có trong danh sách chưa
  const isDuplicate = (email: string): boolean =>
    attendees.value.includes(email);

  // Thêm người tham gia vào danh sách
  const addAttendee = (): void => {
    const email = attendeeInput.value.trim();
    if (!isValidEmail(email) || isDuplicate(email)) return;
    attendees.value.push(email);
    attendeeInput.value = "";
  };

  // Xóa người tham gia khỏi danh sách
  const removeAttendee = (index: number): void => {
    attendees.value.splice(index, 1);
  };

  // Làm mới danh sách người tham gia
  const resetAttendees = (list: string[] = []): void => {
    attendees.value = [...list];
    attendeeInput.value = "";
  };

  return { attendees, attendeeInput, addAttendee, removeAttendee, resetAttendees };
}
