import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { EventFormData } from "@/features/calendar/composable/useEventForm";

export const createInitialFormData = (
  overrides: Partial<EventFormData> = {},
): EventFormData => ({
  title: "",
  description: "",
  eventLink: "",
  eventDate: "",
  endDate: "",
  startTime: "09:00",
  endTime: "10:00",
  recurrenceType: "NONE",
  recurrenceEndDate: undefined,
  allowEditAll: false,
  attendees: [],
  attachments: [],
  callRoomSpaceId: undefined,
  taskSpaceId: undefined,
  taskId: undefined,
  noteSpaceId: undefined,
  noteId: undefined,
  ...overrides,
});

export const createFormDataFromEvent = (event: CalendarEvent): EventFormData =>
  createInitialFormData({
    title: event.title,
    description: event.description || "",
    eventLink: event.eventLink || "",
    eventDate: event.eventDate,
    endDate: event.endDate || event.eventDate,
    startTime: event.startTime.substring(0, 5),
    endTime: event.endTime.substring(0, 5),
    recurrenceType: event.recurrenceType || "NONE",
    recurrenceEndDate: event.recurrenceEndDate,
    allowEditAll: event.allowEditAll,
    attendeeIds: event.attendeeIds || event.attendees?.map((attendee) => attendee.memberId) || [],
    attendees: event.attendees || [],
    attachments: event.attachments || [],
    callRoomSpaceId: event.callRoomSpaceId,
    taskSpaceId: event.taskSpaceId,
    taskId: event.taskId,
    noteSpaceId: event.noteSpaceId,
    noteId: event.noteId,
  });

export const resolveScheduleEvent = (
  event: CalendarEvent,
  events: CalendarEvent[],
): CalendarEvent => {
  if (!event.schedule || !event.scheduleId) return event;

  const group = events.filter((item) => item.scheduleId === event.scheduleId);
  if (!group.length) return event;

  let minDate = event.eventDate;
  let maxDate = event.endDate || event.eventDate;

  for (const item of group) {
    if (item.eventDate && dayjs(item.eventDate).isValid()) {
      if (!minDate || dayjs(item.eventDate).isBefore(dayjs(minDate))) minDate = item.eventDate;
      if (!maxDate || dayjs(item.eventDate).isAfter(dayjs(maxDate))) maxDate = item.eventDate;
    }

    const itemEnd = item.endDate || item.eventDate;
    if (itemEnd && dayjs(itemEnd).isValid()) {
      if (!maxDate || dayjs(itemEnd).isAfter(dayjs(maxDate))) maxDate = itemEnd;
    }
  }

  return {
    ...event,
    eventDate: minDate || event.eventDate,
    endDate: maxDate || event.endDate || event.eventDate,
  };
};

export const escapeHtml = (value: string) =>
  value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&#039;");

export const buildConflictMessage = (conflicts: CalendarEvent[]) => {
  const items = conflicts
    .slice(0, 4)
    .map(
      (event) =>
        `<li class="break-all"><span class="text-foreground font-bold break-all">${escapeHtml(event.title)}</span> (${event.startTime.substring(0, 5)} - ${event.endTime.substring(0, 5)})</li>`,
    )
    .join("");
  const moreCount = conflicts.length - 4;
  const moreMessage = moreCount > 0 ? `<p class="mt-2">VÀ ${moreCount} SỰ KIỆN KHÁC.</p>` : "";

  return `
    <p>CÓ ${conflicts.length} LỊCH ĐANG BỊ TRÙNG THỜI GIAN.</p>
    <ul class="mt-3 ml-5 list-disc space-y-1">${items}</ul>
    ${moreMessage}
    <p class="mt-3">BẠN VẪN MUỐN LƯU SỰ KIỆN NÀY KHÔNG?</p>
  `;
};
