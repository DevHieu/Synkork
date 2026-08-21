import { getFreshToken } from "@/features/auth/utils/auth";
import axios from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import { getCookie, removeCookie } from "./cookies";
import { setAuthFlashMessage } from "@/utils/authFlashMessage";

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL as string,
  timeout: 10000,
  withCredentials: true,
});

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getCookie("accessToken");
    const url = config.url ?? "";
    if (!url.startsWith("/api/auth/") || url === "/api/auth/check") {
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
    const authError = error.response?.data?.error;
    const authMessage = error.response?.data?.message;
    const authFlowPath =
      typeof window !== "undefined" &&
      (window.location.pathname === "/auth" ||
        window.location.pathname.startsWith("/auth/") ||
        window.location.pathname.includes("/oauth2/redirect"));

    if (originalRequest?.url?.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    // Token mà không hợp lệ thì về trang đăng nhập
    if (
      error.response?.status === 401 &&
      ["INVALID_TOKEN", "ACCOUNT_LOCKED", "ACCOUNT_NOT_VERIFIED"].includes(
        authError,
      )
    ) {
      if (authError === "ACCOUNT_NOT_VERIFIED") {
        setAuthFlashMessage(
          authMessage ||
            "Tài khoản này chưa xác minh qua email. Vui lòng kiểm tra email và xác minh trước khi đăng nhập.",
        );
      } else if (authError === "ACCOUNT_LOCKED") {
        setAuthFlashMessage(
          authMessage ||
            "Tài khoản của bạn đang bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.",
        );
      }

      removeCookie("accessToken");
      removeCookie("refreshToken");

      if (!authFlowPath) {
        window.location.href = "/auth";
      }

      return Promise.reject(error);
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
