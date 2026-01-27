<script setup lang="ts">
import type { SidebarProps } from "@/components/ui/sidebar";
import { ref } from "vue";
import { Sidebar, useSidebar } from "@/components/ui/sidebar";

import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import SpaceSidebar from "./sidebar/SpaceSidebar.vue";

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
});

const roomSelected = ref<object | null>(null);
const { setOpen } = useSidebar();

const handleChooseRoom = (roomId: object) => {
  roomSelected.value = roomId;
};
</script>

<template>
  <Sidebar
    class="overflow-hidden *:data-[sidebar=sidebar]:flex-row"
    v-bind="props"
  >
    <RoomSidebar @selectRoom="handleChooseRoom" />
    <SpaceSidebar
      v-if="roomSelected"
      :room="roomSelected"
      @closeSidebar="setOpen(false)"
    />
  </Sidebar>
</template>
