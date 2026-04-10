<template>
  <!-- Text -->
  <h2 class="text-xl font-bold text-foreground mt-1">
    Chào mừng đến với <span class="text-primary"># {{ props.spaceName }}</span
    >!
  </h2>
  <p class="text-sm text-muted-foreground leading-relaxed">
    Đây là nơi bắt đầu của kênh
    <span class="font-medium text-foreground"># {{ props.spaceName }}</span
    >. Hãy gửi tin nhắn đầu tiên để bắt đầu cuộc trò chuyện.
  </p>
</template>

<script setup lang="ts">
const props = defineProps<{
  spaceName: string;
}>();
</script>

<style scoped></style>
