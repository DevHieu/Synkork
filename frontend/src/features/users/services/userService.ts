import axiosClient from "@/lib/axiosClient";
import type { User } from "../types/User";

export function useUserService() {
  const getUserInfo = async () => {
    try {
      return await axiosClient.get<User>("/api/users/me");
    } catch (error) {
      console.error("Error fetching user info:", error);
      throw error;
    }
  };

  const getUserInfoByUsername = async (username: string) => {
    const res = await axiosClient.get<User>(`/api/users/${username}`);

    return res.data;
  };

  const getMe = async () => {
    const res = await axiosClient.get<User>("/api/users/me");
    return res.data;
  };

  const updateProfile = async (data: {
    displayName?: string;
    username?: string;
  }) => {
    const res = await axiosClient.patch<User>("/api/users/me", data);
    return res.data;
  };

  const changePassword = async (data: {
    currentPassword: string;
    newPassword: string;
  }) => {
    const res = await axiosClient.patch("/api/users/me/password", data);
    return res.data;
  };

  // Dành cho tài khoản OAuth chưa có mật khẩu
  const createPassword = async (data: { newPassword: string }) => {
    const res = await axiosClient.post("/api/users/me/password/create", data);
    return res.data;
  };

  const uploadAvatar = async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    const res = await axiosClient.post<User>(
      "/api/users/me/avatar/upload",
      formData,
    );
    return res.data;
  };

  return {
    getUserInfo,
    getUserInfoByUsername,
    getMe,
    updateProfile,
    changePassword,
    createPassword,
    uploadAvatar,
  };
}
