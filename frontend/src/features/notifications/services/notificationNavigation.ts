import router from "@/routers";
import { useRoomsStore } from "@/features/rooms/stores/roomStore.ts";
import type { NotificationDTO } from "@/features/notifications/types/Notification";
import { useRoomComposable } from "@/features/rooms/composables/roomComposable";
import { useSpaceComposable } from "@/features/spaces/composables/spaceComposable";

export async function navigateFromNotification(
  notification: NotificationDTO,
  path: string,
) {
  if (notification.roomId && notification.spaceId) {
    const roomStore = useRoomsStore();
    const roomComposable = useRoomComposable();
    const currentRoomId = roomStore.currentRoom?.id;

    if (currentRoomId != notification.roomId) {
      const targetRoom = roomStore.rooms.find(
        (r) => r.id === notification.roomId,
      );

      if (targetRoom) {
        await roomComposable.changeRoom(
          targetRoom,
          notification.spaceId,
          notification.type,
        );
      }
    } else {
      const spaceComposable = useSpaceComposable();

      await spaceComposable.changeSpaceById(
        notification.spaceId,
        notification.type,
      );
    }
  }
  router.push({
    path,
    query: {
      refId: notification.refId,
    },
  });
}
