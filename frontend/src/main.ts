import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import VueCookies from "vue-cookies";
import router from "@/routers";
import { createPinia } from "pinia";

import axios from "axios";
axios.defaults.withCredentials = true;

// Store management
const pinia = createPinia();

// mấy chỗ use xếp theo thứ tự ưu tiên nha
createApp(App).use(pinia).use(router).use(VueCookies).mount("#app");

// Cấu hình Global cho Axios
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken'); // Kiểm tra xem bà lưu tên là 'accessToken' hay 'token' nhé
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
