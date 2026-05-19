<script setup lang="ts">
import { onMounted } from "vue"
import globalAudio from "@/utils/appAudioManager"

onMounted(async () => {
  await globalAudio.init()
})
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
