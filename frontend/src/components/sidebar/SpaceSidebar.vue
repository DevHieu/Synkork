<script setup lang="ts">
// import { watch } from "vue";
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
const {
  currentSpace,
  chatSpaces,
  voiceSpaces,
  calendarSpaces,
  noteSpaces,
  taskSpaces,
} = storeToRefs(spaceStore);

const changeSpace = async (
  index: number,
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK"
) => {
  await spaceStore.changeSpace(index, type);

  router.push(
    `/rooms/${type.toLowerCase()}/${currentRoom.value?.id}/${
      currentSpace.value?.id
    }`
  );
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
      <!-- TASK -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH TASK
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in taskSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'TASK')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- NOTE -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH GHI CHÚ
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in noteSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'NOTE')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- CALENDAR -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH LỊCH TRÌNH
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in calendarSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'CALENDAR')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- CHAT -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH CHAT
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in chatSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'CHAT')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- VOICE -->
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
                <SidebarMenuItem
                  v-for="(item, index) in voiceSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'VOICE')"
                    :isActive="currentSpace?.id === item.id"
                  >
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
