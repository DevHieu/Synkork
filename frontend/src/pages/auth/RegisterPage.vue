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
import { Separator } from "@/components/ui/separator";

import { ref } from "vue";
import type { RegisterData } from "@/types/RegisterData";
import { register } from "@/services/authService";

const successMessage = ref("");
const serverError = ref("");
const disableSendback = ref(false);

const registerForm = ref({
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
  confirm: "",
});

const errors = ref({
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
  confirm: "",
});

const validate = () => {
  let valid = true;
  errors.value = {
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
    confirm: "",
  };

  if (!registerForm.value.firstName.trim()) {
    errors.value.firstName = "Vui lòng nhập họ";
    valid = false;
  }

  if (!registerForm.value.lastName.trim()) {
    errors.value.lastName = "Vui lòng nhập tên";
    valid = false;
  }

  if (!registerForm.value.username.trim()) {
    errors.value.username = "Vui lòng nhập username";
    valid = false;
  } else if (registerForm.value.username.length < 3) {
    errors.value.username = "Username phải có ít nhất 3 ký tự";
    valid = false;
  }

  if (!registerForm.value.email.trim()) {
    errors.value.email = "Vui lòng nhập email";
    valid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.value.email)) {
    errors.value.email = "Email không hợp lệ";
    valid = false;
  }

  if (!registerForm.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu";
    valid = false;
  } else if (registerForm.value.password.length < 6) {
    errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
    valid = false;
  }

  if (!registerForm.value.confirm) {
    errors.value.confirm = "Vui lòng nhập lại mật khẩu";
    valid = false;
  } else if (registerForm.value.confirm !== registerForm.value.password) {
    errors.value.confirm = "Mật khẩu không khớp";
    valid = false;
  }

  return valid;
};

const submitRegister = async () => {
  serverError.value = "";
  if (!validate()) return;

  const data: RegisterData = {
    firstName: registerForm.value.firstName,
    lastName: registerForm.value.lastName,
    username: registerForm.value.username,
    email: registerForm.value.email,
    password: registerForm.value.password,
  };

  try {
    await register(data);
    successMessage.value = registerForm.value.email;
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Đã xảy ra lỗi, vui lòng thử lại";
  }
};

const sendBack = () => {
  console.log("Resending verification email...");

  submitRegister();
  disableSendback.value = true;
  setTimeout(() => {
    disableSendback.value = false;
  }, 5000);
};
</script>

<template>
  <div class="flex min-h-svh w-full items-center justify-center background">
    <Card v-if="!successMessage" class="w-full max-w-md">
      <CardHeader>
        <CardTitle class="text-2xl text-center">Tạo tài khoản</CardTitle>
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

        <div class="grid grid-cols-2 gap-4">
          <div class="grid gap-2">
            <Label for="firstName">Họ</Label>
            <Input
              id="firstName"
              v-model="registerForm.firstName"
              :class="errors.firstName ? 'border-destructive' : ''"
            />
            <p v-if="errors.firstName" class="text-xs text-destructive">
              {{ errors.firstName }}
            </p>
          </div>

          <div class="grid gap-2">
            <Label for="lastName">Tên</Label>
            <Input
              id="lastName"
              v-model="registerForm.lastName"
              :class="errors.lastName ? 'border-destructive' : ''"
            />
            <p v-if="errors.lastName" class="text-xs text-destructive">
              {{ errors.lastName }}
            </p>
          </div>
        </div>

        <div class="grid gap-2">
          <Label for="username">Username</Label>
          <Input
            id="username"
            type="text"
            v-model="registerForm.username"
            :class="errors.username ? 'border-destructive' : ''"
          />
          <p v-if="errors.username" class="text-xs text-destructive">
            {{ errors.username }}
          </p>
        </div>

        <div class="grid gap-2">
          <Label for="email">Email</Label>
          <Input
            id="email"
            type="email"
            placeholder="m@example.com"
            v-model="registerForm.email"
            :class="errors.email ? 'border-destructive' : ''"
          />
          <p v-if="errors.email" class="text-xs text-destructive">
            {{ errors.email }}
          </p>
        </div>

        <div class="grid gap-2">
          <Label for="password">Mật khẩu</Label>
          <Input
            id="password"
            type="password"
            v-model="registerForm.password"
            :class="errors.password ? 'border-destructive' : ''"
          />
          <p v-if="errors.password" class="text-xs text-destructive">
            {{ errors.password }}
          </p>
        </div>

        <div class="grid gap-2">
          <Label for="confirm">Nhập lại mật khẩu</Label>
          <Input
            id="confirm"
            type="password"
            v-model="registerForm.confirm"
            :class="errors.confirm ? 'border-destructive' : ''"
          />
          <p v-if="errors.confirm" class="text-xs text-destructive">
            {{ errors.confirm }}
          </p>
        </div>
      </CardContent>

      <CardFooter class="flex flex-col gap-4">
        <Button class="w-full" @click="submitRegister">Đăng ký</Button>

        <div class="relative w-full">
          <Separator />
          <span
            class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-background px-2 text-xs text-muted-foreground"
          >
            hoặc
          </span>
        </div>

        <Button variant="outline" class="w-full flex items-center gap-2">
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
          Đã có tài khoản?
          <RouterLink to="/auth/login" class="text-primary hover:underline ml-1"
            >Đăng nhập</RouterLink
          >
        </p>
      </CardFooter>
    </Card>

    <!-- Box thành công - thay thế toàn bộ card -->
    <Card v-else class="w-full max-w-md text-center">
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
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
          </div>
        </div>
        <CardTitle class="text-2xl">Kiểm tra email của bạn</CardTitle>
      </CardHeader>
      <CardContent>
        <p class="text-muted-foreground text-sm">
          Chúng tôi đã gửi link xác thực đến
        </p>
        <p class="font-medium mt-1">{{ successMessage }}</p>
        <p class="text-muted-foreground text-sm mt-3">
          Vui lòng kiểm tra hộp thư và click vào link để kích hoạt tài khoản.
          Link có hiệu lực trong
          <span class="font-medium text-foreground">5 phút</span>.
        </p>
      </CardContent>
      <CardFooter class="flex flex-col gap-2">
        <p class="text-xs text-muted-foreground">
          Không nhận được email?
          <button
            class="text-primary hover:underline disabled:opacity-50 disabled:cursor-not-allowed disabled:no-underline"
            @click="sendBack"
            :disabled="disableSendback"
          >
            Gửi lại
          </button>
        </p>
        <RouterLink
          to="/auth/login"
          class="text-xs text-muted-foreground hover:underline hover:text-primary"
        >
          Quay lại đăng nhập
        </RouterLink>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped>
.background {
  background: radial-gradient(
    circle,
    rgba(2, 60, 61, 1) 0%,
    rgba(237, 221, 83, 1) 67%,
    rgba(87, 199, 133, 1) 100%
  );
}
</style>
