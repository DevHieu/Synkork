<script setup lang="ts">
import { watch } from "vue";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { ChevronRight, Hash, Volume2 } from "lucide-vue-next";

import { useRouter } from "vue-router";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

const router = useRouter();
const roomStore = useRoomsStore();
const spaceStore = useSpaceStore();

const { currentRoom } = storeToRefs(roomStore);
const { currentSpace, chatSpaces, voiceSpaces } = storeToRefs(spaceStore);

// Watch trực tiếp vào hàm getter của props
watch(
  currentRoom,
  async (newRoom) => {
    if (!newRoom) return;

    console.log("Room ID changed:", newRoom.id);
    await spaceStore.fetchSpacesByRoomId(newRoom.id);
    changeSpace(0, "CHAT");
  },
  { immediate: true }
);

const changeSpace = async (
  index: number,
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK"
) => {
  await spaceStore.changeSpace(index, type); // Assuming chatSpaces is used for CHAT type\

  router.push(`/rooms/${currentRoom.value?.id}/${currentSpace.value?.id}`);
};
</script>

<template>
  <Sidebar collapsible="none" class="hidden flex-1 md:flex">
    <SidebarHeader class="gap-3.5 border-b p-4">
      <div class="flex w-full items-center justify-between">
        <div class="text-base font-medium text-foreground">
          {{ currentRoom?.name }}
        </div>
      </div>
    </SidebarHeader>
    <SidebarContent class="mt-5">
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH VĂN BẢN
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in chatSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'CHAT')">
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH ĐÀM THOẠI
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem v-for="item in voiceSpaces" :key="item.id">
                  <SidebarMenuButton>
                    <Volume2 class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>
    </SidebarContent>
  </Sidebar>
</template>

<style scoped></style>
