import { ref, watch } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";
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
  attachments?: { name: string; size: number; file?: File }[];
}

// Quản lý data/validate form
export function useEventForm(
  initialData: EventFormData,
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>,
  isEditing: boolean,
  editingEventId?: string
) {
  const formData = ref<EventFormData>({ ...initialData });
  const conflictEvents = ref<CalendarEvent[]>([]);
  const isCheckingConflict = ref(false);

  const warningMessage = ref("");
  const showWarning = ref(false);

  // Hiện cảnh báo validation
  const showValidationWarning = (message: string): void => {
    warningMessage.value = message;
    showWarning.value = true;
  };

  // Check trùng lịch (debounce)
  let conflictDebounce: ReturnType<typeof setTimeout> | null = null;

  const scheduleConflictCheck = (date: string, start: string, end: string): void => {
    if (conflictDebounce) clearTimeout(conflictDebounce);
    conflictDebounce = setTimeout(async () => {
      isCheckingConflict.value = true;
      try {
        conflictEvents.value = await checkConflicts(
          date,
          start,
          end,
          isEditing ? editingEventId : undefined
        );
      } catch {
        conflictEvents.value = [];
      } finally {
        isCheckingConflict.value = false;
      }
    }, 400);
  };

  // Trigger check khi chọn lại thời gian
  watch(
    () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
    ([date, start, end]) => {
      if (!date || !start || !end) { 
        conflictEvents.value = []; 
        return; 
      }
      scheduleConflictCheck(date as string, start as string, end as string);
    }
  );

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
    conflictEvents.value = [];
  };

  return {
    formData,
    conflictEvents,
    isCheckingConflict,
    warningMessage,
    showWarning,
    validate,
    resetForm,
  };
}
