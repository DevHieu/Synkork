import axios from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";

const axiosClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_API as string,
  timeout: 10000,
  withCredentials: true,
});

axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const url = config.url ?? "";

    if (
      url.startsWith("/user") ||
      url.startsWith("/admin") ||
      url.startsWith("/auth/get-user")
    ) {
      const token = document.cookie
        .split("; ")
        .find((row) => row.startsWith("token="))
        ?.split("=")[1];

      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

export default axiosClient;
