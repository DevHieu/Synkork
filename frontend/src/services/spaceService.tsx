import axios from "axios";

export const getAllSpacesFromRoomId = async (roomId: string) => {
  const res = await axios.get(`/rooms/${roomId}/spaces`);
  return res;
};
