import { ref, watch, onMounted, onUnmounted } from "vue";
import { socketService } from "@/services/websocket/socketService";
import { subscribeCalendarSpace } from "@/services/websocket/calendarSocket";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { Ref } from "vue";

export function useCalendarRealtime(
  spaceIdRef: Ref<string | undefined>,
  events: Ref<CalendarEvent[]>
) {
  const isSocketReady = ref(false);
  let sub: { unsubscribe: () => void } | null = null;

  // Hủy đăng ký lắng nghe hiện tại
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
    // Kết nối WebSocket khi linh kiện được gắn
    socketService.connect(() => {
      isSocketReady.value = true;
    });
  });

  onUnmounted(() => {
    // Hủy đăng ký khi linh kiện bị hủy
    unsubscribeCurrent();
  });

  // Theo dõi ID không gian và trạng thái Socket để đăng ký nhận thông báo
  watch(
    [spaceIdRef, isSocketReady],
    ([spaceId, ready]) => {
      if (!spaceId || !ready) {
        unsubscribeCurrent();
        return;
      }

      unsubscribeCurrent();

      // Đăng ký nhận thông báo về sự kiện lịch (Tạo/Sửa/Xóa)
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
