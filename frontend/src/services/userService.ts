import axiosClient from "@/lib/axiosClient";

export const getUserInfo = async () => {
  try {
    const response = await axiosClient.get("/api/users/me");
    console.log(response);

    return response;
  } catch (error) {
    console.error("Error fetching user info:", error);
    throw error;
  }
};

export const getUserInfoByUsername = async (username: string) => {
  const res = await axiosClient.get(`/api/users/${username}`);

  return res.data;
};

export const userService = {
  async getMe() {
    const res = await axiosClient.get("/api/users/me")
    return res.data
  },
 
  async updateProfile(data: { displayName?: string; username?: string }) {
    const res = await axiosClient.patch("/api/users/me", data)
    return res.data
  },
 
  async changePassword(data: { currentPassword: string; newPassword: string }) {
    const res = await axiosClient.patch("/api/users/me/password", data)
    return res.data
  },
 
  // Dành cho tài khoản OAuth chưa có mật khẩu
  async createPassword(data: { newPassword: string }) {
    const res = await axiosClient.post("/api/users/me/password/create", data)
    return res.data
  },
 
  async uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append("file", file)
  // KHÔNG set Content-Type — axios tự set boundary đúng cho multipart
  const res = await axiosClient.post("/api/users/me/avatar/upload", formData)
  return res.data
},
}