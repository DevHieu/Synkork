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
  { immediate: true },
);
</script>
<template>
  <div>
    <!-- Bên folder layout để xem kĩ hơn -->
    <RouterView />
  </div>

  <!-- Uhhh vấn đề là lúc đầu cái này trong VoiceWindow để nó là chỗ cho thanwgf zego nhét cái audio vào.
   Nhưng nếu để đấy thì phải vào trong window thì mới có tiếng. Nhưng tao lại làm có thể thu nhỏ bên sidebar nên vậy là không ổn
   Nên cái này tao sẽ đưa ra ngoài đây. Để nó luôn có chỗ để zego nó nhét âm thanh. DONE -->
  <div id="audio-players" hidden />
</template>

<style scoped></style>
