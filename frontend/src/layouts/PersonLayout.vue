<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import FriendSidebar from "@/components/sidebar/FriendSidebar.vue";
import { inject, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useSpaceStore } from "@/stores/spaceStore";

const setSpaceOpen = inject<(val: boolean) => void>("setSpaceOpen");
onMounted(() => {
  setSpaceOpen?.(true);
});

const route = useRoute();
// Cái spaceId mà thay đổi thì join space ngay
watch(
  () => route.params.spaceId,
  async (spaceId) => {
    if (spaceId) {
      await useSpaceStore().joinDMSpace(spaceId as string);
    }
  },
  { immediate: true },
);
</script>

<template>
  <BaseLayout>
    <template #room-sidebar>
      <RoomSidebar />
    </template>
    <template #space-sidebar>
      <FriendSidebar />
    </template>

    <router-view />
  </BaseLayout>
</template>
