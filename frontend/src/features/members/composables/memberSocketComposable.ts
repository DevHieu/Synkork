import { roomMemberSocket } from "../services/roomMemberSocket";
import { userSocket } from "@/features/users/services/userSocket";
import { useRoomMemberStore } from "../stores/roomMemberStore";
import { storeToRefs } from "pinia";
import type { Member } from "../types/Member";

export function useMemberSocketComposable() {
  const subscribeSocket = async (
    roomId: string,
    username: string,
    updateMember: (member: Member) => void,
  ) => {
    const roomMemberStore = useRoomMemberStore();
    const { members, currentAuthority } = storeToRefs(roomMemberStore);

    roomMemberSocket.subscribeAuthorityChange(roomId, (member) => {
      const idx = members.value.findIndex(
        (m) => m.memberId === member.memberId,
      );
      if (idx !== -1) {
        members.value[idx] = member;
        roomMemberStore.setInfo(username); // Có đổi quyền thì cái quyền hiện tại sẽ thay đổi
      }
    });

    roomMemberSocket.subscribeMemberKicked(roomId, (memberId) => {
      const kicked = members.value.find((m) => m.memberId === memberId);

      members.value = members.value.filter((m) => m.memberId !== memberId);

      if (kicked?.username === username) {
        currentAuthority.value = null;
      }
    });

    roomMemberSocket.subscribeMemberJoined(roomId, (member) => {
      members.value.push(member);
    });

    roomMemberSocket.subscribeMemberUpdated(roomId, (member) => {
      updateMember(member);
      roomMemberStore.setInfo(username);
    });
    userSocket.subscribeKicked();
    userSocket.subscribeRoomDeleted();
  };

  return {
    subscribeSocket,
  };
}
