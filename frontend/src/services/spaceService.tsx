import axios from "axios";

export const getAllSpacesFromRoomId = async (roomId: string) => {
  const res = await axios.get(`/rooms/${roomId}/spaces`);
  return res;
};

export const createSpace = async (roomId: string, data: any) => {
  const res = await axios.post(`/rooms/${roomId}/spaces`, data);
  return res;
};

export const deleteSpace = async (roomId: string, spaceId: string) => {
  const res = await axios.delete(`/rooms/${roomId}/spaces/${spaceId}`);
  return res;
};

export const renameSpace = async (
  roomId: string,
  spaceId: string,
  newName: string
) => {
  const res = await axios.put(`/rooms/${roomId}/spaces/${spaceId}`, {
    name: newName,
  });
  return res;
};


