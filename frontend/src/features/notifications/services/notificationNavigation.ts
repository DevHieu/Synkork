import router from "@/routers";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import type { NotificationDTO } from "@/features/notifications/types/Notification";

export async function navigateFromNotification(notification: NotificationDTO, path: string) {
    if (notification.roomId && notification.spaceId) {
        const roomStore = useRoomsStore();
        const currentRoomId = roomStore.currentRoom?.id;

        if (currentRoomId != notification.roomId) {
            const targetRoom = roomStore.rooms.find(
                (r) => r.id === notification.roomId,
            );

            if (targetRoom) {
                await roomStore.changeRoom(
                    targetRoom,
                    notification.spaceId,
                    notification.type,
                );
            }
        } else {
            const spaceStore = useSpaceStore();

            await spaceStore.changeSpaceById(
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