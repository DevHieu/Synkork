import axiosClient from "@/lib/axiosClient";

export const getRoomMembers = async (roomId: string) => {
  const res = await axiosClient.get(`/api/rooms/${roomId}/members`);

  return res.data;
};

export const changeMemberAuthority = async (
  data: { memberId: string; newRole: "OWNER" | "ADMIN" | "MEMBER" },
  roomId: string,
) => {
  const res = await axiosClient.put(
    `/api/rooms/${roomId}/members/change-authority`,
    data,
  );

  return res.data;
};

export const leaveRoom = async (roomId: string) => {
  const res = await axiosClient.delete(`/api/rooms/${roomId}/members/leave`);

  return res.data;
};

export const kickMember = async (memberId: string, roomId: string) => {
  const res = await axiosClient.delete(
    `/api/rooms/${roomId}/members/${memberId}`,
  );

  return res;
};

export const muteAudio = async (
  roomId: string,
  memberId: string,
  payload: { muted: boolean | null; deafen: boolean | null },
) => {
  console.log("Service");

  const res = await axiosClient.patch(
    `/api/rooms/${roomId}/members/${memberId}/mute`,
    payload,
  );

  return res;
};

export type ChatDisableTime =
  | "MINUTE"
  | "FIVE_MINUTES"
  | "FIFTEEN_MINUTES"
  | "HOUR"
  | "DAY"
  | "WEEK";

export const muteChatMember = async (
  roomId: string,
  memberId: string,
  time: ChatDisableTime,
) => {
  const res = await axiosClient.patch(
    `/api/rooms/${roomId}/members/${memberId}/chat-mute`,
    null,
    { params: { time } },
  );

  return res;
};
