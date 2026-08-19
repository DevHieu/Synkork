import dayjs from "dayjs";
import type { CalendarEvent } from "@/features/calendar/types/calendar.types";

export const displayTime = (value?: string) => (value || "").substring(0, 5);

export const formatDateTimeLabel = (
  value: string | undefined,
  fallbackDate: string,
  fallbackTime: string,
) => {
  const dateTime = value ? dayjs(value) : dayjs(`${fallbackDate}T${fallbackTime}`);
  return dateTime.isValid()
    ? `${dateTime.format("HH:mm")} ${dateTime.format("DD/MM")}`
    : displayTime(fallbackTime);
};

export const continuationLabel = (
  event: CalendarEvent,
  full = false,
): string => {
  if (event.continuesFromPreviousDay && event.continuesToNextDay) {
    return full
      ? "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC VÀ TIẾP TỤC"
      : "TIẾP TỤC";
  }
  if (event.continuesFromPreviousDay) {
    return full ? "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC" : "TỪ HÔM TRƯỚC";
  }
  if (event.continuesToNextDay) {
    return full ? "TIẾP TỤC Ở NGÀY HÔM SAU" : "SANG HÔM SAU";
  }
  return "";
};

export const scheduleRanges = (events: CalendarEvent[]) => {
  const ranges: Record<string, { startDate: string; endDate: string }> = {};

  for (const event of events) {
    if (!event.schedule || !event.scheduleId) continue;
    const startDate = event.eventDate;
    const endDate = event.endDate || startDate;
    const range = ranges[event.scheduleId];

    if (!range) {
      ranges[event.scheduleId] = { startDate, endDate };
      continue;
    }

    if (dayjs(startDate).isBefore(dayjs(range.startDate))) range.startDate = startDate;
    if (dayjs(endDate).isAfter(dayjs(range.endDate))) range.endDate = endDate;
    if (dayjs(startDate).isAfter(dayjs(range.endDate))) range.endDate = startDate;
  }

  return ranges;
};
