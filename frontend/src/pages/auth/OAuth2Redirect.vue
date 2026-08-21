<script lang="ts" setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/features/users/stores/userStore";
import { removeCookie, setCookie } from "@/lib/cookies";
import { setAuthFlashMessage } from "@/utils/authFlashMessage";
import { LoaderIcon } from "lucide-vue-next";
import { cn } from "@/lib/utils";

const router = useRouter();
const userStore = useUserStore();

const getOAuthErrorMessage = (errorCode: string | null) => {
  if (errorCode === "ACCOUNT_NOT_VERIFIED") {
    return "Tài khoản này chưa xác minh qua email. Vui lòng kiểm tra email và xác minh trước khi đăng nhập bằng Google.";
  }

  if (errorCode === "ACCOUNT_LOCKED") {
    return "Tài khoản của bạn đang bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ.";
  }

  return "Đăng nhập Google không thành công. Vui lòng thử lại.";
};

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  const errorCode = urlParams.get("error");

  if (errorCode) {
    setAuthFlashMessage(getOAuthErrorMessage(errorCode));
    removeCookie("accessToken");
    removeCookie("refreshToken");
    router.replace("/auth");
    return;
  }

  if (!token) {
    setAuthFlashMessage(getOAuthErrorMessage(null));
    removeCookie("accessToken");
    removeCookie("refreshToken");
    router.replace("/auth");
    return;
  }

  try {
    await setCookie("accessToken", token);
    await userStore.getUserInfo();
    if (!userStore.user) {
      setAuthFlashMessage(getOAuthErrorMessage("ACCOUNT_NOT_VERIFIED"));
      removeCookie("accessToken");
      removeCookie("refreshToken");
      router.replace("/auth");
      return;
    }

    router.replace("/me");
  } catch {
    setAuthFlashMessage(getOAuthErrorMessage("ACCOUNT_NOT_VERIFIED"));
    removeCookie("accessToken");
    removeCookie("refreshToken");
    router.replace("/auth");
  }
});
</script>

<template>
  <div class="background">
    <div class="flex flex-col items-center justify-center h-screen">
      <div class="text-center">
        <div class="relative flex items-center justify-center">
          <LoaderIcon role="status" aria-label="Loading" :class="cn('size-30 animate-spin')" />

          <img src="/assets/DauChim.png" alt="logo"
            class="absolute size-12 object-contain invert animate-[spin_1s_linear_infinite_reverse]" />
        </div>
        <h1 class="text-2xl font-bold mb-4">Đang đăng nhập ...</h1>
        <p class="text-gray-600">Vui lòng đợi trong giây lát</p>
      </div>
    </div>
  </div>
</template>
