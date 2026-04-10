import axiosClient from "@/lib/axiosClient";

export const getChatFromSpaceId = async (
  spaceId: string,
  size: number,
  cursor: string | null,
) => {
  const params = new URLSearchParams({ size: size.toString() });
  if (cursor !== null) {
    params.append("cursor", cursor);
  }

  return axiosClient.get(`/api/messages/${spaceId}?${params.toString()}`);
};
