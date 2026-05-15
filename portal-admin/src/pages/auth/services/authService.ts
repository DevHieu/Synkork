import axiosClient from "@/lib/axiosClient";
import { LoginData } from "../types/LoginData";
import { removeCookie, setCookie } from "@/lib/cookies";

export const authService = {
  async checkAuth() {
    return axiosClient.get("/api/auth/check");
  },

  async login(loginData: LoginData) {
    try {
      const res = await axiosClient.post("/api/auth/login", loginData);

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
