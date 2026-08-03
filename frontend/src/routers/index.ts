import { createRouter, createWebHistory } from "vue-router";
// import VideoCall from "@/components/videoCall/VideoCall.vue";

import LandingPage from "@/pages/LandingPage.vue";
import MainPage from "@/pages/MainPage.vue";

import AuthPage from "@/pages/auth/AuthPage.vue";
import ForgotPage from "@/pages/auth/ForgotPage.vue";
import OAuth2Redirect from "@/pages/auth/OAuth2Redirect.vue";
import VerifyPage from "@/pages/auth/VerifyPage.vue";
import OtpPage from "@/pages/auth/OtpPage.vue";
import PasswordResetSuccessPage from "@/pages/auth/PasswordResetSuccessPage.vue";

import ChatWindow from "@/features/chats/index.vue";
import VoiceWindow from "@/features/voice-chat/index.vue";
import NoteWindow from "@/components/windows/NoteWindow.vue";
import TaskWindow from "@/components/windows/TaskWindow.vue";
import CalendarWindowLayout from "@/components/windows/CalendarWindowLayout.vue";

import FriendPage from "@/pages/FriendPage.vue";
import MePage from "@/pages/MePage.vue";
import SubscriptionPage from "@/pages/SubscriptionPage.vue";
import InvitePage from "@/pages/InvitePage.vue";

import PersonLayout from "@/layouts/PersonLayout.vue";
import RoomLayout from "@/layouts/RoomLayout.vue";

import axiosClient from "@/lib/axiosClient";
import { getCookie, setCookie } from "@/lib/cookies";

const routes = [
  {
    path: "/invite/:code",
    component: InvitePage,
    meta: { public: true },
  },

  {
    path: "/auth",
    meta: { public: true },
    children: [
      { path: "", component: AuthPage },
      { path: "forgot-password", component: ForgotPage },
      { path: "otp", component: OtpPage },
      { path: "password-reset-success", component: PasswordResetSuccessPage },
      { path: "verify", component: VerifyPage },
    ],
  },
  {
    path: "/introduce",
    component: LandingPage,
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
        component: PersonLayout,
        children: [
          {
            path: "",
            component: MePage,
          },
          {
            path: "friends",
            component: FriendPage,
          },
          {
            path: "subscriptions",
            component: SubscriptionPage,
          },
          {
            path: ":spaceId",
            component: ChatWindow,
          },
          {
            path: "note/:spaceId",
            component: NoteWindow,
          },
          {
            path: "calendar/:spaceId",
            component: CalendarWindowLayout,
          },
        ],
      },
      {
        path: "rooms",
        component: RoomLayout,
        children: [
          {
            path: "chat/:roomId/:spaceId",
            component: ChatWindow,
            meta: { spaceType: "CHAT" },
          },
          {
            path: "voice/:roomId/:spaceId",
            component: VoiceWindow,
            meta: { spaceType: "VOICE" },
          },
          {
            path: "calendar/:roomId/:spaceId",
            component: CalendarWindowLayout,
            meta: { spaceType: "CALENDAR" },
          },
          {
            path: "note/:roomId/:spaceId",
            component: NoteWindow,
            meta: { spaceType: "NOTE" },
          },
          {
            path: "task/:roomId/:spaceId",
            component: TaskWindow,
            props: true,
            meta: { spaceType: "TASK" },
          },
        ],
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const token = getCookie("accessToken");

  // Bỏ qua oauth2 redirect
  if (to.path.includes("/oauth2")) return;

  // Route public (invite, auth) → không cần check auth
  if (to.meta.public) return;

  // Nếu không có accessToken, thử refresh
  if (!token && !to.path.includes("/auth")) {
    try {
      const response = await axiosClient.post(
        "/api/auth/refresh",
        {},
        { withCredentials: true },
      );
      const newToken = response.data;
      setCookie("accessToken", newToken);
      return;
    } catch {
      return { path: "/auth" };
    }
  }

  if (to.path === "/") {
    return { path: "/me" };
  }
});

export default router;
