import axiosClient from "@/lib/axiosClient";

export const getAllSpacesFromRoomId = async (roomId: string) => {
  const res = await axiosClient.get(`/api/rooms/${roomId}/spaces`);
  return res;
};
