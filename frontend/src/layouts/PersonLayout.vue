<script setup lang="ts">
import BaseLayout from "./BaseLayout.vue";
import RoomSidebar from "@/features/rooms/sidebar.vue";
import FriendSidebar from "@/features/friends/components/FriendSidebar.vue";
import { inject, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useSpaceComposable } from "@/features/spaces/composables/spaceComposable.ts";

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
      const path = ["/me/note", "/me/calendar"].find(p => route.path.includes(p)) ?? "/me";
      await useSpaceComposable().joinDMSpace(spaceId as string, path);
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
