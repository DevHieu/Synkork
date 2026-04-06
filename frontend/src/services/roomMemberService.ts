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

export const kickMember = async (memberId: string, roomId: string) => {
  const res = await axiosClient.delete(
    `/api/rooms/${roomId}/members/${memberId}`,
  );

  return res.data;
};
