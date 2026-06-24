<script setup lang="ts">
import image from '@/assets/images/syn-business.png'

import AuthTitle from './components/auth-title.vue'
import LiquidLoader from './components/liquid-loader.vue'
import LoginForm from './components/login-form.vue'
</script>

<template>
  <div class="w-full h-screen lg:grid lg:min-h-[600px] lg:grid-cols-2 xl:min-h-[800px]">
    <div class="relative hidden lg:flex lg:items-center lg:justify-center">
      <LiquidLoader />
      <AuthTitle class="absolute z-10 left-8 top-8" />
    </div>

    <div class="flex items-center justify-center py-12">
      <div class="mx-auto grid w-[350px] gap-6">
        <LoginForm class="border-0 shadow-none" />
      </div>
    </div>
  </div>
</template>
