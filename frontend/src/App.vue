<script setup lang="ts">
import { useColorMode } from "@vueuse/core";
const mode = useColorMode();

import { RouterView } from "vue-router";
</script>

<template>
  <RouterView />
</template>

<style scoped>
.wrapper {
  display: flex;
}
</style>
