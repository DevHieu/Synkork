<script setup lang="ts">
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ref } from "vue";
import { useRouter } from "vue-router";
import { requestPasswordReset } from "@/services/authService";

const router = useRouter();
const email = ref("");
const emailError = ref("");
const serverError = ref("");
const successEmail = ref("");
const isLoading = ref(false);

const validate = () => {
  emailError.value = "";
  if (!email.value.trim()) {
    emailError.value = "Vui lòng nhập email";
    return false;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    emailError.value = "Email không hợp lệ";
    return false;
  }
  return true;
};

const submitForgotPassword = async () => {
  serverError.value = "";
  if (!validate()) return;

  isLoading.value = true;
  try {
    await requestPasswordReset(email.value);
    successEmail.value = email.value;
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Đã xảy ra lỗi, vui lòng thử lại";
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div
    class="flex min-h-svh w-full items-center justify-center auth_background"
  >
    <!-- Success box -->
    <Card v-if="successEmail" class="w-full max-w-md text-center">
      <CardHeader>
        <div class="flex justify-center mb-2">
          <div class="rounded-full bg-blue-100 p-3">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-8 w-8 text-blue-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
          </div>
        </div>
        <CardTitle class="text-xl">Kiểm tra email của bạn</CardTitle>
      </CardHeader>
      <CardContent class="grid gap-2">
        <p class="text-sm text-muted-foreground">
          Chúng tôi đã gửi link đặt lại mật khẩu đến
        </p>
        <p class="font-medium">{{ successEmail }}</p>
        <p class="text-sm text-muted-foreground mt-1">
          Link có hiệu lực trong
          <span class="font-medium text-foreground">15 phút</span>.
        </p>
      </CardContent>
      <CardFooter class="flex flex-col gap-2">
        <p class="text-xs text-muted-foreground">
          Không nhận được email?
          <button
            class="text-primary hover:underline"
            @click="
              () => {
                successEmail = '';
                email = '';
              }
            "
          >
            Thử lại
          </button>
        </p>
        <button
          class="text-xs text-muted-foreground hover:underline"
          @click="router.push('/auth/login')"
        >
          ← Quay lại đăng nhập
        </button>
      </CardFooter>
    </Card>

    <!-- Form -->
    <Card v-else class="w-full max-w-md">
      <CardHeader>
        <CardTitle class="text-2xl text-center">Quên mật khẩu?</CardTitle>
        <p class="text-sm text-muted-foreground text-center mt-1">
          Nhập email của bạn và chúng tôi sẽ gửi link đặt lại mật khẩu.
        </p>
      </CardHeader>

      <CardContent class="grid gap-4">
        <!-- Server error -->
        <div
          v-if="serverError"
          class="flex items-center gap-2 rounded-md border border-destructive/50 bg-destructive/10 px-3 py-2 text-sm text-destructive"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4 shrink-0"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {{ serverError }}
        </div>

        <div class="grid gap-2">
          <Label for="email">Email</Label>
          <Input
            id="email"
            type="email"
            placeholder="m@example.com"
            v-model="email"
            :class="emailError ? 'border-destructive' : ''"
            @keyup.enter="submitForgotPassword"
          />
          <p v-if="emailError" class="text-xs text-destructive">
            {{ emailError }}
          </p>
        </div>
      </CardContent>

      <CardFooter class="flex flex-col gap-4">
        <Button
          class="w-full"
          @click="submitForgotPassword"
          :disabled="isLoading"
        >
          <svg
            v-if="isLoading"
            class="animate-spin h-4 w-4 mr-2"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="4"
            />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8v8z"
            />
          </svg>
          {{ isLoading ? "Đang gửi..." : "Gửi link đặt lại mật khẩu" }}
        </Button>

        <button
          class="text-sm text-muted-foreground hover:underline"
          @click="router.push('/auth/login')"
        >
          Quay lại đăng nhập
        </button>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped>
.auth_background {
  background: radial-gradient(
    ellipse at top left,
    rgba(99, 57, 199, 1) 0%,
    rgba(30, 30, 60, 1) 45%,
    rgba(199, 57, 130, 1) 100%
  );
}
</style>
