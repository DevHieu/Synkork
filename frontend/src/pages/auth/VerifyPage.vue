<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { verifyAccount } from "@/services/authService";

const router = useRouter();
const route = useRoute();

type Status = "loading" | "success" | "expired" | "invalid";
const status = ref<Status>("loading");

const verify = async () => {
  const token = route.query.token as string;

  if (!token) {
    status.value = "invalid";
    return;
  }

  try {
    await verifyAccount(token);
    status.value = "success";
  } catch (error: any) {
    const httpStatus = error.response?.status;
    status.value = httpStatus === 410 ? "expired" : "invalid";
  }
};

onMounted(() => verify());
</script>

<template>
  <div
    class="flex min-h-svh w-full items-center justify-center auth_background"
  >
    <Card class="w-full max-w-md text-center">
      <!-- Loading -->
      <template v-if="status === 'loading'">
        <CardHeader>
          <div class="flex justify-center mb-2">
            <div class="rounded-full bg-muted p-3 animate-pulse">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-8 w-8 text-muted-foreground"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M12 4v4m0 8v4m8-8h-4M4 12H0m15.07-7.07l-2.83 2.83M6.76 17.24l-2.83 2.83M18.36 18.36l-2.83-2.83M6.76 6.76L3.93 3.93"
                />
              </svg>
            </div>
          </div>
          <CardTitle class="text-xl">Đang xác thực...</CardTitle>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-muted-foreground">
            Vui lòng chờ trong giây lát.
          </p>
        </CardContent>
      </template>

      <!-- Success -->
      <template v-else-if="status === 'success'">
        <CardHeader>
          <div class="flex justify-center mb-2">
            <div class="rounded-full bg-green-100 p-3">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-8 w-8 text-green-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </div>
          <CardTitle class="text-xl">Xác thực thành công!</CardTitle>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-muted-foreground">
            Tài khoản của bạn đã được kích hoạt. Bạn có thể đăng nhập ngay bây
            giờ.
          </p>
        </CardContent>
        <CardFooter class="flex justify-center">
          <Button class="w-full" @click="router.push('/auth')">
            Đăng nhập
          </Button>
        </CardFooter>
      </template>

      <!-- Expired -->
      <template v-else-if="status === 'expired'">
        <CardHeader>
          <div class="flex justify-center mb-2">
            <div class="rounded-full bg-yellow-100 p-3">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-8 w-8 text-yellow-600"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
          <CardTitle class="text-xl">Link đã hết hạn</CardTitle>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-muted-foreground">
            Link xác thực chỉ có hiệu lực trong 5 phút. Vui lòng đăng ký lại để
            nhận link mới.
          </p>
        </CardContent>
        <CardFooter class="flex justify-center">
          <Button
            variant="outline"
            class="w-full"
            @click="router.push('/auth/register')"
          >
            Đăng ký lại
          </Button>
        </CardFooter>
      </template>

      <!-- Invalid -->
      <template v-else-if="status === 'invalid'">
        <CardHeader>
          <div class="flex justify-center mb-2">
            <div class="rounded-full bg-destructive/10 p-3">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-8 w-8 text-destructive"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </div>
          </div>
          <CardTitle class="text-xl">Link không hợp lệ</CardTitle>
        </CardHeader>
        <CardContent>
          <p class="text-sm text-muted-foreground">
            Link xác thực không đúng hoặc đã được sử dụng trước đó.
          </p>
        </CardContent>
        <CardFooter class="flex justify-center">
          <Button
            variant="outline"
            class="w-full"
            @click="router.push('/auth')"
          >
            Về trang đăng nhập
          </Button>
        </CardFooter>
      </template>
    </Card>
  </div>
</template>

<style scoped>
.auth_background {
  background: radial-gradient(
    circle,
    rgba(2, 60, 61, 1) 0%,
    rgba(237, 221, 83, 1) 67%,
    rgba(87, 199, 133, 1) 100%
  );
}
</style>
