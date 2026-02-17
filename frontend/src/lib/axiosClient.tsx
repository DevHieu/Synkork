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
    const token = cookies.get("jwtToken");
    const url = config.url ?? "";
    console.log(token);
    console.log(url);
    
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

export default axiosClient;
