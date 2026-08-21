import axiosClient from "@/lib/axiosClient";

export function useRoomService() {
  const getUserRooms = async () => {
    const response = await axiosClient.get(`/api/rooms/me`);
    return response.data;
  };

  const createRoom = async (roomData: { name: string; imageFile?: File }) => {
    const formData = new FormData();
    formData.append("name", roomData.name);
    if (roomData.imageFile) {
      formData.append("imageFile", roomData.imageFile);
    }
    const response = await axiosClient.post(`/api/rooms`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  };

  const updateRoomInfo = async (
    roomId: string,
    roomData: {
      name: string;
      description: string;
      imageFile?: File;
    },
  ) => {
    const formData = new FormData();
    formData.append("name", roomData.name);
    formData.append("description", roomData.description);
    if (roomData.imageFile) {
      formData.append("imageFile", roomData.imageFile);
    }
    const response = await axiosClient.put(`/api/rooms/${roomId}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  };

  const deleteRoom = async (roomId: string) => {
    const res = await axiosClient.delete(`/api/rooms/${roomId}`);

    return res.data;
  };

  const getInviteCode = async (roomId: string) => {
    const res = await axiosClient.get(`/api/rooms/${roomId}/invites`);
    return res.data;
  };

  const resetInviteCode = async (roomId: string) => {
    const res = await axiosClient.post(`/api/rooms/${roomId}/invites/reset`);
    return res.data;
  };

  const getBasicRoomInfo = async (inviteCode: string) => {
    const res = await axiosClient.get(`/api/rooms/invites/${inviteCode}`);
    return res.data;
  };

  const joinRoom = async (inviteCode: string) => {
    const res = await axiosClient.post(`/api/rooms/invites/${inviteCode}/join`);
    return res.data;
  };

  const inviteFriendToRoom = async (roomId: string, friendId: string) => {
    const res = await axiosClient.post(
      `/api/rooms/${roomId}/invite/${friendId}`,
    );
    return res.data;
  };

  return {
    getUserRooms,
    createRoom,
    updateRoomInfo,
    deleteRoom,
    getInviteCode,
    resetInviteCode,
    getBasicRoomInfo,
    joinRoom,
    inviteFriendToRoom,
  };
}
