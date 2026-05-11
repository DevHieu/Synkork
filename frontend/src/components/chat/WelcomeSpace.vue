<template>
  <template v-if="isDm">
    <h2 class="text-xl font-bold text-foreground mt-1">
      Bắt đầu cuộc trò chuyện với
      <span class="text-primary">{{ friendName }}</span>
    </h2>
    <p class="text-sm text-muted-foreground leading-relaxed">
      Đây là nơi bắt đầu cuộc trò chuyện riêng tư giữa bạn và
      <span class="font-medium text-foreground">{{ friendName }}</span
      >. Hãy gửi tin nhắn đầu tiên!
    </p>
  </template>

  <template v-else>
    <h2 class="text-xl font-bold text-foreground mt-1">
      Chào mừng đến với <span class="text-primary"># {{ spaceName }}</span
      >!
    </h2>
    <p class="text-sm text-muted-foreground leading-relaxed">
      Đây là nơi bắt đầu của kênh
      <span class="font-medium text-foreground"># {{ spaceName }}</span
      >. Hãy gửi tin nhắn đầu tiên để bắt đầu cuộc trò chuyện.
    </p>
  </template>
</template>

<script setup lang="ts">
const props = defineProps<{
  spaceName: string;
  isDm: boolean;
  friendName?: string;
}>();
</script>
