<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { login } from "@/services/authService";
import type { LoginData } from "@/types/LoginData";

const router = useRouter();

const form = ref({ username: "", password: "" });
const errors = ref({ username: "", password: "" });
const serverError = ref("");
const highlightGoogle = ref(false);
const showPassword = ref(false);

const validate = () => {
  let valid = true;
  errors.value = { username: "", password: "" };

  if (!form.value.username.trim()) {
    errors.value.username = "Vui lòng nhập username hoặc email";
    valid = false;
  }
  if (!form.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu";
    valid = false;
  }
  return valid;
};

const submit = async () => {
  serverError.value = "";
  highlightGoogle.value = false;
  if (!validate()) return;

  const data: LoginData = {
    username: form.value.username,
    password: form.value.password,
  };

  try {
    await login(data);
    router.push("/");
  } catch (error: any) {
    const status = error.response?.status;
    const message = error.response?.data;
    if (status === 403) {
      serverError.value = message;
      highlightGoogle.value = true;
    } else {
      serverError.value = message || "Sai email hoặc mật khẩu";
    }
  }
};

const handleGoogleLogin = () => {
  window.location.href = `${import.meta.env.VITE_BACKEND_URL}/api/oauth2/authorization/google`;
};
</script>

<template>
  <div class="form-inner">
    <h2 class="form-title">Đăng nhập</h2>

    <!-- Social -->
    <div class="social-row">
      <button
        class="sbtn"
        :class="{ 'sbtn--highlight': highlightGoogle }"
        title="Đăng nhập với Google"
        @click="handleGoogleLogin"
      >
        <svg width="18" height="18" viewBox="0 0 48 48">
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
      </button>
    </div>

    <p class="or-divider"><span>hoặc dùng tài khoản của bạn</span></p>

    <!-- Server error -->
    <div v-if="serverError" class="server-error">
      <svg
        width="14"
        height="14"
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

    <!-- Username -->
    <div class="field">
      <Label for="l-username" class="field-label">Username hoặc Email</Label>
      <div class="input-wrap">
        <svg
          class="fi-icon"
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
        >
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <Input
          id="l-username"
          v-model="form.username"
          class="fi"
          :class="{ 'fi--error': errors.username }"
          placeholder="username hoặc email"
        />
      </div>
      <p v-if="errors.username" class="err-msg">{{ errors.username }}</p>
    </div>

    <!-- Password -->
    <div class="field">
      <Label for="l-password" class="field-label">Mật khẩu</Label>
      <div class="input-wrap">
        <svg
          class="fi-icon"
          width="15"
          height="15"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
        >
          <rect x="3" y="11" width="18" height="11" rx="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
        <Input
          id="l-password"
          :type="showPassword ? 'text' : 'password'"
          v-model="form.password"
          class="fi fi--padded"
          :class="{ 'fi--error': errors.password }"
          placeholder="••••••••"
        />
        <button
          class="eye-btn"
          type="button"
          @click="showPassword = !showPassword"
        >
          <svg
            v-if="!showPassword"
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
          <svg
            v-else
            width="15"
            height="15"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
          >
            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
            <path
              d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"
            />
            <path
              d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"
            />
            <line x1="2" y1="2" x2="22" y2="22" />
          </svg>
        </button>
      </div>
      <p v-if="errors.password" class="err-msg">{{ errors.password }}</p>
    </div>

    <RouterLink to="/auth/forgot-password" class="forgot-link"
      >Quên mật khẩu?</RouterLink
    >

    <Button class="submit-btn btn-primary" @click="submit">Đăng nhập</Button>
  </div>
</template>

<style scoped>
.sbtn--highlight {
  border-color: var(--clr-p2) !important;
  box-shadow: 0 0 0 3px rgba(200, 82, 30, 0.25) !important;
}
</style>
