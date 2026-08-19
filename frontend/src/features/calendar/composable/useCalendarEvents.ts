import { ref, watch } from "vue";
import {
  getEventsByDateRange,
  createEvent as apiCreateEvent,
  updateEvent as apiUpdateEvent,
  uploadEventAttachments as apiUploadEventAttachments,
  deleteEvent as apiDeleteEvent,
  checkConflicts as apiCheckConflicts,
} from "@/features/calendar/services/calendarService";
import type { CalendarEvent } from "@/features/calendar/types/calendar.types";
import type { Ref } from "vue";
import type dayjs from "dayjs";
import { calculateDateRange, formatPayload, extractNewFiles } from "@/features/calendar/utils/calendar.utils";

// Quản lý fetch và thay đổi event qua API
export function useCalendarEvents(
  spaceIdRef: Ref<string | undefined>,
  currentUserId: any,
  currentDate: Ref<dayjs.Dayjs>,
  viewMode: Ref<"week" | "month" | "year">
) {
  const events = ref<CalendarEvent[]>([]);
  const loading = ref(false);

  // Fetch sự kiện
  const fetchEvents = async () => {
    if (!spaceIdRef.value) return;

    const { start, end } = calculateDateRange(currentDate.value, viewMode.value);
    
    loading.value = true;
    try {
      const response = await getEventsByDateRange(spaceIdRef.value, start, end);
      events.value = response.data;
    } catch (error) {
      console.error("Lỗi khi tải sự kiện:", error);
    } finally {
      loading.value = false;
    }
  };

  const createEvent = async (data: any) => {
    const response = await apiCreateEvent(formatPayload(data, spaceIdRef, currentUserId));
    const files = extractNewFiles(data);
    if (files.length > 0) {
      await apiUploadEventAttachments(response.data.id, files);
    }
    await fetchEvents();
  };

  const updateEvent = async (id: string, data: any) => {
    await apiUpdateEvent(id, formatPayload(data, spaceIdRef, currentUserId, id));
    const files = extractNewFiles(data);
    if (files.length > 0) {
      await apiUploadEventAttachments(id, files);
    }
    await fetchEvents();
  };

  const deleteEvent = async (id: string) => {
    await apiDeleteEvent(id);
    await fetchEvents();
  };

  // Check event trùng lịch
  const checkConflicts = async (date: string, endDate: string, start: string, end: string, excludeId?: string) => {
    if (!spaceIdRef.value) return [];
    try {
      const res = await apiCheckConflicts(spaceIdRef.value, date, endDate, start, end, excludeId);
      return res.data;
    } catch {
      return [];
    }
  };

  // Reactivity fetches
  watch(spaceIdRef, () => fetchEvents(), { immediate: true });
  watch([currentDate, viewMode], () => fetchEvents());

  return {
    events,
    loading,
    fetchEvents,
    createEvent,
    updateEvent,
    deleteEvent,
    checkConflicts,
  };
}
