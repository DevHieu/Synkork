import { getFreshToken } from "@/utils/auth";
import axios from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import VueCookies from "vue-cookies";

const cookies = VueCookies as any;

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL as string,
  timeout: 10000,
  withCredentials: true,
});

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = cookies.get("accessToken");
    const url = config.url ?? "";
    if (!url.includes("/auth/")) {
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

    if (originalRequest.url.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newAccessToken = await getFreshToken();

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError: any) {
        console.log(
          "Refresh failed:",
          refreshError.response?.status,
          refreshError.response?.data
        );
        cookies.remove("accessToken");
        window.location.href = "/auth/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
