<script setup lang="ts">
import { computed, nextTick, onMounted, ref, type ComponentPublicInstance } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { requestPasswordReset, verifyOtp } from "@/services/authService";

const router = useRouter();
const route = useRoute();

const email = ref("");
const digits = ref(["", "", "", "", "", ""]);
const digitRefs = ref<HTMLInputElement[]>([]);
const form = ref({
  password: "",
  confirm: "",
});
const errors = ref({
  otp: "",
  password: "",
  confirm: "",
});
const serverError = ref("");
const isSubmitting = ref(false);
const isResending = ref(false);
const showPassword = ref(false);
const showConfirm = ref(false);

const otpCode = computed(() => digits.value.join(""));

onMounted(() => {
  email.value = (route.query.email as string) || "";

  if (!email.value) {
    router.replace("/auth/forgot-password");
    return;
  }
});

const setDigitRef = (el: Element | ComponentPublicInstance | null, index: number) => {
  if (el instanceof HTMLInputElement) {
    digitRefs.value[index] = el;
  }
};

const focusDigit = async (index: number) => {
  await nextTick();
  digitRefs.value[index]?.focus();
};

const validate = () => {
  let valid = true;
  errors.value = { otp: "", password: "", confirm: "" };

  if (otpCode.value.length !== 6) {
    errors.value.otp = "Vui lòng nhập đủ 6 chữ số";
    valid = false;
  } else if (!/^\d{6}$/.test(otpCode.value)) {
    errors.value.otp = "Mã OTP chỉ gồm chữ số";
    valid = false;
  }

  if (!form.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu mới";
    valid = false;
  } else if (form.value.password.length < 6) {
    errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
    valid = false;
  }

  if (!form.value.confirm) {
    errors.value.confirm = "Vui lòng nhập lại mật khẩu mới";
    valid = false;
  } else if (form.value.confirm !== form.value.password) {
    errors.value.confirm = "Mật khẩu không khớp";
    valid = false;
  }

  return valid;
};

const handleInput = (index: number, event: Event) => {
  errors.value.otp = "";
  const input = event.target as HTMLInputElement;
  const value = input.value.replace(/\D/g, "").slice(-1);

  digits.value[index] = value;
  input.value = value;

  if (value && index < digits.value.length - 1) {
    focusDigit(index + 1);
  }
};

const handleKeydown = (index: number, event: KeyboardEvent) => {
  if (event.key === "Backspace" && !digits.value[index] && index > 0) {
    focusDigit(index - 1);
  }
};

const handlePaste = (event: ClipboardEvent) => {
  const pasted = event.clipboardData?.getData("text").replace(/\D/g, "");
  if (!pasted) return;

  event.preventDefault();
  digits.value = digits.value.map((_, index) => pasted[index] || "");
  const nextEmpty = digits.value.findIndex((digit) => !digit);
  focusDigit(nextEmpty === -1 ? digits.value.length - 1 : nextEmpty);
};

const submitOtp = async () => {
  serverError.value = "";
  if (!validate()) return;

  isSubmitting.value = true;
  try {
    await verifyOtp(email.value, otpCode.value, form.value.password);
    router.replace("/auth/password-reset-success");
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Mã OTP không đúng, vui lòng thử lại";
  } finally {
    isSubmitting.value = false;
  }
};

const resendOtp = async () => {
  serverError.value = "";

  if (!email.value) {
    router.replace("/auth/forgot-password");
    return;
  }

  isResending.value = true;
  try {
    await requestPasswordReset(email.value);

    digits.value = ["", "", "", "", "", ""];
    focusDigit(0);
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Không thể gửi lại mã, vui lòng thử lại";
  } finally {
    isResending.value = false;
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
      <div class="auth-flow-brand auth-flow-brand--secondary">
        <span class="auth-flow-deco auth-flow-deco-1" />
        <span class="auth-flow-deco auth-flow-deco-2" />
        <span class="auth-flow-deco auth-flow-deco-3" />

        <img src="/assets/logo_ngang.png" alt="Logo" class="auth-flow-logo" />
        <p class="auth-flow-brand-title">Xác thực và đổi mật khẩu</p>
        <p class="auth-flow-brand-desc">
          Nhập mã OTP cùng mật khẩu mới. Hệ thống sẽ xác nhận và cập nhật trong
          một bước.
        </p>
        <button class="ov-btn" type="button" @click="router.push('/auth/forgot-password')">
          Đổi email
        </button>
      </div>

      <div class="auth-flow-form">
        <div class="auth-flow-inner">
          <h2 class="form-title">Nhập mã OTP</h2>
          <p class="auth-flow-desc">
            Mã gồm 6 chữ số
            <template v-if="email">
              đã được gửi đến <span>{{ email }}</span>
            </template>
            .
          </p>

          <div v-if="serverError" class="server-error">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            {{ serverError }}
          </div>

          <div class="otp-row" @paste="handlePaste">
            <input v-for="(_, index) in digits" :key="index" :ref="(el) => setDigitRef(el, index)"
              v-model="digits[index]" class="otp-input" :class="{ 'otp-input--error': errors.otp }" inputmode="numeric"
              maxlength="1" autocomplete="one-time-code" @input="handleInput(index, $event)"
              @keydown="handleKeydown(index, $event)" @keyup.enter="submitOtp" />
          </div>
          <p v-if="errors.otp" class="err-msg otp-error">{{ errors.otp }}</p>

          <div class="field my-5">
            <Label for="otp-password" class="field-label">Mật khẩu mới</Label>
            <div class="input-wrap">
              <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <Input id="otp-password" v-model="form.password" :type="showPassword ? 'text' : 'password'"
                class="fi fi--padded" :class="{ 'fi--error': errors.password }" placeholder="••••••••"
                @keyup.enter="submitOtp" />
              <button class="eye-btn" type="button" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="1.8">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="1.8">
                  <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
                  <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
                  <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
                  <line x1="2" y1="2" x2="22" y2="22" />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="err-msg">{{ errors.password }}</p>
          </div>

          <div class="field mt-5 mb-7">
            <Label for="otp-confirm" class="field-label">
              Nhập lại mật khẩu mới
            </Label>
            <div class="input-wrap">
              <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <Input id="otp-confirm" v-model="form.confirm" :type="showConfirm ? 'text' : 'password'"
                class="fi fi--padded" :class="{ 'fi--error': errors.confirm }" placeholder="••••••••"
                @keyup.enter="submitOtp" />
              <button class="eye-btn" type="button" @click="showConfirm = !showConfirm">
                <svg v-if="!showConfirm" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="1.8">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="1.8">
                  <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
                  <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
                  <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
                  <line x1="2" y1="2" x2="22" y2="22" />
                </svg>
              </button>
            </div>
            <p v-if="errors.confirm" class="err-msg">{{ errors.confirm }}</p>
          </div>

          <Button class="submit-btn btn-primary" :disabled="isSubmitting" @click="submitOtp">
            {{ isSubmitting ? "Đang xác nhận..." : "Xác nhận đổi mật khẩu" }}
          </Button>

          <p class="auth-flow-resend">
            Chưa nhận được mã?
            <button class="auth-flow-link-btn" type="button" :disabled="isResending" @click="resendOtp">
              {{ isResending ? "Đang gửi..." : "Gửi lại" }}
            </button>
          </p>

          <button class="auth-flow-back" type="button" @click="router.push('/auth/forgot-password')">
            Quay lại nhập email
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style src="@/components/auth/auth.css"></style>
