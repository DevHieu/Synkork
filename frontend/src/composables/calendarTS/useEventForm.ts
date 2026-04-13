import { ref, watch, computed } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { toMinutes } from "@/composables/calendarTS/useTimeSelector";

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

// Chức năng này để quản lý thông tin sự kiện trong form
export function useEventForm(
  initialData: EventFormData,
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>,
  isEditing: boolean,
  editingEventId?: string
) {
  const formData = ref<EventFormData>({ ...initialData });
  const conflictEvents = ref<CalendarEvent[]>([]);
  const isCheckingConflict = ref(false);

  // Hiển thị thông báo khi có lỗi nhập liệu
  const warningMessage = ref("");
  const showWarning = ref(false);

  const showValidationWarning = (message: string): void => {
    warningMessage.value = message;
    showWarning.value = true;
  };

  // Tạo lời giải thích ngắn gọn về cách lặp lại sự kiện
  const recurrenceSummary = computed((): string => {
    const type = formData.value.recurrenceType;
    if (!type || type === "NONE") return "";

    const date = dayjs(formData.value.eventDate);

    const baseText: Record<string, string> = {
      DAILY:   "Sự kiện sẽ lặp lại vào mỗi ngày.",
      WEEKLY:  `Sự kiện sẽ lặp lại vào mỗi thứ ${date.format("dddd")} hàng tuần.`,
      MONTHLY: `Sự kiện sẽ lặp lại vào ngày ${date.date()} hàng tháng.`,
      YEARLY:  `Sự kiện sẽ lặp lại vào ngày ${date.format("DD [tháng] MM")} hàng năm.`,
    };

    const suffix = formData.value.recurrenceEndDate
      ? ` Kết thúc vào ngày ${dayjs(formData.value.recurrenceEndDate).format("DD/MM/YYYY")}.`
      : " Tiếp diễn trong vòng 1 năm tiếp theo.";

    return (baseText[type] ?? "") + suffix;
  });

  // Kiểm tra xem thời gian có bị trùng với sự kiện khác không
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

  // Tự động kiểm tra trùng lịch khi thay đổi ngày hoặc giờ
  watch(
    () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
    ([date, start, end]) => {
      if (!date || !start || !end) { conflictEvents.value = []; return; }
      scheduleConflictCheck(date as string, start as string, end as string);
    }
  );

  // Kiểm tra các quy tắc khi lưu sự kiện
  const isEndTimeAfterStartTime = (): boolean =>
    toMinutes(formData.value.endTime) > toMinutes(formData.value.startTime);

  const isEventInFuture = (): boolean =>
    dayjs(`${formData.value.eventDate}T${formData.value.startTime}`).isAfter(dayjs());

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

  // Làm mới form dữ liệu về mặc định
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
    recurrenceSummary,
    validate,
    resetForm,
  };
}
