import axiosClient from "@/lib/axiosClient";

export const getUserRooms = async () => {
  const response = await axiosClient.get(`/api/rooms/me`);
  return response.data;
};

export const createRoom = async (roomData: {
  name: string;
  ownerId: string;
  imageFile?: File;
}) => {
  const formData = new FormData();
  formData.append("name", roomData.name);
  formData.append("ownerId", roomData.ownerId);
  if (roomData.imageFile) {
    formData.append("imageFile", roomData.imageFile);
  }
  const response = await axiosClient.post(`/api/rooms`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data;
};

export const getInviteCode = async (roomId: string) => {
  const res = await axiosClient.get(`/api/rooms/${roomId}/invites`);
  return res.data;
};

export const resetInviteCode = async (roomId: string) => {
  const res = await axiosClient.post(`/api/rooms/${roomId}/invites/reset`);
  return res.data;
};

export const getBasicRoomInfo = async (inviteCode: string) => {
  const res = await axiosClient.get(`/api/rooms/invites/${inviteCode}`);
  return res.data;
};

export const joinRoom = async (inviteCode: string) => {
  const res = await axiosClient.post(`/api/rooms/invites/${inviteCode}/join`);
  return res.data;
};
