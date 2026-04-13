import { useCalendarDate } from "./useCalendarDate";
import { useCalendarEvents } from "./useCalendarEvents";
import { useCalendarRealtime } from "./useCalendarRealtime";
import type { CalendarEvent } from "@/types/CalendarEvent";

export type { CalendarEvent };

// Chức năng này để gộp tất cả các xử lý về lịch vào một chỗ
export function useCalendar(spaceIdRef: any, currentUserId: any) {
  // Quản lý việc đổi ngày và xem theo tuần/tháng/năm
  const calendarDate = useCalendarDate();

  // Quản lý thông tin các sự kiện (Thêm, Sửa, Xóa)
  const calendarEvents = useCalendarEvents(
    spaceIdRef,
    currentUserId,
    calendarDate.currentDate,
    calendarDate.viewMode
  );

  // Cập nhật dữ liệu từ xa khi có người khác thay đổi
  useCalendarRealtime(spaceIdRef, calendarEvents.events);

  return {
    ...calendarDate,
    ...calendarEvents,
  };
}
