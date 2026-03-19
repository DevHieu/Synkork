<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import FriendSidebar from "@/components/sidebar/FriendSidebar.vue";
import { inject, onMounted } from "vue";

const setSpaceOpen = inject<(val: boolean) => void>("setSpaceOpen");
onMounted(() => {
  setSpaceOpen?.(true);
});
</script>

<template>
  <BaseLayout>
    <template #room-sidebar>
      <RoomSidebar />
    </template>
    <template #space-sidebar>
      <FriendSidebar />
    </template>
  </BaseLayout>
</template>
