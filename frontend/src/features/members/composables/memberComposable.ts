import { storeToRefs } from "pinia";
import { useRoomMemberStore } from "../stores/roomMemberStore";
import type { Member } from "../types/Member";
import { useMemberService } from "../services/roomMemberService";
import { useMemberSocketComposable } from "./memberSocketComposable";

export function useMemberComposable() {
  const roomMemberStore = useRoomMemberStore();
  const { loading, members } = storeToRefs(roomMemberStore);

  const fetchMembers = async (roomId: string, username: string) => {
    loading.value = true;
    roomMemberStore.clearMembers();
    try {
      const memberService = useMemberService();

      members.value = await memberService.getRoomMembers(roomId);

      // Set thông tin sau khi fetch xong
      roomMemberStore.setInfo(username);

      //subscribe socket
      const { subscribeSocket } = useMemberSocketComposable();
      subscribeSocket(roomId, username, updateMember);
    } finally {
      loading.value = false;
    }
  };

  const updateMember = (member: Member) => {
    const idx = members.value.findIndex((m) => m.memberId === member.memberId);
    if (idx !== -1) {
      members.value[idx] = member;
    }
  };

  return {
    fetchMembers,
    updateMember,
  };
}
