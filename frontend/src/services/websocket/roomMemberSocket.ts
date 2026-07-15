import { socketService } from "./socketService";

export const roomMemberSocket = {
  subscribeAuthorityChange(roomId: string, callback: (member: any) => void) {
    return socketService.subscribe(
      `/topic/room/${roomId}/members/changeAuthority`,
      (member) => {
        callback(member);
      },
    );
  },

  subscribeMemberKicked(roomId: string, callback: (memberId: string) => void) {
    return socketService.subscribe(
      `/topic/room/${roomId}/members/kicked`,
      (memberId: string) => {
        callback(memberId);
      },
    );
  },

  subscribeMemberJoined(roomId: string, callback: (member: any) => void) {
    return socketService.subscribe(
      `/topic/room/${roomId}/members/joined`,
      (member) => {
        callback(member);
      },
    );
  },

  subscribeMemberUpdated(roomId: string, callback: (member: any) => void) {
    return socketService.subscribe(
      `/topic/room/${roomId}/members/updated`,
      (member) => {
        callback(member);
      },
    );
  },
};
