import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "@/routers";
import { createPinia } from "pinia";

import axios from "axios";
const backend = import.meta.env.VITE_BACKEND_URL;

axios.defaults.baseURL = `${backend}/api`;
axios.defaults.withCredentials = true;

// Store management
const pinia = createPinia();

createApp(App).use(router).use(pinia).mount("#app");
