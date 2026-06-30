import { getFreshToken } from "@/utils/auth";
import axios from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import { getCookie, removeCookie } from "./cookies";

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL as string,
  timeout: 10000,
  withCredentials: true,
});

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getCookie("accessToken");
    const url = config.url ?? "";
    if (!url.includes("/auth")) {
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => Promise.reject(error),
);

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (originalRequest.url.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    // Token mà không hợp lệ thì về trang đăng nhập
    if (
      error.response?.status === 401 &&
      ["INVALID_TOKEN", "ACCOUNT_LOCKED"].includes(error.response?.data?.error)
    ) {
      removeCookie("accessToken");
      removeCookie("refreshToken");
      window.location.href = "/auth";
    }

    if (
      error.response?.status === 401 &&
      error.response?.data?.error === "TOKEN_EXPIRED" &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const newAccessToken = await getFreshToken();

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError: any) {
        console.log(
          "Refresh failed:",
          refreshError.response?.status,
          refreshError.response?.data,
        );
        removeCookie("accessToken");
        window.location.href = "/auth";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  },
);

export default axiosClient;
