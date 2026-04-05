<script setup lang="ts">
import { useColorMode } from "@vueuse/core";
const mode = useColorMode();

import { RouterView } from "vue-router";
import { toast, Toaster } from "vue-sonner";
import "vue-sonner/style.css";
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
