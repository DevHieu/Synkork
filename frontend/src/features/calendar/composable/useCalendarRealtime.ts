import { ref, watch, onMounted, onUnmounted } from "vue";
import { socketService } from "@/services/websocket/socketService";
import { subscribeCalendarSpace } from "@/services/websocket/calendarSocket";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { Ref } from "vue";

// Đồng bộ event realtime socket
export function useCalendarRealtime(
  spaceIdRef: Ref<string | undefined>,
  events: Ref<CalendarEvent[]>,
  fetchEvents?: () => Promise<void>
) {
  const isSocketReady = ref(false);
  let sub: { unsubscribe: () => void } | null = null;

  // Hủy lắng nghe
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
    // Mở kết nối
    socketService.connect().then(() => {
      isSocketReady.value = true;
    });
  });

  onUnmounted(() => {
    // Đóng socket
    unsubscribeCurrent();
  });

  // Quản lý subscribe tự động
  watch(
    [spaceIdRef, isSocketReady],
    ([spaceId, ready]) => {
      if (!spaceId || !ready) {
        unsubscribeCurrent();
        return;
      }

      unsubscribeCurrent();

      const handlers: Record<string, (ev: CalendarEvent) => void> = {
        CREATED: (ev) => {
          if (fetchEvents) {
            fetchEvents();
            return;
          }
          if (!events.value.find((e) => e.id === ev.id)) events.value.push(ev);
        },
        UPDATED: (ev) => {
          if (fetchEvents) {
            fetchEvents();
            return;
          }
          const idx = events.value.findIndex((e) => e.id === ev.id);
          if (idx !== -1) events.value[idx] = ev; else events.value.push(ev);
        },
        DELETED: (ev) => {
          events.value = events.value.filter((e) => e.id !== ev.id);
        },
      };

      sub = subscribeCalendarSpace(spaceId, ({ action, event }: any) => {
        handlers[action]?.(event);
      });
    },
    { immediate: true }
  );
}
