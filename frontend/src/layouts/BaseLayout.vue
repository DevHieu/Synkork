<script setup lang="ts">
import {
  SidebarInset,
  SidebarProvider,
  Sidebar,
} from "@/components/ui/sidebar";
import type { SidebarProps } from "@/components/ui/sidebar";

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
});
</script>

<template>
  <SidebarProvider :style="{ '--sidebar-width': '400px' }">
    <Sidebar
      class="overflow-hidden *:data-[sidebar=sidebar]:flex-row"
      v-bind="props"
    >
      <slot name="sidebar" />
    </Sidebar>
    <SidebarInset>
      <RouterView />
    </SidebarInset>
  </SidebarProvider>
</template>
