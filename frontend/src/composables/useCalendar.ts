import { useCalendarDate } from "./useCalendarDate";
import { useCalendarEvents } from "./useCalendarEvents";
import { useCalendarRealtime } from "./useCalendarRealtime";
import type { CalendarEvent } from "@/types/CalendarEvent";

export type { CalendarEvent };

export function useCalendar(spaceIdRef: any, currentUserId: string) {
  // 1. Date & Navigation Logic
  const calendarDate = useCalendarDate();

  // 2. Events & CRUD Logic
  const calendarEvents = useCalendarEvents(
    spaceIdRef,
    currentUserId,
    calendarDate.currentDate,
    calendarDate.viewMode
  );

  // 3. Real-time Synchronization
  useCalendarRealtime(spaceIdRef, calendarEvents.events);

  // Return the combined API (Facade)
  return {
    ...calendarDate,
    ...calendarEvents,
  };
}
