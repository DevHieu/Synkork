import { ref, watch } from "vue";
import {
  getEventsByDateRange,
  createEvent as apiCreateEvent,
  updateEvent as apiUpdateEvent,
  deleteEvent as apiDeleteEvent,
  checkConflicts as apiCheckConflicts,
} from "@/services/calendarService";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { Ref } from "vue";
import type dayjs from "dayjs";

export function useCalendarEvents(
  spaceIdRef: Ref<string | undefined>,
  currentUserId: string,
  currentDate: Ref<dayjs.Dayjs>,
  viewMode: Ref<"week" | "month" | "year">
) {
  const events = ref<CalendarEvent[]>([]);
  const loading = ref(false);

  const fetchEvents = async () => {
    if (!spaceIdRef.value) return;
    loading.value = true;
    let start: string, end: string;

    if (viewMode.value === "week") {
      start = currentDate.value.startOf("week").format("YYYY-MM-DD");
      end = currentDate.value.endOf("week").format("YYYY-MM-DD");
    } else if (viewMode.value === "year") {
      start = currentDate.value.startOf("year").format("YYYY-MM-DD");
      end = currentDate.value.endOf("year").format("YYYY-MM-DD");
    } else {
      start = currentDate.value.startOf("month").subtract(7, "day").format("YYYY-MM-DD");
      end = currentDate.value.endOf("month").add(7, "day").format("YYYY-MM-DD");
    }

    try {
      const res = await getEventsByDateRange(spaceIdRef.value, start, end);
      events.value = res.data;
    } catch (err) {
      console.error("Error fetching events:", err);
    } finally {
      loading.value = false;
    }
  };

  const createEvent = async (data: any) => {
    await apiCreateEvent({ ...data, spaceId: spaceIdRef.value, createdById: currentUserId });
    await fetchEvents();
  };

  const updateEvent = async (id: string, data: any) => {
    await apiUpdateEvent(id, { ...data, spaceId: spaceIdRef.value, createdById: currentUserId });
    await fetchEvents();
  };

  const deleteEvent = async (id: string) => {
    await apiDeleteEvent(id, currentUserId);
    await fetchEvents();
  };

  const checkConflicts = async (date: string, start: string, end: string, excludeId?: string) => {
    if (!spaceIdRef.value) return [];
    try {
      const res = await apiCheckConflicts(spaceIdRef.value, date, start, end, excludeId);
      return res.data;
    } catch {
      return [];
    }
  };

  // Watchers for fetching
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
