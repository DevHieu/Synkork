<script setup lang="ts">
import AuthTitle from './components/auth-title.vue'
import LoginForm from './components/login-form.vue'

import image from "@/assets/images/syn-business.png"
</script>

<template>
  <div class="w-full h-screen lg:grid lg:min-h-[600px] lg:grid-cols-2 xl:min-h-[800px]">
    <div class="relative hidden bg-muted lg:block bg-gradient-to-br from-[oklch(0.56_0.17_36.15)] to-[oklch(0.32_0.05_196.69)]">
      <img
        :src="image"
        alt="Image"
        class="absolute inset-0 h-full object-cover object-center"
      >
      <AuthTitle class="absolute z-10 left-8 top-8" />
    </div>

    <div class="flex items-center justify-center py-12">
      <div class="mx-auto grid w-[350px] gap-6">
        <LoginForm class="border-0 shadow-none" />
      </div>
    </div>
  </div>
</template>
