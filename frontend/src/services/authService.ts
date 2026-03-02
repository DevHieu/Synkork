import axiosClient from "@/lib/axiosClient";
import VueCookies from "vue-cookies";
import type { LoginData } from "@/types/LoginData";
import type { RegisterData } from "@/types/RegisterData";

export const login = async (loginData: LoginData) => {
    const res = await axiosClient.post("/api/auth/login", loginData);

    const { accessToken, refreshToken } = res.data;
    console.log(res.data);
    console.log(accessToken, refreshToken);
    
    

  // Lưu vào cookie
  VueCookies.set("accessToken", accessToken); // access token 10 phút
  VueCookies.set("refreshToken", refreshToken, "7d"); // refresh token 7 ngày

  return res.data;
}

export const register = async (registerData: RegisterData) => {
    const res = await axiosClient.post("/api/auth/register", registerData);

    const { accessToken, refreshToken } = res.data;

  // Lưu vào cookie
  VueCookies.set("accessToken", accessToken, "15m"); // access token 10 phút
  VueCookies.set("refreshToken", refreshToken, "7d"); // refresh token 7 ngày

  return res.data;
}