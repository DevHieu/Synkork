import { useCalendarDate } from "./useCalendarDate";
import { useCalendarEvents } from "./useCalendarEvents";
import { useCalendarRealtime } from "./useCalendarRealtime";
import type { CalendarEvent } from "@/types/CalendarEvent";

export type { CalendarEvent };

// Điều phối logic lịch, event & realtime
export function useCalendar(spaceIdRef: any, currentUserId: any) {
  // Trạng thái ngày giờ
  const calendarDate = useCalendarDate();

  // Thao tác CRUD event
  const calendarEvents = useCalendarEvents(
    spaceIdRef,
    currentUserId,
    calendarDate.currentDate,
    calendarDate.viewMode
  );

  // Realtime update
  useCalendarRealtime(spaceIdRef, calendarEvents.events, calendarEvents.fetchEvents);

  return {
    ...calendarDate,
    ...calendarEvents,
  };
}
