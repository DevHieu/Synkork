// import { socketService } from "./socketService";

// export const friendSocket = {
//      subscribeFriendRequest(callback: (data: any) => void) {
//         return socketService.subscribe("/user/queue/friend-request", callback);
//     }
// }
import { socketService } from "./socketService";

export const friendSocket = {
  subscribeFriendRequest(callback: (data: any) => void) {
    // Nếu đã connected rồi thì subscribe thẳng
    if (socketService.isConnected()) {
      return socketService.subscribe("/user/queue/friend-request", callback);
    }

    // Chưa connected → connect trước, rồi subscribe trong onConnected callback
    socketService.connect(() => {
      socketService.subscribe("/user/queue/friend-request", callback);
    });
  },
};