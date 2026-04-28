import { socketService } from "./socketService";

export const friendSocket = {
  subscribeFriendRequest(callback: (data: any) => void) {
    // Nếu đã connected rồi thì subscribe thẳng
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-request", callback);
    }

    // Chưa connected → connect trước, rồi subscribe trong onConnected callback
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-request", callback);
    });
  },

  subscribeFriendAccept(callback: (data: any) => void) {
    // Nếu đã connected rồi thì subscribe thẳng
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-accept", callback);
    }

    // Chưa connected → connect trước, rồi subscribe trong onConnected callback
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-accept", callback);
    });
  },
  subscribeFriendReject(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-reject", callback);
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-reject", callback);
    });
  },

  subscribeFriendCancel(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-cancel", callback);
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-cancel", callback);
    });
  },

  subscribeFriendRemove(callback: (data: any) => void) {
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-remove", callback);
    }
    socketService.connect().then(() => {
      socketService.subscribe("/user/queue/friend-remove", callback);
    });
  },
};

