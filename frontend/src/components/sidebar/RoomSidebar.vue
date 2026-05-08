<script setup lang="ts">
import { watch, ref, inject } from "vue";
import { Command, Plus } from "lucide-vue-next";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";

import { RouterLink, useRoute } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "../ui/tooltip";

import AddRoomDialog from "@/components/dialog/RoomDialog/AddRoomDialog.vue";
import type { Room } from "@/types/Room";

const dialogOpen = ref(false);

const emit = defineEmits<{
  roomSelected: [roomItem: any, spaceId?: string];
}>();

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
    await roomStore.fetchRooms();
    await initCurrentRoom();
  },
  { immediate: true },
);

// Hàm này dùng check xem là cái url hiện tại có phải đang trong 1 room và space ko
const initCurrentRoom = async () => {
  if (!route.fullPath.includes("/rooms/")) return; // Không phải thì chim cúc

  const roomId = route.params.roomId as string;
  const roomItem = rooms.value.find((room: Room) => room.id === roomId);
  if (!roomItem) return;

  const spaceId = route.params.spaceId as string;
  await selectRoom(roomItem, spaceId);
};

const setSpaceOpen = inject<(val: boolean) => void>("setSpaceOpen");

const selectRoom = async (roomItem: Room, spaceId?: string) => {
  activeItem.value = roomItem;
  setSpaceOpen?.(true);
  await roomStore.changeRoom(roomItem, spaceId);
};
</script>

<template>
  <Sidebar
    collapsible="none"
    class="w-[calc(var(--sidebar-width-icon)+20px)]! border-r"
  >
    <SidebarHeader>
      <SidebarMenu class="gap-4">
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger as-child>
              <SidebarMenuItem>
                <SidebarMenuButton size="lg" as-child class="md:h-10 md:p-0">
                  <RouterLink
                    to="/me"
                    class="px-0 flex justify-center items-center hover:bg-transparent active:bg-transparent"
                  >
                    <div
                      class="text-sidebar-primary-foreground flex aspect-square size-10 items-center justify-center rounded-lg hover:bg-primary/80 active:bg-primary/95 active:translate-y-1 aria-[current=page]:bg-primary transition-all duration-200"
                      :class="
                        route.fullPath.includes('/me')
                          ? 'bg-primary'
                          : 'bg-muted'
                      "
                    >
                      <Command class="size-8" />
                    </div>
                  </RouterLink>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </TooltipTrigger>
            <TooltipContent
              side="right"
              align="center"
              :side-offset="8"
              class="bg-secondary text-secondary-foreground px-4 py-1 text-sm"
              >{{ "Tin nhắn trực tiếp" }}</TooltipContent
            >
          </Tooltip>
        </TooltipProvider>
      </SidebarMenu>
    </SidebarHeader>
    <Separator />
    <SidebarContent class="overflow-y-auto no-scrollbar">
      <SidebarGroup>
        <SidebarGroupContent class="px-1.5 md:px-0">
          <SidebarMenu>
            <SidebarMenuItem v-for="item in rooms" :key="item.name">
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger as-child>
                    <SidebarMenuButton
                      class="w-12 h-12 mt-3 flex items-center justify-center rounded-[14px] bg-transparent hover:bg-primary/80 active:bg-primary/95 active:translate-y-1 aria-[current=page]:bg-primary transition-all duration-200 overflow-hidden p-0"
                      :is-active="activeItem?.name === item.name"
                      :aria-current="
                        activeItem?.name === item.name ? 'page' : undefined
                      "
                      :class="
                        activeItem?.name === item.name
                          ? 'bg-primary!'
                          : 'bg-muted'
                      "
                      @click="selectRoom(item)"
                    >
                      <div v-if="!item.roomAvatar">
                        {{ item?.name?.slice(0, 2).toUpperCase() }}
                      </div>
                      <img
                        v-else
                        :src="item.roomAvatar"
                        :alt="item.name"
                        class="w-full h-full object-cover"
                      />
                    </SidebarMenuButton>
                  </TooltipTrigger>
                  <TooltipContent
                    side="right"
                    align="center"
                    :side-offset="8"
                    class="bg-secondary text-secondary-foreground px-4 py-1 text-sm"
                    >{{ item.name }}</TooltipContent
                  >
                </Tooltip>
              </TooltipProvider>
            </SidebarMenuItem>
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger as-child>
                  <SidebarMenuButton
                    class="w-12 h-12 mt-3 flex items-center justify-center rounded-[14px] bg-transparent hover:bg-primary/80 active:bg-primary/95 active:translate-y-1 aria-[current=page]:bg-primary transition-all duration-200"
                    :is-active="activeItem?.name === 'Thêm máy chủ'"
                    :aria-current="
                      activeItem?.name === 'Thêm máy chủ' ? 'page' : undefined
                    "
                    :class="
                      activeItem?.name === 'Thêm máy chủ'
                        ? 'bg-primary!'
                        : 'bg-muted'
                    "
                    @click="dialogOpen = true"
                  >
                    <div class="w-full h-full flex items-center justify-center">
                      <Plus class="font-black" />
                    </div>
                  </SidebarMenuButton>
                </TooltipTrigger>
                <TooltipContent
                  side="right"
                  align="center"
                  :side-offset="8"
                  class="bg-secondary text-secondary-foreground px-4 py-1 text-sm"
                  >{{ "Thêm máy chủ" }}</TooltipContent
                >
              </Tooltip>
            </TooltipProvider>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  </Sidebar>

  <AddRoomDialog v-model:open="dialogOpen" />
</template>

<style scoped></style>
