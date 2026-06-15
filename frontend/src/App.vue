<script setup lang="ts">
import { onMounted } from "vue";
import { RouterView } from "vue-router";
import { Toaster } from "vue-sonner";
import "vue-sonner/style.css";
import { useThemeStore } from "./stores/themeStore";

onMounted(() => {
  useThemeStore().init()
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
