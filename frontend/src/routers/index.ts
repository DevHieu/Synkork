import { createRouter, createWebHistory } from "vue-router";
// import VideoCall from "@/components/videoCall/VideoCall.vue";

import MainPage from "@/pages/MainPage.vue";

import LoginPage from "@/pages/auth/LoginPage.vue";
import RegisterPage from "@/pages/auth/RegisterPage.vue";
import ForgotPage from "@/pages/auth/ForgotPage.vue";
import OAuth2Redirect from "@/pages/auth/OAuth2Redirect.vue";
import VerifyPage from "@/pages/auth/VerifyPage.vue";
import ResetPassword from "@/pages/auth/ResetPassword.vue";

import ChatWindowLayout from "@/components/windows/ChatWindowLayout.vue";
import VoiceWindowLayout from "@/components/windows/VoiceWindowLayout.vue";
import CalendarWindowLayout from "@/components/windows/CalendarWindowLayout.vue";
import NoteWindowLayout from "@/components/windows/NoteWindowLayout.vue";
import TaskWindowLayout from "@/components/windows/TaskWindowLayout.vue";

import MePage from "@/pages/MePage.vue";

import VueCookies from "vue-cookies";
import axiosClient from "@/lib/axiosClient";

const cookies = VueCookies as any;

const routes = [
  {
    path: "/auth",
    children: [
      { path: "login", component: LoginPage },
      { path: "register", component: RegisterPage },
      { path: "forgot-password", component: ForgotPage },
      { path: "verify", component: VerifyPage },
      { path: "reset-password", component: ResetPassword },
    ],
  },
  {
    path: "/",
    component: MainPage,
    meta: {
      requiredAuth: true,
    },
    children: [
      {
        path: "/oauth2/redirect",
        component: OAuth2Redirect,
      },
      {
        path: "me",
        component: MePage,
      },
      {
        path: "rooms/chat/:roomId/:spaceId",
        component: ChatWindowLayout,
      },
      {
        path: "rooms/voice/:roomId/:spaceId",
        component: VoiceWindowLayout,
      },
      {
        path: "rooms/calendar/:roomId/:spaceId",
        component: CalendarWindowLayout,
      },
      {
        path: "rooms/note/:roomId/:spaceId",
        component: NoteWindowLayout,
      },
      {
        path: "rooms/task/:roomId/:spaceId",
        component: TaskWindowLayout,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from) => {
  const token = cookies.get("accessToken");

  // Bỏ qua oauth2 redirect
  if (to.path.includes("/oauth2")) return;

  // Nếu không có accessToken, thử refresh
  if (!token && !to.path.includes("/auth")) {
    try {
      const response = await axiosClient.post(
        "/api/auth/refresh",
        {},
        { withCredentials: true }
      );
      const newToken = response.data;
      cookies.set("accessToken", newToken, "15m");
      return;
    } catch {
      return { path: "/auth/login" };
    }
  }

  // Đã login rồi mà vào auth pages
  if (token && to.path.includes("/auth")) {
    return { path: "/me" };
  }

  if (to.path === "/") {
    return { path: "/me" };
  }
});

export default router;
