<script setup lang="ts">
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";

import { ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "@/services/authService";
import type { LoginData } from "@/types/LoginData";

const router = useRouter();

const highlightGoogle = ref(false);
const serverError = ref("");

const loginForm = ref({
  username: "",
  password: "",
});

const errors = ref({
  username: "",
  password: "",
});

const validate = () => {
  let valid = true;
  errors.value = { username: "", password: "" };

  if (!loginForm.value.username.trim()) {
    errors.value.username = "Vui lòng nhập username hoặc email";
    valid = false;
  }

  if (!loginForm.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu";
    valid = false;
  }

  return valid;
};

const submitLogin = async () => {
  serverError.value = "";
  highlightGoogle.value = false;
  if (!validate()) return;

  const data: LoginData = {
    username: loginForm.value.username,
    password: loginForm.value.password,
  };

  try {
    await login(data);
    router.push("/");
  } catch (error: any) {
    const status = error.response?.status;
    const message = error.response?.data;

    if (status === 403) {
      serverError.value = message;
      highlightGoogle.value = true; // highlight nút Google
    } else {
      serverError.value = message || "Sai email hoặc mật khẩu";
    }
  }
};

const handleGoogleLogin = () => {
  window.location.href = `${
    import.meta.env.VITE_BACKEND_URL
  }/api/oauth2/authorization/google`;
};
</script>

<template>
  <div
    class="flex min-h-svh w-full items-center justify-center p-6 md:p-10 auth_background"
  >
    <Card class="w-full max-w-md">
      <CardHeader>
        <CardTitle class="text-2xl text-center">Chào mừng trở lại!</CardTitle>
        <CardDescription />

        <!-- Server error -->
        <div
          v-if="serverError"
          class="flex items-center gap-2 rounded-md border border-destructive/50 bg-destructive/10 px-3 py-2 text-sm text-destructive mt-2"
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
      </CardHeader>

      <CardContent class="grid gap-4">
        <div class="grid gap-2">
          <Label for="email">Username hoặc Email</Label>
          <Input
            id="email"
            type="email"
            placeholder="m@example.com"
            v-model="loginForm.username"
            :class="errors.username ? 'border-destructive' : ''"
          />
          <p v-if="errors.username" class="text-xs text-destructive">
            {{ errors.username }}
          </p>
        </div>

        <div class="grid gap-2">
          <Label for="password">Mật khẩu</Label>
          <Input
            id="password"
            type="password"
            v-model="loginForm.password"
            :class="errors.password ? 'border-destructive' : ''"
          />
          <p v-if="errors.password" class="text-xs text-destructive">
            {{ errors.password }}
          </p>
        </div>
      </CardContent>

      <CardFooter class="flex flex-col gap-4">
        <Button
          variant="secondary"
          size="lg"
          class="w-full"
          @click="submitLogin"
        >
          Đăng nhập
        </Button>

        <RouterLink
          to="/auth/forgot-password"
          class="text-sm text-muted-foreground hover:underline text-center"
        >
          Quên mật khẩu?
        </RouterLink>

        <div class="relative w-full">
          <Separator />
          <span
            class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-background px-2 text-xs text-muted-foreground"
          >
            hoặc
          </span>
        </div>

        <Button
          variant="outline"
          class="w-full flex items-center gap-2 transition-all"
          :class="highlightGoogle ? 'border-primary ring-2 ring-primary' : ''"
          @click="handleGoogleLogin"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 48 48"
            class="h-4 w-4"
          >
            <path
              fill="#EA4335"
              d="M24 9.5c3.54 0 6.7 1.23 9.2 3.64l6.9-6.9C35.9 2.34 30.4 0 24 0 14.6 0 6.6 5.38 2.7 13.22l8.4 6.53C13.1 13.3 18.1 9.5 24 9.5z"
            />
            <path
              fill="#4285F4"
              d="M46.1 24.5c0-1.64-.15-3.21-.43-4.73H24v9.02h12.4c-.54 2.9-2.2 5.36-4.67 7.02l7.1 5.5c4.15-3.83 6.27-9.47 6.27-16.82z"
            />
            <path
              fill="#FBBC05"
              d="M11.1 28.75c-.54-1.6-.85-3.3-.85-5.05s.3-3.45.85-5.05l-8.4-6.53C.98 15.3 0 19.55 0 24s.98 8.7 2.7 12.38l8.4-6.53z"
            />
            <path
              fill="#34A853"
              d="M24 48c6.4 0 11.9-2.1 15.9-5.7l-7.1-5.5c-2 1.35-4.55 2.15-8.8 2.15-5.9 0-10.9-3.8-12.9-9.25l-8.4 6.53C6.6 42.62 14.6 48 24 48z"
            />
          </svg>
          Đăng nhập với Google
        </Button>

        <p class="text-sm text-center text-muted-foreground">
          Chưa có tài khoản?
          <RouterLink
            to="/auth/register"
            class="text-primary hover:underline ml-1"
          >
            Đăng ký
          </RouterLink>
        </p>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped>
.auth_background {
  background: radial-gradient(
    circle,
    rgba(197, 69, 32, 1) 0%,
    rgba(237, 221, 83, 1) 70%,
    rgba(255, 121, 84, 1) 100%
  );
}
</style>
