<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { authService } from '@/pages/auth/services/authService.ts'

import AuthTitle from './components/auth-title.vue'

const router = useRouter()
const route = useRoute()

const email = route.query.email as string
if (!email) {
  router.replace('forgot-password')
}

const otpValue = ref<string[]>([])
const newPassword = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirm = ref(false)
const loading = ref(false)
const resendCooldown = ref(0)

async function handleVerify() {
  if (otpValue.value.filter(Boolean).length < 6)
    return

  if (!newPassword.value || newPassword.value.length < 6) {
    toast.error('Mật khẩu phải có ít nhất 6 ký tự')
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    toast.error('Mật khẩu xác nhận không khớp')
    return
  }

  loading.value = true
  try {
    await authService.verifyOtp(email, otpValue.value.join(''), newPassword.value)
    router.push({ path: 'reset-password-success' })
  }
  catch (error: any) {
    toast.error(error.response?.data || 'Mã OTP không hợp lệ')
    otpValue.value = []
  }
  finally {
    loading.value = false
  }
}

async function handleResend() {
  if (resendCooldown.value > 0)
    return
  try {
    await authService.requestPasswordReset(email)
    toast.success('Đã gửi lại mã OTP')
    startCooldown()
  }
  catch (error: any) {
    toast.error(error.response?.data || 'Không thể gửi lại mã')
  }
}

function startCooldown() {
  resendCooldown.value = 60
  const interval = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0)
      clearInterval(interval)
  }, 1000)
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen p-4 min-w-screen">
    <main class="flex flex-col gap-4 w-full max-w-sm">
      <AuthTitle />
      <UiCard class="w-full">
        <UiCardHeader>
          <UiCardTitle class="text-2xl">
            Verify OTP
          </UiCardTitle>
          <UiCardDescription>
            Enter the 6-digit code sent to <strong>{{ email }}</strong> and set your new password.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="grid gap-4">
          <!-- OTP input -->
          <div class="flex items-center justify-center">
            <UiPinInput
              id="pin-input"
              v-model="otpValue"
              placeholder="○"
            >
              <UiPinInputGroup>
                <UiPinInputInput
                  v-for="(_, index) in 6"
                  :key="index"
                  :index="index"
                />
              </UiPinInputGroup>
            </UiPinInput>
          </div>

          <!-- New password -->
          <div class="grid gap-2">
            <UiLabel for="new-password">
              New Password
            </UiLabel>
            <div class="relative">
              <UiInput
                id="new-password"
                v-model="newPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                class="pr-9"
                @keyup.enter="handleVerify"
              />
              <button
                type="button"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                @click="showPassword = !showPassword"
              >
                <!-- eye-off -->
                <svg v-if="showPassword" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
                  <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
                  <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
                  <line x1="2" y1="2" x2="22" y2="22" />
                </svg>
                <!-- eye -->
                <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Confirm password -->
          <div class="grid gap-2">
            <UiLabel for="confirm-password">
              Confirm Password
            </UiLabel>
            <div class="relative">
              <UiInput
                id="confirm-password"
                v-model="confirmPassword"
                :type="showConfirm ? 'text' : 'password'"
                placeholder="••••••••"
                class="pr-9"
                @keyup.enter="handleVerify"
              />
              <button
                type="button"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                @click="showConfirm = !showConfirm"
              >
                <svg v-if="showConfirm" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
                  <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
                  <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
                  <line x1="2" y1="2" x2="22" y2="22" />
                </svg>
                <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              </button>
            </div>
          </div>
        </UiCardContent>

        <UiCardFooter class="flex flex-col gap-2">
          <UiButton
            class="w-full"
            :disabled="otpValue.filter(Boolean).length < 6 || !newPassword || !confirmPassword || loading"
            @click="handleVerify"
          >
            <span v-if="loading">Verifying...</span>
            <span v-else>Confirm Reset</span>
          </UiButton>
          <UiCardDescription class="text-center">
            Haven't received it?
            <UiButton
              variant="link"
              class="px-0 text-muted-foreground"
              :disabled="resendCooldown > 0"
              @click="handleResend"
            >
              {{ resendCooldown > 0 ? `Resend in ${resendCooldown}s` : 'Resend a new code.' }}
            </UiButton>
          </UiCardDescription>
          <UiButton variant="link" class="text-muted-foreground" @click="router.back()">
            Back
          </UiButton>
        </UiCardFooter>
      </UiCard>
    </main>
  </div>
</template>
