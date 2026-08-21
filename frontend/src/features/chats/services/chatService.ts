import axiosClient from "@/lib/axiosClient";
import type { MessageRequest } from "@/features/chats/types/MessageTypes";
import axios from "axios";
import { toast } from "vue-sonner";

export function chatService() {
  const sendMessage = async (spaceId: string, msg: MessageRequest) => {
    return axiosClient.post(`/api/spaces/${spaceId}/messages`, msg);
  };

  const updateMessage = async (
    spaceId: string,
    messageId: string,
    message: MessageRequest,
  ) => {
    try {
      return await axiosClient.put(
        `/api/spaces/${spaceId}/messages/${messageId}`,
        message,
      );
    } catch (e) {
      if (axios.isAxiosError(e) && e.response?.status === 409) {
        toast.error("Tin nhắn đã bị thay đổi bởi người khác", {
          description: "Vui lòng tải lại và thử lại",
        });
        return;
      }
      throw e;
    }
  };

  const deleteMessage = async (spaceId: string, messageId: string) => {
    return axiosClient.delete(`/api/spaces/${spaceId}/messages/${messageId}`);
  };

  const getChatFromSpaceId = async (
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

  const getPinnedChatList = async (
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

  const changePinStatus = async (spaceId: string, messageId: string) => {
    const res = axiosClient.put(
      `/api/spaces/${spaceId}/messages/pin/${messageId}`,
    );

    return res;
  };

  const getAroundMessage = async (
    spaceId: string,
    messageId: string,
    limit = 20,
  ) => {
    const params = new URLSearchParams({ limit: limit.toString() });

    console.log(params);

    return axiosClient.get(
      `/api/spaces/${spaceId}/messages/around/${messageId}?${params}`,
    );
  };

  const searchMessage = async (
    spaceId: string,
    keyword: string,
    cursor: string | null,
    limit: number,
  ) => {
    const params = new URLSearchParams({
      keyword,
      limit: limit.toString(),
    });
    if (cursor !== null) {
      params.append("cursor", cursor);
    }

    const res = await axiosClient.get(
      `/api/spaces/${spaceId}/messages/search?${params.toString()}`,
    );
    return res.data;
  };

  const sendFileMessage = async (spaceId: string, formData: FormData) => {
    const res = await axiosClient.post(
      `/api/spaces/${spaceId}/messages/file`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
        timeout: 60000,
      },
    );

    return res.data;
  };

  return {
    sendMessage,
    updateMessage,
    deleteMessage,
    getChatFromSpaceId,
    getPinnedChatList,
    changePinStatus,
    getAroundMessage,
    searchMessage,
    sendFileMessage,
  };
}
