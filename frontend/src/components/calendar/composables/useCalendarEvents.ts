import { ref, watch, unref } from "vue";
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

  // Tính range lấy dữ liệu theo chế độ xem
  const calculateDateRange = (date: dayjs.Dayjs, mode: string) => {
    if (mode === "week") {
      return {
        start: date.startOf("week").format("YYYY-MM-DD"),
        end: date.endOf("week").format("YYYY-MM-DD")
      };
    }
    
    if (mode === "year") {
      return {
        start: date.startOf("year").format("YYYY-MM-DD"),
        end: date.endOf("year").format("YYYY-MM-DD")
      };
    }

    // View month default (đệm 7 ngày)
    return {
      start: date.startOf("month").subtract(7, "day").format("YYYY-MM-DD"),
      end: date.endOf("month").add(7, "day").format("YYYY-MM-DD")
    };
  };

  // Chuẩn hóa payload
  const formatPayload = (data: any, id?: string) => {
    const payload = {
      ...data,
      startTime: data.startTime.length === 5 ? `${data.startTime}:00` : data.startTime,
      endTime: data.endTime.length === 5 ? `${data.endTime}:00` : data.endTime,
      spaceId: spaceIdRef.value,
      createdById: unref(currentUserId)
    };
    if (id) payload.id = id;
    // Xóa recurrenceEndDate khi không áp dụng
    if (payload.recurrenceType === 'NONE' || !payload.recurrenceEndDate) {
      delete payload.recurrenceEndDate;
    }
    return payload;
  };

  const createEvent = async (data: any) => {
    await apiCreateEvent(formatPayload(data));
    await fetchEvents();
  };

  const updateEvent = async (id: string, data: any) => {
    await apiUpdateEvent(id, formatPayload(data, id));
    await fetchEvents();
  };

  const deleteEvent = async (id: string) => {
    await apiDeleteEvent(id, currentUserId);
    await fetchEvents();
  };

  // Check event trùng lịch
  const checkConflicts = async (date: string, start: string, end: string, excludeId?: string) => {
    if (!spaceIdRef.value) return [];
    try {
      const res = await apiCheckConflicts(spaceIdRef.value, date, start, end, excludeId);
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
