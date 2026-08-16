import { ref } from "vue";
import type { CalendarEventAttachment } from "@/types/CalendarEvent";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { toMinutes } from "@/features/calendar/composable/useTimeSelector";

dayjs.locale("vi");

export interface EventFormData {
  title: string;
  description: string;
  eventLink?: string;
  eventDate: string;
  endDate: string;
  startTime: string;
  endTime: string;
  recurrenceType?: string;
  recurrenceEndDate?: string;
  allowEditAll: boolean;
  attendeeIds?: string[];
  attendees?: any[];
  attachments?: (CalendarEventAttachment & { file?: File })[];
  callRoomSpaceId?: string;
  taskSpaceId?: string;
  taskId?: string;
  noteSpaceId?: string;
  noteId?: string;
  version?: number;
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

  const isEndTimeAfterStartTime = (): boolean => {
    const startStr = `${formData.value.eventDate}T${formData.value.startTime}`;
    let endStr = `${formData.value.endDate || formData.value.eventDate}T${formData.value.endTime}`;
    
    const startDt = dayjs(startStr);
    let endDt = dayjs(endStr);

    if (startDt.isValid() && endDt.isValid()) {
      return endDt.isAfter(startDt);
    }
    return false;
  };

  const isEventInFuture = (): boolean =>
    dayjs(`${formData.value.eventDate}T${formData.value.startTime}`).isAfter(dayjs());

  const isValidUrl = (value: string): boolean => {
    try {
      const url = new URL(value);
      return url.protocol === "http:" || url.protocol === "https:";
    } catch {
      return false;
    }
  };

  // Validate form
  const validate = (): boolean => {
    if (!formData.value.title.trim()) return false;

    const eventLink = formData.value.eventLink?.trim();
    if (eventLink && !isValidUrl(eventLink)) {
      showValidationWarning("Link sự kiện không hợp lệ. Vui lòng nhập đúng định dạng URL bắt đầu bằng HTTP:// hoặc HTTPS://.");
      return false;
    }

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

  const resetForm = (data: EventFormData) => {
    formData.value = { ...data };
    warningMessage.value = "";
    showWarning.value = false;
  };

  return {
    formData,
    warningMessage,
    showWarning,
    validate,
    resetForm,
  };
}
