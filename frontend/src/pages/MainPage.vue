<script setup lang="ts">
import { watch } from "vue";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";

import type { SidebarProps } from "@/components/ui/sidebar";
import { Sidebar } from "@/components/ui/sidebar";

import RoomSidebar from "@/components/sidebar/RoomSidebar.vue";
import SpaceSidebar from "@/components/sidebar/SpaceSidebar.vue";

const props = withDefaults(defineProps<SidebarProps>(), {
  collapsible: "icon",
});

import VueCookies from "vue-cookies";
const cookies = VueCookies as any;

import { useUserStore } from "@/stores/userStore";
const userStore = useUserStore();
watch(
  () => cookies.get("accessToken"),
  async (newToken) => {
    console.log("Access token changed:", newToken);

    if (newToken && !userStore.user) {
      await userStore.getUserInfo();
    }
  },
  { immediate: true }
);
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
