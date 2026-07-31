<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { register } from "@/features/auth/services/authService";
import type { RegisterData } from "../types/AuthTypes";

defineEmits<{ backToLogin: [] }>();

const form = ref({
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

const serverError = ref("");
const successEmail = ref("");
const disableSendback = ref(false);
const showPassword = ref(false);
const showConfirm = ref(false);

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

  if (!form.value.firstName.trim()) {
    errors.value.firstName = "Vui lòng nhập họ";
    valid = false;
  }
  if (!form.value.lastName.trim()) {
    errors.value.lastName = "Vui lòng nhập tên";
    valid = false;
  }

  if (!form.value.username.trim()) {
    errors.value.username = "Vui lòng nhập username";
    valid = false;
  } else if (form.value.username.length < 3) {
    errors.value.username = "Username phải có ít nhất 3 ký tự";
    valid = false;
  }

  if (!form.value.email.trim()) {
    errors.value.email = "Vui lòng nhập email";
    valid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = "Email không hợp lệ";
    valid = false;
  }

  if (!form.value.password) {
    errors.value.password = "Vui lòng nhập mật khẩu";
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

const submit = async () => {
  serverError.value = "";
  if (!validate()) return;

  const data: RegisterData = {
    firstName: form.value.firstName,
    lastName: form.value.lastName,
    username: form.value.username,
    email: form.value.email,
    password: form.value.password,
  };

  try {
    await register(data);
    successEmail.value = form.value.email;
  } catch (error: any) {
    serverError.value =
      error.response?.data || "Đã xảy ra lỗi, vui lòng thử lại";
  }
};

const sendBack = () => {
  disableSendback.value = true;
  submit();
  setTimeout(() => (disableSendback.value = false), 5000);
};
</script>

<template>
  <!-- ── Success state ── -->
  <div v-if="successEmail" class="form-inner success-wrap">
    <div class="success-icon">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.85)" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
      </svg>
    </div>
    <h2 class="form-title" style="margin-bottom: 6px">Kiểm tra email!</h2>
    <p class="success-sub">Chúng tôi đã gửi link xác thực đến</p>
    <p class="success-email">{{ successEmail }}</p>
    <p class="success-note">
      Link có hiệu lực trong
      <span class="font-extrabold text-ring">5 phút</span>.
    </p>
    <p class="resend-row">
      Không nhận được?
      <button class="resend-btn font-extrabold" @click="sendBack" :disabled="disableSendback">
        Gửi lại
      </button>
    </p>
    <button class="back-login" @click="$emit('backToLogin')">
      ← Quay lại đăng nhập
    </button>
  </div>

  <!-- ── Register form ── -->
  <div v-else class="form-inner">
    <h2 class="form-title">Tạo tài khoản</h2>

    <div v-if="serverError" class="server-error">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="8" x2="12" y2="12" />
        <line x1="12" y1="16" x2="12.01" y2="16" />
      </svg>
      {{ serverError }}
    </div>

    <!-- Họ & Tên -->
    <div class="nrow">
      <div class="field">
        <Label for="r-first" class="field-label">Họ</Label>
        <Input id="r-first" v-model="form.firstName" class="fi" :class="{ 'fi--error': errors.firstName }"
          placeholder="Nguyễn" />
        <p v-if="errors.firstName" class="err-msg">{{ errors.firstName }}</p>
      </div>
      <div class="field">
        <Label for="r-last" class="field-label">Tên</Label>
        <Input id="r-last" v-model="form.lastName" class="fi" :class="{ 'fi--error': errors.lastName }"
          placeholder="Văn A" />
        <p v-if="errors.lastName" class="err-msg">{{ errors.lastName }}</p>
      </div>
    </div>

    <!-- Email -->
    <div class="field">
      <Label for="r-email" class="field-label">Email</Label>
      <div class="input-wrap">
        <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.8">
          <rect x="2" y="4" width="20" height="16" rx="2" />
          <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
        </svg>
        <Input id="r-email" type="email" v-model="form.email" class="fi" :class="{ 'fi--error': errors.email }"
          placeholder="m@example.com" />
      </div>
      <p v-if="errors.email" class="err-msg">{{ errors.email }}</p>
    </div>

    <!-- Username -->
    <div class="field">
      <Label for="r-username" class="field-label">Username</Label>
      <div class="input-wrap">
        <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.8">
          <circle cx="12" cy="8" r="4" />
          <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
        </svg>
        <Input id="r-username" v-model="form.username" class="fi" :class="{ 'fi--error': errors.username }"
          placeholder="ten_cua_ban" />
      </div>
      <p v-if="errors.username" class="err-msg">{{ errors.username }}</p>
    </div>

    <!-- Password -->
    <div class="field">
      <Label for="r-pass" class="field-label">Mật khẩu</Label>
      <div class="input-wrap">
        <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.8">
          <rect x="3" y="11" width="18" height="11" rx="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
        <Input id="r-pass" :type="showPassword ? 'text' : 'password'" v-model="form.password" class="fi fi--padded"
          :class="{ 'fi--error': errors.password }" placeholder="••••••••" />
        <button class="eye-btn" type="button" @click="showPassword = !showPassword">
          <svg v-if="!showPassword" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="1.8">
            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
            <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
            <line x1="2" y1="2" x2="22" y2="22" />
          </svg>
        </button>
      </div>
      <p v-if="errors.password" class="err-msg">{{ errors.password }}</p>
    </div>

    <!-- Confirm -->
    <div class="field">
      <Label for="r-confirm" class="field-label">Nhập lại mật khẩu</Label>
      <div class="input-wrap">
        <svg class="fi-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
          stroke-width="1.8">
          <rect x="3" y="11" width="18" height="11" rx="2" />
          <path d="M7 11V7a5 5 0 0 1 10 0v4" />
        </svg>
        <Input id="r-confirm" :type="showConfirm ? 'text' : 'password'" v-model="form.confirm" class="fi fi--padded"
          :class="{ 'fi--error': errors.confirm }" placeholder="••••••••" />
        <button class="eye-btn" type="button" @click="showConfirm = !showConfirm">
          <svg v-if="!showConfirm" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
            stroke-width="1.8">
            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
            <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
            <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
            <line x1="2" y1="2" x2="22" y2="22" />
          </svg>
        </button>
      </div>
      <p v-if="errors.confirm" class="err-msg">{{ errors.confirm }}</p>
    </div>

    <Button class="submit-btn btn-secondary" @click="submit">Đăng ký</Button>
  </div>
</template>

<style scoped>
/* Success */
.success-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.success-icon {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--clr-s), var(--clr-s2));
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  box-shadow: 0 4px 20px rgba(26, 92, 86, 0.35);
}

.success-sub {
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.5);
}

.success-email {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin: 4px 0 8px;
}

.success-note {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
  margin-bottom: 14px;
}

.resend-row {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  margin-bottom: 8px;
}

.resend-btn {
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 600;
  color: var(--clr-p2);
  cursor: pointer;
  transition: opacity 0.2s;
}

.resend-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.back-login {
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.3);
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}

.back-login:hover {
  color: rgba(255, 255, 255, 0.7);
}
</style>
