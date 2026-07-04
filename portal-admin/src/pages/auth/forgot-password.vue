<script setup lang="ts">
import { ref } from 'vue'
import AuthTitle from './components/auth-title.vue'
import { authService } from '@/pages/auth/services/authService.ts'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

const router = useRouter()
const email = ref('')
const loading = ref(false)

async function handleSubmit() {
  loading.value = true
  try {
    await authService.requestPasswordReset(email.value)
    router.push({ path: 'verify-otp', query: { email: email.value } })
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
            Enter your registered email. We will send an OTP to verify.
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
              @keyup.enter="handleSubmit"
            />
          </div>
        </UiCardContent>
        <UiCardFooter class="flex flex-col gap-2">
          <UiButton
            class="w-full"
            :disabled="loading || !email"
            @click="handleSubmit"
          >
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