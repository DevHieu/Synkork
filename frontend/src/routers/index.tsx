import { createRouter, createWebHistory } from "vue-router";
// import VideoCall from "@/components/videoCall/VideoCall.vue";

import LoginPage from "@/pages/LoginPage.vue";
import RegisterPage from "@/pages/RegisterPage.vue";
import MainPage from "@/pages/MainPage.vue";

import ChatWindowLayout from "@/components/windows/ChatWindowLayout.vue";
import VoiceWindowLayout from "@/components/windows/VoiceWindowLayout.vue";
import CalendarWindowLayout from "@/components/windows/CalendarWindowLayout.vue";
import NoteWindowLayout from "@/components/windows/NoteWindowLayout.vue";
import TaskWindowLayout from "@/components/windows/TaskWindowLayout.vue";

import VueCookies from 'vue-cookies'

import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import MePage from "@/pages/MePage.vue";

const routes = [
  { path: "/login", component: LoginPage },
  { path: "/register", component: RegisterPage },
  {
    path: "/",
    component: MainPage,
    meta: {
      requiredAuth: true
    },
    children: [
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

  const cookies = VueCookies as any

  const token = cookies.get("jwtToken")

  if (!token && to.meta.requiredAuth) {
    return { path: "/login" };
  }

  if (user.value === null && token) {
    userStore.getUserInfo();

    return;
  }

  if (token.path === "/") {
    return { path: "/me" };
  }
})

export default router;
