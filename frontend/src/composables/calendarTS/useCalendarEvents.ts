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

// Chức năng này để gọi API lấy và thay đổi sự kiện trong lịch
export function useCalendarEvents(
  spaceIdRef: Ref<string | undefined>,
  currentUserId: any,
  currentDate: Ref<dayjs.Dayjs>,
  viewMode: Ref<"week" | "month" | "year">
) {
  const events = ref<CalendarEvent[]>([]);
  const loading = ref(false);

  // Tải dữ liệu sự kiện từ máy chủ
  const fetchEvents = async () => {
    if (!spaceIdRef.value) return;

    const { start, end } = calculateDateRange(currentDate.value, viewMode.value);
    
    loading.value = true;
    try {
      const response = await getEventsByDateRange(spaceIdRef.value, start, end);
      events.value = response.data;
    } catch (error) {
      console.error("Không thể tải sự kiện:", error);
    } finally {
      loading.value = false;
    }
  };

  // Tính xem cần lấy dữ liệu từ ngày nào đến ngày nào
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

    // Mặc định cho xem tháng (lấy rộng ra chút để chuyển trang mượt)
    return {
      start: date.startOf("month").subtract(7, "day").format("YYYY-MM-DD"),
      end: date.endOf("month").add(7, "day").format("YYYY-MM-DD")
    };
  };

  // Làm sạch dữ liệu trước khi lưu
  const formatPayload = (data: any, id?: string) => {
    const payload = {
      ...data,
      startTime: data.startTime.length === 5 ? `${data.startTime}:00` : data.startTime,
      endTime: data.endTime.length === 5 ? `${data.endTime}:00` : data.endTime,
      spaceId: spaceIdRef.value,
      createdById: unref(currentUserId)
    };
    if (id) payload.id = id;
    if (payload.recurrenceType === 'NONE') {
      delete payload.recurrenceEndDate;
    } else if (!payload.recurrenceEndDate) {
      delete payload.recurrenceEndDate;
    }
    return payload;
  };

  // Lưu một sự kiện mới
  const createEvent = async (data: any) => {
    await apiCreateEvent(formatPayload(data));
    await fetchEvents();
  };

  // Lưu thay đổi của một sự kiện
  const updateEvent = async (id: string, data: any) => {
    await apiUpdateEvent(id, formatPayload(data, id));
    await fetchEvents();
  };

  // Xóa bỏ một sự kiện
  const deleteEvent = async (id: string) => {
    await apiDeleteEvent(id, currentUserId);
    await fetchEvents();
  };

  // Tìm xem các sự kiện có bị trùng giờ nhau không
  const checkConflicts = async (date: string, start: string, end: string, excludeId?: string) => {
    if (!spaceIdRef.value) return [];
    try {
      const res = await apiCheckConflicts(spaceIdRef.value, date, start, end, excludeId);
      return res.data;
    } catch {
      return [];
    }
  };

  // Tự động tải lại sự kiện khi đổi không gian hoặc thời gian
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
