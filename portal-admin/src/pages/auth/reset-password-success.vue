<script setup lang="ts">
import AuthTitle from './components/auth-title.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="flex items-center justify-center min-h-screen p-4 min-w-screen">
    <main class="flex flex-col gap-4 w-full max-w-md">
      <AuthTitle />
      <UiCard class="w-full">
        <UiCardHeader class="flex flex-col items-center text-center gap-2">
          <div class="flex items-center justify-center w-16 h-16 rounded-full bg-green-100">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <UiCardTitle class="text-2xl">
            Request Submitted
          </UiCardTitle>
          <UiCardDescription>
            Your identity has been verified. Your password reset request has been sent to the administrator for approval.
            You will receive an email notification once it has been reviewed.
          </UiCardDescription>
        </UiCardHeader>
        <UiCardFooter class="flex flex-col gap-2">
          <UiButton class="w-full" @click="router.push('/auth/sign-in')">
            Back to Login
          </UiButton>
        </UiCardFooter>
      </UiCard>
    </main>
  </div>
</template>