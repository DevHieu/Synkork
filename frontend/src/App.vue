<script setup lang="ts">
import { useColorMode } from "@vueuse/core";
const mode = useColorMode();
import { useNotificationStore } from '@/stores/notificationStore'
import { onMounted } from 'vue'

import { RouterView } from "vue-router";
import { Toaster } from "vue-sonner";
import "vue-sonner/style.css";


const notificationStore = useNotificationStore()

onMounted(async () => {
  if (user.value?.id) {
    await notificationStore.fetchNotifications()
    await notificationStore.connect(user.value.id)
  }
})
</script>

<template>
  <!-- Hiện thông báo ấy mà -->
  <Toaster position="top-center" richColors :theme="mode" />

  <RouterView />
</template>

<style scoped>
.wrapper {
  display: flex;
}
</style>
