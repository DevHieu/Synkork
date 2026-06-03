<script setup lang="ts">
import AuthTitle from './components/auth-title.vue'
import { authService } from '@/pages/auth/services/authService.ts'
import { useRouter, useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

const router = useRouter()
const route = useRoute()

const token = route.query.token as string
const otpValue = ref<string[]>([])
const loading = ref(false)
const resendCooldown = ref(0)

if (!token) {
  router.replace('forgot-password')
}

async function handleVerify() {
  if (otpValue.value.filter(Boolean).length < 6) return

  loading.value = true
  try {
    await authService.verifyOtp(token, otpValue.value.join(''))
    router.push({ path: 'reset-password-success' })
  } catch (error: any) {
    toast.error(error.response?.data || 'Mã OTP không hợp lệ')
    otpValue.value = []
  } finally {
    loading.value = false
  }
}

function startCooldown() {
  resendCooldown.value = 60
  const interval = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) clearInterval(interval)
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
            Please enter the 6-digit code sent to your email.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="grid gap-4">
          <div class="flex items-center justify-center">
            <UiPinInput
              id="pin-input"
              v-model="otpValue"
              placeholder="○"
              @complete="handleVerify"
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
        </UiCardContent>
        <UiCardFooter class="flex flex-col gap-2">
          <UiButton
            class="w-full"
            :disabled="otpValue.filter(Boolean).length < 6 || loading"
            @click="handleVerify"
          >
            <span v-if="loading">Verifying...</span>
            <span v-else>Verify</span>
          </UiButton>
          <UiCardDescription class="text-center">
            Haven't received it?
            <UiButton
              variant="link"
              class="px-0 text-muted-foreground"
              :disabled="resendCooldown > 0"
              @click="startCooldown"
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