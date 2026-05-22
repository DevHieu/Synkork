import axiosClient from "@/lib/axiosClient";
import { LoginData } from "../types/LoginData";
import { removeCookie, setCookie } from "@/lib/cookies";

export const authService = {
  async checkAuth() {
    const res = await axiosClient.get('/api/manage/auth/check')
    return res.data
  },

  async getUserInfo() {
    try {
      const response = await axiosClient.get("/api/users/me");
      return response.data;
    } catch (error) {
      console.error("Error fetching user info:", error);
      throw error;
    }
  },

  async login(loginData: LoginData) {
    try {
      const res = await axiosClient.post("/api/manage/auth/login", loginData);

      setCookie("accessToken", res.data, 60 * 60 * 15); // 15 minutes

      console.log(res);

      return res.data;
    } catch (error: any) {
      throw error;
    }
  },

  async logout() {
    try {
      await axiosClient.post("/api/auth/logout");
    } catch (error) {
      console.error("Error during logout:", error);
    } finally {
      removeCookie("accessToken");
      window.location.href = "/auth";
    }
  },
};
