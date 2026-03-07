import axiosClient from "@/lib/axiosClient";

export const getUserRooms = async (userId: string) => {
  try {
    const response = await axiosClient.get(`/api/rooms/${userId}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching user rooms:", error);
    throw error;
  }
};
