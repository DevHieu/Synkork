import axiosClient from "@/lib/axiosClient";
import type { ChatDisableTime } from "@/features/members/types/Member";

export function useMemberService() {
  const getRoomMembers = async (roomId: string) => {
    const res = await axiosClient.get(`/api/rooms/${roomId}/members`);

    return res.data;
  };

  const changeMemberAuthority = async (
    data: { memberId: string; newRole: "OWNER" | "ADMIN" | "MEMBER" },
    roomId: string,
  ) => {
    const res = await axiosClient.put(
      `/api/rooms/${roomId}/members/change-authority`,
      data,
    );

    return res.data;
  };

  const leaveRoom = async (roomId: string) => {
    const res = await axiosClient.delete(`/api/rooms/${roomId}/members/leave`);

    return res.data;
  };

  const kickMember = async (memberId: string, roomId: string) => {
    const res = await axiosClient.delete(
      `/api/rooms/${roomId}/members/${memberId}`,
    );

    return res;
  };

  const muteAudio = async (
    roomId: string,
    memberId: string,
    payload: { muted: boolean | null; deafen: boolean | null },
  ) => {
    const res = await axiosClient.patch(
      `/api/rooms/${roomId}/members/${memberId}/mute`,
      payload,
    );

    return res;
  };

  const muteChatMember = async (
    roomId: string,
    memberId: string,
    time: ChatDisableTime,
  ) => {
    const res = await axiosClient.patch(
      `/api/rooms/${roomId}/members/${memberId}/chat-mute`,
      null,
      { params: { time } },
    );

    return res.data;
  };

  return {
    getRoomMembers,
    changeMemberAuthority,
    leaveRoom,
    kickMember,
    muteAudio,
    muteChatMember,
  };
}
