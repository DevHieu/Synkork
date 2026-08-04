<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { requestPasswordReset } from "@/features/auth/services/authService";

const router = useRouter();

const email = ref("");
const emailError = ref("");
const serverError = ref("");
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

    router.push({
      path: "/auth/otp",
      query: { email: email.value },
    });
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Đã xảy ra lỗi, vui lòng thử lại";
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="auth-flow-page">
    <div class="auth-flow-orb auth-flow-orb-1" />
    <div class="auth-flow-orb auth-flow-orb-2" />
    <div class="auth-flow-orb auth-flow-orb-3" />
    <div class="auth-flow-orb auth-flow-orb-4" />
    <div class="auth-flow-orb auth-flow-orb-5" />

    <section class="auth-flow-panel">
      <div class="auth-flow-brand">
        <span class="auth-flow-deco auth-flow-deco-1" />
        <span class="auth-flow-deco auth-flow-deco-2" />
        <span class="auth-flow-deco auth-flow-deco-3" />

        <img src="/assets/logo_ngang.png" alt="Logo" class="auth-flow-logo" />
        <p class="auth-flow-brand-title">Đặt lại mật khẩu</p>
        <p class="auth-flow-brand-desc">
          Nhập email tài khoản. Chúng tôi sẽ gửi mã OTP để bạn xác nhận và tạo
          mật khẩu mới ở bước tiếp theo.
        </p>
      </div>

      <div class="auth-flow-form">
        <div class="auth-flow-inner">
          <h2 class="form-title">Quên mật khẩu?</h2>
          <p class="auth-flow-desc">
            Mã OTP sẽ được gửi đến email đã đăng ký của bạn.
          </p>

          <div v-if="serverError" class="server-error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            {{ serverError }}
          </div>

          <div class="field">
            <Label for="forgot-email" class="field-label">Email</Label>
            <div class="input-wrap">
              <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="1.8">
                <rect x="2" y="4" width="20" height="16" rx="2" />
                <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
              </svg>
              <Input id="forgot-email" v-model="email" type="email" class="fi" :class="{ 'fi--error': emailError }"
                placeholder="m@example.com" @keyup.enter="submitForgotPassword" />
            </div>
            <p v-if="emailError" class="err-msg">{{ emailError }}</p>
          </div>

          <Button class="submit-btn btn-primary" :disabled="isLoading" @click="submitForgotPassword">
            {{ isLoading ? "Đang gửi OTP..." : "Gửi mã OTP" }}
          </Button>

          <button class="auth-flow-back" type="button" @click="router.push('/auth')">
            Quay lại đăng nhập
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style src="@/features/auth/auth.css"></style>
