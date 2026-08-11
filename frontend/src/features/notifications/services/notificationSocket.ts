import { socketService } from "@/services/socketService";

export const notificationSocket = {
  async subscribeNotifications(callback: (notification: any) => void) {
    await socketService.connect();

    return socketService.subscribe(
      `/user/queue/notifications`,
      (notification) => {
        callback(notification);
      },
      { persistent: true },
    );
  },

  unsubscribeNotifications() {
    socketService.unsubscribeByDestination(`/user/queue/notifications`);
  },
};
