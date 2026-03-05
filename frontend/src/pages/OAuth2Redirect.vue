<script lang="ts" setup>
import { onMounted, getCurrentInstance } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const { proxy } = getCurrentInstance();

onMounted(() => {
  const token = route.query.token;

  if (!token) {
    router.push("/login");
    return;
  }

  // lưu token bằng vue-cookies
  proxy.$cookies.set("token", token, "7d", "/");

  router.push("/");
});
</script>

<template>
  <div>Loading...</div>
</template>
