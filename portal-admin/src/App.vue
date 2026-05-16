<script setup lang="ts">
import Loading from '@/components/loading.vue'
import { Toaster } from '@/components/ui/sonner'
import { useSystemTheme } from '@/composables/use-system-theme'
import { authService } from './pages/auth/services/authService'

useSystemTheme()

onMounted(() => {
  authService.checkAuth()
})

</script>

<template>
  <Toaster />

  <Suspense>
    <router-view v-slot="{ Component, route }">
      <component :is="Component" :key="route" />
    </router-view>

    <template #fallback>
      <Loading />
    </template>
  </Suspense>
</template>
