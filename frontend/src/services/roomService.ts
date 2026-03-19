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

export const createRoom = async (roomData: {
  name: string;
  ownerId: string;
  imageFile?: File;
}) => {
  try {
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
  } catch (error) {
    console.error("Error creating room:", error);
    throw error;
  }
};
