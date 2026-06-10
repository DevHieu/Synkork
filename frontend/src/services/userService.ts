import axiosClient from "@/lib/axiosClient";
import type { User } from "@/types/User";

export const getUserInfo = async () => {
  try {
    return await axiosClient.get<User>("/api/users/me");
  } catch (error) {
    console.error("Error fetching user info:", error);
    throw error;
  }
};

export const getUserInfoByUsername = async (username: string) => {
  const res = await axiosClient.get<User>(`/api/users/${username}`);

  return res.data;
};

export const userService = {
  async getMe() {
    const res = await axiosClient.get<User>("/api/users/me")
    return res.data
  },
 
  async updateProfile(data: { displayName?: string; username?: string }) {
    const res = await axiosClient.patch<User>("/api/users/me", data)
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
  const res = await axiosClient.post<User>("/api/users/me/avatar/upload", formData)
  return res.data
},
}
