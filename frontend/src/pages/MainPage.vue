<script setup lang="ts">
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";

import type { SidebarProps } from "@/components/ui/sidebar";
import { Sidebar } from "@/components/ui/sidebar";

import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import SpaceSidebar from "@/components/sidebar/SpaceSidebar.vue";

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
});
</script>
<template>
  <div>
    <SidebarProvider
      :style="{
        '--sidebar-width': '400px',
      }"
    >
      <div class="app-sidebar">
        <Sidebar
          class="overflow-hidden *:data-[sidebar=sidebar]:flex-row"
          v-bind="props"
        >
          <RoomSidebar />
          <SpaceSidebar />
        </Sidebar>
      </div>
      <SidebarInset>
        <RouterView />
      </SidebarInset>
    </SidebarProvider>
  </div>
</template>

<style scoped></style>
