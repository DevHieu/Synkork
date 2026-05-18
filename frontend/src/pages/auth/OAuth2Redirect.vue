<script lang="ts" setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

import { useUserStore } from "@/stores/userStore";
import { setCookie } from "@/lib/cookies";
const userStore = useUserStore();

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = await urlParams.get("token");
  console.log("Received token:", token);

  if (token) {
    await setCookie("accessToken", token, 60 * 60 * 15); // 15 minutes
    await userStore.getUserInfo();
    router.push("/me");
  } else {
    router.push("/login");
  }
});
</script>

<template>
  <div>Loading...</div>
</template>
