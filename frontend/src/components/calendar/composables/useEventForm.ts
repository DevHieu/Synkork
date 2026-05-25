import { ref } from "vue";
import type { CalendarEventAttachment } from "@/types/CalendarEvent";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { toMinutes } from "@/components/calendar/composables/useTimeSelector";

dayjs.locale("vi");

export interface EventFormData {
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  recurrenceType?: string;
  recurrenceEndDate?: string;
  allowEditAll: boolean;
  attendees?: string[];
  attachments?: CalendarEventAttachment[];
}

// Quản lý data/validate form
export function useEventForm(
  initialData: EventFormData,
  isEditing: boolean,
) {
  const formData = ref<EventFormData>({ ...initialData });

  const warningMessage = ref("");
  const showWarning = ref(false);

  // Hiện cảnh báo validation
  const showValidationWarning = (message: string): void => {
    warningMessage.value = message;
    showWarning.value = true;
  };

  const isEndTimeAfterStartTime = (): boolean =>
    toMinutes(formData.value.endTime) > toMinutes(formData.value.startTime);

  const isEventInFuture = (): boolean =>
    dayjs(`${formData.value.eventDate}T${formData.value.startTime}`).isAfter(dayjs());

  // Validate form
  const validate = (): boolean => {
    if (!formData.value.title.trim()) return false;

    if (!isEndTimeAfterStartTime()) {
      showValidationWarning("Giờ kết thúc phải sau giờ bắt đầu! Vui lòng chọn lại thời gian cho phù hợp.");
      return false;
    }

    if (!isEditing && !isEventInFuture()) {
      showValidationWarning("Bạn không thể tạo sự kiện với thời gian nằm ở trong quá khứ! Vui lòng chọn lại ngày và giờ phù hợp.");
      return false;
    }

    return true;
  };

  // Reset form
  const resetForm = (data: EventFormData): void => {
    formData.value = { ...data };
  };

  return {
    formData,
    warningMessage,
    showWarning,
    validate,
    resetForm,
  };
}
