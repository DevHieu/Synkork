import { useCalendarDate } from "./useCalendarDate";
import { useCalendarEvents } from "./useCalendarEvents";
import { useCalendarRealtime } from "./useCalendarRealtime";
import type { CalendarEvent } from "@/types/CalendarEvent";

export type { CalendarEvent };

export function useCalendar(spaceIdRef: any, currentUserId: any) {
  // 1. Logic về Ngày & Điều hướng
  const calendarDate = useCalendarDate();

  // 2. Logic về Sự kiện (CRUD)
  const calendarEvents = useCalendarEvents(
    spaceIdRef,
    currentUserId,
    calendarDate.currentDate,
    calendarDate.viewMode
  );

  // 3. Đồng bộ hóa thời gian thực (WebSocket)
  useCalendarRealtime(spaceIdRef, calendarEvents.events);

  // Trả về API tổng hợp (Facade)
  return {
    ...calendarDate,
    ...calendarEvents,
  };
}
