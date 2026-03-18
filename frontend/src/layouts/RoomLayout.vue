<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import SpaceSidebar from "@/components/sidebar/SpaceSidebar.vue";
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
