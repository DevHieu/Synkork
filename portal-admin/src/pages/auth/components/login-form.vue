<script lang="ts" setup>
import { useAuth } from '@/composables/use-auth'
import GoogleButton from './google-button.vue'
import ToForgotPasswordLink from './to-forgot-password-link.vue'
import { LoginData } from '../types/LoginData'

const { login, loading, error } = useAuth()

const loginForm = ref<LoginData>({
  username: '',
  password: ''
})
</script>

<template>
  <UiCard class="w-full max-w-md">
    <UiCardHeader>
      <UiCardTitle class="text-2xl">
        Login
      </UiCardTitle>
      <UiCardDescription>
        Enter your email and password below to log into your account.
        Not have an account?
        <UiButton
          variant="link" class="px-0 text-muted-foreground"
          @click="$router.push('/auth/sign-up')"
        >
          Sign Up
        </UiButton>
      </UiCardDescription>
    </UiCardHeader>
    <UiCardContent class="grid gap-4">
      <div class="grid gap-2">
        <UiLabel for="email">
          {{ $t('email') }}
        </UiLabel>
        <UiInput id="email" v-model="loginForm.username" type="email" placeholder="m@example.com" required />
      </div>
      <div class="grid gap-2">
        <div class="flex items-center justify-between">
          <UiLabel for="password">
            {{ $t('password') }}
          </UiLabel>
          <ToForgotPasswordLink />
        </div>
        <UiInput id="password" v-model="loginForm.password" type="password" required placeholder="*********" />
      </div>
      <p v-if="error" class="text-sm text-destructive">
        {{ error }}
      </p>
      <UiButton class="w-full" @click="login(loginForm)">
        <UiSpinner v-if="loading" class="mr-2" />
        {{ $t('login') }}
      </UiButton>

      <UiSeparator label="Or continue with" />

      <div class="flex flex-col items-center justify-between gap-4">
        <GoogleButton />
      </div>
    </UiCardContent>
  </UiCard>
</template>