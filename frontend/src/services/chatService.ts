import axiosClient from "@/lib/axiosClient";

export const getChatFromSpaceId = async (
  spaceId: string,
  cursor: string | null,
  isUp: boolean,
  limit: number,
) => {
  const params = new URLSearchParams({
    limit: limit.toString(),
    isUp: String(isUp),
  });
  if (cursor !== null) {
    params.append("cursor", cursor);
  }

  return axiosClient.get(
    `/api/spaces/${spaceId}/messages?${params.toString()}`,
  );
};

export const getPinnedChatList = async (
  spaceId: string,
  cursor: string | null,
  limit: number,
) => {
  const params = new URLSearchParams({ limit: limit.toString() });
  if (cursor !== null) {
    params.append("cursor", cursor);
  }

  return axiosClient.get(
    `/api/spaces/${spaceId}/messages/pin?${params.toString()}`,
  );
};

export const changePinStatus = async (spaceId: string, messageId: string) => {
  const res = axiosClient.put(
    `/api/spaces/${spaceId}/messages/pin/${messageId}`,
  );

  return res;
};

export const getAroundMessage = async (
  spaceId: string,
  messageId: string,
  limit = 20,
) => {
  const params = new URLSearchParams({ limit: limit.toString() });

  return axiosClient.get(
    `/api/spaces/${spaceId}/messages/around/${messageId}?${params}`,
  );
};

export const sendFileMessage = async (spaceId: string, formData: FormData) => {
  const res = await axiosClient.post(
    `/api/spaces/${spaceId}/messages/file`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );

  return res.data;
};
