import axios from "axios";

export const getChatFromSpaceId = async (
  spaceId: string,
  page: number,
  size: number
) => {
  const res = await axios.get(`/messages/${spaceId}?page=${page}&size=${size}`);
  return res;
};
