import { ref, watch, onMounted, onUnmounted } from "vue";
import { socketService } from "@/services/websocket/socketService";
import { subscribeCalendarSpace } from "@/services/websocket/calendarSocket";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { Ref } from "vue";

// Chức năng này để tự động cập nhật lịch khi người khác thay đổi (thời gian thực)
export function useCalendarRealtime(
  spaceIdRef: Ref<string | undefined>,
  events: Ref<CalendarEvent[]>
) {
  const isSocketReady = ref(false);
  let sub: { unsubscribe: () => void } | null = null;

  // Ngừng lắng nghe dữ liệu
  const unsubscribeCurrent = () => {
    if (sub) {
      try {
        sub.unsubscribe();
      } catch (err) {
        console.warn("[Lịch] Lỗi khi hủy đăng ký:", err);
      }
      sub = null;
    }
  };

  onMounted(() => {
    // Mở cổng kết nối dữ liệu trực tuyến
    socketService.connect().then(() => {
      isSocketReady.value = true;
    });
  });

  onUnmounted(() => {
    // Đóng cổng kết nối khi không dùng nữa
    unsubscribeCurrent();
  });

  // Tự động bật/tắt lắng nghe khi vào đúng phòng hoặc mất mạng
  watch(
    [spaceIdRef, isSocketReady],
    ([spaceId, ready]) => {
      if (!spaceId || !ready) {
        unsubscribeCurrent();
        return;
      }

      unsubscribeCurrent();

      // Theo dõi biến động: Tạo mới, Sửa, Xóa sự kiện
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
    isSocketReady,
  };
}
