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

const routes = [
  { path: "/login", component: LoginPage },
  { path: "/register", component: RegisterPage },
  {
    path: "/",
    component: MainPage,
    children: [
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

// router.beforeEach(async (to, from) => {
//   const isAuthenticated = sessionStorage.getItem("loggedIn") === "true";
//   console.log(to);
//   if (!isAuthenticated && (to.path === "/uploads" || to.path === "/profile")) {
//     return { path: "/auth/login" };
//   }
// });

export default router;
