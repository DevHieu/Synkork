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
