import axios from "axios";
import { getCookie } from "@/lib/cookies";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api",
});

// ✅ thêm interceptor gắn token
axiosInstance.interceptors.request.use((config) => {
  const token = getCookie("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosInstance;