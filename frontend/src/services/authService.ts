import axiosClient from "@/lib/axiosClient";
import VueCookies from "vue-cookies";
import type { LoginData } from "@/types/LoginData";
import type { RegisterData } from "@/types/RegisterData";

export const login = async (loginData: LoginData) => {
  try {
    const res = await axiosClient.post("/api/auth/login", loginData);
    VueCookies.set("accessToken", res.data, "15m");
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
    VueCookies.remove("accessToken");
    window.location.href = "/auth/login";
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

export const requestPasswordReset = async (email: string) => {
  try {
    const res = await axiosClient.post("/api/auth/request-password-reset", {
      email,
    });
    return res.data;
  } catch (error: any) {
    throw error;
  }
};

export const resetPassword = async (token: string, password: string) => {
  try {
    const res = await axiosClient.post("/api/auth/reset-password", {
      token,
      password,
    });
    return res.data;
  } catch (error: any) {
    throw error; // giữ nguyên error để ResetPasswordPage check status code
  }
};
