<script setup lang="ts">
import { watch } from "vue";

import VueCookies from "vue-cookies";
const cookies = VueCookies as any;

import { useUserStore } from "@/stores/userStore";
const userStore = useUserStore();
watch(
  () => cookies.get("accessToken"),
  async (newToken) => {
    console.log("Access token changed:", newToken);

    if (newToken && !userStore.user) {
      await userStore.getUserInfo();
    }
  },
  { immediate: true }
);
</script>
<template>
  <div>
    <RouterView />
  </div>
</template>

<style scoped></style>
