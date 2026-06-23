import { socketService } from "./socketService";

export const friendSocket = {
  subscribeFriendRequest(callback: (data: any) => void) {
    // Nếu đã connected rồi thì subscribe thẳng
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-request", callback, {
        persistent: true,
      });
    }

    // Chưa connected → connect trước, rồi subscribe trong onConnected callback
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-request", callback, {
        persistent: true,
      });
    });
  },

  subscribeFriendAccept(callback: (data: any) => void) {
    // Nếu đã connected rồi thì subscribe thẳng
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-accept", callback, {
        persistent: true,
      });
    }

    // Chưa connected → connect trước, rồi subscribe trong onConnected callback
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-accept", callback, {
        persistent: true,
      });
    });
  },
  subscribeFriendReject(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-reject", callback, {
        persistent: true,
      });
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-reject", callback, {
        persistent: true,
      });
    });
  },

  subscribeFriendCancel(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-cancel", callback, {
        persistent: true,
      });
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-cancel", callback, {
        persistent: true,
      });
    });
  },

  subscribeFriendRemove(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-remove", callback, {
        persistent: true,
      });
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-remove", callback, {
        persistent: true,
      });
    });
  },
};

