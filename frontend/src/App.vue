<script setup lang="ts">
import { RouterView } from "vue-router";
import { Toaster } from "vue-sonner";
import "vue-sonner/style.css";
</script>

<template>
  <!-- Hiện thông báo ấy mà -->
  <Toaster position="top-center" richColors />

  <RouterView />
</template>

<style scoped>
.wrapper {
  display: flex;
}
</style>
