import axiosClient from "@/lib/axiosClient";

export const getChatFromSpaceId = async (
  spaceId: string,
  page: number,
  size: number
) => {
  const res = await axiosClient.get(`/api/messages/${spaceId}?page=${page}&size=${size}`);
  return res;
};
