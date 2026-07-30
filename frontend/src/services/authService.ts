import axiosClient from "@/lib/axiosClient";
import { removeCookie, setCookie } from "@/lib/cookies";
import type { LoginData } from "@/types/LoginData";
import type { RegisterData } from "@/types/RegisterData";

export const login = async (loginData: LoginData) => {
  try {
    const res = await axiosClient.post("/api/auth/login", loginData);
    setCookie("accessToken", res.data.accessToken);
    return res.data;
  } catch (error: any) {
    throw error;
  }
};

export const register = async (registerData: RegisterData) => {
  try {
    const res = await axiosClient.post("/api/auth/register", registerData);
    return res.data;
  } catch (error: any) {
    throw error;
  }
};

export const logout = async () => {
  try {
    await axiosClient.post("/api/auth/logout");
  } catch (error) {
    console.error("Error during logout:", error);
  } finally {
    removeCookie("accessToken");
    window.location.href = "/auth";
  }
};

export const verifyAccount = async (token: string) => {
  try {
    const res = await axiosClient.get(`/api/auth/verify?token=${token}`);
    return res.data;
  } catch (error: any) {
    throw error;
  }
};

export type PasswordResetRequest = {
  email: string;
};

export const requestPasswordReset = async (email: string) => {
  const res = await axiosClient.post("/api/auth/request-password-reset", {
    email,
  });
  return res.data;
};

export const verifyOtp = async (
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

export const checkIsLogin = async () => {
  const res = await axiosClient.get("/api/auth/check-login");
  return res.data;
};
