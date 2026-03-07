<script lang="ts" setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import VueCookies from "vue-cookies";

const router = useRouter();
const cookies = VueCookies as any;

import { useUserStore } from "@/stores/userStore";
const userStore = useUserStore();

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = await urlParams.get("token");
  console.log("Received token:", token);

  if (token) {
    await cookies.set("accessToken", token, "15m");
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
