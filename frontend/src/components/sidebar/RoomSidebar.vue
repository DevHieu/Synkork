<script setup lang="ts">
import { onMounted } from "vue";
import { Command } from "lucide-vue-next";
import { h, ref } from "vue";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import NavUser from "./NavUser.vue";

import { useRouter } from "vue-router";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";

const activeItem = ref<any>(null);

const router = useRouter();
const roomStore = useRoomsStore();
const { rooms } = storeToRefs(roomStore);

onMounted(async () => {
  const userId = sessionStorage.getItem("userId");

  if (!userId) {
    router.push("/login");
    return;
  }

  await roomStore.fetchRooms(userId);
  selectRoom(rooms.value[0]);
});

const selectRoom = (roomItem: any) => {
  activeItem.value = roomItem;
  setOpen(true);
  roomStore.changeRoom(roomItem);
};

// const mails = ref(data.mails);
const { setOpen } = useSidebar();
</script>

<template>
  <Sidebar
    collapsible="none"
    class="w-[calc(var(--sidebar-width-icon)+20px)]! border-r"
  >
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg" as-child class="md:h-10 md:p-0">
            <a href="#" class="px-0 flex justify-center items-center">
              <div
                class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg"
              >
                <Command class="size-6" />
              </div>
            </a>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>
    <Separator />
    <SidebarContent class="overflow-y-auto no-scrollbar">
      <SidebarGroup>
        <SidebarGroupContent class="px-1.5 md:px-0">
          <SidebarMenu>
            <SidebarMenuItem v-for="item in rooms" :key="item.name">
              <SidebarMenuButton
                :tooltip="h('div', { hidden: false }, item.name)"
                class="w-12 h-12 mt-3 flex items-center justify-center rounded-[14px] bg-transparent hover:bg-primary/80 active:bg-primary/95 aria-[current=page]:bg-primary transition-all duration-200"
                :is-active="activeItem?.name === item.name"
                :aria-current="
                  activeItem?.name === item.name ? 'page' : undefined
                "
                :class="
                  activeItem?.name === item.name
                    ? 'bg-primary!'
                    : 'bg-transparent'
                "
                @click="selectRoom(item)"
              >
                <div
                  class="w-full h-full flex items-center justify-center text-white"
                >
                  {{ item.name.charAt(0).toUpperCase() }}
                </div>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
      <NavUser :user="{ name: 'Hiếu', email: 'hàasf', avatar: 'hello' }" />
    </SidebarFooter>
  </Sidebar>
</template>

<style scoped></style>
