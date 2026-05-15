import { setupLayouts } from 'virtual:generated-layouts'
import { createRouter, createWebHistory } from 'vue-router'
import { handleHotUpdate, routes } from 'vue-router/auto-routes'

import { setupRouterGuard } from './guard'
import { getCookie, setCookie } from '@/lib/cookies'
import axiosClient from '@/lib/axiosClient'

const router = createRouter({
  history: createWebHistory(),
  routes: setupLayouts(routes),

  scrollBehavior() {
    return { left: 0, top: 0, behavior: 'smooth' }
  },
})

router.beforeEach(async (to) => {
  const token = getCookie("accessToken");

  // Bỏ qua oauth2 redirect
  if (to.path.includes("/oauth2")) return;

  // Nếu không có accessToken, thử refresh
  if (!token && !to.path.includes("/auth")) {
    try {
      const response = await axiosClient.post(
        "/api/auth/refresh",
        {},
        { withCredentials: true },
      );
      const newToken = response.data;
      setCookie("accessToken", newToken, 60 * 60 * 15); // 15 minutes
      return;
    } catch {
      return { path: "/auth/sign-in" };
    }
  }

  // Đã login rồi mà vào auth pages
  if (token && to.path.includes("/auth")) {
    return { path: "/dashboard" };
  }

  if (to.path === "/") {
    return { path: "/dashboard" };
  }
});

setupRouterGuard(router)

export default router

if (import.meta.hot) {
  handleHotUpdate(router)
}
