import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
import router from "@/routers";
import { createPinia } from "pinia";

import { library } from "@fortawesome/fontawesome-svg-core";
import { faCalendarAlt, faChevronLeft, faChevronRight, faPlus, faTrash, faEdit, faUser, faClock, faExclamationTriangle } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

library.add(faCalendarAlt, faChevronLeft, faChevronRight, faPlus, faTrash, faEdit, faUser, faClock, faExclamationTriangle);

import axios from "axios";
const backend = import.meta.env.VITE_BACKEND_URL;

axios.defaults.baseURL = `${backend}/api`;
axios.defaults.withCredentials = true;

// Store management
const pinia = createPinia();
const app = createApp(App);

app.component("font-awesome-icon", FontAwesomeIcon);
app.use(router).use(pinia).mount("#app");
