import { createRouter, createWebHistory } from "vue-router";
// import VideoCall from "@/components/videoCall/VideoCall.vue";

import LoginPage from "@/pages/LoginPage.vue";
import RegisterPage from "@/pages/RegisterPage.vue";
import MainPage from "@/pages/MainPage.vue";

import ChatWindowLayout from "@/components/ChatWindowLayout.vue";

const routes = [
  // { path: "/", component: MainPage },
  { path: "/login", component: LoginPage },
  { path: "/register", component: RegisterPage },
  // { path: "/call", component: MainView },
  // { path: "/call2", component: VideoCall },

  {
    path: "/",
    component: MainPage,
    children: [
      {
        path: "rooms/:roomId/:spaceId",
        component: ChatWindowLayout,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// router.beforeEach(async (to, from) => {
//   const isAuthenticated = sessionStorage.getItem("loggedIn") === "true";
//   console.log(to);
//   if (!isAuthenticated && (to.path === "/uploads" || to.path === "/profile")) {
//     return { path: "/auth/login" };
//   }
// });

export default router;
