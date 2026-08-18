import axiosClient from "@/lib/axiosClient";

export function useSpaceService() {
  const getAllSpacesFromRoomId = async (roomId: string) => {
    const res = await axiosClient.get(`/api/rooms/${roomId}/spaces`);
    return res;
  };

  const createSpace = async (roomId: string, spaceData: {}) => {
    const res = await axiosClient.post(
      `/api/rooms/${roomId}/spaces`,
      spaceData,
    );
    return res.data;
  };

  const getSpaceById = async (spaceId: string) => {
    // Cái roomId đang để null vì cái hàm này chỉ dùng để join DM space, mà DM space thì không có roomId, Nên để vậy luôn
    const res = await axiosClient.get(`/api/rooms/null/spaces/${spaceId}`);
    return res.data;
  };

  const updateSpace = async (
    roomId: string,
    spaceId: string,
    spaceData: {},
  ) => {
    const res = await axiosClient.put(
      `/api/rooms/${roomId}/spaces/${spaceId}`,
      spaceData,
    );
    return res.data;
  };

  const deleteSpace = async (roomId: string, spaceId: string) => {
    const res = await axiosClient.delete(
      `/api/rooms/${roomId}/spaces/${spaceId}`,
    );
    return res.data;
  };

  const getZegoToken = async (userId: string) => {
    const res = await axiosClient.get(`/api/zego/token/${userId}`);
    console.log("token: " + res.data);

    return res.data;
  };

  return {
    getAllSpacesFromRoomId,
    createSpace,
    getSpaceById,
    updateSpace,
    deleteSpace,
    getZegoToken,
  };
}
