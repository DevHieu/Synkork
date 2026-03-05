import { createRouter, createWebHistory } from "vue-router";
// import VideoCall from "@/components/videoCall/VideoCall.vue";

import LoginPage from "@/pages/LoginPage.vue";
import RegisterPage from "@/pages/RegisterPage.vue";
import MainPage from "@/pages/MainPage.vue";
import OAuth2Redirect from "@/pages/OAuth2Redirect.vue";

import ChatWindowLayout from "@/components/windows/ChatWindowLayout.vue";
import VoiceWindowLayout from "@/components/windows/VoiceWindowLayout.vue";
import CalendarWindowLayout from "@/components/windows/CalendarWindowLayout.vue";
import NoteWindowLayout from "@/components/windows/NoteWindowLayout.vue";
import TaskWindowLayout from "@/components/windows/TaskWindowLayout.vue";

import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import MePage from "@/pages/MePage.vue";

const routes = [
  {
    path: "/auth",
    children: [
      { path: "login", component: LoginPage },
      { path: "register", component: RegisterPage },
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

router.beforeEach((to, from) => {
  //Tạo sotre lấy thông tin user
  const userStore = useUserStore();
  const { user } = storeToRefs(userStore);

  if (user.value === null && !to.path.includes("/auth")) {
    userStore.getUserInfo();

    return;
  }

  if (to.path === "/") {
    return { path: "/me" };
  }

  if (to.path.includes("/auth") && user.value !== null) {
    return { path: "/me" };
  }
});

export default router;
