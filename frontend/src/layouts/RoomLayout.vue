<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/features/rooms/sidebar.vue";
import SpaceSidebar from "@/features/spaces/sidebar.vue";
</script>

<template>
  <BaseLayout>
    <template #room-sidebar>
      <RoomSidebar />
    </template>
    <template #space-sidebar>
      <SpaceSidebar />
    </template>
  </BaseLayout>
</template>
