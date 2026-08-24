import axiosClient from "@/lib/axiosClient";
import { removeCookie, setCookie } from "@/lib/cookies";
import type { LoginType, RegisterType } from "@/features/auth/types/AuthTypes";

export function useAuthService() {
  const login = async (loginData: LoginType) => {
    try {
      const res = await axiosClient.post("/api/auth/login", loginData);
      setCookie("accessToken", res.data.accessToken);
      return res.data;
    } catch (error: any) {
      throw error;
    }
  };

  const register = async (registerData: RegisterType) => {
    try {
      const res = await axiosClient.post("/api/auth/register", registerData);
      return res.data;
    } catch (error: any) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await axiosClient.post("/api/auth/logout");
    } catch (error) {
      console.error("Error during logout:", error);
    } finally {
      removeCookie("accessToken");
      window.location.href = "/auth";
    }
  };

  const verifyAccount = async (token: string) => {
    try {
      const res = await axiosClient.get(`/api/auth/verify?token=${token}`);
      return res.data;
    } catch (error: any) {
      throw error;
    }
  };

  const requestPasswordReset = async (email: string) => {
    const res = await axiosClient.post("/api/auth/request-password-reset", {
      email,
    });
    return res.data;
  };

  const verifyOtp = async (
    email: string,
    otpCode: string,
    password?: string,
  ) => {
    const res = await axiosClient.post("/api/auth/reset-password", {
      email,
      otpCode,
      password,
    });
    return res.data;
  };

  const checkIsLogin = async () => {
    const res = await axiosClient.get("/api/auth/check");
    return res.data;
  };

  return {
    login,
    register,
    logout,
    verifyAccount,
    requestPasswordReset,
    verifyOtp,
    checkIsLogin,
  };
}
