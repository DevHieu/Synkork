import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import VueCookies from "vue-cookies";
import router from "@/routers";
import { createPinia } from "pinia";

import axios from "axios";
axios.defaults.withCredentials = true;

// ── Khởi tạo theme từ localStorage TRƯỚC KHI mount ──────────
// Phải chạy sớm nhất có thể để tránh flash trắng khi tải trang
;(function initTheme() {
  const html = document.documentElement
  const savedTheme = localStorage.getItem("synkork_theme_id")
  const savedMode  = localStorage.getItem("synkork_theme_mode") as "light" | "dark" | "system" | null
 
  // Áp data-theme attribute (CSS selector [data-theme="..."] trong themes.css)
  if (savedTheme && savedTheme !== "default") {
    html.setAttribute("data-theme", savedTheme)
  } else {
    html.removeAttribute("data-theme")
  }
 
  // Áp dark / light class
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
  const isDark =
    savedMode === "dark" ||
    savedMode === null ||           // chưa từng lưu → mặc định dark
    (savedMode === "system" && prefersDark)
 
  html.classList.toggle("dark", isDark)
})()

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
