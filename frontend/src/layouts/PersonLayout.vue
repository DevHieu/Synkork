<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
</script>

<template>
  <BaseLayout>
    <template #sidebar>
      <RoomSidebar />
      <h1 class="text-2xl font-bold text-center mt-4">Person Layout</h1>
    </template>
  </BaseLayout>
</template>

<style scoped></style>
