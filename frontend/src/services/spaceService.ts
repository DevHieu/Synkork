import axiosClient from "@/lib/axiosClient";

export const getAllSpacesFromRoomId = async (roomId: string) => {
  const res = await axiosClient.get(`/api/rooms/${roomId}/spaces`);
  return res;
};

export const createSpace = async (roomId: string, spaceData: {}) => {
  const res = await axiosClient.post(`/api/rooms/${roomId}/spaces`, spaceData);
  return res.data;
};

export const getZegoToken = async (userId: string) => {
  const res = await axiosClient.get(`/api/zego/token/${userId}`);
  console.log("token: " + res.data);

  return res.data;
};
