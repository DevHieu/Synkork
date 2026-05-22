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
  await notificationStore.fetchNotifications()
  await notificationStore.connect()
})
</script>

<template>
  <!-- Hiện thông báo ấy mà -->
  <Toaster position="bottom-right" richColors class="synkork-toaster" />

  <RouterView />
</template>

<style scoped>
.wrapper {
  display: flex;
}
</style>
