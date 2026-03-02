import axios from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import VueCookies from 'vue-cookies'

const cookies = VueCookies as any

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL as string,
  timeout: 10000,
  withCredentials: true,
});

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = cookies.get("accessToken");
    const url = config.url ?? "";
    if (
      !url.startsWith("/login") && !url.startsWith("/register")
    ) {
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Kiểm tra nếu lỗi 401 (Unauthorized)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = cookies.get("refreshToken");
        if (!refreshToken) {
          return Promise.reject(error);
        }

        const response = await axiosClient.post("auth/refresh", refreshToken);
        const { accessToken } = response.data;

        // Cập nhật token mới vào cookie
        cookies.set("accessToken", accessToken, "10s");

        // Chạy lại request gốc với token mới
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Nếu refresh token cũng bị lỗi, xóa cookie và trả về lỗi
        cookies.remove("accessToken");
        cookies.remove("refreshToken");
        window.location.href = "/auth/login";
        return Promise.reject(refreshError);
      }
    }
  }
)

export default axiosClient;
