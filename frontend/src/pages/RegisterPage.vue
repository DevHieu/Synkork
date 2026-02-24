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

const registerForm = ref({
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
});

const submitRegister = async () => {
  const data: RegisterData = {
    firstName: registerForm.value.firstName,
    lastName: registerForm.value.lastName,
    username: registerForm.value.username,
    email: registerForm.value.email,
    password: registerForm.value.password,
  };

  try {
    const response = await register(data);
    console.log("Register successful:", response.data);
    
  } catch (error) {
    console.error("Register failed:", error);
    // Handle register error (e.g., show error message)
  }

  console.log(data);
};
</script>

<template>
  <div className="flex min-h-svh w-full items-center justify-center background">
    <Card class="w-full max-w-md">
      <CardHeader>
        <CardTitle class="text-2xl text-center"> Tạo tài khoản </CardTitle>
      </CardHeader>

      <CardContent class="grid gap-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="grid gap-2">
            <Label for="firstName">First name</Label>
            <Input id="firstName" v-model="registerForm.firstName" />
          </div>

          <div class="grid gap-2">
            <Label for="lastName">Last name</Label>
            <Input id="lastName" v-model="registerForm.lastName" />
          </div>
        </div>

        <div class="grid gap-2">
          <Label for="username">Username</Label>
          <Input id="username" type="text" v-model="registerForm.username" />
        </div>

        <div class="grid gap-2">
          <Label for="email">Email</Label>
          <Input
            id="email"
            type="email"
            placeholder="m@example.com"
            v-model="registerForm.email"
          />
        </div>

        <div class="grid gap-2">
          <Label for="password">Mật khẩu</Label>
          <Input
            id="password"
            type="password"
            v-model="registerForm.password"
          />
        </div>

        <div class="grid gap-2">
          <Label for="confirm">Nhập lại mật khẩu</Label>
          <Input id="confirm" type="password" />
        </div>
      </CardContent>
      <CardFooter class="flex flex-col gap-4">
        <Button class="w-full" @click="submitRegister"> Đăng ký </Button>
        <!-- Separator -->
        <div class="relative w-full">
          <Separator />
          <span
            class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-background px-2 text-xs text-muted-foreground"
          >
            hoặc
          </span>
        </div>

        <!-- Google login -->
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

        <!-- Chuyển sang đăng ký -->
        <p class="text-sm text-center text-muted-foreground">
          Chưa có tài khoản?
          <RouterLink to="/auth/login" class="text-primary hover:underline ml-1">
            Đăng nhập
          </RouterLink>
        </p>
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
