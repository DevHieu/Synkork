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
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { resetPassword } from "@/services/authService";

const router = useRouter();
const route = useRoute();

type Status = "form" | "success" | "expired" | "invalid";
const status = ref<Status>("form");
const isLoading = ref(false);
const serverError = ref("");
const token = ref("");

const form = ref({
  password: "",
  confirm: "",
});

const errors = ref({
  password: "",
  confirm: "",
});

onMounted(() => {
  const t = route.query.token as string;
  if (!t) {
    status.value = "invalid";
    return;
  }
  token.value = t;
});

const validate = () => {
  let valid = true;
  errors.value = { password: "", confirm: "" };

  if (!form.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu mới";
    valid = false;
  } else if (form.value.password.length < 6) {
    errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
    valid = false;
  }

  if (!form.value.confirm) {
    errors.value.confirm = "Vui lòng nhập lại mật khẩu";
    valid = false;
  } else if (form.value.confirm !== form.value.password) {
    errors.value.confirm = "Mật khẩu không khớp";
    valid = false;
  }

  return valid;
};

const submitReset = async () => {
  serverError.value = "";
  if (!validate()) return;

  isLoading.value = true;
  try {
    await resetPassword(token.value, form.value.password);
    status.value = "success";
  } catch (error: any) {
    const httpStatus = error.response?.status;
    if (httpStatus === 410) {
      status.value = "expired";
    } else if (httpStatus === 404) {
      status.value = "invalid";
    } else {
      serverError.value = error.message || "Đã xảy ra lỗi, vui lòng thử lại";
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div
    class="flex min-h-svh w-full items-center justify-center auth_background"
  >
    <!-- Success -->
    <Card v-if="status === 'success'" class="w-full max-w-md text-center">
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
        <CardTitle class="text-xl">Đặt lại mật khẩu thành công!</CardTitle>
      </CardHeader>
      <CardContent>
        <p class="text-sm text-muted-foreground">
          Mật khẩu của bạn đã được cập nhật. Bạn có thể đăng nhập ngay bây giờ.
        </p>
      </CardContent>
      <CardFooter class="flex justify-center">
        <Button class="w-full" @click="router.push('/auth')">
          Đăng nhập
        </Button>
      </CardFooter>
    </Card>

    <!-- Expired -->
    <Card v-else-if="status === 'expired'" class="w-full max-w-md text-center">
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
          Link đặt lại mật khẩu chỉ có hiệu lực trong 15 phút. Vui lòng yêu cầu
          link mới.
        </p>
      </CardContent>
      <CardFooter class="flex justify-center">
        <Button
          variant="outline"
          class="w-full"
          @click="router.push('/auth/forgot-password')"
        >
          Gửi lại link
        </Button>
      </CardFooter>
    </Card>

    <!-- Invalid -->
    <Card v-else-if="status === 'invalid'" class="w-full max-w-md text-center">
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
          Link đặt lại mật khẩu không đúng hoặc đã được sử dụng trước đó.
        </p>
      </CardContent>
      <CardFooter class="flex justify-center">
        <Button
          variant="outline"
          class="w-full"
          @click="router.push('/auth/forgot-password')"
        >
          Thử lại
        </Button>
      </CardFooter>
    </Card>

    <!-- Form -->
    <Card v-else class="w-full max-w-md">
      <CardHeader>
        <CardTitle class="text-2xl text-center">Đặt lại mật khẩu</CardTitle>
        <p class="text-sm text-muted-foreground text-center mt-1">
          Nhập mật khẩu mới cho tài khoản của bạn.
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
          <Label for="password">Mật khẩu mới</Label>
          <Input
            id="password"
            type="password"
            v-model="form.password"
            :class="errors.password ? 'border-destructive' : ''"
            @keyup.enter="submitReset"
          />
          <p v-if="errors.password" class="text-xs text-destructive">
            {{ errors.password }}
          </p>
        </div>

        <div class="grid gap-2">
          <Label for="confirm">Nhập lại mật khẩu mới</Label>
          <Input
            id="confirm"
            type="password"
            v-model="form.confirm"
            :class="errors.confirm ? 'border-destructive' : ''"
            @keyup.enter="submitReset"
          />
          <p v-if="errors.confirm" class="text-xs text-destructive">
            {{ errors.confirm }}
          </p>
        </div>
      </CardContent>

      <CardFooter class="flex flex-col gap-4">
        <Button class="w-full" @click="submitReset" :disabled="isLoading">
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
          {{ isLoading ? "Đang xử lý..." : "Đặt lại mật khẩu" }}
        </Button>

        <button
          class="text-sm text-muted-foreground hover:underline"
          @click="router.push('/auth')"
        >
          ← Quay lại đăng nhập
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
