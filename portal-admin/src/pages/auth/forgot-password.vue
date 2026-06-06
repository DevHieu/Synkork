<script setup lang="ts">
import AuthTitle from './components/auth-title.vue'
import { authService } from '@/pages/auth/services/authService.ts'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

const router = useRouter()
const email = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (newPassword.value !== confirmPassword.value) {
    toast.error('Mật khẩu xác nhận không khớp')
    return
  }

  loading.value = true
  try {
    const token = await authService.requestPasswordReset(email.value, newPassword.value)
    router.push({ path: 'verify-otp', query: { token } })
  } catch (error: any) {
    toast.error(error.response?.data || 'Đã có lỗi xảy ra')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen p-4 min-w-screen">
    <main class="flex flex-col gap-4 w-full max-w-md">
      <AuthTitle />
      <UiCard class="w-full">
        <UiCardHeader>
          <UiCardTitle class="text-2xl">
            Forgot Password
          </UiCardTitle>
          <UiCardDescription>
            Enter your registered email and new password. We will send an OTP to verify.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="grid gap-4">
          <div class="grid gap-2">
            <UiLabel for="email">{{ $t('email') }}</UiLabel>
            <UiInput
              id="email"
              v-model="email"
              type="email"
              placeholder="m@example.com"
              required
            />
          </div>
          <div class="grid gap-2">
            <UiLabel for="new-password">New Password</UiLabel>
            <UiInput
              id="new-password"
              v-model="newPassword"
              type="password"
              placeholder="••••••••"
              required
            />
          </div>
          <div class="grid gap-2">
            <UiLabel for="confirm-password">Confirm Password</UiLabel>
            <UiInput
              id="confirm-password"
              v-model="confirmPassword"
              type="password"
              placeholder="••••••••"
              required
            />
          </div>
        </UiCardContent>
        <UiCardFooter class="flex flex-col gap-2">
          <UiButton class="w-full" :disabled="loading || !email || !newPassword || !confirmPassword" @click="handleSubmit">
            <span v-if="loading">Sending...</span>
            <span v-else>{{ $t('forgotPasswordPage.continue') }}</span>
          </UiButton>
          <UiButton variant="link" class="text-muted-foreground" @click="router.back()">
            Back to login
          </UiButton>
        </UiCardFooter>
      </UiCard>
    </main>
  </div>
</template>