import axiosClient from "@/lib/axiosClient";
import VueCookies from "vue-cookies";
import type { LoginData } from "@/types/LoginData";
import type { RegisterData } from "@/types/RegisterData";

export const login = async (loginData: LoginData) => {
  const res = await axiosClient.post("/api/auth/login", loginData);

  const accessToken = res.data;

  // Lưu vào cookie
  VueCookies.set("accessToken", accessToken, "15m"); // access token 15 phút

  return res.data;
};

export const register = async (registerData: RegisterData) => {
  const res = await axiosClient.post("/api/auth/register", registerData);

  const accessToken = res.data;

  // Lưu vào cookie
  VueCookies.set("accessToken", accessToken, "15m"); // access token 15 phút

  return res.data;
};
