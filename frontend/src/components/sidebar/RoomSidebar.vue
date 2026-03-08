<script setup lang="ts">
import { onMounted, watch } from "vue";
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

import { RouterLink, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";

const route = useRoute();

const activeItem = ref<any>(null);

const userStore = useUserStore();
const { user } = storeToRefs(userStore);
const roomStore = useRoomsStore();
const { rooms } = storeToRefs(roomStore);

watch(
  () => user.value?.id,
  async (userId) => {
    if (!userId) return;
    await roomStore.fetchRooms(userId);
    await initCurrentRoom();
  },
  { immediate: true }
);

// Hàm này dùng check xem là cái url hiện tại có phải đang trong 1 room và space ko
const initCurrentRoom = async () => {
  if (!route.fullPath.includes("/rooms/")) return; // Không phải thì chim cúc

  const roomId = route.params.roomId as string;
  const roomItem = rooms.value.find((room: any) => room.id === roomId);
  if (!roomItem) return;

  const spaceId = route.params.spaceId as string;
  await selectRoom(roomItem, spaceId);
};

const selectRoom = async (roomItem: any, spaceId?: string) => {
  activeItem.value = roomItem;
  setOpen(true);
  await roomStore.changeRoom(roomItem, spaceId);
};

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
            <RouterLink to="/me" class="px-0 flex justify-center items-center">
              <div
                class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg"
              >
                <Command class="size-6" />
              </div>
            </RouterLink>
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
                  activeItem?.name === item.name ? 'bg-primary!' : 'bg-muted'
                "
                @click="selectRoom(item)"
              >
                <div class="w-full h-full flex items-center justify-center">
                  {{ item?.name?.charAt(0).toUpperCase() }}
                </div>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter>
      <SidebarMenuItem
        ><NavUser :user="{ name: 'Hiếu', email: 'hàasf', avatar: 'hello' }"
      /></SidebarMenuItem>
    </SidebarFooter>
  </Sidebar>
</template>

<style scoped></style>
