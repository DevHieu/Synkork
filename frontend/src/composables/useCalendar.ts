import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs";
import {
  getEventsByDateRange,
  createEvent as apiCreateEvent,
  updateEvent as apiUpdateEvent,
  deleteEvent as apiDeleteEvent,
  checkConflicts as apiCheckConflicts,
} from "@/services/calendarService";
import { socketService } from "@/services/websocket/socketService";
import {
  subscribeCalendarSpace,
} from "@/services/websocket/calendarSocket";

export interface CalendarEvent {
  id: string;
  spaceId: string;
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  allowEditAll: boolean;
  createdById: string;
  createdByUsername: string;
  createdByDisplayName: string;
  createdAt: string;
  updatedAt: string;
}

export function useCalendar(spaceIdRef: any, currentUserId: string) {
  const viewMode = ref<"week" | "month" | "year">("month");
  const currentDate = ref(dayjs());
  const selectedDate = ref(dayjs());
  const events = ref<CalendarEvent[]>([]);
  const loading = ref(false);

  // ===== Navigation & Date Computations =====
  const goNext = () => {
    if (viewMode.value === "week") currentDate.value = currentDate.value.add(1, "week");
    else if (viewMode.value === "month") currentDate.value = currentDate.value.add(1, "month");
    else currentDate.value = currentDate.value.add(1, "year");
  };

  const goPrev = () => {
    if (viewMode.value === "week") currentDate.value = currentDate.value.subtract(1, "week");
    else if (viewMode.value === "month") currentDate.value = currentDate.value.subtract(1, "month");
    else currentDate.value = currentDate.value.subtract(1, "year");
  };

  const goToday = () => {
    currentDate.value = dayjs();
    selectedDate.value = dayjs();
  };

  const selectDate = (date: dayjs.Dayjs) => {
    selectedDate.value = date;
  };

  const setYearMonth = (monthIndex: number) => {
    currentDate.value = currentDate.value.month(monthIndex);
    viewMode.value = "month";
  };

  const headerTitle = computed(() => {
    if (viewMode.value === "week") {
      const start = currentDate.value.startOf("week");
      const end = currentDate.value.endOf("week");
      return `${start.format("DD/MM")} - ${end.format("DD/MM/YYYY")}`;
    } else if (viewMode.value === "year") {
      return currentDate.value.format("YYYY");
    }
    return currentDate.value.format("MMMM YYYY");
  });

  const relativeTimeText = computed(() => {
    const now = dayjs();
    if (viewMode.value === "week") {
      const diff = currentDate.value.startOf("week").diff(now.startOf("week"), "week");
      if (diff === 0) return "Tuần này";
      if (diff === -1) return "Tuần trước";
      if (diff === 1) return "Tuần sau";
      if (diff < -1) return `${-diff} tuần trước`;
      return `${diff} tuần sau`;
    } else if (viewMode.value === "month") {
      const diff = currentDate.value.startOf("month").diff(now.startOf("month"), "month");
      if (diff === 0) return "Tháng này";
      if (diff === -1) return "Tháng trước";
      if (diff === 1) return "Tháng sau";
      if (diff < -1) return `${-diff} tháng trước`;
      return `${diff} tháng sau`;
    } else {
      const diff = currentDate.value.year() - now.year();
      if (diff === 0) return "Năm nay";
      if (diff === -1) return "Năm ngoái";
      if (diff === 1) return "Năm sau";
      return `Năm ${currentDate.value.year()}`;
    }
  });

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

  // WebSocket Logic
  const isSocketReady = ref(false);
  let sub: any = null;

  onMounted(() => {
    socketService.connect(() => {
      isSocketReady.value = true;
    });
  });

  onUnmounted(() => {
    if (sub) {
      sub.unsubscribe();
    }
  });

  watch(
    [spaceIdRef, isSocketReady],
    ([spaceId, ready]) => {
      if (!spaceId || !ready) return;
      if (sub) sub.unsubscribe();
      sub = subscribeCalendarSpace(spaceId, (payload: any) => {
        const { action, event } = payload;
        if (action === "CREATED") {
          if (!events.value.find((e) => e.id === event.id)) events.value.push(event);
        } else if (action === "UPDATED") {
          const idx = events.value.findIndex((e) => e.id === event.id);
          if (idx !== -1) events.value[idx] = event;
          else events.value.push(event);
        } else if (action === "DELETED") {
          events.value = events.value.filter((e) => e.id !== event.id);
        }
      });
    },
    { immediate: true }
  );

  return {
    viewMode,
    currentDate,
    selectedDate,
    events,
    loading,
    headerTitle,
    relativeTimeText,
    goNext,
    goPrev,
    goToday,
    selectDate,
    setYearMonth,
    createEvent,
    updateEvent,
    deleteEvent,
    checkConflicts,
    fetchEvents,
  };
}
